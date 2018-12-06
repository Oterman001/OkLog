# OkLog
封装了Android Log

如何引入呢 

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
	        implementation 'com.github.Oterman1314:OkLog:{latestVersion}'
	}
	

```
latestVersion的值为TAG的最新值；

## 第三步 在Application中初始化  
以下代码仅供参考，可自定义；
```
        LogConfig logConfig = new LogConfig.Builder()
                .logLevel(BuildConfig.DEBUG ? LogLevel.ALL : LogLevel.OFF)
                .ignoreTag(true) // 是否忽略tag，默认tag为（xx.java:123）形式，可快速定位
                .formatJson(true)
                .printOneline(false)
                .printCrash(true)
                .detectANR(false)
                .tag("musesLog")
                .build();

        FilePrinter filePrinter = new FilePrinter
                .Builder("bootwizard", this)
                .logConfig(logConfig)
                .build();

        ConsolePrinter consolePrinter = new ConsolePrinter();
        OkLog.init(this, logConfig,filePrinter,consolePrinter);
```