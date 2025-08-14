package com.desaysv.moduleusbvideo.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.adapter.USBVideoAllListAdapter;
import com.desaysv.moduleusbvideo.adapter.USBVideoFolderListAdapter;
import com.desaysv.moduleusbvideo.base.VideoBaseFragment;
import com.desaysv.moduleusbvideo.bean.FolderBean;
import com.desaysv.moduleusbvideo.bean.SelectModeBean;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.moduleusbvideo.util.VideoFileListTypeTool;
import com.desaysv.moduleusbvideo.view.VideoGridLayoutManager;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.statussubject.SearchType;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Create by extodc87 on 2022-11-1
 * Author: extodc87
 */
public abstract class BaseVideoListFragment extends VideoBaseFragment {

    private SelectModeBean.ModeListener modeOnClickListener;
    private TextView tvFolderName;
    private ImageView iv_back;
    private RecyclerView rcvList;
    private RelativeLayout rlNoDataTips;
    private RelativeLayout rlLoading;
    private ImageView ivLoading;

    /**
     * 加载的动画
     */
    private ObjectAnimator rotateAnim;

    private USBVideoAllListAdapter usbVideoAllListAdapter;
    private USBVideoFolderListAdapter usbVideoFolderListAdapter;

    private String mCurrentPlayPath = null;
    private String mFolderPath = null;

    /**
     * 根据选择 显示对应列表
     * 1、全部列表
     * 2、文件夹列表
     */
    private static final int UPDATE_LIST_TYPE = 1;
    /**
     * 更新视频文件夹路径
     */
    private static final int MSG_UPDATE_FOLDER_PATH = UPDATE_LIST_TYPE + 1;

    private static final int MSG_UPDATE_FILE_MESSAGE = MSG_UPDATE_FOLDER_PATH + 1;

    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        private final WeakReference<BaseVideoListFragment> weakReference;

        MyHandler(BaseVideoListFragment baseVideoListFragment) {
            weakReference = new WeakReference<>(baseVideoListFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final BaseVideoListFragment baseVideoListFragment = weakReference.get();
            switch (msg.what) {
                case UPDATE_LIST_TYPE:
                    baseVideoListFragment.updateAdapterList();
                    break;
                case MSG_UPDATE_FOLDER_PATH:
                    baseVideoListFragment.updateFolderPathShow((String) msg.obj);
                    break;
                case MSG_UPDATE_FILE_MESSAGE:
                    baseVideoListFragment.updateCurrentFileMessage();
                    break;
            }
        }
    }

    @Override
    public int getLayoutResID() {
        return R.layout.usb_video_fragment_list;
    }

    @Override
    public void initView(View view) {
        boolean rtl = Constant.isRtl();
        Log.d(TAG, "initView() called with: view = [" + view + "] , rtl = [" + rtl + "]");
        rcvList = view.findViewById(R.id.rcv_list);
        rlNoDataTips = view.findViewById(R.id.rl_no_data_tips);
        rlLoading = view.findViewById(R.id.rl_loading);
        ivLoading = view.findViewById(R.id.iv_loading);

        // Android 已知的BUG，需要在 onLayoutChildren通过try-catch解决，没有副作用
        RecyclerView.LayoutManager layoutManager = rcvList.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int spanCount = gridLayoutManager.getSpanCount();
            VideoGridLayoutManager videoGridLayoutManager = new VideoGridLayoutManager(getContext(), spanCount);
            rcvList.setLayoutManager(videoGridLayoutManager);
        }

        //去除 列表动画
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(0);
        animator.setRemoveDuration(0);
        animator.setChangeDuration(0);
        animator.setMoveDuration(0);
        rcvList.setItemAnimator(animator);

        if (rtl){
            if(!Constant.isExeed()){
                rcvList.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
        }else {
            //右舵，左英
            if(!Constant.isExeed()){
                boolean leftOrRightConfig = Constant.isLeftOrRightConfig();
                if(leftOrRightConfig){
                    // 滑动条保持国内方向LTR
                    rcvList.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                }


            }
        }
    }

    @Override
    public void initData() {
        if (null == mHandler) {
            mHandler = new MyHandler(this);
            Log.i(TAG, "initData: new mHandler");
        }
    }

    @Override
    public void initViewListener() {

    }

