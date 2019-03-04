package com.oterman.oklog;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
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

        Log.d("OkLog", "OkLog初始化成功");
    }

    /**
     * 每隔1s去检测ANR是否发生
     */
    private static void initDetectAnr() {
        if (sLogConfig.mDetectANR){
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
        }else {
            Log.e("OkLog","不检测anr");
        }


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

    public static void v(String msg,int stackTraceDepth) {
        v(null, msg,stackTraceDepth);
    }

    public static void v(String tag, String msg) {
        v(tag, msg, 1);
    }

    public static void v(String tag, String msg, int  stackTraceDepth) {
        checkInitialized();
        handlePrint(LogLevel.VERBOSE, tag, msg, stackTraceDepth);
    }

    public static void v(String msg, Throwable throwable) {
        v(null, msg, throwable);
    }
    public static void v(String tag, String msg, Throwable throwable) {
        checkInitialized();
        handlePrint (LogLevel.VERBOSE, tag, msg, throwable);
    }

    public static void d(String msg) {
        d(null, msg);
    }

    public static void d(String msg,int stackTraceDepth) {
        d(null, msg,stackTraceDepth);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, 1);
    }

    public static void d(String tag, String msg, int  stackTraceDepth) {
        checkInitialized();
        handlePrint(LogLevel.DEBUG, tag, msg, stackTraceDepth);
    }

    public static void d(String msg, Throwable throwable) {
        d(null, msg, throwable);
    }
    public static void d(String tag, String msg, Throwable throwable) {
        checkInitialized();
        handlePrint (LogLevel.DEBUG, tag, msg, throwable);
    }
    

    public static void i(String msg) {
        i(null, msg);
    }

    public static void i(String msg,int stackTraceDepth) {
        i(null, msg,stackTraceDepth);
    }

    public static void i(String tag, String msg) {
        i(tag, msg, 1);
    }

    public static void i(String tag, String msg, int  stackTraceDepth) {
        checkInitialized();
        handlePrint(LogLevel.INFO, tag, msg, stackTraceDepth);
    }

    public static void i(String msg, Throwable throwable) {
        i(null, msg, throwable);
    }
    public static void i(String tag, String msg, Throwable throwable) {
        checkInitialized();
        handlePrint (LogLevel.INFO, tag, msg, throwable);
    }


    public static void w(String msg) {
        w(null, msg);
    }

    public static void w(String msg,int stackTraceDepth) {
        w(null, msg,stackTraceDepth);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, 1);
    }

    public static void w(String tag, String msg, int  stackTraceDepth) {
        checkInitialized();
        handlePrint(LogLevel.WARNING, tag, msg, stackTraceDepth);
    }

    public static void w(String msg, Throwable throwable) {
        w(null, msg, throwable);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        checkInitialized();
        handlePrint(LogLevel.WARNING, tag, msg, throwable);
    }

    public static void e(String msg) {
        e(null, msg);
    }

    public static void e(String msg,int stackTraceDepth) {
        e(null, msg,stackTraceDepth);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, 1);
    }

    public static void e(String tag, String msg, int  stackTraceDepth) {
        checkInitialized();
        handlePrint(LogLevel.ERROR, tag, msg, stackTraceDepth);
    }

    public static void e(String msg, Throwable throwable) {
        e(null, msg, throwable);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        checkInitialized();
        handlePrint(LogLevel.ERROR, tag, msg, throwable);
    }

    /**
     * 处理日志的输出
     */
    private static void handlePrint(int logLevel, String tag, String msg, Throwable tr) {
        String trMsg = LogUtils.getThrowableInfo(tr);
        msg = msg + "\n" + trMsg;
        handlePrint(logLevel, tag, msg,1);
    }


    /**
     * 处理日志的输出
     */
    private static void handlePrint(int logLevel, String tag, String msg, int stackTraceDepth ) {
        //需要输出
        if(sLogConfig.mIgnoreTag&&stackTraceDepth==1){
            tag =sLogConfig.getStackTraceInfo(true);
        }else {
            tag = TextUtils.isEmpty(tag) ? sLogConfig.mTag :  sLogConfig.mTag+"_"+tag;
        }
        sPrinterSet.handlePrintln(logLevel, tag, msg,stackTraceDepth);
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


}
