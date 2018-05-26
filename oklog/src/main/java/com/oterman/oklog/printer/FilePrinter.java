package com.oterman.oklog.printer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oterman.oklog.common.LogConfig;
import com.oterman.oklog.common.LogConstants;
import com.oterman.oklog.common.LogItem;
import com.oterman.oklog.common.LogLevel;
import com.oterman.oklog.common.LogUtils;

/**
 * Created by tian on 2017/8/21 0021.
 * 输出到文件
 * 日志模块主要对外接口
 * 在使用BHLog.d(mTag,mMsg)等方法时，需要先进行初始化：OkLog.init(sContext,logconfig,print1...);
 */
public class FilePrinter implements Printer {
    private static LogConfig sLogConfig;
    private static Context sContext;
    private static ExecutorService sThreadPool;

    private static Worker sWorker;
    private static LogWriter sLogWriter;

    private String mCurrentFileName;
    private int mCurrentFilePart = 0;//超过大小，自动增加

    private String mSeedFileName;
    private String mLogDir;
    private List<LogItem> logCacheList;//日志缓存

    @Override
    public void println(int logLevel, String tag, String msg) {
        //新开线程处理文件
        if (!sWorker.isWorking()) {
            sWorker.start();
        }
        //添加日志条目到缓存中
        sWorker.addLogItem(new LogItem(logLevel, tag, msg));
    }

    /**
     * 将缓存日志写入到文件中，解决app被杀时，缓存日志丢失问题
     */
    public void flushCacheLogToFile() {
        synchronized (sWorker) {
            Log.d("FilePrinter", "缓存大小：" + logCacheList.size());
            if (logCacheList != null && logCacheList.size() > 0) {
                if (checkTargetFilePrepared()) {
                    sLogWriter.writeCacheToFile(logCacheList);
                    Log.d("FilePrinter", "缓存日志被写入到文件了");
                }
            }

        }
    }

    @Override
    public void printCrash(String tag, String message) {
        //新开线程处理文件
        if (!sWorker.isWorking()) {
            sWorker.start();
        }

        LogItem logItem = new LogItem(LogLevel.ERROR, tag, message);
        logItem.setCrashInfo(true);
        //添加日志条目到缓存中
        sWorker.addLogItem(logItem);
    }

    @Override
    public LogConfig getLogConfig() {
        return sLogConfig;
    }

    @Override
    public void setLogConfig(LogConfig logConfig) {
        this.sLogConfig = logConfig;
    }

    public static class Builder {
        FilePrinter filePrinter;

        /**
         * 传入模块名，根据模块名生成文件名  如传入modulexx 则生成文件名类似  2017_08_23_modulexx_0.txt
         */
        public Builder(String seedFileName, Context context) {
            filePrinter = new FilePrinter();
            seedFileName(seedFileName);
            filePrinter.sContext = context;
        }

