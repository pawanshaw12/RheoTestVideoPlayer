package com.example.rheotestvideoplayer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DownloadService.DownloadBinder downloadBinder;
    private Button startDownload;
    private BroadcastReceiver updateUIReceiver;
    private BroadcastReceiver progressUpdate;
    private ProgressBar pgsBar;



    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pgsBar=findViewById(R.id.progressBar);


        //Setting up the Player
        CustomVideoPlayer customVideoPlayer = findViewById(R.id.customVideoPlayer);
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();
        File file = new File(directory + "/test.mp4");
        String Url= String.valueOf(Uri.fromFile(file));
        //If File exists then Show the Player
        if(file.exists()) {

            customVideoPlayer.setVisibility(View.VISIBLE);
            customVideoPlayer.setMediaUrl(Url)
                    .enableAutoMute(false)
                    .enableAutoPlay(true)
                    .hideControllers(false)
                    .setMaxHeight(500)
                    .build();
            pgsBar.setVisibility(View.GONE); //
        }

        //Update ProgressBar using BroadCast Receiver

        IntentFilter progressUpdateFilter = new IntentFilter();
        progressUpdateFilter.addAction("ProgressUpdate");

        progressUpdate=new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                Bundle bundle=intent.getExtras();
                pgsBar.setProgress(bundle.getInt("Progress"));

            }
        };
        registerReceiver(progressUpdate,progressUpdateFilter);



        //Show player After File Downloaded using BroadCast Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("DownloadSuccess");

         updateUIReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                CustomVideoPlayer customVideoPlayer = findViewById(R.id.customVideoPlayer);
                customVideoPlayer.setVisibility(View.VISIBLE);
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
                File file = new File(directory + "test.mp4");
                String Url= String.valueOf(Uri.fromFile(file));
                customVideoPlayer.setMediaUrl(Url)
                        .enableAutoMute(false)
                        .enableAutoPlay(true)
                        .hideControllers(false)
                        .setMaxHeight(500)
                        .build();
                pgsBar.setVisibility(View.GONE);// hiding the progressBar after Downloading

            }
        };
        registerReceiver(updateUIReceiver,filter);

        initView();
        initListener();
        Intent intent = new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }


    @Override
    public void onClick(View v) {
        if(downloadBinder == null){
            return;
        }
        switch (v.getId()){

            case R.id.start_download:
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
                File file = new File(directory + "/test.mp4");

                String url ="http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4" ;
                if(!file.exists()){
                    downloadBinder.startDownload(url);

                }else{
                    Toast.makeText(MainActivity.this,"File Already Exists", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void initView(){
        startDownload = findViewById(R.id.start_download);
    }

    public void initListener(){
        startDownload.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Please Give the Permissions", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        CustomVideoPlayer customVideoPlayer = findViewById(R.id.customVideoPlayer);
        customVideoPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(updateUIReceiver);
        unregisterReceiver(progressUpdate);
        unbindService(connection);
    }

}
