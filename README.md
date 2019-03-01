# OkLog

阅读他人代码时，往往会打日志分析实际运行流程

查看输出日志时，我们希望能够快速定位到某个文件的某一行

更有甚者，我们希望观察某个方法在运行时的调用过程，即打印出堆栈信息

但不能所有日志都打印堆栈信息，不然会影响输出体验

每次都要输入TAG,可否不用输入TAG呢

这些需求，使用OkLog爽爽的搞定！

# 引入OkLog
## 第一步  添加仓库地址
在Android 工程根目录的grade文件中添加仓库： 

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

## 第二步  
在你的app模块里引入依赖：

```
	dependencies {
	        implementation 'com.github.Oterman001:OkLog:{latestVersion}'
	}
	

```
latestVersion的值为TAG的最新值；

## 第三步 在Application中初始化  
以下代码仅供参考，可自定义；

```
    private void initLog() {
        LogConfig logConfig = new LogConfig.Builder()
                .logLevel(BuildConfig.DEBUG ? LogLevel.ALL : LogLevel.OFF)
                .formatJson(true) // 是否格式化json数据
                .printCrash(true) // 是否打印崩溃
                .detectANR(false) // 是否检测anr 
                .tag("xsuper")
                .build();
    
        // 输出到文件中  输出到文件中要写文件权限
        FilePrinter filePrinter = new FilePrinter
                .Builder("supperapp", this)
                .logConfig(logConfig)
                .build();
        // 输出到控制台 
        ConsolePrinter consolePrinter = new ConsolePrinter();
        
        OkLog.init(this, logConfig,filePrinter,consolePrinter);
    }
```

## 效果 

当你在代码里进行如下的打点Log时

```
        OkLog.v("onCreate:不打印堆栈深度，没有tag，居然可以不传tag，默认tag由初始化时配置");
        OkLog.v("onCreate:堆栈深度为2", 2);
        OkLog.v("onCreate:堆栈深度为5", 5);
        
        OkLog.v(TAG, "传入tag,，传入tag，不用默认tag，打印1级堆栈深度！", 1);
        OkLog.v(TAG, "传入tag,打印3级堆栈深度！！", 3);
        OkLog.v(TAG, "传入tag,打印100级堆栈深度！！", 100);

        OkLog.v(TAG, "传入tag,这是一个json字符串！！！json={\"level\": \"较不适宜\",\"name\": \"wash_car\","
                + "\"sugg\": \"不适合洗车，未来24小时内有雨，雨水可能会再次弄脏你的车\"}", 100);

        OkLog.v("这里是一个异常！",new Throwable("哈哈哈，异常来啦！"));

```

控制台输出结果

