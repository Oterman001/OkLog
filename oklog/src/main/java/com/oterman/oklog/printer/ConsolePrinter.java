package com.oterman.oklog.printer;

import android.util.Log;

import com.oterman.oklog.OkLog;
import com.oterman.oklog.common.LogLevel;
import com.oterman.oklog.common.LogUtils;
import com.oterman.oklog.common.LogConfig;

/**
 * Created by Oterman on 2017/8/21 0021.
 *
 * 默认logcat输出
 */

public class ConsolePrinter implements Printer {
    private  LogConfig mLogConfig;
    private  boolean mHasPrintedHead =false;
    @Override
    public void println(int logLevel, String tag, String message) {
        //打印头信息
        if(!mHasPrintedHead){
            Log.println(LogLevel.INFO,tag, LogUtils.getLogHeadInfo(OkLog.sContext));
            mHasPrintedHead =true;
        }
        Log.println(logLevel,tag,message);
    }

    @Override
    public void printCrash(String tag, String message) {
        //打印头信息
        if(!mHasPrintedHead){
            Log.println(LogLevel.INFO,tag, LogUtils.getLogHeadInfo(OkLog.sContext));
            mHasPrintedHead =true;
        }
        Log.println(LogLevel.ERROR,tag,message);
    }

    @Override
    public LogConfig getLogConfig() {
        return mLogConfig;
    }

    @Override
    public void setLogConfig(LogConfig logConfig) {
        this.mLogConfig =logConfig;
    }

}
