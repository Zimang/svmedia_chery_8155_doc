package com.desaysv.moduleusbmusic.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.desaysv.mediacommonlib.utils.ProductConfig;
import com.desaysv.moduleusbmusic.R;

/**
 * @author uidq1846
 * @desc 本地音乐删除弹窗
 * @time 2023-1-30 13:52
 */
public class LocalMusicDeleteDialog extends BaseBackgroundDialog {

    private Button btConfirm;
    private Button btCancel;
    private String projectName;
    private ButtonClickListener buttonClickListener;
    private ImageButton ibBackground;
    private Drawable blurBitmap;

    public LocalMusicDeleteDialog() {
        Log.d(TAG, "LocalMusicDeleteDialog: create by System");
    }

    @SuppressLint("ValidFragment")
    public LocalMusicDeleteDialog(String projectName) {
        Log.d(TAG, "LocalMusicDeleteDialog: create by user");
        //提供给用户调用，当切换语言等时，此项为null，和原先activity无关联，调用取消无效
        this.projectName = projectName;
    }

    @Override
    protected boolean isCanceledOnTouchOutside() {
        return false;
    }

    @Override
    protected int getLayout() {
        if (ProductConfig.isTheme2(context)) {
            return R.layout.local_music_delete_dialog_theme2;
        }
        return R.layout.local_music_delete_dialog;
    }

    @Override
    protected void findViewId(View inflateView) {
        btConfirm = inflateView.findViewById(R.id.bt_confirm);
        btCancel = inflateView.findViewById(R.id.bt_cancel);
        ibBackground = inflateView.findViewById(R.id.ib_background);
    }

    @Override
    protected void initData() {
        if (blurBitmap != null) {
            ibBackground.setBackground(blurBitmap);
        }
    }

    @Override
    protected void blurBitmapLoadFinish(Bitmap bitmap) {
        blurBitmap = new BitmapDrawable(bitmap);
        if (ibBackground != null) {
            ibBackground.post(new Runnable() {
                @Override
                public void run() {
                    ibBackground.setBackground(blurBitmap);
                }
            });
        }
    }

    @Override
    protected void setListener() {
        btConfirm.setOnClickListener(mOnClickListener);
        btCancel.setOnClickListener(mOnClickListener);
    }

    /**
     * 监听按钮动作
     */
    public void setButtonClickListener(ButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    /**
     * 点击监听
     */
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.bt_cancel) {
                if (buttonClickListener != null) {
                    buttonClickListener.onCancel();
                }
            } else if (v.getId() == R.id.bt_confirm) {
                if (buttonClickListener != null) {
                    buttonClickListener.onConfirm();
                }
            }
            //点击后弹窗消失
            dismiss();
        }
    };

    /**
     * 按钮点击监听
     */
    public interface ButtonClickListener {
        /**
         * 点击确认
         */
        void onConfirm();

        /**
         * 点击取消
         */
        void onCancel();
    }
}
