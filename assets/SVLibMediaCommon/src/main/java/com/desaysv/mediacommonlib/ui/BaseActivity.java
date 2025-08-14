package com.desaysv.mediacommonlib.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

/**
 * @author LZM
 * @date 2019-7-4
 * Comment 这里是common lib里面的基础activity，实现了界面前后台的记录
 */
public abstract class BaseActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.d(TAG, "onCreate: start " + this);
        setContentView(getLayoutResID());
        initView();
        initData();
        initViewListener();
        Log.d(TAG, "onCreate: end " + this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewRecording.getInstance().setViewIsFg(this.getClass().getName(),true);
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewRecording.getInstance().setViewIsFg(this.getClass().getName(),false);
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    /**
     * 获取界面的res id
     *
     * @return 界面layout的资源ID
     */
    public abstract int getLayoutResID();

    /**
     * 初始化视图，子类实现
     */
    public abstract void initView();

    /**
     * 初始化数据，子类实现
     */
    public abstract void initData();

    /**
     * 初始化各种监听器，子类可以不实现
     */
    public void initViewListener() {
    }

}
