package com.desaysv.svaudioapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.moduleradio.ui.RadioPlayFragment;
import com.desaysv.svaudioapp.R;

/**
 * created by ZNB on 2022-10-14
 * 一个用来装载各个 Module的 壳
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MediaMainActivity";

    private FragmentManager fragmentManager;
    private RadioPlayFragment radioPlayFragment;
    private String source = Constants.Source.SOURCE_RADIO;//打开哪个音源
    private int navigation = Constants.NavigationFlag.FLAG_MAIN;//打开音源的哪个界面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initViewListener();
        Intent intent = getIntent();
        if (intent != null) {
            source = intent.getStringExtra(Constants.Source.SOURCE_KEY);
            navigation = intent.getIntExtra(Constants.NavigationFlag.KEY, -1);
            if (source != null && navigation != -1 && radioPlayFragment != null) {
                radioPlayFragment.setBandWithVR(navigation);
                radioPlayFragment.setListWithVR(navigation);
            }
        }
        Log.d(TAG, "onCreate");
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        if (intent != null) {
            source = intent.getStringExtra(Constants.Source.SOURCE_KEY);
            navigation = intent.getIntExtra(Constants.NavigationFlag.KEY, -1);
            if (source != null && navigation != -1 && radioPlayFragment != null) {
                radioPlayFragment.openBandWithVR(navigation);
                radioPlayFragment.openListWithVR(navigation);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initView() {
        fragmentManager = getSupportFragmentManager();
        radioPlayFragment = new RadioPlayFragment();
        gotoFragment();
    }

    private void initViewListener() {

    }

    /**
     * 打开对应Fragment
     */
    private void gotoFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flContent, radioPlayFragment);
        transaction.commit();
    }
}