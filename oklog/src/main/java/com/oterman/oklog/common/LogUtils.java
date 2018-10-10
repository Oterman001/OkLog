package com.oterman.oklog.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.oterman.oklog.OkLog;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Oterman on 2017/8/22 0022.
 */

public class LogUtils {
    private static  final String FORMAT_DATE="yyyy_MM_dd";
    private static  final String FORMAT_TIME="yyyy-MM-dd HH:mm:ss.SSS";

    private  static  String HDLOG_STACK_TACE_ORIGIN;
    private  static  ThreadLocal<SimpleDateFormat> sDateThreadLocal;
    private  static  ThreadLocal<SimpleDateFormat> sTimeThreadLocal;

    private  static  SimpleDateFormat sDateFormat;
    private  static  SimpleDateFormat sTimeFormat;
    private  static  NumberFormat sNumberFormat;

    static {
        String xlogClassName = OkLog.class.getName();
        HDLOG_STACK_TACE_ORIGIN = xlogClassName.substring(0, xlogClassName.lastIndexOf('.') + 1);

        sDateThreadLocal =new ThreadLocal<SimpleDateFormat>(){
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(FORMAT_DATE);
            }
        };

        sTimeThreadLocal =new ThreadLocal<SimpleDateFormat>(){
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(FORMAT_TIME);
            }
        };

        sNumberFormat = NumberFormat.getNumberInstance();
        sNumberFormat.setMinimumIntegerDigits(2);
    }
    private static SimpleDateFormat getDateFormat(){
        sDateFormat = sDateThreadLocal.get();
        if(sDateFormat ==null){
            sDateFormat =new SimpleDateFormat(FORMAT_DATE);
            sDateThreadLocal.set(sDateFormat);
        }

        return sDateFormat;
    }

    private  static SimpleDateFormat getTimeFormat(){
        sTimeFormat = sTimeThreadLocal.get();
        if(sTimeFormat ==null){
            sTimeFormat =new SimpleDateFormat(FORMAT_TIME);
            sTimeThreadLocal.set(sTimeFormat);
        }
        return sTimeFormat;
    }

    /**
     * 获取最新的日志文件  根据文件名排序  仅当天的文件
     */
    public static  String  getLatestFileName(String dirPath,final String moduleName){
        File dir=new File(dirPath);

        final String curentDate=getDateFormat().format(new Date());

        if (dir.exists()&&dir.isDirectory()){
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().contains(curentDate)&&file.getName().contains(moduleName);
                }
            });

            List<File> fileList = Arrays.asList(files);
            //文件名从大到小排列
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return -o1.getName().compareTo(o2.getName());
                }
            });

            return fileList.size()>0?fileList.get(0).getName():null;

        }else {
            return null;
        }
    }

    /**
     * 获取方法调用栈信息
     */
    public static StackTraceElement[] getStackTrace() {
        StackTraceElement[] realStack=null;

        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace=throwable.getStackTrace();
        int ignoreDepth = 0;
        int allDepth = stackTrace.length;
        String className;

        for (int i = allDepth - 1; i >= 0; i--) {
            className = stackTrace[i].getClassName();
            if (className.startsWith(HDLOG_STACK_TACE_ORIGIN)) {//排除掉干扰方法栈信息
                ignoreDepth = i + 1;
                break;
            }
        }
        int realDepth = allDepth - ignoreDepth;
        realStack= new StackTraceElement[realDepth];

        System.arraycopy(stackTrace, ignoreDepth, realStack, 0, realDepth);

        return realStack;
    }

    public static String  getSimpleStackTrace(){
        StackTraceElement[] stackTrace = getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[0];

        if(stackTraceElement.getFileName().contains("LogUtil")){
            stackTraceElement=stackTrace[1];
        }

        StringBuilder sb=new StringBuilder("(")
                .append(stackTraceElement.getFileName())
                .append(":")
                .append(stackTraceElement.getLineNumber())
                .append(")");

        return sb.toString();
    }


    //获取当前线程的栈帧信息
    public static StackTraceElement getTargetStackTraceElement() {
        StackTraceElement targetStackTrace = null;
        boolean shouldTrace = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
                boolean isLogMethod = stackTraceElement.getClassName().equals(OkLog.class.getName());
                if (shouldTrace && !isLogMethod) {
                    targetStackTrace = stackTraceElement;
                    break;
                }
            shouldTrace = isLogMethod;
        }
        return targetStackTrace;
    }

    /**
     * 根据日期产生文件名
     * @return
     */
    public static String generateFileName() {
        return generateFileName(null);
    }

    //根据传入的引子生成文件名
    public static String generateFileName(String seedFileName) {
        return  generateFileName(seedFileName,0);
    }

    public static String generateFileName(String seedFileName,int part) {
        if (TextUtils.isEmpty(seedFileName)){
            return  getDateFormat().format(new Date())+"_"+ sNumberFormat.format(part)+".txt";
        }
        return  getDateFormat().format(new Date())+"_"+seedFileName+"_"+ sNumberFormat.format(part)+".txt";
    }

    /**
     * 获取运行进程信息
     * @param cxt
     * @param pid
     * @return
     */
    public static ActivityManager.RunningAppProcessInfo getRunningProcessInfo(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }

        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo;
            }
        }
        return null;
    }

    public static String getNthDayBeforeStr(int nthDayBefore){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,-nthDayBefore);
        return getDateFormat().format(cal.getTime());
    }

    /**
     * 格式化日期
     */
    public static String formatTime(long timeMillis) {
        return getTimeFormat().format(new Date(timeMillis));
    }

    /**
     * 格式化时间戳
     */
    public static String formatCurrentTime() {
        return getTimeFormat().format(new Date());
    }

    /**
     *获取日志头信息
     */
    public static String getLogHeadInfo(Context context){
        String appVersionName = getAppVersionName(context);
        int appVersionCode = getAppVersionCode(context);

//        TelephonyManager telephonyManager= (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
//        String deviceId = telephonyManager.getDeviceId();

        String  headLog = "\n************* OkLog Log Head ****************" +
                "\nDevice Manufacturer : " + Build.MANUFACTURER +// 设备厂商
                "\nDevice Model        : " + Build.MODEL +// 设备型号
                "\nAndroid Version     : " + Build.VERSION.RELEASE +// 系统版本
                "\nAndroid SDK         : " + Build.VERSION.SDK_INT +// SDK版本
                "\nApp VersionName     : " + appVersionName +
                "\nApp VersionCode     : " + appVersionCode +
                "\nApp Max Mem         : " + Runtime.getRuntime().maxMemory()/1024/1024+"MB" +
//                "\nProcess             : " + getRunningProcessInfo(sContext, Process.myPid()).processName +
                "\nTimestamp           : " + LogUtils.formatCurrentTime() +
                "\nTotalMem\\AvailMem   : " + LogUtils.getMemoryInfo(context) +
                "\n************* OkLog Log Head ****************\n\n";
        return headLog;
    }

    public static String getCrashInfo(Context context,Thread t,Throwable tr){
        String appVersionName = getAppVersionName(context);
        int appVersionCode = getAppVersionCode(context);

        String  crashLog =
                "\n*********************** Crash Log start **************************" +
                "\nDevice Manufacturer : " + Build.MANUFACTURER +// 设备厂商
                "\nDevice Model        : " + Build.MODEL +// 设备型号
                "\nAndroid Version     : " + Build.VERSION.RELEASE +// 系统版本
                "\nAndroid SDK         : " + Build.VERSION.SDK_INT +// SDK版本
                "\nApp VersionName     : " + appVersionName +
                "\nApp VersionCode     : " + appVersionCode +
                "\nApp Max Mem         : " + Runtime.getRuntime().maxMemory()/1024/1024+"MB" +
//                "\nProcess             : " + getRunningProcessInfo(sContext, Process.myPid()).processName +
                "\nTimestamp           : " + LogUtils.formatCurrentTime() +
                "\nCurrentThread       : " + t.getName()+"#"+t.getId()+
                "\nTotalMem\\AvailMem   : " + LogUtils.getMemoryInfo(context) +
                "\nCrash Detail        : \n\n" + LogUtils.getThrowableInfo(tr)+
                "\n*********************** Crash Log end **************************\n\n";

        return crashLog;
    }


    /**
     * 获取版本名
     */
    public static String getAppVersionName(Context context){
        PackageInfo packageInfo = getPackageInfo(context);
        return packageInfo==null?null:packageInfo.versionName;
    }

    /**
     * 获取版本号
     */
    public static int getAppVersionCode(Context context){
        PackageInfo packageInfo = getPackageInfo(context);
        return packageInfo==null?null:packageInfo.versionCode;
    }

    /**
     * 获取包信息
     */
    private static PackageInfo getPackageInfo(Context context){
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(packageName,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取内存信息
     */
    private static String getMemoryInfo(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem/1024/1024+"MB\\"+memoryInfo.availMem/1024/1024+"MB";
    }

    /**
     * 获取异常信息
     */
    public static String  getThrowableInfo(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * 获取Anr信息
     * @param processInfo
     * @return
     */
    public static String getAnrInfo(ActivityManager.ProcessErrorStateInfo processInfo,String traceFileName,Context context) {
        String appVersionName = getAppVersionName(context);
        int appVersionCode = getAppVersionCode(context);
        if(TextUtils.isEmpty(traceFileName)){
            traceFileName="traces.txt";
        }

        String anrInfo =
                "\n*********************** ANR Log start **************************" +
                "\nDevice Manufacturer : " + Build.MANUFACTURER +// 设备厂商
                "\nDevice Model        : " + Build.MODEL +// 设备型号
                "\nAndroid Version     : " + Build.VERSION.RELEASE +// 系统版本
                "\nAndroid SDK         : " + Build.VERSION.SDK_INT +// SDK版本
                "\nApp VersionName     : " + appVersionName +
                "\nApp VersionCode     : " + appVersionCode +
                "\nApp Max Mem         : " + Runtime.getRuntime().maxMemory()/1024/1024+"MB" +
                "\nProcessName         : " + processInfo.processName +
                "\nProcessId           : " + processInfo.pid +
                "\nANR_Tag             : " + processInfo.tag +
                "\nANR_StackTrace      : " + processInfo.stackTrace +
                "\nANR_ShortMsg        : " + processInfo.shortMsg +
                "\nANR_LONGMSG         : \n\n" + processInfo.longMsg +
                "\n\nFor more details,please refer to "+traceFileName+"!!!!"+
                "\n*********************** ANR Log end **************************\n\n";
        return anrInfo;
    }



    /**
     * 导出trace文件
     */
    public static String exportTraceFile() {
        BufferedWriter bufferedWriter=null;
        BufferedReader bufferedReader=null;
        String fileName=null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File("/data/anr/traces.txt")));
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            fileName="traces_"+dateFormat.format(new Date())+".txt";

            File destFile = new File(LogConstants.DEFAULT_LOG_DIR, fileName);
            bufferedWriter = new BufferedWriter(new FileWriter(destFile));

            Log.d("LogUtils", "----------------开始导出traces.txt文件--------------");
            String line=null;
            while((line=bufferedReader.readLine())!=null){
               bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            Log.d("LogUtils", "----------------traces.txt导出完毕-->"+destFile.getCanonicalPath()+"--------------");


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //关闭资源
            if(bufferedReader!=null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    bufferedReader=null;
                }
            }

            if(bufferedWriter!=null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    bufferedWriter=null;
                }
            }

            return fileName;

        }


    }
}
