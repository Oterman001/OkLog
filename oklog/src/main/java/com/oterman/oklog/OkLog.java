package com.oterman.oklog;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.oterman.oklog.common.LogConfig;
import com.oterman.oklog.common.LogItem;
import com.oterman.oklog.common.LogLevel;
import com.oterman.oklog.common.LogUtils;
import com.oterman.oklog.printer.ConsolePrinter;
import com.oterman.oklog.printer.FilePrinter;
import com.oterman.oklog.printer.Printer;
import com.oterman.oklog.printer.PrinterSet;

/**
 * Created by tian on 2017/8/21 0021.
 */
public class OkLog {
    public static LogConfig sLogConfig;//日志配置
    public static Context sContext;

    private static PrinterSet sPrinterSet;//输出渠道
    private static boolean  sIsInitialized = false;//检查是否初始化
    private static BlockingQueue<LogItem> sLogItems = null;
    private static volatile boolean needOnlineDebug = false;
    private static ScheduledExecutorService sScheduledThreadPool;

    /**
     * 初始化，默认配置，默认输出渠道为控制台
     */
    public static void init(Context context) {
        sLogConfig = new LogConfig.Builder().build();
        init(context, sLogConfig, new ConsolePrinter());
    }

    /**
     * 初始化  默认配置  自定义输出渠道
     *
     * @param printer 自定义输出渠道
     */
    public static void init(Context context, Printer... printer) {
        sLogConfig = new LogConfig.Builder().build();
        init(context, sLogConfig, printer);
    }

    /**
     * 自定义初始化
     *
     * @param config  自定义Logcat配置
     * @param printer 自定义日志输出渠道
     */
    public static void init(Context context, LogConfig config, Printer... printer) {
        if (sIsInitialized) {
            Log.d("OkLog", "已经初始化成功");
            return;
        }
        sLogConfig = config;
        OkLog.sContext = context;
        OkLog.sPrinterSet = new PrinterSet(printer);
        sIsInitialized = true;

        //打印log头信息
//        sPrinterSet.handlePrintln(LogLevel.VERBOSE,sLogConfig.mTag,LogUtils.getLogHeadInfo(sContext));

        //是否捕获crash信息
        initCrashCofig();

        //初始化检测anr
        initDetectAnr();

        Log.d("OkLog", "BHLog初始化成功");
    }

