package com.desaysv.moduleusbvideo.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.base.FragmentInfo;
import com.desaysv.moduleusbvideo.base.VideoBaseActivity;
import com.desaysv.moduleusbvideo.bean.SelectModeBean;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.moduleusbvideo.util.TextViewUtils;
import com.desaysv.moduleusbvideo.util.VideoFileListTypeTool;
import com.desaysv.moduleusbvideo.view.VideoModePopupWindow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by extodc87 on 2022-11-1
 * Author: extodc87
 */
public class VideoMainActivity extends VideoBaseActivity implements View.OnClickListener {
    private RelativeLayout rl_button_main;
    private LinearLayout ll_folder_back;
    private LinearLayout ll_back_ltr;
    private TextView tv_folder_name;

    private TextView tv_usb, tv_usb1, tv_usb2;
    private TextView[] tvUsb;

    private LinearLayout ll_mode;
    private RelativeLayout rl_select_mode;
    private TextView tv_all_list;
    private TextView tv_folder_list;
    private TextView[] list;

    private TextView tv_select_mode;

    private VideoModePopupWindow videoModePopupWindow;

    private VideoFileListTypeTool videoFileListTypeTool;

    private USBVideoDevicesFragment usbVideoDevicesFragment;
    private USB1VideoListFragment usb1VideoListFragment;
    private USB2VideoListFragment usb2VideoListFragment;


    private static final int UPDATE_MODE_TYPE = 1;

    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        private final WeakReference<VideoMainActivity> weakReference;

        MyHandler(VideoMainActivity target) {
            weakReference = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final VideoMainActivity target = weakReference.get();
            switch (msg.what) {
                case UPDATE_MODE_TYPE:
                    target.updateModeSelect((Boolean) msg.obj);
                    break;
            }
        }
    }


    @Override
    public int getLayoutResID() {
        return R.layout.usb_video_activity;
    }

    @Override
    public void initView() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        rl_button_main = findViewById(R.id.rl_button_main);
        ll_folder_back = findViewById(R.id.ll_folder_back);
        tv_folder_name = findViewById(R.id.tv_folder_name);
        ll_back_ltr = findViewById(R.id.ll_back_ltr);
        tv_usb = findViewById(R.id.tv_usb);
        tv_usb1 = findViewById(R.id.tv_usb1);
        tv_usb2 = findViewById(R.id.tv_usb2);

        ll_mode = findViewById(R.id.ll_mode);
        rl_select_mode = findViewById(R.id.rl_select_mode);
        tv_all_list = findViewById(R.id.tv_all_list);
        tv_folder_list = findViewById(R.id.tv_folder_list);
        list = new TextView[]{tv_all_list, tv_folder_list};

        tv_select_mode = findViewById(R.id.tv_select_mode);