    @Override
    public void setInitData(Object data) {
        super.setInitData(data);
        Log.d(TAG, "setInitData: " + data);
        if (null == mHandler) {
            mHandler = new MyHandler(this);
            Log.i(TAG, "setInitData: new mHandler");
        }
        SelectModeBean bean = (SelectModeBean) data;
        this.modeOnClickListener = bean.getOnClickListener();
        if (VideoFileListTypeTool.STYLE_TYPE_FOLDER == bean.getVideoFileListType()) {
            LinearLayout llFolder = bean.getLlFolder();
//            boolean rtl = Constant.isRtl();
//            Log.i(TAG, "setInitData: rtl: " + rtl);
//            if (rtl) {
//                // 强制LTR（返回按钮）
//                if(!Constant.isExeed()){
//                    llFolder.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
//                }
//            }
            tvFolderName = llFolder.findViewById(R.id.tv_folder_name);
            if (Constant.isT19CFlavor()) {
                llFolder.setOnClickListener(onClickListener);
            } else {
                iv_back = llFolder.findViewById(R.id.iv_back);
                iv_back.setOnClickListener(onClickListener);
            }
            getVideoFileListTypeUtils().setStyleType(VideoFileListTypeTool.STYLE_TYPE_FOLDER);
        } else {
            getVideoFileListTypeUtils().setStyleType(VideoFileListTypeTool.STYLE_TYPE_ALL);
        }
        mHandler.sendEmptyMessage(UPDATE_LIST_TYPE);
    }

    @Override
    public void onStart() {
        super.onStart();
        attachObserver(listStatusChangeObserver);
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 刷新列表前，获取当前保存的视频地址，匹配列表播放状态
        mHandler.sendEmptyMessage(MSG_UPDATE_FILE_MESSAGE);
        mHandler.sendEmptyMessage(UPDATE_LIST_TYPE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachObserver(listStatusChangeObserver);
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
    }

    /**
     * 设备状态发生改变的时候会触发的监听
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            if (path.equals(getRootPath()) && !status) {
                // 退出到文件夹根部
                mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FOLDER_PATH, getRootPath()));
            }
        }
    };

    /**
     * 刷新列表
     * 判断界面是否有数据
     * 判断当前模式类型
     */
    private void updateAdapterList() {
        SearchType searchType = getSearchType();
        Log.d(TAG, "updateAdapterList: searchType = " + searchType + " size = " + getAllVideoList().size() + " , modeOnClickListener: " + modeOnClickListener);
        if (searchType.ordinal() == SearchType.SEARCHING.ordinal()) {
            loading();
            // 刷新中
            rcvList.setVisibility(View.VISIBLE);
            rlNoDataTips.setVisibility(View.GONE);
            if (null != modeOnClickListener) {
                modeOnClickListener.onClick(true);
            }
        } else {
            stopLoading();
            if (searchType.ordinal() == SearchType.NO_DATA.ordinal() && getAllVideoList().isEmpty()) {
                // 没有数据
                rcvList.setVisibility(View.GONE);
                rlNoDataTips.setVisibility(View.VISIBLE);
                if (null != modeOnClickListener) {
                    modeOnClickListener.onClick(true);
                }
            } else {
                // 显示界面
                rcvList.setVisibility(View.VISIBLE);
                rlNoDataTips.setVisibility(View.GONE);
                if (null != modeOnClickListener) {
                    modeOnClickListener.onClick(false);
                }
            }
        }
        switch (getVideoFileListTypeUtils().getStyleType()) {
            case VideoFileListTypeTool.STYLE_TYPE_ALL:
                showAllList();
                break;
            case VideoFileListTypeTool.STYLE_TYPE_FOLDER:
                Log.d(TAG, "updateAdapterList: mFolderPath = " + mFolderPath);
                String tempPath = null == mFolderPath ? getRootPath() : mFolderPath;
                showFolderList(tempPath);
                break;
        }
    }


    private void loading() {
        rlLoading.setVisibility(View.VISIBLE);
        if (null == rotateAnim) {
            // 初始化旋转动画，旋转中心默认为控件中点
            rotateAnim = ObjectAnimator.ofFloat(ivLoading, "rotation", 0f, 360f);
            rotateAnim.setDuration(1000);
            rotateAnim.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);//循环
            rotateAnim.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (!rotateAnim.isStarted()) {
            rotateAnim.start();
        }
    }

    private void stopLoading() {
        rlLoading.setVisibility(View.GONE);
        if (null != rotateAnim && rotateAnim.isStarted()) {
            rotateAnim.end();
        }
    }

    /**
     * 更新当前歌曲信息
     */
    private void updateCurrentFileMessage() {
        this.mCurrentPlayPath = MediaPlayStatusSaveUtils.getInstance().getMediaPlayPath(getMediaType());
        Log.d(TAG, "updateCurrentFileMessage: mCurrentPlayPath = " + mCurrentPlayPath);
    }

    /**
     * 跳转到对应的位置，并且在指定的位置
     *
     * @param recyclerView 列表
     * @param position     需要显示的item的位置
     * @param pagerCount   一页显示多少个item
     */
    private void scrollToShowPosition(RecyclerView recyclerView, int position, int pagerCount, GridLayoutManager gridLayoutManager) {
        //减去1，是为了和实际的位置做对应
        int itemCount = recyclerView.getAdapter().getItemCount();
        Log.d(TAG, "scrollToShowPosition() called with: position = [" + position + "], pagerCount = [" + pagerCount + "], itemCount = [" + itemCount + "]");
        if (itemCount == -1 || itemCount == 0) {
            Log.e(TAG, "scrollToShowPosition: No jump");
            return;
        }
        // to grid layout
        double ceil = Math.ceil(position / pagerCount);
        position = (int) (pagerCount * ceil);
        Log.d(TAG, "scrollToShowPosition: jumpPosition = " + position + " , ceil: " + (ceil) + " , position: " + position);
        gridLayoutManager.scrollToPositionWithOffset(position, 0);
//        gridLayoutManager.setStackFromEnd(true);
    }

    /**
     * 显示全部地址
     */
    @SuppressLint("NotifyDataSetChanged")
    private void showAllList() {
        Log.d(TAG, "showAllList: mCurrentPlayPath = " + mCurrentPlayPath);
        if (null == usbVideoAllListAdapter) {
            usbVideoAllListAdapter = new USBVideoAllListAdapter(getContext(), getAllVideoList(), itemClickListener);
            if (Constant.isT19CFlavor()) {
                usbVideoAllListAdapter.setFooterView(false);
            }
        }
        RecyclerView.Adapter adapter = rcvList.getAdapter();
        // 当前为空，或者当前是文件夹，则需要重新设置adapter
        if (null == adapter || adapter instanceof USBVideoFolderListAdapter) {
            rcvList.setAdapter(usbVideoAllListAdapter);
        }
        usbVideoAllListAdapter.updateData(getAllVideoList());
        usbVideoAllListAdapter.updateCurrentPlayPath(mCurrentPlayPath);
        int position = usbVideoAllListAdapter.getFileMessagePosition(mCurrentPlayPath);
        // 跳转到对应的位置
        RecyclerView.LayoutManager layoutManager = rcvList.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int spanCount = gridLayoutManager.getSpanCount();
            scrollToShowPosition(rcvList, position, spanCount, gridLayoutManager);
        }
    }

