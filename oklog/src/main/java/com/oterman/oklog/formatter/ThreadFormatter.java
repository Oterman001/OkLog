package com.oterman.oklog.formatter;

/**
 * Created by tian on 2017/8/21 0021.
 * 格式化线程信息
 */

public class ThreadFormatter implements  Formatter<Thread> {
    @Override
    public String format(Thread data,boolean oneline){

        if (oneline){
//            return "Tid:"+data.getName()+"#"+data.getId()+"  ";
            return "Tid:"+data.getId()+"  ";
        }else {
//            return "Tid:"+data.getName()+"#"+data.getId()+"\n";
            return "Tid:"+data.getId()+"\n";
        }

    }
}
