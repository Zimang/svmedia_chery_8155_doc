package com.desaysv.usbpicture.view;

import android.graphics.Canvas;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.usbpicture.adapter.USB1PictureFolderAdapter;

/**
 * 自定义ItemTouchHelper
 */
public class MyItemTouchHelper extends ItemTouchHelper.Callback {

    private static final String TAG = "MyItemTouchHelper";

    private USB1PictureFolderAdapter adapter;

    public MyItemTouchHelper(USB1PictureFolderAdapter adapter) {
        this.adapter = adapter;
        Log.d(TAG,"MyItemTouchHelper construct");
    }

    /**
     * swipe/drag 对应的viewHolder改变的时候调用，可用来判断开始和结束的时机：viewHolder非空时为开始，viewHolder为空时为结束
     * @param viewHolder
     * @param actionState
     */
    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * 针对drag状态，当前target对应的item是否允许move，可以自定义条件
     * @param recyclerView
     * @param current
     * @param target
     * @return
     */
    @Override
    public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
        return super.canDropOver(recyclerView, current, target);
    }

    /**
     * 这个是 drag 状态，会回调这个方法，需要在这里自行去处理拖动换位置的逻辑
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d(TAG,"onMove");
        //todo 交换List中的位置
        adapter.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }

    /**
     * 设置支持的方向
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG,"getMovementFlags");
        //MakeFlag是原生的方法，需要使用这个返回flag值，不可以单独返回方向
        return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,ItemTouchHelper.LEFT)|makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT|ItemTouchHelper.UP|ItemTouchHelper.DOWN);
    }

    /**
     * swipe 达到条件时，回调这个方法，在这里做删除的实现
     * @param viewHolder
     * @param direction
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d(TAG,"onSwiped,:"+direction);
        //实现swipe处理
//        adapter.swipe(viewHolder.getAdapterPosition());
    }

    /**
     * swipe 滑动位置超过 百分之几 就消失
     * @param viewHolder
     * @return
     */
    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG,"getSwipeThreshold");
        return super.getSwipeThreshold(viewHolder);
    }
    /**
     * 滑动的阻尼系数，这里指的是滑动的角度，值越大，允许的角度就越大，值越小，允许的角度就越小
     * 即，值越大，斜斜的滑动即可，值越小，那就要平直的滑动
     * @param defaultValue
     * @return
     */
    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        Log.d(TAG,"getSwipeVelocityThreshold");
        return super.getSwipeVelocityThreshold(defaultValue);
    }
    /**
     * swipe的逃逸速度，即 就算没有达到设置的距离(getSwipeThreshold)，达到这个逃逸速度也会触发对应的swipe处理
     * @param defaultValue
     * @return
     */
    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        Log.d(TAG,"getSwipeEscapeVelocity");
        return super.getSwipeEscapeVelocity(defaultValue);
    }

    /**
     * 滑动过程中会一直回调这个，可以在这里处理一些动画效果
     * @param c
     * @param recyclerView
     * @param viewHolder
     * @param dX
     * @param dY
     * @param actionState
     * @param isCurrentlyActive
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        Log.d(TAG,"onChildDraw");
    }
}
