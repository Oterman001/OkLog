package com.oterman.oklog.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author：Oterman on 2017/10/10 0010 10:11
 */
public class LogMsgFilter implements  IFilter {
    String mTag;//根据tag来过滤
    boolean mSupportReg;//是否支持正则表达式

    Pattern mPattern;

    @Override
    public boolean accept(String logMsg) {
//        Log.e("LogMsgFilter_tj", "过滤日志内容：" + logMsg);
        if(mTag ==null)return  true;
        boolean flag=false;
        //判断是否过滤
        if(mSupportReg){//使用正则表达式
            Matcher matcher = mPattern.matcher(logMsg);
            flag=matcher.matches();
        }else {//不使用正则表达式
            flag=mTag.equals(logMsg);
        }

//        Log.e("LogMsgFilter_tj", "过滤日志内容->" + flag);

        return flag;
    }

    public LogMsgFilter(String tag){
        this.mTag =tag;
        this.mSupportReg =true;
        mPattern =Pattern.compile(tag,Pattern.MULTILINE);
    }

    public LogMsgFilter() {

    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    public boolean isSupportReg() {
        return mSupportReg;
    }

    public void setSupportReg(boolean supportReg) {
        this.mSupportReg = supportReg;
    }
}
