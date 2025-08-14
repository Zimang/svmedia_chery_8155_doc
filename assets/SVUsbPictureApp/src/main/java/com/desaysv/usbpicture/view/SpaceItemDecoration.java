package com.desaysv.usbpicture.view;

/**
 * Created by uidp5686 on 2019/11/06.
 */

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int leftRight;
    private int topBottom;
    private int spanCount = 5;
    public SpaceItemDecoration(int leftRight, int topBottom) {
        this.leftRight = leftRight;
        this.topBottom = topBottom;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    /**
     * 如果每个item的左右间距之和不相等，则会导致最终每个item不是均分
     * 所以在贴边的情况，需要考虑让 left+right 都相等
     * 下面的计算，实际上让每个 left+right 都等于 4/5 * leftright
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 获取view 在adapter中的位置。
        int position = parent.getChildAdapterPosition(view);
        // view 所在的列
        int column = position % spanCount;

        // column * (列间距 * (1f / 列数))
        outRect.left = column * leftRight / spanCount;
        // 列间距 - (column + 1) * (列间距 * (1f /列数))
        outRect.right = leftRight - (column + 1) * leftRight / spanCount;


        // 如果position > 行数，说明不是在第一行，则不指定行高，其他行的上间距为 top=mRowSpacing
        if (position >= spanCount) {
            // item top
            outRect.top = topBottom;
        }
    }


}
