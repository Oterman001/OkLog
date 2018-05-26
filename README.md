# OkLog
封装了Android Log

#如何引入呢

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
	        implementation 'com.github.Oterman1314:OkLog:v1.0.1'
	}
```