```

02-28 16:49:28.533 23134-23134/com.oterman.oklogtest I/xsuper: ************* OkLog Log Head ****************
                                                               Device Manufacturer : Xiaomi
                                                               Device Model        : MIX 3
                                                               Android Version     : 9
                                                               Android SDK         : 28
                                                               App VersionName     : 1.0
                                                               App VersionCode     : 1
                                                               App Max Mem         : 512MB
                                                               Timestamp           : 2019-02-28 16:49:28.532
                                                               TotalMem\AvailMem   : 5631MB\1306MB
                                                               ************* OkLog Log Head ****************
02-28 16:49:28.533 23134-23134/com.oterman.oklogtest V/xsuper: onCreate:不打印堆栈深度，没有tag，居然可以不传tag，默认tag由初始化时配置(MainActivity.java:48)
02-28 16:49:28.533 23134-23134/com.oterman.oklogtest V/xsuper: onCreate:堆栈深度为2(MainActivity.java:49)
                                                               |_android.app.Activity.performCreate(Activity.java:7224)
02-28 16:49:28.533 23134-23134/com.oterman.oklogtest V/xsuper: onCreate:堆栈深度为5(MainActivity.java:50)
                                                               |_android.app.Activity.performCreate(Activity.java:7224)
                                                               |__android.app.Activity.performCreate(Activity.java:7213)
                                                               |___android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1272)
                                                               |____android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2926)
02-28 16:49:28.534 23134-23134/com.oterman.oklogtest V/MainActivity: 传入tag,，传入tag，不用默认tag，打印1级堆栈深度！(MainActivity.java:52)
02-28 16:49:28.534 23134-23134/com.oterman.oklogtest V/MainActivity: 传入tag,打印3级堆栈深度！！(MainActivity.java:53)
                                                                     |_android.app.Activity.performCreate(Activity.java:7224)
                                                                     |__android.app.Activity.performCreate(Activity.java:7213)
02-28 16:49:28.535 23134-23134/com.oterman.oklogtest V/MainActivity: 传入tag,打印100级堆栈深度！！(MainActivity.java:54)
                                                                     |_android.app.Activity.performCreate(Activity.java:7224)
                                                                     |__android.app.Activity.performCreate(Activity.java:7213)
                                                                     |___android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1272)
                                                                     |____android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2926)
                                                                     |_____android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3081)
                                                                     |______android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:78)
                                                                     |_______android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108)
                                                                     |________android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68)
                                                                     |_________android.app.ActivityThread$H.handleMessage(ActivityThread.java:1831)
                                                                     |__________android.os.Handler.dispatchMessage(Handler.java:106)
                                                                     |___________android.os.Looper.loop(Looper.java:201)
                                                                     |____________android.app.ActivityThread.main(ActivityThread.java:6806)
                                                                     |_____________java.lang.reflect.Method.invoke(Native Method)
                                                                     |______________com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:547)
                                                                     |_______________com.android.internal.os.ZygoteInit.main(ZygoteInit.java:873)
02-28 16:49:28.537 23134-23134/com.oterman.oklogtest V/MainActivity: 传入tag,这是一个json字符串！！！json=
                                                                     {
                                                                         "level": "较不适宜",
                                                                         "name": "wash_car",
                                                                         "sugg": "不适合洗车，未来24小时内有雨，雨水可能会再次弄脏你的车"
                                                                     }
                                                                     (MainActivity.java:56)
                                                                     |_android.app.Activity.performCreate(Activity.java:7224)
                                                                     |__android.app.Activity.performCreate(Activity.java:7213)
                                                                     |___android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1272)
                                                                     |____android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2926)
                                                                     |_____android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3081)
                                                                     |______android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:78)
                                                                     |_______android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108)
                                                                     |________android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68)
                                                                     |_________android.app.ActivityThread$H.handleMessage(ActivityThread.java:1831)
                                                                     |__________android.os.Handler.dispatchMessage(Handler.java:106)
                                                                     |___________android.os.Looper.loop(Looper.java:201)
                                                                     |____________android.app.ActivityThread.main(ActivityThread.java:6806)
                                                                     |_____________java.lang.reflect.Method.invoke(Native Method)
                                                                     |______________com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:547)
                                                                     |_______________com.android.internal.os.ZygoteInit.main(ZygoteInit.java:873)
02-28 16:49:28.537 23134-23134/com.oterman.oklogtest V/xsuper: 这里是一个异常！
                                                               java.lang.Throwable: 哈哈哈，异常来啦！
                                                                   at com.oterman.oklogtest.MainActivity.onCreate(MainActivity.java:59)
                                                                   at android.app.Activity.performCreate(Activity.java:7224)
                                                                   at android.app.Activity.performCreate(Activity.java:7213)
                                                                   at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1272)
                                                                   at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2926)
                                                                   at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3081)
                                                                   at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:78)
                                                                   at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108)
                                                                   at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68)
                                                                   at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1831)
                                                                   at android.os.Handler.dispatchMessage(Handler.java:106)
                                                                   at android.os.Looper.loop(Looper.java:201)
                                                                   at android.app.ActivityThread.main(ActivityThread.java:6806)
                                                                   at java.lang.reflect.Method.invoke(Native Method)
                                                                   at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:547)
                                                                   at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:873)
                                                               (MainActivity.java:59)



```