    /**
     * 显示文件夹列表
     *
     * @param path 文件夹地址
     */
    private void showFolderList(String path) {
        Log.d(TAG, "showFolderList: showPath = " + path + " mCurrentPlayPath = " + mCurrentPlayPath);
        if (null == usbVideoFolderListAdapter) {
            usbVideoFolderListAdapter = new USBVideoFolderListAdapter(getContext(), folderListListener);
            if (Constant.isT19CFlavor()) {
                usbVideoFolderListAdapter.setFooterView(false);
            }
        }
        RecyclerView.Adapter adapter = rcvList.getAdapter();
        // 当前为空，或者当前是全部列表，则需要重新设置adapter
        if (null == adapter || adapter instanceof USBVideoAllListAdapter) {
            rcvList.setAdapter(usbVideoFolderListAdapter);
        }
        usbVideoFolderListAdapter.updateCurrentPlayPath(mCurrentPlayPath);
        usbVideoFolderListAdapter.setFolderBeanListPath(path);
    }

    /**
     * 每次列表刷新后，都需要跳转到播放位置
     * 文件夹列表界面，跳转到播放位置
     */
    private void folderListToJump() {
        if (null == usbVideoFolderListAdapter) {
            Log.e(TAG, "folderListToJump: usbVideoFolderListAdapter is null ");
            return;
        }
        int position = usbVideoFolderListAdapter.getFileMessagePosition(mCurrentPlayPath);
        Log.i(TAG, "folderListToJump: TO jump list");
        // 跳转到对应的位置
        RecyclerView.LayoutManager layoutManager = rcvList.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int spanCount = gridLayoutManager.getSpanCount();
            scrollToShowPosition(rcvList, position, spanCount, gridLayoutManager);
        }
    }

    /**
     * 更新adapter的路径显示
     *
     * @param path 路径
     */
    @SuppressLint("SetTextI18n")
    private void updateFolderPathShow(String path) {
        mFolderPath = path;
        // 跳转位置
        folderListToJump();
        if (path.equals(getRootPath())) {
            Log.d(TAG, "updateFolderPathShow: base path");
            if (null != tvFolderName) {
                tvFolderName.setText(null);
            }
            return;
        }
        // /storage/usb0/media
        Log.d(TAG, "updateFolderPathShow: path = " + path);
        //路径信息更改- /storage/usb  替换为linux 显示路径
        if (null != tvFolderName) {
//            tv_folder_name.setText("/media/usbstrage" + path.substring(12));
            tvFolderName.setText(path.substring(path.lastIndexOf("/") + 1));
        }
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 文件夹列表 ，点击返回操作
            if (v.getId() == R.id.ll_folder_back || v.getId() == R.id.iv_back) {
                String folderPath = usbVideoFolderListAdapter.getFolderPath();
                boolean isRoot = usbVideoFolderListAdapter.moveToParentFolder();
                Log.d(TAG, "onClick: folderPath: " + folderPath + ", isRoot: " + isRoot);
                if (isRoot) {
                    tvFolderName.setText(null);
                }
            }
        }
    };

    /**
     * 数据状态发生变更，刷新界面
     */
    private final Observer listStatusChangeObserver = new Observer() {
        @Override
        public void onUpdate() {
            Log.d(TAG, "onUpdate: to video list");
            mHandler.sendEmptyMessage(UPDATE_LIST_TYPE);
        }
    };

    /**
     * 文件夹列表的点击回调
     */
    private final USBVideoFolderListAdapter.FolderItemClickListener folderListListener =
            new USBVideoFolderListAdapter.FolderItemClickListener() {

                /**
                 * adapter的路径发送改变的时候，会触发的回调
                 *
                 * @param folderPath 文件夹路径
                 */
                @Override
                public void onFolderPathChange(String folderPath) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FOLDER_PATH, folderPath));
                }

                /**
                 * 从界面那里获取文件夹列表
                 *
                 * @param path 文件夹列表的路径
                 * @return 文件夹列表
                 */
                @Override
                public List<FolderBean> getFolderBeanList(String path) {
                    return BaseVideoListFragment.this.getFolderBeanList(path);
                }

                /**
                 * 文件夹里面的文件被点击了
                 *
                 * @param path 路径
                 */
                @Override
                public void onFolderClick(String path) {
                    Log.d(TAG, "onFolderClick: path = " + path);
                    showFolderList(path);
                }

                @Override
                public void onItemClick(FileMessage fileMessage) {
                    Log.d(TAG, "FolderItemClickListener onItemClick: fileMessage = " + fileMessage);
                    startActivity(VideoFileListTypeTool.STYLE_TYPE_FOLDER, fileMessage);
                }
            };


    /**
     * 全部列表点击事件
     */
    private final USBVideoAllListAdapter.ItemClickListener itemClickListener =
            new USBVideoAllListAdapter.ItemClickListener() {
                @Override
                public void onItemClick(FileMessage fileMessage) {
                    Log.d(TAG, "onItemClick: fileMessage = " + fileMessage);
                    startActivity(VideoFileListTypeTool.STYLE_TYPE_ALL, fileMessage);
                }
            };


    /**
     * 注册观察者
     *
     * @param observer 观察者
     */
    protected abstract void attachObserver(Observer observer);

    protected abstract void detachObserver(Observer observer);

    /**
     * 获取扫描状态
     */
    protected abstract SearchType getSearchType();

    /**
     * 获取文件夹列表
     *
     * @param path 文件夹列表的路径
     * @return 文件夹列表
     */
    protected abstract List<FolderBean> getFolderBeanList(String path);

    protected abstract String getRootPath();

    protected abstract List<FileMessage> getAllVideoList();

    protected abstract VideoFileListTypeTool getVideoFileListTypeUtils();

    protected abstract void startActivity(int styleType, FileMessage fileMessage);

    protected abstract MediaType getMediaType();
}
