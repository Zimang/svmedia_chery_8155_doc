package com.desaysv.usbpicture.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.querypicture.QueryManager;
import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.adapter.USB1PictureFolderAdapter;
import com.desaysv.usbpicture.trigger.VRSlideReceiver;
import com.desaysv.usbpicture.ui.BasePictureActivity;
import com.desaysv.usbpicture.view.SpaceItemDecoration;
import com.desaysv.usbpicture.view.VerticalRecycleView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class BaseUSBPictureListFragment extends Fragment implements View.OnClickListener,USB1PictureFolderAdapter.IGotoPreviewListener{
    protected   final String TAG = this.getClass().getSimpleName();

    //define view here
    protected VerticalRecycleView rv_picture_list;
    protected GridLayoutManager mGridLayoutManager;
    protected USB1PictureFolderAdapter folderAdapter;
    protected RelativeLayout rl_pictureList_root;
    protected RelativeLayout ll_no_connect;
    protected RelativeLayout  ll_no_content;
    protected TextView tv_reloading;
    private RelativeLayout rl_loading;
    private ImageView iv_loading;
    protected ImageView iv_back;
    protected TextView tv_current_dir;
    protected TextView tv_usb_name;
    protected TextView tv_usb_all_picture;
    protected TextView tv_usb_folder;
    protected RelativeLayout rl_pictureList_root_control;
    protected RelativeLayout rl_pictureList_root_name;
    protected RelativeLayout rl_usb_pictureList_root_type;

    //define var here
    public static final int STYLE_TYPE_ALL_PICTURE = 0;// All picture
    public static final int STYLE_TYPE_FOLDER = 1;// folder
    protected int styleType = 0;// default All picture
    protected boolean shouldUpdate = true;
    protected boolean isFirst = true;
    protected boolean hadShowToast = false;

    public static final int ANIM_TIME_CLICK_FOLDER = 150;//点击文件夹的动画时长
    public static final int ANIM_NEXT_ARG = 2;//启动下一步的因子，即动画启动多久之后，开始进行下一步处理

    public static final int CONTENT_STATE_EMPTY = 0;//内容为空
    public static final int CONTENT_STATE_NO_EMPTY = 1;//内容不为空

    public IStatueChange folderPathChangeListener;

    protected ImageView iv_masktop;
    protected ImageView iv_maskbottom;

    public VRSlideReceiver slideReceiver;

    /**
     * 公用View，因此不需要复写
     * @return
     */
    protected  int getLayoutResID(){
        return R.layout.layout_list_fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    protected  void initView(View view){
        rv_picture_list = view.findViewById(R.id.rv_picture_list);
        rl_pictureList_root = view.findViewById(R.id.rl_pictureList_root);
        ll_no_connect = view.findViewById(R.id.ll_no_connect);
        ll_no_content = view.findViewById(R.id.ll_no_content);
        tv_reloading = view.findViewById(R.id.tv_reloading);

        tv_current_dir = view.findViewById(R.id.tv_current_dir);
        iv_back = view.findViewById(R.id.iv_back);
        tv_usb_name = view.findViewById(R.id.tv_usb_name);
        tv_usb_all_picture = view.findViewById(R.id.tv_usb_all_picture);
        tv_usb_folder = view.findViewById(R.id.tv_usb_folder);
        rl_pictureList_root_control = view.findViewById(R.id.rl_pictureList_root_control);
        rl_pictureList_root_name = view.findViewById(R.id.rl_pictureList_root_name);
        rl_usb_pictureList_root_type = view.findViewById(R.id.rl_usb_pictureList_root_type);

        mGridLayoutManager = new GridLayoutManager(getContext(), 5);
        rv_picture_list.addItemDecoration(new SpaceItemDecoration(20, 20));
        rv_picture_list.setLayoutManager(mGridLayoutManager);
        folderAdapter = new USB1PictureFolderAdapter(getContext(),getUSBType(),this);
        rv_picture_list.setAdapter(folderAdapter);
        rl_loading = view.findViewById(R.id.rl_loading);
        iv_loading = view.findViewById(R.id.iv_loading);
        myHandler = new MyHandler(this);
        updateViewWithStyleChanged(STYLE_TYPE_ALL_PICTURE);
        updateViewWithCheckDeviceStatues(checkUSBStatus());

        iv_masktop = view.findViewById(R.id.iv_masktop);
        iv_maskbottom = view.findViewById(R.id.iv_maskbottom);

        slideReceiver = new VRSlideReceiver(rv_picture_list);

        rv_picture_list.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }


    protected abstract int getUSBType();

    protected abstract void initData();

    protected abstract   void updateData();

    protected abstract  void forceUpdateData();

    protected  void initViewListener(){
        tv_reloading.setOnClickListener(this);
        tv_usb_all_picture.setOnClickListener(this);
        tv_usb_folder.setOnClickListener(this);
        rl_pictureList_root_control.setOnClickListener(this);
//        待UI那边确认场景再放开
//        rv_picture_list.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                updateMask(true,mGridLayoutManager.findFirstCompletelyVisibleItemPosition() > 4);//4是span count
//            }
//        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void updateViewWithUSBStatus(boolean connected){
        Log.d(TAG,"updateViewWithUSBStatus :" + connected);
        rl_pictureList_root_control.setVisibility(View.GONE);//设备状态变化时，显示路径都要隐藏
        if (connected) {
            ll_no_connect.setVisibility(View.GONE);
            rl_pictureList_root_name.setVisibility(View.GONE);
            rl_usb_pictureList_root_type.setVisibility(View.GONE);
            tv_usb_name.setText(getUUID());
            updateViewWithScanStatus();
        }else {
//            mLoadingDialog.dismiss();
            startLoadingAni(false);
            updateListAdapter(null,null);
            ll_no_connect.setVisibility(View.VISIBLE);
            rl_pictureList_root_name.setVisibility(View.GONE);
            rl_usb_pictureList_root_type.setVisibility(View.GONE);
            updateViewWithNoContent(true);//需要隐藏
            folderAdapter.setCurrentFolderPath(null);
            iv_masktop.setVisibility(View.GONE);
            iv_maskbottom.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    protected void updateViewWithCurrentPath(String path){
        Log.d(TAG,"path:" + path);
        String showName = "";
        if (path != null && path.length() > USBConstants.USBPath.USB0_PATH.length()) {
            rl_pictureList_root_control.setVisibility(View.GONE);
            rl_pictureList_root_name.setVisibility(View.GONE);
            showName = path.substring(path.lastIndexOf("/") + 1);
            tv_current_dir.setText(showName);
            folderPathChangeListener.changed(false,showName);
        }else {//当前是根目录
            rl_pictureList_root_control.setVisibility(View.GONE);
            rl_pictureList_root_name.setVisibility(View.GONE);
            folderPathChangeListener.changed(true,path);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    protected void updateViewWithCheckDeviceStatues(boolean connected){


    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    protected String getUUID(){
//        StorageManager storageManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);
//        File file = new File(getUSBPath());
//        assert storageManager != null;
//        StorageVolume storageVolume = storageManager.getStorageVolume(file);
//        Log.d(TAG, "getUSBDeviceUUID: storageVolume = " + storageVolume);
//        if (storageVolume != null) {
//            return storageVolume.getDescription(getContext());
//        }

        return getString(R.string.usb_name);//getUSBPath();
    }

    public abstract String getUSBPath();


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("hadShowToast",hadShowToast);
        outState.putInt("styleType",styleType);
        Log.d(TAG,"store save");
        super.onSaveInstanceState(outState);
    }

    protected void updateViewWithStyleChanged(int style){
        if (style == STYLE_TYPE_ALL_PICTURE){
            tv_usb_all_picture.setSelected(true);
            tv_usb_folder.setSelected(false);
            tv_usb_all_picture.setAlpha(1);
            tv_usb_folder.setAlpha(0.6f);
        }else {
            tv_usb_all_picture.setSelected(false);
            tv_usb_folder.setSelected(true);
            tv_usb_folder.setAlpha(1);
            tv_usb_all_picture.setAlpha(0.6f);
        }
    }


    public void updateViewWithNoContent(boolean hasContent){
        ll_no_content.setVisibility(!checkUSBStatus() || hasContent  ? View.GONE : View.VISIBLE);
        rl_pictureList_root.setVisibility( !checkUSBStatus() ? View.GONE : hasContent ? View.VISIBLE : View.GONE);
        folderPathChangeListener.onContentChange(hasContent ? CONTENT_STATE_NO_EMPTY : CONTENT_STATE_EMPTY);
        iv_masktop.setVisibility(!checkUSBStatus() ? View.GONE : hasContent ? View.VISIBLE : View.GONE);
        iv_maskbottom.setVisibility(!checkUSBStatus() ? View.GONE : hasContent ? View.VISIBLE : View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public abstract boolean handleShowAllPictures();

    @RequiresApi(api = Build.VERSION_CODES.R)
    public abstract boolean handleShowFolderPictures();


    public void reSetStyleWithDeviceStatues(){
        shouldUpdate = true;
        hadShowToast = false;
        styleType = STYLE_TYPE_ALL_PICTURE;
        updateViewWithStyleChanged(styleType);
        folderPathChangeListener.changed(true,"");
    }

    protected void updateListAdapter(List<FileMessage> pictures, List<FolderMessage> folders){
        folderAdapter.updatePictureListFileMessage(pictures,folders);
        slideReceiver.updateParameters();
    }

    protected abstract void updateList();

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG,"onHiddenChanged");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG,"onAttach");
        if (context instanceof IStatueChange){
            folderPathChangeListener = (IStatueChange) context;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        if (savedInstanceState != null){
            hadShowToast = savedInstanceState.getBoolean("hadShowToast");
            styleType = savedInstanceState.getInt("styleType");
            Log.d(TAG,"restore save");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        return inflater.inflate(getLayoutResID(), null);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initViewListener();
        Log.d(TAG,"onViewCreated");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        Log.d(TAG,"onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart,shouldUpdate " + shouldUpdate);
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
//        EventBus.getDefault().register(this);
        if (shouldUpdate) {
            updateData();
        }
    }



//    @RequiresApi(api = Build.VERSION_CODES.R)
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void eventBusMsg(MessageBean messageBean) {
//        Log.d(TAG, "onEventBusMsg:  = " + messageBean.getType());
//        if (MessageBean.GO_TO_PREVIEW == messageBean.getType()){
//            shouldUpdate = false;// 进入到预览界面
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        slideReceiver.registerReceiver(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        slideReceiver.unregisterReceiver(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
//        mLoadingDialog.dismiss();
        startLoadingAni(false);
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG,"onDestroyView");
        if (loadingAnimator != null){
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG,"onDetach");
    }

    public abstract void updateViewWithContent();

    protected QueryManager.IQueryListener mListener =  new QueryManager.IQueryListener() {
        @Override
        public void onPreExecute(int count) {

        }

        @Override
        public void onProgressUpdate(int progress) {

        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onResult(List<FileMessage> pictureList, List<FolderMessage> folderList) {
            Log.d(TAG,"onResult");
        }
    };


    protected abstract void updateCurrentPictureList(List<FileMessage> pictureList);

    protected abstract boolean checkUSBStatus();


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_reloading){
            updateList();
        }else if (v.getId() == R.id.rl_pictureList_root_control){
            handleClick();
        }else if (v.getId() == R.id.tv_usb_all_picture){
            //show all current pictures
            myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
            myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
            myHandler.removeMessages(MSG_CLICK_FOLDER);
            myHandler.removeMessages(MSG_CLICK_PREVIEW);
            myHandler.sendEmptyMessageDelayed(MSG_HANDLE_CLICK_ALL_PICTURE,5);
        }else if (v.getId() == R.id.tv_usb_folder){
            //show all current folders picture
            myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
            myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
            myHandler.removeMessages(MSG_CLICK_FOLDER);
            myHandler.removeMessages(MSG_CLICK_PREVIEW);
            myHandler.sendEmptyMessageDelayed(MSG_HANDLE_CLICK_SHOW_FOLDER,5);
        }
    }

    public void handleClick(){
        String queryPath = folderAdapter.getCurrentFolderParentPath();
        Log.d(TAG,"queryPath" + queryPath);
        if (!(queryPath.length() < USBConstants.USBPath.USB0_PATH.length())) {//最多只能查询到根目录
            folderAdapter.setCurrentFolderPath(queryPath);
            QueryManager.getInstance().startQueryUSB1PictureWithDir(queryPath);
        }
    }

    protected static final int MSG_UPDATE_LIST = 0;
    protected static final int MSG_UPDATE_SEARCHING = 1;
    protected static final int MSG_UPDATE_SCAN_STATUS = 2;
    protected static final int MSG_HANDLE_CLICK_ALL_PICTURE = 3;
    protected static final int MSG_HANDLE_CLICK_SHOW_FOLDER = 4;
    protected static final int MSG_CLICK_FOLDER = 5;
    protected static final int MSG_CLICK_PREVIEW = 6;
    protected MyHandler myHandler;
    protected static class MyHandler extends Handler {
        private WeakReference<BaseUSBPictureListFragment> weakReference;
        public MyHandler(BaseUSBPictureListFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_LIST:
                    weakReference.get().updateList();
                    break;
                case MSG_UPDATE_SEARCHING:
                    weakReference.get().updateViewWithScanStatus();
                    break;
                case MSG_UPDATE_SCAN_STATUS:
                    weakReference.get().updateQueryWithScanStatus();
                    break;
                case MSG_HANDLE_CLICK_ALL_PICTURE:
                    weakReference.get().handleShowAllPictures();
                    weakReference.get().updateViewWithStyleChanged(STYLE_TYPE_ALL_PICTURE);
                    break;
                case MSG_HANDLE_CLICK_SHOW_FOLDER:
                    weakReference.get().handleShowFolderPictures();
                    weakReference.get().updateViewWithStyleChanged(STYLE_TYPE_FOLDER);
                    break;

                case MSG_CLICK_FOLDER:
                    weakReference.get().handleClickFolder((String) msg.obj);
                    break;
                case MSG_CLICK_PREVIEW:
                    weakReference.get().handleGotoPreView((Integer) msg.obj);
                    break;
            }

        }
    };

    public abstract void updateViewWithScanStatus();

    public abstract void updateQueryWithScanStatus();

    private ObjectAnimator loadingAnimator;
    protected void startLoadingAni(boolean start){
        if (loadingAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingAnimator = ObjectAnimator.ofFloat(iv_loading, "rotation", 0f, 360f);
            loadingAnimator.setDuration(1000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (start) {
            rl_loading.setVisibility(View.VISIBLE);
            if (!loadingAnimator.isStarted()) {
                loadingAnimator.start();
            }
        }else {
            rl_loading.setVisibility(View.GONE);
            if (loadingAnimator.isStarted()) {
                loadingAnimator.end();
            }
        }
    }

    @Override
    public void gotoPreView(int position) {
        myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
        myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
        myHandler.removeMessages(MSG_CLICK_FOLDER);
        myHandler.removeMessages(MSG_CLICK_PREVIEW);
        myHandler.removeMessages(MSG_UPDATE_LIST);
        Message message = new Message();
        message.what = MSG_CLICK_PREVIEW;
        message.obj = position;
        myHandler.sendMessageDelayed(message,5);
    }


    @Override
    public void clickFolder(String folderPath) {
        folderPathChangeListener.onFolderClick(folderPath);
        myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
        myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
        myHandler.removeMessages(MSG_CLICK_FOLDER);
        myHandler.removeMessages(MSG_CLICK_PREVIEW);
        myHandler.removeMessages(MSG_UPDATE_LIST);
        Message message = new Message();
        message.what = MSG_CLICK_FOLDER;
        message.obj = folderPath;
        myHandler.sendMessage(message);
        /*
        //启动一个动画，用于从 1--0 透明度渐变
        ValueAnimator animatorClickFolder = ValueAnimator.ofFloat(1f,0f);//设置属性动画
        animatorClickFolder.setDuration(ANIM_TIME_CLICK_FOLDER);
        animatorClickFolder.start();
        animatorClickFolder.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                    //消失的动画结束时，开始执行下一个阶段的处理
                    myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
                    myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
                    myHandler.removeMessages(MSG_CLICK_FOLDER);
                    myHandler.removeMessages(MSG_CLICK_PREVIEW);
                    myHandler.removeMessages(MSG_UPDATE_LIST);
                    Message message = new Message();
                    message.what = MSG_CLICK_FOLDER;
                    message.obj = folderPath;
                    myHandler.sendMessage(message);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                //消失的动画结束时，开始执行下一个阶段的处理
                myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
                myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
                myHandler.removeMessages(MSG_CLICK_FOLDER);
                myHandler.removeMessages(MSG_CLICK_PREVIEW);
                myHandler.removeMessages(MSG_UPDATE_LIST);
                Message message = new Message();
                message.what = MSG_CLICK_FOLDER;
                message.obj = folderPath;
                myHandler.sendMessage(message);
            }
        });
        animatorClickFolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rl_pictureList_root.setAlpha((Float) animation.getAnimatedValue());
            }
        });
         */
    }

    protected void handleClickFolder(String folderPath){
            folderAdapter.setCurrentFolderPath(folderPath);
            Log.d(TAG, "currentFolderPath:" + folderPath);
            if (MediaKey.SUPPORT_SUBSECTION_LOADING) {
                updateList();
            }else {
                QueryManager.getInstance().startQueryUSB1PictureWithDir(folderPath);
            }
//        //重新启动一个动画，用于从 0--1 透明度渐变
//        ValueAnimator animatorClickFolder = ValueAnimator.ofFloat(0f,1f);//设置属性动画
//        animatorClickFolder.setDuration(ANIM_TIME_CLICK_FOLDER);
//        animatorClickFolder.start();
//        animatorClickFolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                rl_pictureList_root.setAlpha((Float) animation.getAnimatedValue());
//            }
//        });

    }

    protected void handleGotoPreView(int position){
        Intent intent = new Intent(getContext(), BasePictureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BasePictureActivity.INTENT_POSITION, position);
        intent.putExtra(BasePictureActivity.INTENT_STYLE_TYPE, styleType);
        intent.putExtra(BasePictureActivity.INTENT_USB_TYPE, getUSBType());
        intent.putExtra(BasePictureActivity.INTENT_FOLDER_PATH, folderAdapter.getCurrentFolderPath());
        getContext().startActivity(intent);
        shouldUpdate();
    }

    protected abstract void shouldUpdate();


    /**
     * 设备状态变化的监听回调
     */
    private DeviceListener deviceListener = new DeviceListener() {
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            updateViewWithUSBStatus(status);
            reSetStyleWithDeviceStatues();
            folderPathChangeListener.onUSBStatueChange(path,status);
        }
    };


    public interface IStatueChange {
        //文件夹类型的根目录，用来确定是否显示Tab
        void changed(boolean isRoot,String path);
        void onFolderClick(String folder);
        void onUSBStatueChange(String path, boolean status);
        void onContentChange(int state);
    }


    /**
     * 用于区分USB插拔是否需要更新
     * 例如插拔USB1时不需要更新USB2
     * @param path
     * @param status
     * @return
     */
    public abstract boolean needUpdateWithUSBStatus(String path, boolean status);
}
