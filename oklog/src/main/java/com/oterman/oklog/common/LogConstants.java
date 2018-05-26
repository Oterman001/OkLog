package com.oterman.oklog.common;

import android.os.Environment;

/**
 * Author：Oterman on 2017/8/25 0025 12:50
 * Email：oterman@126.com
 *
 * 日志模块相关常量
 */
public class LogConstants {
    //日志文件默认目录
    public static final String DEFAULT_LOG_DIR= Environment.getExternalStorageDirectory().getAbsolutePath()+"/OkLog/";
//    public static final String DEFAULT_LOG_DIR= Environment.getExternalStorageDirectory()+"/OkLog/log/";
    public static final String DEFAULT_LOG_TAG="OkLog";//默认全局tag
    public static long LOG_WRITE_TIME_INTERVAL =1000*60;//日志写入文件间隔
    public static int LOG_WRITE_NUM_THRESHOLD=200;//条数阈值  达到该阈值才会写出
    public static final int LOG_FILE_KEEP_DAYS = 10;//文件默认保存时间  天数
    public static final int LOG_FILE_SIZE_THRESHOLD = 1024*1024*300;//日志文件大小控制
    public static final int JSON_INDENT = 4;


}
