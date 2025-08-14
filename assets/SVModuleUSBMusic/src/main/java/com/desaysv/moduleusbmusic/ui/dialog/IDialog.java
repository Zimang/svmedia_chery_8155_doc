package com.desaysv.moduleusbmusic.ui.dialog;

import android.content.Context;

/**
 * @author uidq1846
 * @desc
 * @time 2022-1-18 18:39
 */
public interface IDialog {
    /**
     * 显示弹窗
     *
     * @param context context
     */
    void show(Context context);

    /**
     * 关闭弹窗
     */
    void dismiss();
}
