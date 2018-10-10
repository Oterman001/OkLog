package com.oterman.oklog.printer;

import static com.oterman.oklog.OkLog.sLogConfig;

import java.util.Arrays;
import java.util.List;

import com.oterman.oklog.OkLog;
import com.oterman.oklog.common.LogConfig;

import android.os.Process;

/**
 * Created by tian on 2017/8/21 0021.
 */

public class PrinterSet {
    //    private Printer[] mPrinters;
    //    private RemotePrinter mRemotePrinter;
    private List<Printer> mPrinterList;

    public PrinterSet(Printer... printer) {
        //        mPrinters =printer;
        mPrinterList = Arrays.asList(printer);
    }

    /**
     * 检查是否需要打印
     */
    private static boolean checkNeedPrint(LogConfig logConfig, int loglevel, String tag, String msg) {
        if (loglevel < logConfig.mLogLevel) {
            return false;//日志级别小于配置的级别  不打印
        }

        boolean flag = false;
        //检查过滤器，没有过滤器默认需要打印
        if (logConfig.mLogTagFilter == null || logConfig.mLogTagFilter.accept(tag)) {
            flag = true;
        }

        //内容过滤器
        if (logConfig.mLogMsgFilter == null || logConfig.mLogMsgFilter.accept(msg)) {
            flag = true;
            //            Log.e("PrinterSet_tj", "logMsgFilter="+logConfig.mLogMsgFilter);
        }

        return flag;
    }

    public void handlePrintln(int logLevel, String tag, String message) {
        for (Printer printer : mPrinterList) {
            LogConfig logConfig = printer.getLogConfig();
            if (logConfig == null) {//没有个性化配置，使用默认配置
                logConfig = sLogConfig;
            }

            //根据配置，检查是否需要打印
            checkNeedPrint(logConfig, logLevel, tag, message);

            //根据配置，获取拼接后的msg
            String fullMsg = getFullMsg(logConfig, message);

            printer.println(logLevel, tag, fullMsg);

        }

    }

    public void handlePrintln(int logLevel, String tag, String message, boolean printTreadInfo,
                              boolean printProcessInfo, boolean printStackTrace) {
        for (Printer printer : mPrinterList) {
            LogConfig logConfig = printer.getLogConfig();
            if (logConfig == null) {//没有个性化配置，使用默认配置
                logConfig = sLogConfig;
            }

            //根据配置，检查是否需要打印
            checkNeedPrint(logConfig, logLevel, tag, message);

            //根据配置，获取拼接后的msg
            String fullMsg = getFullMsg(logConfig, message, printTreadInfo, printProcessInfo, printStackTrace);

            printer.println(logLevel, tag, fullMsg);

        }

    }

    public void printCrash(String tag, String message) {
        for (Printer printer : mPrinterList) {
            printer.printCrash(tag, message);
        }

    }

    /**
     * 根据logConfig获取完整信息 包含线程信息  调用栈等
     */
    private String getFullMsg(LogConfig logConfig, String msg) {
        return getFullMsg(logConfig, msg, logConfig.mPrintThreadInfo, logConfig.mPrintProcessInfo,
                logConfig.mPrintStackTrace);
    }

    private String getFullMsg(LogConfig logConfig, String msg, boolean printTreadInfo, boolean printProcessInfo,
                              boolean printStackTrace) {

        StringBuilder sb = new StringBuilder();

        sb.append(logConfig.getProcessInfo(OkLog.sContext, Process.myPid(), printProcessInfo));
        sb.append(logConfig.getThreadInfo(printTreadInfo));
        sb.append(logConfig.getFormattedJson(msg));

        //如果不忽略自己的tag时，末尾加上堆栈信息

        if (!logConfig.mIgnoreTag) {
            sb.append(logConfig.getStackTraceInfo(true));
        }

        return sb.toString().trim();
    }

    public FilePrinter getFilePrinter() {
        for (Printer printer : mPrinterList) {
            if (printer instanceof FilePrinter) {
                return (FilePrinter) printer;
            }
        }
        return null;
    }

    /**
     * 添加远程输出
     */
    public boolean startRemotePrinter(String cmdLine, String ip, int port) {
        //        if(mRemotePrinter!=null){
        //            return false;
        //        }
        //
        ////        mRemotePrinter = new RemotePrinter();
        //        mPrinterList.add(mRemotePrinter);
        //
        //        mRemotePrinter.start(cmdLine,ip,port);

        return true;
    }

    /**
     * 停止远程输出
     */
    public boolean stopRemotePrinter() {
        //        if(mRemotePrinter==null){
        //            return false;
        //        }
        //
        //        mRemotePrinter.stop();
        //
        //        boolean flag = mPrinterList.remove(mRemotePrinter);
        //
        //        mRemotePrinter=null;
        //
        //        return flag;
        return false;
    }
}
