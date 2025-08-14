package com.desaysv.moduleusbpicture.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.desaysv.mediacommonlib.ui.BaseActivity;
import com.desaysv.moduleusbpicture.R;
import com.desaysv.moduleusbpicture.adapter.PhotoPagerAdapter;

/**
 * Created by LZM on 2019-9-18
 * Comment 图片浏览界面的base activity
 */
public abstract class BasePictureActivity extends BaseActivity {

    private final String TAG = this.getClass().getSimpleName();

    public static final String INTENT_POSITION = "INTENT_POSITION";

    private ViewPager vpPicture;
    private PhotoPagerAdapter mPicturePagerAdapter;

    private Button btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public int getLayoutResID() {
        return R.layout.usb_picture_activity;
    }

    @Override
    public void initView() {
        vpPicture = findViewById(R.id.vpPicture);
        btnBack = findViewById(R.id.btnBack);
    }

    @Override
    public void initData() {
        updatePicturePagerAdapter();
    }

    @Override
    public void initViewListener() {
        super.initViewListener();
        btnBack.setOnClickListener(onClickListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null) {
            int position = intent.getIntExtra(INTENT_POSITION, 0);
            openPhoto(position);
        }
    }

    private void openPhoto(int position) {
        Log.d(TAG, "openPhoto: position = " + position);
        vpPicture.setCurrentItem(position, false);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btnBack) {
                finish();
            }
        }
    };

    private void updatePicturePagerAdapter() {
        Log.d(TAG, "updatePicturePagerAdapter: mPicturePagerAdapter = " + mPicturePagerAdapter);
        if (mPicturePagerAdapter == null) {
            mPicturePagerAdapter = initAdapter();
            vpPicture.setAdapter(mPicturePagerAdapter);
        } else {
            mPicturePagerAdapter.notifyDataSetChanged();
        }
    }

    protected abstract PhotoPagerAdapter initAdapter();
}