    /**
     * 每隔1s去检测ANR是否发生
     */
    private static void initDetectAnr() {
        sScheduledThreadPool = Executors.newScheduledThreadPool(1);
        final ActivityManager activityManager = (ActivityManager) sContext.getSystemService(Context.ACTIVITY_SERVICE);

        sScheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                    List<ActivityManager.ProcessErrorStateInfo> processList = activityManager.getProcessesInErrorState();
                    if (processList != null) {
                        Iterator<ActivityManager.ProcessErrorStateInfo> iterator = processList.iterator();
                        while (iterator.hasNext()) {
                            ActivityManager.ProcessErrorStateInfo processInfo = iterator.next();
                            Log.d("OkLog", "ANR come!!!!!!!!");
                            //导出trace文件到目录下
                            String traceFileName = LogUtils.exportTraceFile();
                            //日志文件中打印anr信息
                            String anrInfo = LogUtils.getAnrInfo(processInfo, traceFileName, sContext);
                            sPrinterSet.printCrash(sLogConfig.mTag, anrInfo);

                            sScheduledThreadPool.shutdownNow();
                        }
                    }

            }

        }, 0, 1, TimeUnit.SECONDS);

    }

    /**
     * 初始化捕获crash信息
     */
    private static void initCrashCofig() {
        if (sLogConfig.mPrintCrash) {
            //捕获全局信息
            final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    Log.d("OkLog", "发生了crash!");
                    e.printStackTrace();
                    //输出crash信息
                    sPrinterSet.printCrash(sLogConfig.mTag, LogUtils.getCrashInfo(sContext, t, e));

                    //调用默认处理
                    if (defaultUncaughtExceptionHandler != null) {
                        defaultUncaughtExceptionHandler.uncaughtException(t, e);
                    }
                }
            });

        } else {
            // 不捕获全局信息
            Log.d("OkLog", "不捕获全局信息");
        }
    }

    private static void checkInitialized() {
        if (!sIsInitialized) {
            throw new RuntimeException("先初始化后才能使用哦!");
        }
    }

    public static void v(String msg) {
        v(null, msg);
    }

    public static void v(String tag, String msg) {
        checkInitialized();
        v(tag, msg, true);
    }

    public static void v(String tag, String msg, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.VERBOSE, tag, msg, printToRemote);
    }

    public static void v(String msg, Throwable throwable) {
        v(null, msg, throwable);
    }

    public static void v(String tag, String msg, Throwable throwable) {
        checkInitialized();
        handlePrint(LogLevel.VERBOSE, tag, msg, throwable, true);
    }

    /**
     * 自定义打印日志信息
     *
     * @param tag              标签
     * @param msg              信息
     * @param printThreadInfo  是否打印线程信息
     * @param printProcessInfo 是否打印进程信息
     * @param printStackTrace  是否答应调用栈信息
     */
    public static void v(String tag, String msg, boolean printThreadInfo, boolean printProcessInfo, boolean printStackTrace) {
        checkInitialized();
        handlePrint(LogLevel.VERBOSE, tag, msg, printThreadInfo, printProcessInfo, printStackTrace, true);
    }

    public static void i(String msg) {
        i(null, msg);
    }

    public static void i(String tag, String msg) {
        i(tag, msg, true);
    }

    public static void i(String tag, String msg, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.INFO, tag, msg, printToRemote);
    }

    public static void i(String msg, Throwable throwable) {
        i(null, msg, throwable);
    }

    public static void i(String tag, String msg, Throwable throwable) {
        checkInitialized();
        handlePrint(LogLevel.INFO, tag, msg, throwable, true);
    }


    /**
     * 自定义打印日志信息
     *
     * @param tag              标签
     * @param msg              信息
     * @param printThreadInfo  是否打印线程信息
     * @param printProcessInfo 是否打印进程信息
     * @param printStackTrace  是否答应调用栈信息
     */
    public static void i(String tag, String msg, boolean printThreadInfo, boolean printProcessInfo, boolean printStackTrace) {
        checkInitialized();
        handlePrint(LogLevel.INFO, tag, msg, printThreadInfo, printProcessInfo, printStackTrace, true);
    }

    public static void d(String msg) {
        d(null, msg);
    }

    public static void d(String tag, String msg) {
        checkInitialized();
        handlePrint(LogLevel.DEBUG, tag, msg, true);
    }

    public static void d(String tag, String msg, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.DEBUG, tag, msg, printToRemote);
    }

    public static void d(String msg, Throwable throwable) {
        d(null, msg, throwable);
    }

    public static void d(String tag, String msg, Throwable throwable) {
        checkInitialized();
        d(tag, msg, throwable, true);
    }

    public static void d(String tag, String msg, Throwable throwable, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.DEBUG, tag, msg, throwable, printToRemote);
    }

    /**
     * 自定义打印日志信息
     *
     * @param tag              标签
     * @param msg              信息
     * @param printThreadInfo  是否打印线程信息
     * @param printProcessInfo 是否打印进程信息
     * @param printStackTrace  是否答应调用栈信息
     */
    public static void d(String tag, String msg, boolean printThreadInfo, boolean printProcessInfo, boolean printStackTrace) {
        checkInitialized();
        handlePrint(LogLevel.DEBUG, tag, msg, printThreadInfo, printProcessInfo, printStackTrace, true);
    }

    public static void w(String msg) {
        w(null, msg);
    }

    public static void w(String tag, String msg) {
        checkInitialized();
        handlePrint(LogLevel.WARN, tag, msg, true);
    }

    public static void w(String tag, String msg, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.WARN, tag, msg, printToRemote);
    }

    public static void w(String msg, Throwable throwable) {
        w(null, msg, throwable);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        checkInitialized();
        w(tag, msg, throwable, true);
    }

    public static void w(String tag, String msg, Throwable throwable, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.WARN, tag, msg, throwable, printToRemote);
    }

    /**
     * 自定义打印日志信息
     *
     * @param tag              标签
     * @param msg              信息
     * @param printThreadInfo  是否打印线程信息
     * @param printProcessInfo 是否打印进程信息
     * @param printStackTrace  是否答应调用栈信息
     */
    public static void w(String tag, String msg, boolean printThreadInfo, boolean printProcessInfo, boolean printStackTrace) {
        checkInitialized();
        handlePrint(LogLevel.WARN, tag, msg, printThreadInfo, printProcessInfo, printStackTrace, true);
    }

    public static void e(String msg) {
        e(null, msg);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, true);
    }

    public static void e(String tag, String msg, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.ERROR, tag, msg, printToRemote);
    }

    public static void e(String msg, Throwable throwable) {
        e(null, msg, throwable, true);
    }

    public static void e(String msg, Throwable throwable, boolean printToRemote) {
        e(null, msg, throwable, printToRemote);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        e(tag, msg, throwable, true);
    }

    public static void e(String tag, String msg, Throwable throwable, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.ERROR, tag, msg, throwable, printToRemote);
    }

    /**
     * 自定义打印日志信息
     *
     * @param tag              标签
     * @param msg              信息
     * @param printThreadInfo  是否打印线程信息
     * @param printProcessInfo 是否打印进程信息
     * @param printStackTrace  是否答应调用栈信息
     */
    public static void e(String tag, String msg, boolean printThreadInfo, boolean printProcessInfo, boolean printStackTrace, boolean printToRemote) {
        checkInitialized();
        handlePrint(LogLevel.ERROR, tag, msg, printThreadInfo, printProcessInfo, printStackTrace, printToRemote);
    }

    /**
     * 将缓存的日志立即写入到文件中，主要解决app被杀掉时日志丢失问题
     */
    public static void flushCacheLogToFile() {
        checkInitialized();
        FilePrinter filePrinter = sPrinterSet.getFilePrinter();

        if (filePrinter != null) {
            Log.d("OkLog", "开始将缓存写入文件");
            filePrinter.flushCacheLogToFile();
        }
    }

    /**
     * 处理日志的输出
     */
    private static void handlePrint(int logLevel, String tag, String msg, boolean printToRemote) {
        //需要输出
        if(sLogConfig.mIgnoreTag){
            tag =sLogConfig.getStackTraceInfo(true);
        }else {
            tag = TextUtils.isEmpty(tag) ? sLogConfig.getStackTraceInfo(true) : tag;
        }

        if (printToRemote) {
            putLogItem(logLevel, tag, msg);
        }


        sPrinterSet.handlePrintln(logLevel, tag, msg);
    }

    private static void handlePrint(int logLevel, String tag, String msg, boolean printTreadInfo, boolean printProcessInfo, boolean printStackTrace, boolean printToRemote) {
        tag = TextUtils.isEmpty(tag) ? sLogConfig.mTag : tag;

        if (printToRemote) {
            putLogItem(logLevel, tag, msg);
        }

        sPrinterSet.handlePrintln(logLevel, tag, msg, printTreadInfo, printProcessInfo, printStackTrace);
    }

    /**
     * 处理日志的输出
     */
    private static void handlePrint(int logLevel, String tag, String msg, Throwable tr, boolean printToRemote) {
        String trMsg = LogUtils.getThrowableInfo(tr);
        msg = msg + "\n" + trMsg;

        handlePrint(logLevel, tag, msg, printToRemote);
    }

    /**
     * 开启实时日志
     */
    public static void startOnlineDebug() {
        Log.e("BHLog_tj", "startOnlineDebug");
        if (!needOnlineDebug) {
            needOnlineDebug = true;
            sLogItems = new LinkedBlockingQueue<>();
        }
    }

    /**
     * 停止实时日志
     */
    public static void stopOnlineDebug() {
        needOnlineDebug = false;
        if (sLogItems != null) {
            sLogItems.clear();
            sLogItems = null;
        }
    }

    private static void putLogItem(int loglevel, String tag, String msg) {
        //检查是否开启实施日志
        if (!needOnlineDebug || sLogItems == null) {
            return;
        }

        try {
            String simpleStackTrace = LogUtils.getSimpleStackTrace();

//            sLogItems.put(new LogItem(loglevel,tag,msg,simpleStackTrace));
            sLogItems.offer(new LogItem(loglevel, msg, tag, simpleStackTrace));

            Log.e("BHLog_tj", "putLogItem()  blockingqueue size=" + sLogItems.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LogItem getLogItem() {
        if (sLogItems == null || !needOnlineDebug) {
            return null;
        }

        try {
            Log.e("BHLog_tj", "getLogItem(),size=" + sLogItems.size());

            if (sLogItems != null) {
                return sLogItems.take();
            }
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
