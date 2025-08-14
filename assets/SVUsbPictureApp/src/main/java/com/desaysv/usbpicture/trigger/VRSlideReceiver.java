package com.desaysv.usbpicture.trigger;

/**
 * created by ZNB on 2022-08-24
 * 用于接收语音的上滑/下滑 广播
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VRSlideReceiver extends BroadcastReceiver {
    private static final String TAG = "VRSlideReceiver";

    public static final String ACTION_VR_SLIDE = "com.desaysv.slide.action";
    public static final String OPERATION_VR = "operation";
    public static final String OPERATION_VR_UP = "up";
    public static final String OPERATION_VR_DOWN = "down";

    private RecyclerView recyclerView;
    //对自己应用来讲，这两个是固定的，所以不做动态处理
    private int spanCount = 5;//每一行显示的个数
    private int perPage = 2;//每一页能放多少行

    private int slide = spanCount * perPage;//每次翻页需要滚动的位置

    private int total;//列表总数，这个随着界面切换会变动
    private boolean hasRegister = false;

    public VRSlideReceiver(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        updateParameters();
    }

    //初始化需要用到的参数
    public void updateParameters(){
        total = recyclerView.getAdapter().getItemCount();//获取显示内容的总数
        Log.d(TAG,"updateParameters,total:"+total);
    }

    public void registerReceiver(Context context){
        if (!hasRegister) {
            Log.d(TAG, "registerReceiver");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_VR_SLIDE);
            context.registerReceiver(this, intentFilter);
            hasRegister = true;
        }
    }


    public void unregisterReceiver(Context context){
        Log.d(TAG,"unregisterReceiver");
        context.unregisterReceiver(this);
        hasRegister = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String operation = intent.getStringExtra(OPERATION_VR);
            Log.d(TAG, "onReceive,action:"+action+",operation:"+operation);
            if (ACTION_VR_SLIDE.equals(action)){
                if (OPERATION_VR_UP.equals(operation)){
                    slideView(true);
                }else if (OPERATION_VR_DOWN.equals(operation)){
                    slideView(false);
                }
            }
        }
    }

    /**
     * 实现滑动效果，需要根据内容进行计算
     * @param slideUp
     */
    public void slideView(boolean slideUp){
        Log.d(TAG,"slideView,slideUp:"+slideUp);
        if (recyclerView == null || recyclerView.getLayoutManager() == null || !recyclerView.getLayoutManager().canScrollVertically()){
            Log.d(TAG,"can not Scroll Vertically!");
            return;
        }

        int position = 0;
        if (slideUp){//向上滑动一页
            position = slide + ((GridLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        }else {//向下滑动一页
            position = ((GridLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() - slide;
        }
        Log.d(TAG,"slideView,position:"+position);
        if (position > total){
            position = total;
        }
        if (position < 0){
            position = 0;
        }
        recyclerView.smoothScrollToPosition(position);
    }
}
