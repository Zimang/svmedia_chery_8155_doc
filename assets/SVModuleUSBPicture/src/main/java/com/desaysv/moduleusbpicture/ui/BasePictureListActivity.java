package com.desaysv.moduleusbpicture.ui;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.mediacommonlib.ui.BaseActivity;
import com.desaysv.moduleusbpicture.R;
import com.desaysv.moduleusbpicture.adapter.PictureListAdapter;
import com.desaysv.usbbaselib.observer.Observer;

import java.lang.ref.WeakReference;


/**
 * Created by LZM on 2019-9-18
 * Comment 图片列表界面的base activity
 */
public abstract class BasePictureListActivity extends BaseActivity {

    private final String TAG = this.getClass().getSimpleName();

    private RecyclerView rlPictureList;
    private PictureListAdapter mPictureListAdapter;

    private Button btnBack;

    private static final int UPDATE_PICTURE_LIST_ADAPTER = 1;
    private MyHandler mHandler;

    private static class MyHandler extends Handler {

        private WeakReference<BasePictureListActivity> weakReference;

        MyHandler(BasePictureListActivity basePictureListActivity) {
            weakReference = new WeakReference<>(basePictureListActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            final BasePictureListActivity basePictureListActivity = weakReference.get();
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_PICTURE_LIST_ADAPTER:
                    basePictureListActivity.updatePictureListAdapter();
                    break;
            }
        }
    }


    @Override
    public int getLayoutResID() {
        return R.layout.usb_picture_list_activity;
    }

    @Override
    public void initView() {
        rlPictureList = findViewById(R.id.rlPictureList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        rlPictureList.setLayoutManager(gridLayoutManager);
        btnBack = findViewById(R.id.btnBack);
    }

    @Override
    public void initData() {
        mHandler = new MyHandler(this);
        updatePictureListAdapter();
    }

    @Override
    public void initViewListener() {
        super.initViewListener();
        attachObserver(ListStatusChangeObserver);
        btnBack.setOnClickListener(onClickListener);
    }


    private void updatePictureListAdapter() {
        Log.d(TAG, "updatePictureListAdapter: mPictureListAdapter = " + mPictureListAdapter);
        if (mPictureListAdapter == null) {
            mPictureListAdapter = initAdapter();
            rlPictureList.setAdapter(mPictureListAdapter);
        } else {
            mPictureListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 初始化适配器
     * @return 获取初始化的适配器
     */
    protected abstract PictureListAdapter initAdapter();

    /**
     * 注册观察者
     * @param observer 观察者
     */
    protected abstract void attachObserver(Observer observer);

    Observer ListStatusChangeObserver = new Observer() {
        @Override
        public void onUpdate() {
            Log.d(TAG, "onUpdate: ");
            mHandler.sendEmptyMessage(UPDATE_PICTURE_LIST_ADAPTER);
        }
    };

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btnBack) {
                finish();
            }
        }
    };

}
