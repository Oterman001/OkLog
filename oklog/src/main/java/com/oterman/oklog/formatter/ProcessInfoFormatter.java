package com.oterman.oklog.formatter;

import android.app.ActivityManager;

/**
 * Created by Oterman on 2017/8/22 0022.
 * 格式化进程信息
 */

public class ProcessInfoFormatter implements Formatter<ActivityManager.RunningAppProcessInfo> {

    @Override
    public String format(ActivityManager.RunningAppProcessInfo data, boolean printOneline){
        if (data == null) {
            return "";
        }

        if (printOneline) {

            return "Pid:" + data.pid + "  ";
        }else {
            return "Pid:" + data.pid + "\n";

        }
    }

}
