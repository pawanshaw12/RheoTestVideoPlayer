package com.example.rheotestvideoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

public class DownloadService extends Service {

    private DownLoadTask downloadTask;

    private String downloadUri;

    private Notification.Builder builder;

    private static final String CHANNEL_ID = "DOWNLOAD_CHANNEL_ID";

    private static final String CHANNEL_NAME ="DOWNLOAD_CHANNEL_NAME";

    private DownLoadListener listener = new DownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("Downloading...",progress,CHANNEL_ID,CHANNEL_NAME));

            Intent local = new Intent();
            local.putExtra("Progress",progress);
            local.setAction("ProgressUpdate");

            sendBroadcast(local);

        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Success",-1,CHANNEL_ID,CHANNEL_NAME));
            Toast.makeText(DownloadService.this,"Download Success in Movie Folder", Toast.LENGTH_SHORT).show();
            Intent local = new Intent();

            local.setAction("DownloadSuccess");

            sendBroadcast(local);
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1,CHANNEL_ID,CHANNEL_NAME));
            Toast.makeText(DownloadService.this,"Download Failed", Toast.LENGTH_SHORT).show();
        }



    };
    private DownloadBinder mBinder = new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder {
        public void startDownload(String url){

            if(downloadTask == null){
                downloadUri = url;
                downloadTask = new DownLoadTask(listener,getApplicationContext());
                downloadTask.execute(downloadUri);
              startForeground(1,getNotification("Downloading...",0,CHANNEL_ID,CHANNEL_NAME));
                Toast.makeText(DownloadService.this,"Downloading...", Toast.LENGTH_SHORT).show();
            }
        }



    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress, String CHANNEL_ID, String CHANNEL_NAME){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager!=null;
            manager.createNotificationChannel(channel);
            builder = new Notification.Builder(this,CHANNEL_ID);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentIntent(pi);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
            builder.setContentTitle(title);
            if(progress > 0){
                builder.setContentText(progress + "%");
                builder.setProgress(100,progress,false);
            }
        }
        return builder.build();
    }
}
