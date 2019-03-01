package com.oterman.oklog.common;

import android.util.Log;

/**
 * Created by tian on 2017/8/21 0021.
 */

public class LogLevel {
    public static  final int ALL=1;

    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARNING = Log.WARN;
    public static final int ERROR = Log.ERROR;

    public static  final int OFF=7;

    public static String getLevelName(int logLevel) {
        String levelName;
        switch (logLevel) {
            case VERBOSE:
                levelName = "VERBOSE";
                break;
            case DEBUG:
                levelName = "DEBUG";
                break;
            case INFO:
                levelName = "INFO";
                break;
            case WARNING:
                levelName = "WARNING";
                break;
            case ERROR:
                levelName = "ERROR";
                break;
            default:
                throw  new RuntimeException("logLevel输入不正确");
        }
        return levelName;
    }

    public static String getShortLevelName(int logLevel) {
        String levelName;
        switch (logLevel) {
            case VERBOSE:
                levelName = "V";
                break;
            case DEBUG:
                levelName = "D";
                break;
            case INFO:
                levelName = "I";
                break;
            case WARNING:
                levelName = "W";
                break;
            case ERROR:
                levelName = "E";
                break;
            default:
                    throw  new RuntimeException("logLevel输入不正确");
        }
        return levelName;
    }

    public static int parseNameToLevel(String shortName){
        int logLevel;
        switch (shortName){
            case "V":
            case "v":
                logLevel =VERBOSE;
                break;
            case "D":
            case "d":
                logLevel = DEBUG;
                break;
            case "I":
            case "i":
                logLevel = INFO;
                break;
            case "W":
            case "w":
                logLevel = WARNING;
                break;
            case "E":
            case "e":
                logLevel = ERROR;
                break;

            case "all":
            case "ALL":
            default:
                logLevel=LogLevel.ALL;
        }
        return logLevel;
    }
}
