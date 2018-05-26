package com.oterman.oklog.common;

/**
 * Created by Oterman on 2017/8/21 0021.
 */

public class LogItem {
    public  int mLogLevel;//日志级别
    public  String mTimeStamp;
    public String mMsg;
    public String mTag;
    public String simpleStackTrace;//文件名加行号 方便快速定位

    public String getSimpleStackTrace() {
        return simpleStackTrace;
    }

    public void setSimpleStackTrace(String simpleStackTrace) {
        this.simpleStackTrace = simpleStackTrace;
    }

    boolean mIsCrashInfo =false;

    public LogItem(int logLevel, String tag, String message) {
        this.mLogLevel =logLevel;
        this.mMsg =message;
        this.mTag =tag;
        this.mTimeStamp = LogUtils.formatTime(System.currentTimeMillis());
    }

    public LogItem(int logLevel, String msg, String tag, String simpleStackTrace) {
        mLogLevel = logLevel;
        mMsg = msg;
        mTag = tag;
        this.simpleStackTrace = simpleStackTrace;
        this.mTimeStamp = LogUtils.formatTime(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(mTimeStamp).append("  ")
                .append(LogLevel.getShortLevelName(mLogLevel)).append("/")
                .append(mTag).append("  ")
                .append(mMsg);
        return sb.toString();
    }

    public void setCrashInfo(boolean crashInfo) {
        mIsCrashInfo = crashInfo;
    }

    public boolean isCrashInfo() {
        return mIsCrashInfo;
    }

}
