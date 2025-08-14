package com.ysbing.yadb;
import android.os.Looper;
import android.widget.Toast;
import android.app.ActivityThread;
import android.content.Context;

public class Main {

    public static void main(String[] args) {
        try {
            // 获取系统Context
            ActivityThread activityThread = ActivityThread.systemMain();
            Context context = activityThread.getSystemContext();
            
            // 准备主线程Looper
            Looper.prepareMainLooper();
            
            // 显示Toast
            Toast.makeText(context, "Hello", Toast.LENGTH_LONG).show();
            
            // 保持进程运行足够长时间以显示Toast
            Thread.sleep(3500);
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 退出Looper
            Looper.myLooper().quit();
        }
    }
}