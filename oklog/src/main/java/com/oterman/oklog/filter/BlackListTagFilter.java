package com.oterman.oklog.filter;

/**
 * Author：Oterman on 2017/10/10 0010 14:45
 */

/**
 * 黑名单过滤，黑名单中的tag不输出，不支持正则
 */
public class BlackListTagFilter implements IFilter {
    String[] blackListTags;

    @Override
    public boolean accept(String targetTag) {
        if (blackListTags !=null&& blackListTags.length>0){

            for (int i = 0; i< blackListTags.length; i++){
                String whiteListTag = blackListTags[i];

                if (whiteListTag.equals(targetTag)){
                    return false;
                }
            }

        }

        return true;
    }

    public BlackListTagFilter(String ... rejectedTags ){
        blackListTags =rejectedTags;
    }

}
