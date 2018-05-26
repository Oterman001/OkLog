package com.oterman.oklog.common;

import android.content.Context;

import com.oterman.oklog.filter.IFilter;
import com.oterman.oklog.filter.LogMsgFilter;
import com.oterman.oklog.formatter.JsonFormater;
import com.oterman.oklog.formatter.ProcessInfoFormatter;
import com.oterman.oklog.formatter.StackTraceFormatter;
import com.oterman.oklog.formatter.ThreadFormatter;


/**
 * Created by tian on 2017/8/21 0021.
 * 日志配置
 */
public class LogConfig {
    public int mLogLevel = LogLevel.ALL;//控制打印日志的级别,低于级别的不能打印
    public String mTag = LogConstants.DEFAULT_LOG_TAG;//打印日志的标签
    public String mLogDir =LogConstants.DEFAULT_LOG_DIR;//日志默认目录
    public IFilter mLogTagFilter =null;//日志过滤器
    public IFilter mLogMsgFilter =null;//日志过滤器

    public boolean mPrintThreadInfo =false;//是否打印线程信息 默认打印
    public boolean mPrintStackTrace =true;//是否打印调用栈信息
    public boolean mPrintProcessInfo =false;//是否打印进程信息
    public boolean mShouldFormatJson =true;//是否格式化json
    public boolean mPrintCrash =true;
    public boolean mPrintOneline=false;//打印成一行

    private ThreadFormatter mThreadFormatter;
    private StackTraceFormatter mStackTraceFormatter;
    private ProcessInfoFormatter mProcessInfoFormatter;
    private JsonFormater mJsonFormater;

    /**
     * 格式化json
     */
    public String getFormattedJson(String msg){

        if(!mShouldFormatJson)return msg;

        if(mJsonFormater ==null){
            mJsonFormater =new JsonFormater();
        }
        return mJsonFormater.format(msg,mPrintOneline);
    }

    /**
     *格式化线程信息
     */
    public String  getThreadInfo(){
        if (!mPrintThreadInfo) return "";
        if (mThreadFormatter ==null) mThreadFormatter =new ThreadFormatter();
        return mThreadFormatter.format(Thread.currentThread(),mPrintOneline);
    }

    public String  getThreadInfo(boolean shouldPrintThradInfo){
        if (!shouldPrintThradInfo) return "";

        if (mThreadFormatter ==null){
            mThreadFormatter =new ThreadFormatter();
        }

        return mThreadFormatter.format(Thread.currentThread(),mPrintOneline);
    }

    /**
     * 格式化进程信息
     */
    public String  getProcessInfo(Context contxt,int pid){
        return getProcessInfo(contxt,pid, mPrintProcessInfo);
    }

    public String  getProcessInfo(Context contxt,int pid,boolean shouldPrintProcessInfo){
        if (!shouldPrintProcessInfo) return "";

        if (mProcessInfoFormatter ==null){
            mProcessInfoFormatter =new ProcessInfoFormatter();
        }

        return mProcessInfoFormatter.format(LogUtils.getRunningProcessInfo(contxt, pid),mPrintOneline);
    }

    /**
     * 格式化调用栈信息
     */
    public String getStackTraceInfo(){
        return getStackTraceInfo(mPrintStackTrace);
    }

    public String getStackTraceInfo(boolean shoudPrintStackTrace){
        if (!shoudPrintStackTrace)return "";

        return  LogUtils.getSimpleStackTrace();
    }

    /**
     * 防止外部实例化
     */
    private LogConfig() {
    }

    public static class Builder{
        LogConfig logConfig;
        public Builder() {
            logConfig=new LogConfig();
        }

        /**
         * 是否格式化json数据
         */
        public Builder formatJson(boolean shouldFormatJson){
            logConfig.mShouldFormatJson =shouldFormatJson;
            return this;
        }

        /**
         * 配置日志输出级别  低于该级别的日志不会输出
         */
        public Builder logLevel(int logLevel){
           logConfig.mLogLevel =logLevel;
            return this;
        }

        /**
         * 日志的全局tag
         */
        public Builder tag(String tag){
            logConfig.mTag =tag;
            return this;
        }

        /**
         * 是否打印线程信息(ThreadInfo)
         */
        public Builder ti(boolean printThreadInfo){
            logConfig.mPrintThreadInfo =printThreadInfo;
            return this;
        }

        /**
         * 是否输出crash信息
         */
        public Builder printCrash(boolean shouldPrintCrash){
            logConfig.mPrintCrash =shouldPrintCrash;
            return this;
        }

        /**
         * 是否打印进程信息（ProcessInfo）
         */
        public Builder pi(boolean printProcessInfo){
            logConfig.mPrintProcessInfo =printProcessInfo;
            return this;
        }

        public  Builder printOneline(boolean printOneline){
            logConfig.mPrintOneline=printOneline;
            return this;
        }

        /**
         * 是否打印调用栈信息(StackTrace)
         */
        public Builder st(boolean printStackTrace){
            logConfig.mPrintStackTrace =printStackTrace;
            return this;
        }

        /**
         * 日志绝对目录名
         */
        public Builder logDir(String logDir){
            logConfig.mLogDir =logDir;
            return this;
        }

        /**
         * 日志过滤器
         * @return
         */
        public Builder logTagFilter(IFilter logFilter){
            logConfig.mLogTagFilter =logFilter;
            return this;
        }

        /**
         * 内容过滤
         */
        public Builder logMsgFilter(LogMsgFilter logMsgFilter){
            logConfig.mLogMsgFilter=logMsgFilter;
            return this;
        }

        public  LogConfig build(){
            return logConfig;
        }
    }

    @Override
    public String toString() {
        return "LogConfig{" +
                "mLogLevel=" + mLogLevel +
                ", mTag='" + mTag + '\'' +
                ", mLogTagFilter=" + mLogTagFilter +
                ", mPrintThreadInfo=" + mPrintThreadInfo +
                ", mPrintStackTrace=" + mPrintStackTrace +
                ", mPrintProcessInfo=" + mPrintProcessInfo +
                ", mShouldFormatJson=" + mShouldFormatJson +
                '}';
    }
}
