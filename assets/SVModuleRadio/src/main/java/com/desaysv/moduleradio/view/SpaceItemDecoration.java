package com.desaysv.moduleradio.view;

/**
 * Created by uidp5686 on 2019/11/06.
 */

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int leftRight;
    public SpaceItemDecoration(int leftRight) {
        this.leftRight = leftRight;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 获取view 在adapter中的位置。
        int position = parent.getChildAdapterPosition(view);
        if (position % 2 == 0){

        }else {
            outRect.left = leftRight;
        }
    }


}
