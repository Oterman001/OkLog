package com.oterman.oklog.test;

import com.oterman.oklog.OkLog;

/**
 * Author：Oterman on 2017/10/27 0027 11:14
 * Email：oterman@126.com
 */

public class StackTraceTest {
    /**
     * 获取方法调用栈信息
     */
    public static StackTraceElement[] getStackTrace() {
        StackTraceElement[] stackTrace=new Throwable().getStackTrace();
        int ignoreDepth = 0;
        int allDepth = stackTrace.length;
        String className;
        for (int i = allDepth - 1; i >= 0; i--) {
            className = stackTrace[i].getClassName();
            if (className.startsWith("")) {//排除掉干扰方法栈信息
                ignoreDepth = i + 1;
                break;
            }
        }
        int realDepth = allDepth - ignoreDepth;
        StackTraceElement[] realStack = new StackTraceElement[realDepth];
        System.arraycopy(stackTrace, ignoreDepth, realStack, 0, realDepth);
        return realStack;
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


    public static void main(String[] args) {
//        StackTraceElement[] stackTraces = new Throwable().getStackTrace();
        f1();
    }

    public  static  void f1(){
        f2();
    }

    public static  void f2(){
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        for (int i=0;i<stackTraces.length;i++){
            StackTraceElement stackTrace = stackTraces[i];

            String methodName = stackTrace.getMethodName();
            String className = stackTrace.getClassName();
            String fileName = stackTrace.getFileName();
            int lineNumber = stackTrace.getLineNumber();

            System.out.print("  linenum:"+lineNumber);
            System.out.print("   methodName:"+methodName);
            System.out.print("   className:"+className);
            System.out.println("   fileName:"+fileName);
            System.out.println("--------------------------------");
        }
    }

}
