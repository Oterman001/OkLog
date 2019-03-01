package com.oterman.oklogtest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.oterman.oklog.OkLog;
import com.oterman.oklog.common.LogConfig;
import com.oterman.oklog.common.LogLevel;
import com.oterman.oklog.printer.ConsolePrinter;
import com.oterman.oklog.printer.FilePrinter;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private TextView mTextMessage;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initLog();

        OkLog.v("onCreate:不打印堆栈深度，没有tag，居然可以不传tag，默认tag由初始化时配置");
        OkLog.v("onCreate:堆栈深度为2", 2);
        OkLog.v("onCreate:堆栈深度为5", 5);

        OkLog.v(TAG, "传入tag,，传入tag，不用默认tag，打印1级堆栈深度！", 1);
        OkLog.v(TAG, "传入tag,打印3级堆栈深度！！", 3);
        OkLog.v(TAG, "传入tag,打印100级堆栈深度！！", 100);

        OkLog.v(TAG, "传入tag,这是一个json字符串！！！json={\"level\": \"较不适宜\",\"name\": \"wash_car\","
                + "\"sugg\": \"不适合洗车，未来24小时内有雨，雨水可能会再次弄脏你的车\"}", 100);

        OkLog.v("这里是一个异常！",new Throwable("哈哈哈，异常来啦！"));

        OkLog.d("onCreate:不打印堆栈深度，没有tag");
        OkLog.d("onCreate:长度2", 2);
        OkLog.d("onCreate:堆栈深度为5", 5);
        OkLog.d(TAG, "传入tag,打印1级堆栈深度！", 1);
        OkLog.d(TAG, "传入tag,打印3级堆栈深度！！", 3);
        OkLog.d(TAG, "传入tag,打印100级堆栈深度！！！！！", 100);

        OkLog.i("onCreate:不打印堆栈深度，没有tag");
        OkLog.i("onCreate:长度2", 2);
        OkLog.i("onCreate:堆栈深度为5", 5);
        OkLog.i(TAG, "传入tag,打印1级堆栈深度！", 1);
        OkLog.i(TAG, "传入tag,打印3级堆栈深度！！", 3);
        OkLog.i(TAG, "传入tag,打印100级堆栈深度！！！！！", 100);
        
        
        OkLog.w("onCreate:不打印堆栈深度，没有tag");
        OkLog.w("onCreate:长度2", 2);
        OkLog.w("onCreate:堆栈深度为5", 5);
        OkLog.w(TAG, "传入tag,打印1级堆栈深度！", 1);
        OkLog.w(TAG, "传入tag,打印3级堆栈深度！！", 3);
        OkLog.w(TAG, "传入tag,打印100级堆栈深度！！！！！", 100);

        OkLog.e("onCreate:不打印堆栈深度，没有tag");
        OkLog.e("onCreate:长度2", 2);
        OkLog.e("onCreate:堆栈深度为5", 5);
        OkLog.e(TAG, "传入tag,打印1级堆栈深度！", 1);
        OkLog.e(TAG, "传入tag,打印3级堆栈深度！！", 3);
        OkLog.e(TAG, "传入tag,打印100级堆栈深度！！！！！", 100);
        OkLog.e("出现异常了！", new RuntimeException("hahah"));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                int i=1/0;
            }
        });

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void initLog() {
        LogConfig logConfig = new LogConfig.Builder()
                .logLevel(BuildConfig.DEBUG ? LogLevel.ALL : LogLevel.OFF)
                .formatJson(true)
                .printCrash(true)
                .detectANR(false)
                .tag("xsuper")
                .build();

        FilePrinter filePrinter = new FilePrinter
                .Builder("supperapp", this)
                .logConfig(logConfig)
                .build();

        ConsolePrinter consolePrinter = new ConsolePrinter();
        OkLog.init(this, logConfig,filePrinter,consolePrinter);
    }

}
