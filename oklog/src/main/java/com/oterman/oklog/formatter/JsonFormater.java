package com.oterman.oklog.formatter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.oterman.oklog.common.LogConstants;

import static com.oterman.oklog.common.LogConstants.JSON_INDENT;

/**
 * Author：Oterman on 2017/8/31 0031 08:28
 * Email：oterman@126.com
 */

public class JsonFormater implements Formatter<String> {

    @Override
    public String format(String msg,boolean printOneline) {
        if (msg == null || msg.trim().length() == 0|| printOneline) {
            return msg;
        }

        //从一个字符串中提取json
        int firstIndexOfBrace = msg.indexOf("{");//大括号首次索引
        int firstIndexOfBracket = msg.indexOf("[");//中括号首次索引

        if(firstIndexOfBrace==-1){//不是json串
            return msg;
        }

        StringBuilder sb=new StringBuilder();
        try {
            int lastIndexOfBraket=msg.lastIndexOf("]");
            if(firstIndexOfBrace<firstIndexOfBracket||firstIndexOfBracket==-1||lastIndexOfBraket<firstIndexOfBrace){//jsonObject
//            if(firstIndexOfBrace<firstIndexOfBracket||firstIndexOfBracket==-1){//jsonObject
                int lastIndexOfBrace = msg.lastIndexOf("}");
                JSONObject jsonObject;

                if(firstIndexOfBrace==0){
                    String json=msg.substring(0,lastIndexOfBrace+1);
                    jsonObject= new JSONObject(json);

                     sb.append(jsonObject.toString(LogConstants.JSON_INDENT))
                       .append("\n")
                       .append(msg.substring(lastIndexOfBrace+1));

                }else {//前部有干扰信息
                    String head=msg.substring(0,firstIndexOfBrace);
                    String json=msg.substring(firstIndexOfBrace,lastIndexOfBrace+1);
                    String tail=msg.substring(lastIndexOfBrace+1);

                    jsonObject= new JSONObject(json);

                    sb.append(head).append("\n")
                      .append(jsonObject.toString(LogConstants.JSON_INDENT))
                      .append("\n")
                      .append(tail);
                }

            }else {//jsonArray
//                int lastIndexOfBraket=msg.lastIndexOf("]");
                JSONArray jsonArray;

                if(firstIndexOfBracket==0){
                    String json=msg.substring(0,lastIndexOfBraket+1);
                    jsonArray= new JSONArray(json);

                    sb.append(jsonArray.toString(JSON_INDENT))
                            .append("\n")
                            .append(msg.substring(lastIndexOfBraket+1));

                }else {
                    String head=msg.substring(0,firstIndexOfBracket);
                    String json=msg.substring(firstIndexOfBracket,lastIndexOfBraket+1);
                    String tail=msg.substring(lastIndexOfBraket+1);

                    jsonArray= new JSONArray(json);

                    sb.append(head).append("\n")
                            .append(jsonArray.toString(JSON_INDENT))
                            .append("\n")
                            .append(tail);
                }
            }

        } catch (Exception e) {
//            Log.i("JsonFormater", "not a json：" + e.getMessage());
            return msg;
        }
        return sb.toString();
    }

}
