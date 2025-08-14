package com.desaysv.moduleusbvideo.view;

import static com.desaysv.libusbmedia.bean.MediaAction.OPEN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.adapter.USBVideoFolderListAdapter;
import com.desaysv.moduleusbvideo.adapter.USBVideoPlayListAdapter;
import com.desaysv.moduleusbvideo.util.Constant;

/**
 * Create by extodc87 on 2022-11-4
 * Author: extodc87
 */
public class VideoPlayListPopupWindow {
    private static final String TAG = "VideoPlayListPopupWindow";
    private PopupWindow pwd;
    private final Activity mContext;
    private View mView;

    private LinearLayout rooView;
    private RecyclerView rcvList;

    private USBVideoPlayListAdapter usbVideoPlayListAdapter;

    private final IControlTool iControlTool;
    private final IStatusTool iStatusTool;
    private final MediaType mediaType;

    private static final int UPDATE_ADAPTER_MESSAGE_TO_POSITION = 1;
    private static final int UPDATE_ADAPTER_MESSAGE_TO_LAST_FILE_MESSAGE = UPDATE_ADAPTER_MESSAGE_TO_POSITION + 1;
    private static final int UPDATE_ADAPTER_MESSAGE_TO_PLAY_STATUS = UPDATE_ADAPTER_MESSAGE_TO_LAST_FILE_MESSAGE + 1;

