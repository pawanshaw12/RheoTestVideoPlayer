package com.example.rheotestvideoplayer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownLoadTask extends AsyncTask<String, Integer, Integer> {
    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private DownLoadListener listener;
    private Context context;


    private int lastProgress = 0;





    public DownLoadTask(DownLoadListener listener, Context context){
        this.listener = listener;
        this.context=context;


    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];

        if(progress > lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;

            Intent local = new Intent();
            local.putExtra("Progress",progress);
            local.setAction("ProgressUpdate");

            context.sendBroadcast(local);


        }
    }



    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try{
            long downloadLength = 0;
            String downloadUri = params[0];
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + "/test.mp4");
            if(file.exists()){
                downloadLength = file.length();
            }
            long contentLength = getContentLength(downloadUri);
            if(contentLength == 0){
                return TYPE_FAILED;
            }else if(contentLength == downloadLength){
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes=" + downloadLength + "-")
                    .url(downloadUri)
                    .build();
            Response response = client.newCall(request).execute();
            if(response != null){
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file,"rw");
                savedFile.seek(downloadLength);
                byte [] b =new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b))!= -1){

                        total += len;
                        savedFile.write(b,0,len);
                        int progress = (int) ((total + downloadLength)*100/contentLength);
                        publishProgress(progress);


                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(is != null){
                    is.close();
                }
                if(savedFile != null){
                    savedFile.close();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if(response != null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }

}