//        FrameLayout rl_content = findViewById(R.id.rl_content);

        tvUsb = new TextView[]{tv_usb, tv_usb1, tv_usb2};
    }
    private boolean isRtl;
    @Override
    public void initData() {
        mHandler = new MyHandler(this);
        // 把USB1 显示USB
        tv_usb1.setText(getString(R.string.tab_usb));
        isRtl = Constant.isRtl();
        // T19C 渠道下，显示图标；其他渠道显示popWindow
        if (Constant.isT19CFlavor()) {
            rl_select_mode.setVisibility(View.VISIBLE);
            tv_select_mode.setVisibility(View.GONE);
        } else {
            rl_select_mode.setVisibility(View.GONE);
            tv_select_mode.setVisibility(View.VISIBLE);
        }
        if(isRtl){
//            ll_folder_back.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }else{
            if(!Constant.isExeed()){
                boolean leftOrRightConfig = Constant.isLeftOrRightConfig();
                if(leftOrRightConfig){
                    ll_folder_back.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    ll_back_ltr.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    rl_button_main.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                }

            }
        }
    }

    @Override
    public void initViewListener() {
        super.initViewListener();
        tv_usb1.setOnClickListener(this);
        tv_usb2.setOnClickListener(this);
        tv_all_list.setOnClickListener(this);
        tv_folder_list.setOnClickListener(this);
        tv_select_mode.setOnClickListener(this);
        tv_folder_name.addTextChangedListener(textWatcher);
    }

    @Override
    public void initFragments() {
        super.initFragments();
        //这一部分很重要，用于重载之后，从系统保存的对象取出 fragment(例如白天黑夜切换)
        //否则因为系统默认恢复的是保存的fragment，走的是恢复对象的生命周期，
        //而这里又new了新的fragment对象，就会导致new的对象里面的参数实际并没有初始化，因为new这个fragment不会被add进去，不会执行相应的生命周期
        //就会导致各种空指针异常
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Log.d(TAG, "initFragments: fragments = " + fragments);
        if (null != fragments && 0 != fragments.size()) {
            List<FragmentInfo> infos = new ArrayList<>();
            for (int i = 0; i < fragments.size(); i++) {
                Fragment fragment = fragments.get(i);
                if (fragment instanceof USBVideoDevicesFragment) {
                    usbVideoDevicesFragment = (USBVideoDevicesFragment) fragment;
                    FragmentInfo usbVideoDevices = createAndAddFragmentInfo(R.id.rl_content, R.layout.usb_video_devices_fragment, usbVideoDevicesFragment);
                    // 只有在界面显示的时候，才需要加入当前显示列表
                    if (!usbVideoDevicesFragment.isHidden()) {
                        infos.add(usbVideoDevices);
                    }
                } else if (fragment instanceof USB1VideoListFragment) {
                    usb1VideoListFragment = (USB1VideoListFragment) fragment;
                    FragmentInfo usb1VideoList = createAndAddFragmentInfo(R.id.rl_content, Constant.USB1_FRAGMENT_ID, usb1VideoListFragment);
                    // 只有在界面显示的时候，才需要加入当前显示列表
                    if (!usb1VideoListFragment.isHidden()) {
                        infos.add(usb1VideoList);
                    }
                } else if (fragment instanceof USB2VideoListFragment) {
                    usb2VideoListFragment = (USB2VideoListFragment) fragment;
                    FragmentInfo usb2VideoList = createAndAddFragmentInfo(R.id.rl_content, Constant.USB2_FRAGMENT_ID, usb2VideoListFragment);
                    // 只有在界面显示的时候，才需要加入当前显示列表
                    if (!usb2VideoListFragment.isHidden()) {
                        infos.add(usb2VideoList);
                    }
                }
            }
            // 切换主题后，数据需要加载的默认列表，
            initCurrentVideInfos(infos);
        }

        if (null == usbVideoDevicesFragment) {
            usbVideoDevicesFragment = USBVideoDevicesFragment.newInstance();
        }
        if (null == usb1VideoListFragment) {
            usb1VideoListFragment = USB1VideoListFragment.newInstance();
        }
        if (null == usb2VideoListFragment) {
            usb2VideoListFragment = USB2VideoListFragment.newInstance();
        }

        setCommunications();
        loadFragments();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);

        updateUSBStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
           int navigation = intent.getIntExtra(Constants.NavigationFlag.KEY, -1);
           if (navigation == Constants.NavigationFlag.FLAG_PLAY) {
               String path = intent.getStringExtra(Constants.FileMessageData.PATH);
               Log.d(TAG, "onNewIntent: VR OPEN PLAY PAGE path = " + path);
               if (path == null || path.isEmpty()) {
                   Log.w(TAG, "onNewIntent: VR OPEN PLAY PAGE path IS NULL ");
                   return;
               }
               USB1VideoPlayActivity.startUSB1VideoPlayActivity(this, path);
           }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
    }

    private void setCommunications() {
        usb1VideoListFragment.setCommunication(this);
        usb2VideoListFragment.setCommunication(this);
    }

    private void loadFragments() {
        createAndAddFragmentInfo(R.id.rl_content, R.layout.usb_video_devices_fragment, usbVideoDevicesFragment);
        createAndAddFragmentInfo(R.id.rl_content, Constant.USB1_FRAGMENT_ID, usb1VideoListFragment);
        createAndAddFragmentInfo(R.id.rl_content, Constant.USB2_FRAGMENT_ID, usb2VideoListFragment);
    }

    /**
     * USB插拔
     * 更新标签栏 和 页面
     */
    private void updateUSBStatus() {
        Log.d(TAG, "updateUSBStatus: ");

        if (null != videoModePopupWindow) {
            videoModePopupWindow.dismiss();
            Log.d(TAG, "updateUSBStatus: videoModePopupWindow.dismiss();");
        }

        boolean usb1Connect = DeviceStatusBean.getInstance().isUSB1Connect();
        boolean usb2Connect = DeviceStatusBean.getInstance().isUSB2Connect();
        // 没有USB链接
        if (!usb1Connect && !usb2Connect) {
            tv_usb.setVisibility(View.VISIBLE);

            tv_usb1.setVisibility(View.GONE);
            tv_usb2.setVisibility(View.GONE);
            // T19C 时,显示按钮
            if (Constant.isT19CFlavor()) {
                selectMode(null);
                ll_mode.setVisibility(View.VISIBLE);
            } else {
                ll_mode.setVisibility(View.GONE);
            }

            selectFragment(R.layout.usb_video_devices_fragment);

            Log.d(TAG, "updateUSBStatus: not connect");
            return;
        }

        tv_usb.setVisibility(View.GONE);
        ll_mode.setVisibility(View.VISIBLE);

        int usb1Visibility = usb1Connect ? View.VISIBLE : View.GONE;
        tv_usb1.setVisibility(usb1Visibility);
        int usb2Visibility = usb2Connect ? View.VISIBLE : View.GONE;
        tv_usb2.setVisibility(usb2Visibility);
        if (!usb1Connect) {
            selectFragment(Constant.USB2_FRAGMENT_ID);
        } else {
            selectFragment(Constant.USB1_FRAGMENT_ID);
        }
    }

    /**
     * 选中页签，跳转操作
     *
     * @param usb 无USB，USB1，USB2
     */
    private void selectFragment(int usb) {
        Log.d(TAG, "selectFragment: usb = " + usb);
        if (usb == R.layout.usb_video_devices_fragment) {
            Log.d(TAG, "selectFragment: usb_video_devices_fragment");
            //usb default
            showFragment(R.layout.usb_video_devices_fragment);
            selectTv(tv_usb);
            videoFileListTypeTool = null;
        } else if (usb == Constant.USB1_FRAGMENT_ID) {
            Log.d(TAG, "selectFragment: USB1_FRAGMENT_ID");
            //usb 1
            showFragment(Constant.USB1_FRAGMENT_ID);
            selectTv(tv_usb1);
            videoFileListTypeTool = VideoFileListTypeTool.getInstance(DeviceConstants.DevicePath.USB0_PATH);
        } else if (usb == Constant.USB2_FRAGMENT_ID) {
            Log.d(TAG, "selectFragment: USB2_FRAGMENT_ID");
            //usb 2
            showFragment(Constant.USB2_FRAGMENT_ID);
            selectTv(tv_usb2);
            videoFileListTypeTool = VideoFileListTypeTool.getInstance(DeviceConstants.DevicePath.USB1_PATH);
        }
        Log.i(TAG, "selectFragment: videoFileListTypeTool: " + videoFileListTypeTool);
        if (null != videoFileListTypeTool) {
            setFragmentData(videoFileListTypeTool.getStyleType());
        } else {
            updateModeName();
        }
        Log.d(TAG, "selectFragment: end");
    }

    /**
     * 更新模式 文本信息
     */
    private void updateModeName() {
        if (null == videoFileListTypeTool) {
            Log.w(TAG, "updateModeName: is null return");
            return;
        }
        if (VideoFileListTypeTool.STYLE_TYPE_ALL == videoFileListTypeTool.getStyleType()) {
            if (Constant.isT19CFlavor()) {
                selectMode(tv_all_list);
            } else {
                tv_select_mode.setText(getString(R.string.tab_video_mode));
            }
        } else if (VideoFileListTypeTool.STYLE_TYPE_FOLDER == videoFileListTypeTool.getStyleType()) {
            if (Constant.isT19CFlavor()) {
                selectMode(tv_folder_list);
            } else {
                tv_select_mode.setText(getString(R.string.tab_folder_mode));
            }
        }
    }

    /**
     * T19C渠道下，全部列表和文件夹列表图标选择
     *
     * @param view view
     */
    private void selectMode(View view) {
        if (null == view) {
            for (TextView textView : list) {
                Log.i(TAG, "selectMode: " + textView);
                textView.setSelected(false);
                textView.setEnabled(false);
            }
            Log.e(TAG, "selectMode: view is null");
            return;
        }
        for (TextView textView : list) {
            textView.setSelected(textView.getId() == view.getId());
            textView.setEnabled(true);
        }
    }


    /**
     * 设备状态发生改变的时候会触发的监听
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status + ", hasWindowFocus() = " + hasWindowFocus());
            updateUSBStatus();
        }
    };

    /**
     * 标签栏 选中样式
     */
    private void selectTv(TextView tView) {
        for (TextView view : tvUsb) {
            if (View.VISIBLE != view.getVisibility()) {
                continue;
            }
            if (view.getId() == tView.getId()) {
                TextViewUtils.getInstance().setHeightLightItem(this, view, R.mipmap.usb_video_title_bg_p);
            } else {
                TextViewUtils.getInstance().setHeightLightItem(this, view, R.mipmap.usb_video_title_bg_n);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_usb1) {
            Log.d(TAG, "onClick: USB1");
            selectFragment(Constant.USB1_FRAGMENT_ID);
        } else if (id == R.id.tv_usb2) {
            Log.d(TAG, "onClick: USB2");
            selectFragment(Constant.USB2_FRAGMENT_ID);
        } else if (id == R.id.tv_select_mode) {
            Log.d(TAG, "onClick: tv_select_mode");
            if (null == videoModePopupWindow) {
                videoModePopupWindow = new VideoModePopupWindow(this);
                videoModePopupWindow.setOnclickListener(this);
            }
            videoModePopupWindow.showPopupWindow(tv_select_mode, videoFileListTypeTool);
        } else if (id == R.id.ll_folder_mode || id == R.id.tv_folder_list) {
            // 文件夹模式， 文件夹列表
            Log.d(TAG, "onClick: ll_folder_mode or tv_folder_list");
            setFragmentData(VideoFileListTypeTool.STYLE_TYPE_FOLDER);
        } else if (id == R.id.ll_video_mode || id == R.id.tv_all_list) {
            // 视频模式，全部列表
            Log.d(TAG, "onClick: ll_video_mode or tv_all_list ");
            setFragmentData(VideoFileListTypeTool.STYLE_TYPE_ALL);
        }
    }

    /**
     * 设置 模式
     * 并通知 到fragment 页面
     *
     * @param styleType {@link VideoFileListTypeTool#STYLE_TYPE_ALL}
     *                  {@link VideoFileListTypeTool#STYLE_TYPE_FOLDER}
     */
    private void setFragmentData(int styleType) {
        List<Fragment> currentFragments = getCurrentFragments();
        if (currentFragments.isEmpty()) {
            Log.d(TAG, "setFragmentData: currentFragments.isEmpty()");
            return;
        }
        Fragment fragment = currentFragments.get(0);
        if (null == fragment) {
            Log.d(TAG, "setFragmentData: videoBaseFragment ");
            return;
        }
        if (fragment instanceof BaseVideoListFragment) {
            LinearLayout llFolder = null;
            BaseVideoListFragment baseVideoList = (BaseVideoListFragment) fragment;
            SelectModeBean bean = new SelectModeBean();
            bean.setLlFolder(llFolder);
            bean.setOnClickListener(onClickListener);
            bean.setVideoFileListType(styleType);
            if (VideoFileListTypeTool.STYLE_TYPE_FOLDER == styleType) {
                llFolder = ll_folder_back;
                bean.setLlFolder(llFolder);
            }
            baseVideoList.setInitData(bean);
        }
        updateModeName();
        Log.d(TAG, "setFragmentData: end");
    }

    private void updateModeSelect(boolean isEnable) {
        Log.d(TAG, "updateModeSelect() called with: isEnable = [" + isEnable + "]");
        if (isEnable) {
            selectMode(null);
        } else {
            updateModeName();
        }
    }

    private final SelectModeBean.ModeListener onClickListener = new SelectModeBean.ModeListener() {
        @Override
        public void onClick(boolean isEnable) {
            boolean t19CFlavor = Constant.isT19CFlavor();
            Log.d(TAG, "SelectModeBean onClick() called with: isEnable = [" + isEnable + "], t19CFlavor = [" + t19CFlavor + "]");
            if (t19CFlavor) {
                mHandler.sendMessage(mHandler.obtainMessage(UPDATE_MODE_TYPE, isEnable));
            }
        }
    };

    /**
     * 根据文件夹模式下的 文本信息，判断显示全部还是显示文件夹模式
     * 数据显示为空：当前是根目录；显示控制栏、隐藏文件夹名称
     * 数据部不为空：具体的目录，隐藏控制栏，显示文件夹名称
     */
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforeTextChanged: s = " + s);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "onTextChanged: s = " + s);
            // 判断 显示 文件夹模式下的返回上级操作/ 或显示全部文件
            if (null == s || s.toString().isEmpty()) {
                Log.d(TAG, "onTextChanged: 111");
                rl_button_main.setVisibility(View.VISIBLE);
                ll_folder_back.setVisibility(View.GONE);
                ll_back_ltr.setVisibility(View.GONE);
            } else {
                rl_button_main.setVisibility(View.GONE);
                ll_folder_back.setVisibility(View.VISIBLE);
                ll_back_ltr.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged: s = " + s);
        }
    };
}
