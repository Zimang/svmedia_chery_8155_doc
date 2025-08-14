package com.desaysv.svlibusbdialog.manager;


import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.desaysv.svlibusbdialog.query.USB1MusicQuery;
import com.desaysv.svlibusbdialog.query.USB1PictureQuery;
import com.desaysv.svlibusbdialog.query.USB1VideoQuery;
import com.desaysv.svlibusbdialog.query.USB2MusicQuery;
import com.desaysv.svlibusbdialog.query.USB2PictureQuery;
import com.desaysv.svlibusbdialog.query.USB2VideoQuery;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueryManager {

    private static QueryManager mInstance;

    public static QueryManager getInstance(){
        if (mInstance == null){
            synchronized (QueryManager.class){
                if (mInstance == null){
                    mInstance = new QueryManager();
                }
            }
        }
        return mInstance;
    }

    private Context mContext;
    private ExecutorService singleUSB1PictureThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB2PictureThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB1MusicThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB2MusicThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB1VideoThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB2VideoThreadPool = Executors.newSingleThreadExecutor();


    /**
     * 启动查询USB1的媒体库内容
     */
    public void startQueryUSB1(Context context){
        mContext = context;
        startQueryUSB1Music();
        startQueryUSB1Picture();
        startQueryUSB1Video();
    }

    /**
     * 启动查询USB2的媒体库内容
     */
    public void startQueryUSB2(Context context){
        mContext = context;
        startQueryUSB2Music();
        startQueryUSB2Picture();
        startQueryUSB2Video();
    }


    private void startQueryUSB1Music(){
        singleUSB1MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new USB1MusicQuery(mContext).doInBackGround();
            }
        });
    }

    private void startQueryUSB1Picture(){
        singleUSB1PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new USB1PictureQuery(mContext).doInBackGround();
            }
        });
    }

    private void startQueryUSB1Video(){
        singleUSB1VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new USB1VideoQuery(mContext).doInBackGround();
            }
        });
    }

    private void startQueryUSB2Music(){
        singleUSB2MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new USB2MusicQuery(mContext).doInBackGround();
            }
        });
    }

    private void startQueryUSB2Picture(){
        singleUSB2PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new USB2PictureQuery(mContext).doInBackGround();
            }
        });
    }

    private void startQueryUSB2Video(){
        singleUSB2VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new USB2VideoQuery(mContext).doInBackGround();
            }
        });
    }

}
