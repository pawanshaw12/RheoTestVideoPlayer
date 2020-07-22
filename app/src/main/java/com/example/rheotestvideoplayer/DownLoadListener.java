package com.example.rheotestvideoplayer;

public interface DownLoadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

}
