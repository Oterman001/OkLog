package com.oterman.oklog.printer;

import com.oterman.oklog.common.LogConfig;

/**
 * Created by Oterman on 2017/8/21 0021.
 */

public interface Printer {
    /**
     * 打印日志并换行
     * @param logLevel 日志级别
     * @param tag 日志标签
     * @param message 日志消息
     */
    void println(int logLevel,String tag,String message);

    /**
     * crash时打印日志
     * @param tag 日志标签
     * @param message 日志消息
     */
    void printCrash(String tag,String message);

    /**
     * 获取输出渠道的个性化配置
     */
    LogConfig getLogConfig();

    /**
     * 为输出渠道单独配置
     */
    void setLogConfig(LogConfig logConfig);
}
