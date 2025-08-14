package com.desaysv.moduleusbmusic.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.utils.BlurBitmapUtil;

/**
 * @author uidq1846
 * @desc 服务类型弹窗的基类
 * @time 2022-1-18 17:08
 */
public abstract class BaseBackgroundDialog implements IDialog {
    protected final String TAG = this.getClass().getSimpleName();
    protected Context context;
    private boolean needBlur = true;
    private Dialog dialog;
    private View inflateView;
    private BlurBitmapUtil blurBitmap;
    private Handler handler;
    private static final int SHOW = 0x01;
    private static final int HIDE = SHOW + 1;

    @Override
    public void show(Context context) {
        Log.d(TAG, "show: ");
        this.context = context;
        initHandler();
        initBlurBitmapUtil();
        createDialog();
    }

    public boolean isNeedBlur() {
        return needBlur;
    }

    public void setNeedBlur(boolean needBlur) {
        this.needBlur = needBlur;
    }

    private void initHandler() {
        Log.d(TAG, "initHandler: handler = " + handler);
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Log.d(TAG, "handleMessage: " + msg.what);
                    if (msg.what == SHOW) {
                        if (getDialog() != null) {
                            hideSystemLayout();
                            getDialog().show();
                        }
                    } else if (msg.what == HIDE) {
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                }
            };
        }
    }

    @Override
    public void dismiss() {
        Log.d(TAG, "dismiss: ");
        handler.removeMessages(HIDE);
        handler.sendEmptyMessage(HIDE);
        if (blurBitmap != null) {
            blurBitmap.onDestroy();
        }
    }

    /**
     * 创建弹窗
     */
    private void createDialog() {
        if (dialog == null) {
            dialog = new Dialog(context, R.style.dialog_style);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(getInflateView());
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setOnKeyListener(keyListener);
            dialog.setOnDismissListener(dismissListener);
            dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside());
            //设置背景之后的变暗
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            //布局当中的选项只有在onStart设置才会生效
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            initData();
            setListener();
            Log.d(TAG, "createDialog: ");
        }
    }

    /**
     * 取消弹窗回调
     */
    private final DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            Log.d(TAG, "onDismiss: ");
            showSystemLayout();
        }
    };

    /**
     * 监听按键值
     */
    private final DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            Log.d(TAG, "onKey: keyCode = " + keyCode + " event = " + event.getAction());
            //消费，不响应任何按键值
            return true;
        }
    };

    /**
     * 创建填充的布局
     *
     * @return View
     */
    protected View getInflateView() {
        if (inflateView == null) {
            inflateView = LayoutInflater.from(context).inflate(getLayout(), null);
            findViewId(inflateView);
        }
        return inflateView;
    }

    protected Dialog getDialog() {
        return dialog;
    }

    /**
     * 取消弹窗创建
     */
    private void destroyDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog.cancel();
            dialog = null;
        }
        if (inflateView != null) {
            ViewGroup parent = (ViewGroup) inflateView.getParent();
            if (parent != null) {
                parent.removeView(inflateView);
            }
            inflateView = null;
        }
    }

    /**
     * 初始化高斯模糊背景
     */
    private void initBlurBitmapUtil() {
        Log.d(TAG, "initBlurBitmapUtil: isNeedBlur = " + isNeedBlur());
        if (isNeedBlur()) {
            blurBitmap = new BlurBitmapUtil(context);
            blurBitmap.setFinishBlurBitmap(iFinishBlurBitmap);
            blurBitmap.start();
        } else {
            handler.removeMessages(SHOW);
            handler.sendEmptyMessage(SHOW);
        }
    }

    /**
     * 歌词弹窗背景完成回调
     */
    BlurBitmapUtil.IFinishBlurBitmap iFinishBlurBitmap = new BlurBitmapUtil.IFinishBlurBitmap() {
        @Override
        public void finish(Bitmap bitmap) {
            Log.d(TAG, "iFinishBlurBitmap: finish: ");
            blurBitmapLoadFinish(bitmap);
            handler.removeMessages(SHOW);
            handler.sendEmptyMessage(SHOW);
        }
    };

    /**
     * 显示状态栏和导航栏
     */
    private void showSystemLayout() {
        if (getDialog() == null) {
            return;
        }
        Log.d(TAG, "showSystemLayout:");
        getDialog().getWindow().getDecorView().setSystemUiVisibility(0);
    }

    /**
     * 收起状态栏和导航栏
     */
    private void hideSystemLayout() {
        if (getDialog() == null) {
            return;
        }
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.MATCH_PARENT;
        Window window = getDialog().getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        // 在show之后设置宽高才有效
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = width;
        lp.height = height;
        getDialog().getWindow().setAttributes(lp);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        Log.d(TAG, "hideSystemLayout:");
    }

    /**
     * 设置按键监听
     */
    protected abstract void setListener();

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 通知子view更新背景
     *
     * @param bitmap bitmap
     */
    protected abstract void blurBitmapLoadFinish(Bitmap bitmap);

    /**
     * 点击面积外是否取消
     *
     * @return boolean
     */
    protected abstract boolean isCanceledOnTouchOutside();

    /**
     * 获取布局填充id
     *
     * @return layout id
     */
    protected abstract int getLayout();

    /**
     * 获取布局对应的id和配置
     *
     * @param inflateView inflateView
     */
    protected abstract void findViewId(View inflateView);
}
