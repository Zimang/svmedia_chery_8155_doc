package com.desaysv.usbpicture.fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.querypicture.QueryManager;
import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibpicturebean.manager.ScrollListManager;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.adapter.USB1PictureFolderAdapter;
import com.desaysv.usbpicture.ui.BasePictureActivity;
import com.desaysv.usbpicture.utils.ProductUtils;
import com.desaysv.usbpicture.view.VerticalRecycleView;

import java.lang.ref.WeakReference;
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
    protected ImageView ivTopMask;

    protected RelativeLayout rl_toast_bad;
    protected RelativeLayout rl_empty_folder;

    //define var here
    public static final int STYLE_TYPE_ALL_PICTURE = 0;// All picture
    public static final int STYLE_TYPE_FOLDER = 1;// folder
    protected int styleType = 0;// default All picture
    protected boolean shouldUpdate = true;
    protected boolean isFirst = true;
    protected boolean hadShowToast = false;

    public static final int ANIM_TIME_CLICK_FOLDER = 240;//点击文件夹的动画时长
    public static final int ANIM_NEXT_ARG = 2;//启动下一步的因子，即动画启动多久之后，开始进行下一步处理
    protected boolean isStartClickFolder = false;

    public static final int CONTENT_STATE_EMPTY = 0;//内容为空
    public static final int CONTENT_STATE_NO_EMPTY = 1;//内容不为空
    private int currentFolderFlag = -1;

    public IStatueChange folderPathChangeListener;

    protected boolean isBackToPreDirectory = false;//是否返回上一级
    private boolean isRightRudder = ProductUtils.isRightRudder();
    /**
     * 公用View，因此不需要复写
     * @return
     */
    protected  int getLayoutResID(){
        return isRightRudder ? R.layout.layout_list_fragment_right: R.layout.layout_list_fragment;
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
        ivTopMask =view.findViewById(R.id.ivTopMask);

        rl_toast_bad = view.findViewById(R.id.rl_toast_bad);
        rl_empty_folder = view.findViewById(R.id.rl_empty_folder);


        mGridLayoutManager = new GridLayoutManager(getContext(), 5);
//        rv_picture_list.addItemDecoration(new SpaceItemDecoration(0, 0));
        rv_picture_list.setLayoutManager(mGridLayoutManager);
        folderAdapter = new USB1PictureFolderAdapter(getContext(),getUSBType(),this);
        rv_picture_list.setAdapter(folderAdapter);
        rl_loading = view.findViewById(R.id.rl_loading);
        iv_loading = view.findViewById(R.id.iv_loading);
        myHandler = new MyHandler(this);
        updateViewWithStyleChanged(STYLE_TYPE_ALL_PICTURE);
        updateViewWithCheckDeviceStatues(checkUSBStatus());
        rv_picture_list.setOverScrollMode(View.OVER_SCROLL_NEVER);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        if (configuration != null) {
            if(configuration.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL){
                int paddingStart = (int) getResources().getDimension(R.dimen.usb_picture_padding_start);
                int paddingEnd = (int) getResources().getDimension(R.dimen.usb_picture_padding_end);
                Log.d(TAG,"isRTLView, set paddingStart = " + paddingStart + ", paddingEnd = " + paddingEnd);
                if(isRightRudder){
                    rv_picture_list.setPadding(paddingEnd, 0,paddingStart,0);
                } else {
                    rv_picture_list.setPadding(paddingStart, paddingEnd,0,0);
                }
            }
        }

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
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void updateViewWithUSBStatus(boolean connected){
        Log.d(TAG,"updateViewWithUSBStatus :" + connected);
        rl_pictureList_root_control.setVisibility(View.GONE);//设备状态变化时，显示路径都要隐藏
        ScrollListManager.getInstance().cleanOffsetList();
        rv_picture_list.scrollBy(0,0);
        if (connected) {
            ll_no_connect.setVisibility(View.GONE);
            rl_pictureList_root_name.setVisibility(View.GONE);
            rl_usb_pictureList_root_type.setVisibility(View.GONE);
            tv_usb_name.setText(getUUID());
            updateViewWithScanStatus();
        }else {
//            mLoadingDialog.dismiss();
            startLoadingAni(false);
            updateListAdapter(null,null,null);
            ll_no_connect.setVisibility(View.VISIBLE);
            rl_pictureList_root_name.setVisibility(View.GONE);
            rl_usb_pictureList_root_type.setVisibility(View.GONE);
            updateViewWithNoContent(true);//需要隐藏
            folderAdapter.setCurrentFolderPath(null);
            ivTopMask.setVisibility(View.GONE);
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
            folderPathChangeListener.changed(true,USBConstants.USBPath.USB0_PATH);
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

        if (currentFolderFlag == USB1PictureFolderAdapter.FLAG_EMPTY_FOLDER){
            rl_empty_folder.setVisibility(View.VISIBLE);
        }else {
            ll_no_content.setVisibility(!checkUSBStatus() || hasContent && currentFolderFlag != USB1PictureFolderAdapter.FLAG_EMPTY_FOLDER ? View.GONE : View.VISIBLE);
            rl_empty_folder.setVisibility(View.GONE);
        }

        rl_pictureList_root.setVisibility( !checkUSBStatus() ? View.GONE : hasContent && currentFolderFlag != USB1PictureFolderAdapter.FLAG_EMPTY_FOLDER ? View.VISIBLE : View.GONE);
        ivTopMask.setVisibility( !checkUSBStatus() ? View.GONE : hasContent && currentFolderFlag != USB1PictureFolderAdapter.FLAG_EMPTY_FOLDER ? View.VISIBLE : View.GONE);
        folderPathChangeListener.onContentChange(hasContent && currentFolderFlag != USB1PictureFolderAdapter.FLAG_EMPTY_FOLDER ? CONTENT_STATE_NO_EMPTY : CONTENT_STATE_EMPTY);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public abstract boolean handleShowAllPictures();

    @RequiresApi(api = Build.VERSION_CODES.R)
    public abstract boolean handleShowFolderPictures();


    public void reSetStyleWithDeviceStatues(){
        shouldUpdate = true;
        hadShowToast = false;
        currentFolderFlag = -1;
        styleType = STYLE_TYPE_ALL_PICTURE;
        updateViewWithStyleChanged(styleType);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("hadShowToast",hadShowToast);
        outState.putInt("styleType",styleType);
        Log.d(TAG,"store save");
        super.onSaveInstanceState(outState);
    }

    protected void updateListAdapter(List<FileMessage> pictures, List<FolderMessage> folders, List<FolderMessage> emptyFolders){
        folderAdapter.updatePictureListFileMessage(pictures,folders,emptyFolders);
        if(isBackToPreDirectory) {
            Log.d(TAG,"BackToPreDirectory and scroll");
            rv_picture_list.scrollBy(0,0);
            rv_picture_list.scrollBy(0, ScrollListManager.getInstance().getOffset(folderAdapter.getCurrentFolderPath()));
            isBackToPreDirectory = false;//阅后即焚
            folderAdapter.removeBackListTop();//阅后即焚
        }
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
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
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
        folderPathChangeListener = null;
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
        isBackToPreDirectory = true;
        String queryPath = folderAdapter.getCurrentFolderParentPath();
        Log.d(TAG,"queryPath" + queryPath);
        if (!(queryPath.length() < USBConstants.USBPath.USB0_PATH.length())) {//最多只能查询到根目录
            rl_empty_folder.setVisibility(View.GONE);
            rl_pictureList_root.setVisibility(View.VISIBLE);
            currentFolderFlag = -1;
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
    //Added by ZNB for BugBUG202210091838_68704 on 2022-10-09 begin
    protected static final int CLICK_BADITEM_TIMEOUT = 2000;
    protected static final int MSG_CLICK_BADITEM_TIMEOUT = 7;
    //Added by ZNB for BugBUG202210091838_68704 on 2022-10-09 end
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
                    weakReference.get().handleClickFolder((String) msg.obj, msg.arg1);
                    break;
                case MSG_CLICK_PREVIEW:
                    weakReference.get().handleGotoPreView((Integer) msg.obj);
                    break;
                //Added by ZNB for BugBUG202210091838_68704 on 2022-10-09 begin
                case MSG_CLICK_BADITEM_TIMEOUT:
                    weakReference.get().handleClickBadItem(false);
                    break;
                //Added by ZNB for BugBUG202210091838_68704 on 2022-10-09 end
            }

        }
    };

    public abstract void updateViewWithScanStatus();

    public abstract void updateQueryWithScanStatus();

    private ObjectAnimator loadingAnimator;
    protected void startLoadingAni(boolean start){
        Log.d(TAG,"startLoadingAni: "+start);
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
    public void clickFolder(String folderPath, int flag) {
        folderPathChangeListener.onFolderClick(folderPath);
        ValueAnimator animatorClickFolder = ValueAnimator.ofFloat(1f,0f,1f);//设置属性动画
        animatorClickFolder.setDuration(ANIM_TIME_CLICK_FOLDER);
        animatorClickFolder.start();
        isStartClickFolder = true;
        animatorClickFolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rl_pictureList_root.setAlpha((Float) animation.getAnimatedValue());
                long playTime = animation.getCurrentPlayTime();
                if (playTime > ANIM_TIME_CLICK_FOLDER/ANIM_NEXT_ARG && isStartClickFolder){
                    isStartClickFolder = false;
                    myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
                    myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
                    myHandler.removeMessages(MSG_CLICK_FOLDER);
                    myHandler.removeMessages(MSG_CLICK_PREVIEW);
                    myHandler.removeMessages(MSG_UPDATE_LIST);
                    Message message = new Message();
                    message.what = MSG_CLICK_FOLDER;
                    message.obj = folderPath;
                    message.arg1 = flag;
                    myHandler.sendMessage(message);
                }
            }
        });
    }

    //Added by ZNB for BugBUG202210091838_68704 on 2022-10-09 begin
    @Override
    public void clickBadItem() {
        handleClickBadItem(true);
        myHandler.removeMessages(MSG_CLICK_BADITEM_TIMEOUT);
        myHandler.sendEmptyMessageDelayed(MSG_CLICK_BADITEM_TIMEOUT,CLICK_BADITEM_TIMEOUT);
    }

    protected void handleClickBadItem(boolean showToast){
        if (showToast){
            rl_toast_bad.setVisibility(View.VISIBLE);
        }else {
            rl_toast_bad.setVisibility(View.GONE);
        }
    }
    //Added by ZNB for BugBUG202210091838_68704 on 2022-10-09 end

    protected void handleClickFolder(String folderPath, int flag){
        folderAdapter.setCurrentFolderPath(folderPath);
        Log.d(TAG, "currentFolderPath:" + folderPath + ",flag:" + flag);
        ScrollListManager.getInstance().saveCurrentOffset(folderAdapter.getCurrentFolderParentPath(),rv_picture_list.getVerticalScrollOffset());
        currentFolderFlag = flag;
        if (MediaKey.SUPPORT_SUBSECTION_LOADING) {
            updateList();
            rv_picture_list.scrollToPosition(0);//因为使用的是同一个RecycleView,如果当前已经滚动过，进入文件夹的时候，位置会处于滚动的位置
            if (flag == USB1PictureFolderAdapter.FLAG_EMPTY_FOLDER) {
                rl_empty_folder.setVisibility(View.VISIBLE);
                rl_pictureList_root.setVisibility(View.GONE);
            }
        } else {
            QueryManager.getInstance().startQueryUSB1PictureWithDir(folderPath);
        }
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
        void changed(boolean isRoot, String showName);
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
