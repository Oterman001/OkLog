package com.oterman.oklog.formatter;

/**
 * Created by tian on 2017/8/21 0021.
 *
 * 格式化调用栈信息
 */

public class StackTraceFormatter implements Formatter<StackTraceElement[]> {
    @Override
    public String format(StackTraceElement[] stackTrace,boolean printOneline) {
//        StringBuilder sb = new StringBuilder(256);
        if (stackTrace == null || stackTrace.length == 0) {
            return null;
        } else {
            return "__" + stackTrace[0].toString()+"\n";
        }
    }


}
