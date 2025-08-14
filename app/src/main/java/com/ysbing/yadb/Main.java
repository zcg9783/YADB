package com.ysbing.yadb;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class Notify {

    public static void main(String[] args) {
        try {
            Context context = (Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
            
            NotificationManager manager = (NotificationManager) 
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            String channelId = "app_process_channel";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "App Process",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                manager.createNotificationChannel(channel);
            }
            
            // 构建通知
            Notification.Builder builder = new Notification.Builder(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(channelId);
            }
            
            Notification notification = builder
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("你好")
                    .setContentText("Hello")
                    .setAutoCancel(true)
                    .build();
            
            // 发送通知
            manager.notify(1, notification);
            System.out.println("通知已发送");
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}