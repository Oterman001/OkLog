package com.oterman.oklog.filter;

/**
 * Author：Oterman on 2017/10/10 0010 14:45
 * Email：oterman@126.com
 */

/**
 * 白名单过滤，只有指定的tag才输出
 */
public class WhiteListTagFilter implements IFilter {
    String[] whiteListTags;

    @Override
    public boolean accept(String tag) {
        if (whiteListTags!=null&&whiteListTags.length>0){

            for (int i=0;i<whiteListTags.length;i++){

                String whiteListTag = whiteListTags[i];
                if (whiteListTag.equals(tag)){
                    return true;
                }
            }
        }
        return false;
    }

    public  WhiteListTagFilter(String ... acceptedTags ){
        whiteListTags=acceptedTags;
    }

}
