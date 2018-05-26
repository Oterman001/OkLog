package com.oterman.oklog.filter;

/**
 * Author：Oterman on 2017/10/10 0010 10:08
 */

public interface IFilter {

    /**
     * 日志过滤器  根据tag来过滤
     * 返回true表示需要输出，false表示不输出
     */
    public boolean accept(String tag);

}