        /**
         * 根据传入的种子文件名生成新的文件
         */
        private Builder seedFileName(String seedFileName) {
            //获取当天最新文件名
            String latestFileName = LogUtils.getLatestFileName(LogConstants.DEFAULT_LOG_DIR, seedFileName);
            Log.d("Builder", "当天最新日志文件名：" + latestFileName);

            if (!TextUtils.isEmpty(latestFileName)) { //当天有日志文件，提取filepart
                filePrinter.mCurrentFileName = latestFileName;
                int start = latestFileName.lastIndexOf("_") + 1;
                int end = latestFileName.lastIndexOf(".");
                if (end > start) {
                    try {
                        int part = Integer.parseInt(latestFileName.substring(start, end));
                        filePrinter.mCurrentFilePart = part;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                filePrinter.mCurrentFilePart = 0;
                filePrinter.mCurrentFileName = LogUtils.generateFileName(seedFileName);
            }

            filePrinter.mSeedFileName = seedFileName;

            //删除旧文件
            filePrinter.delOldFiles();

            return this;
        }

        public Builder logConfig(LogConfig logConfig) {
            filePrinter.sLogConfig = logConfig;
            return this;
        }

        private Builder logDir(String logDir) {
            if (!TextUtils.isEmpty(logDir)) {
                filePrinter.mLogDir = logDir;
            }
            return this;
        }

        public FilePrinter build() {
            return filePrinter;
        }
    }


    /**
     * 异步处理文件输出
     */
    class Worker implements Runnable {
        long lastWriteTime;//上次写文件的时间
        volatile boolean isWorking;//是否正在工作
        List<LogItem> tempList;
        volatile boolean shouldStop;
        FilePrinter filePrinter;

        boolean isCrashInfo = false;

        public Worker(FilePrinter filePrinter) {
            this.filePrinter = filePrinter;
            logCacheList = new ArrayList<>();
            tempList = new ArrayList<>();
        }

        @Override
        public void run() {
            while (isWorking) {
                if (checkWritePrepared() || isCrashInfo) {
                    //条件满足，写入文件
                    synchronized (this) {
                        tempList.addAll(logCacheList);
                        logCacheList.clear();
                    }

                    filePrinter.doPrintln(tempList);//处理打印

                    isCrashInfo = false;
                } else {
                    //不满足写日志条件
                }
            }
        }

        /**
         * 检查是否满足将缓存写到文件中
         */
        private boolean checkWritePrepared() {
            if (lastWriteTime == 0) {
                lastWriteTime = System.currentTimeMillis();
                return false;
            }

            if (logCacheList.size() >= LogConstants.LOG_WRITE_NUM_THRESHOLD || ((lastWriteTime + LogConstants.LOG_WRITE_TIME_INTERVAL <= System.currentTimeMillis()) && logCacheList.size() > 0)) {
                lastWriteTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        /**
         * 添加日志条目
         */
        public synchronized void addLogItem(LogItem logItem) {
            logCacheList.add(logItem);
            if (logItem.isCrashInfo()) {
                isCrashInfo = true;
                Log.d("Worker", "crash come");
            }
        }

        /**
         * 新开线程执行
         */
        public synchronized void start() {
            if (isWorking) {
                Log.d("Worker", "已经开始运行了");
                return;
            }
            Log.d("Worker", "日志开始运行。。");
            sThreadPool.execute(this);
            isWorking = true;
        }

        public boolean isWorking() {
            return isWorking;
        }
    }

    /**
     * 负责具体操作文件  如打开 关闭 写入
     */
    static class LogWriter implements Serializable {
        File targetFile;
        BufferedWriter bufferedWriter;

        //判断是否已经打开
        public boolean isOpened() {
            return bufferedWriter != null;
        }

        public File getCurrentFile() {
            return targetFile;
        }

        //打开要写入的目标文件
        public boolean open(String logDir, String fileName) {
            Log.d("LogWriter:Open", logDir + fileName);
            File dirFile = new File(logDir);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.d("LogWriter", "目录创建失败，what a fuck!");
                    return false;
                }
            }
            targetFile = new File(dirFile, fileName);

//            if (!targetFile.getParentFile().exists()){
//                targetFile.getParentFile().mkdirs();
//                Log.d("LogWriter", "c");
//            }

            if (!targetFile.exists()) {
                try {
                    targetFile.createNewFile();
                } catch (IOException e) {
                    Log.e("LogWriter", "创建文件失败：" + targetFile.getName() + "," + e.getMessage());
                    return false;
                }
            }

            try {
                if (bufferedWriter != null) {
                    close();
                }
                bufferedWriter = new BufferedWriter(new FileWriter(targetFile, true));

                //日志头 创建新文件时打印日志头
                writeLog(LogUtils.getLogHeadInfo(sContext));

            } catch (IOException e) {
                Log.e("LogWriter", "创建writer失败", e);
                return false;
            }
            return true;
        }

        //写入条目
        public void writeLog(String logStr) {
            try {
                bufferedWriter.write(logStr);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 将缓存写入到文件
         */
        public synchronized void writeCacheToFile(List<LogItem> logCache) {
            try {
                for (LogItem logItem : logCache) {
                    bufferedWriter.write(logItem.toString());
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
                logCache.clear();
                Log.d("LogWriter", "缓存写入文件成功:" + targetFile.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //关闭资源
        public boolean close() {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    bufferedWriter = null;
                    targetFile = null;
                    Log.d("LogWriter", "关闭成功");
                }
            }
            return true;
        }
    }

    private FilePrinter() {
        mLogDir = LogConstants.DEFAULT_LOG_DIR;
        mCurrentFileName = LogUtils.generateFileName();
        sWorker = new Worker(this);
        sLogWriter = new LogWriter();
        sThreadPool = Executors.newFixedThreadPool(1);
    }

    /**
     * 不存在多线程问题
     * 处理输出到文件
     */
    private void doPrintln(List<LogItem> logList) {
        Log.d("FilePrinter", "doPrintln()，日志条数：" + logList.size() + "，threadId:" + Thread.currentThread().getId());
        if (checkTargetFilePrepared()) {
            sLogWriter.writeCacheToFile(logList);
        } else {
            Log.d("FilePrinter", "写入失败");
        }
    }


    /**
     * 检查目标文件是否存在 如果不存在则创建新文件
     * 产生新文件策略：
     * 1.根据日期生成文件名，检查文件名是否和当前文件名一致，如果不一致，表明到了新的一天，需要产生新文件；
     * 2.在上一步基础上，如果一致，检查当前文件大小是否超过限制，如果超过限制，则需要产生新文件；一致则不需要产生新文件
     */
    private boolean checkTargetFilePrepared() {
        //检查是否写入到新的文件中；
        String newFileName = LogUtils.generateFileName(mSeedFileName, mCurrentFilePart);
        boolean needGenNewFile = false;

        if (!newFileName.equals(mCurrentFileName)) {//当前操作文件名和目标文件名不一致
            //需要将currentFilePart归零后，重新产生文件,否则新一天的文件名会和前一天的filePart关联起来
            mCurrentFilePart = 0;
            newFileName = LogUtils.generateFileName(mSeedFileName, mCurrentFilePart);
            needGenNewFile = true;
        } else {
            //检查文件大小是否超过阈值
            File currentFile = sLogWriter.getCurrentFile();

            if (currentFile != null && currentFile.length() >= LogConstants.LOG_FILE_SIZE_THRESHOLD) {//文件大小超过限制
                int start = currentFile.getName().lastIndexOf("_") + 1;
                int end = currentFile.getName().lastIndexOf(".");
                if (end > start) {
                    try {
                        int part = Integer.parseInt(currentFile.getName().substring(start, end));
                        newFileName = LogUtils.generateFileName(mSeedFileName, ++part);
                        mCurrentFilePart = part;
                        Log.d("FilePrinter", "大小超过限制，产生新文件" + newFileName);
                        needGenNewFile = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else {
                Log.d("FilePrinter", "文件大小没有超过限制");
            }
        }

        if (needGenNewFile) {//需要写入到新文件中
            //删除旧文件
            delOldFiles();

            if (sLogWriter.isOpened()) {
                sLogWriter.close();
            }
            mCurrentFileName = newFileName;

            return sLogWriter.open(mLogDir, mCurrentFileName);
        } else {//不需要写入到新文件
            if (!sLogWriter.isOpened()) {
                return sLogWriter.open(mLogDir, mCurrentFileName);
            }
            return true;
        }

    }

    /**
     * 删除旧文件
     */
    private void delOldFiles() {
        Log.d("FilePrinter", "删除文件 delOldFiles");
        File dir = new File(LogConstants.DEFAULT_LOG_DIR);

        if (!dir.exists() || !dir.isDirectory()) {
            Log.d("FilePrinter", dir.getAbsolutePath() + "要删除文件的目标目录不存在！");
            return;
        }

        //得到n天前的日期串
        final String baseDelName = LogUtils.getNthDayBeforeStr(LogConstants.LOG_FILE_KEEP_DAYS);

        Log.d("FilePrinter", "要删除的字符文件名字：" + baseDelName);

        String reg = "(.*?)(\\d{4}_\\d{2}_\\d{2}).*";
        final Pattern pattern = Pattern.compile(reg);

        //找到要删除的目标文件
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                Log.d("FilePrinter", "fileName=" + fileName);

                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    String group = matcher.group(2);
                    Log.d("FilePrinter", "group=" + group);

                    return baseDelName.compareTo(group) >= 0;
                }


                return false;
            }
        });

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.exists() && file.isFile()) {
                Log.d("FilePrinter", "删除文件：" + file.getName());
                file.delete();
            }
        }

    }

    public static void main(String[] args) {

//        String fileName="2017_10_08_ddd.txt";
//        String fileName="ddd.txt";
        String fileName = "traces_2017_10_08_xxx.txt";

        String baseDel = "2017_09_08_dddsdad.txt";

        String reg = "(.*?)(\\d{4}_\\d{2}_\\d{2}).*";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.matches()) {
            String group = matcher.group(2);
            System.out.print("group:" + group);

            if (baseDel.compareTo(fileName) >= 0) {
                System.out.println("匹配，找到目标：" + fileName);
            } else {
                System.out.println("不匹配：" + fileName);

            }
        } else {
            System.out.println("不匹配");
        }

        int[] array = new int[5];

    }


}
