package com.oterman.oklog.formatter;

/**
 * Created by tian on 2017/8/21 0021.
 */

public interface Formatter<T> {

    /**
     * 将data 格式化成字符串
     */
    public String format(T data,boolean printOneline);

}