    private static final long TIMER_DELAY_OFF = 5000L;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_ADAPTER_MESSAGE_TO_POSITION) {
                updatePosition();
            } else if (msg.what == UPDATE_ADAPTER_MESSAGE_TO_LAST_FILE_MESSAGE) {
                updateLastMessageAdapter();
            } else if (msg.what == UPDATE_ADAPTER_MESSAGE_TO_PLAY_STATUS) {
                updatePlayStatusAdapter();
            }
        }
    };
    /**
     * 5s无操作自动关闭
     */
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Runnable: dismiss");
            dismiss();
        }
    };

    public VideoPlayListPopupWindow(Activity context, IControlTool iControlTool, IStatusTool iStatusTool, MediaType mediaType) {
        this.mContext = context;
        this.iControlTool = iControlTool;
        this.iStatusTool = iStatusTool;
        this.mediaType = mediaType;
        initView();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        Log.d(TAG, "initView: ");
        mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_play_list, null);
        rooView = mView.findViewById(R.id.roo_view);
        rcvList = mView.findViewById(R.id.rcv_list);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        rcvList.setLayoutManager(layoutManager);

        initPlayListAdapter();
        rcvList.setAdapter(usbVideoPlayListAdapter);

        rcvList.addOnScrollListener(onScrollListener);
        //右舵，左英
        if(!Constant.isExeed()){
            boolean leftOrRightConfig = Constant.isLeftOrRightConfig();
            if(leftOrRightConfig){
                rcvList.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
        }
    }

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                Log.i(TAG, "onScrollStateChanged: RecyclerView.SCROLL_STATE_IDLE");
                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, TIMER_DELAY_OFF);
            } else {
                Log.w(TAG, "onScrollStateChanged: removeCallbacks(mRunnable)");
                mHandler.removeCallbacks(mRunnable);
            }
        }
    };

    /**
     * 播放列表下的点击事件
     */
    private void initPlayListAdapter() {
        if (null == usbVideoPlayListAdapter) {
            if (null == iStatusTool.getPlayList()) {
                Log.e(TAG, "initPlayListAdapter: getPlayList is null");
                return;
            }
            usbVideoPlayListAdapter = new USBVideoPlayListAdapter(mContext, iStatusTool.getPlayList(), new USBVideoPlayListAdapter.ItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Log.d(TAG, "onItemClick: position = " + position);
                    dismiss();
                    iControlTool.processCommand(OPEN, ChangeReasonData.CLICK_ITEM, position);
                }
            });
        }
    }

    /**
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    public void showPopupWindow() {
        if (null == pwd) {
            pwd = new PopupWindow(mView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
            pwd.setAnimationStyle(R.style.popupAnimation);
        }
        usbVideoPlayListAdapter.updateData(iStatusTool.getPlayList());
        mHandler.sendEmptyMessage(UPDATE_ADAPTER_MESSAGE_TO_POSITION);
        mHandler.sendEmptyMessage(UPDATE_ADAPTER_MESSAGE_TO_LAST_FILE_MESSAGE);
        mHandler.sendEmptyMessage(UPDATE_ADAPTER_MESSAGE_TO_PLAY_STATUS);
        rooView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: root event = " + event);
                dismiss();
                return true;
            }
        });
        backgroundAlpha(1.0f);
        pwd.showAtLocation(mView, Gravity.END, Constant.POSITION_X, Constant.POSITION_Y);

        pwd.setBackgroundDrawable(new ColorDrawable(0));
//        pwd.setFocusable(true);
//        pwd.setClippingEnabled(false);
        pwd.setOutsideTouchable(true);
        pwd.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Log.w(TAG, "onDismiss: ");
                backgroundAlpha(1.0f);
                mHandler.removeCallbacks(mRunnable);
            }
        });
        mHandler.postDelayed(mRunnable, TIMER_DELAY_OFF);
    }

    /**
     * 更新当前播放列表的位置
     */
    private void updatePosition() {
        //  获取最新搞得位置
        usbVideoPlayListAdapter.updatePosition(iStatusTool.getCurrentItemPosition());

        int currentPosition = usbVideoPlayListAdapter.getCurrentPosition();
        jumpToAllListPosition((LinearLayoutManager) rcvList.getLayoutManager(), currentPosition);
        rcvList.requestFocus();
    }

    public void updateMessage() {
        Log.i(TAG, "updateMessage: ");
        mHandler.sendEmptyMessage(UPDATE_ADAPTER_MESSAGE_TO_POSITION);
    }

    /**
     * 更新当前播放列表的位置
     */
    private void updateLastMessageAdapter() {
        String lastMediaPlayPath = MediaPlayStatusSaveUtils.getInstance().getLastMediaPlayPath(mediaType);
        Log.i(TAG, "updateLastMessageAdapter: lastMediaPlayPath: " + lastMediaPlayPath);
        usbVideoPlayListAdapter.updateLastPath(lastMediaPlayPath);
    }

    public void updatePlayStatusMessage() {
        Log.i(TAG, "updatePlayStatusMessage: ");
        mHandler.sendEmptyMessage(UPDATE_ADAPTER_MESSAGE_TO_PLAY_STATUS);
    }

    private void updatePlayStatusAdapter() {
        boolean playing = iStatusTool.isPlaying();
        Log.i(TAG, "updatePlayStatusAdapter: playing: " + playing);
        usbVideoPlayListAdapter.updatePlayStatus(playing);
    }

    public void updateLastMessage() {
        Log.i(TAG, "updateLastMessage: ");
        mHandler.sendEmptyMessage(UPDATE_ADAPTER_MESSAGE_TO_LAST_FILE_MESSAGE);
    }

    /**
     * 隐藏PopupWindow
     */
    public void dismiss() {
        if (pwd != null && pwd.isShowing()) {
            pwd.dismiss();
        }
    }


    public boolean isShowing() {
        if (pwd != null) {
            return pwd.isShowing();
        }
        return false;
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha bgAlpha 0.0-1.0
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        //0.0-1.0
        lp.alpha = bgAlpha;
        mContext.getWindow().setAttributes(lp);
    }

    /**
     * 跳转到全部列表的播放位置
     */
    private void jumpToAllListPosition(LinearLayoutManager linearLayoutManager, int position) {
//        FileMessage currentFileMessage = getStatusTool().getCurrentPlayItem();
//        int position = usbVideoAllListAdapter.getFileMessagePosition(currentFileMessage);
        Log.d(TAG, "jumpToAllListPosition: position = " + position);
        if (position != USBVideoFolderListAdapter.NOT_IN_LIST) {
            scrollToShowPosition(rcvList, position, 4, 1, linearLayoutManager);
        }
    }


    /**
     * 跳转到对应的位置，并且在指定的位置
     *
     * @param recyclerView 列表
     * @param position     需要显示的item的位置
     * @param pagerCount   一页显示多少个item
     * @param showPosition 需要显示在一页中的那个位置
     */
    private void scrollToShowPosition(RecyclerView recyclerView, int position, int pagerCount,
                                      int showPosition, LinearLayoutManager linearLayoutManager) {
        //减去1，是为了和实际的位置做对应
        int itemCount = recyclerView.getAdapter().getItemCount() - 1;
        Log.d(TAG, "scrollToShowPosition: position = " + position + " itemCount = " + itemCount);
        //如果到了整个列表的最后几位，那就直接跳转到列表的最末尾
        if (position > (itemCount - (pagerCount - showPosition))) {
            position = itemCount;
        } else {
            //需要减去需要跳转的位置
            position = position - showPosition;
            if (position < 0) {
                position = 0;
            }
        }
        Log.d(TAG, "scrollToShowPosition: jumpPosition = " + position);
//        recyclerView.scrollToPositionWithOffset(position, 0);
        linearLayoutManager.scrollToPositionWithOffset(position, 0);
//        linearLayoutManager.setStackFromEnd(true);
    }
}
