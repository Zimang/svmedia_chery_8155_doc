package com.desaysv.moduleusbvideo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.desaysv.moduleusbvideo.BuildConfig;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.moduleusbvideo.util.VideoFileListTypeTool;

import java.util.Locale;

/**
 * 视频模式/文件夹模式 切换显示
 */
public class VideoModePopupWindow {
    private static final String TAG = "VideoModePopupWindow";
    private PopupWindow pwd;
    private final Context mContext;
    private View mView;
    private TextView tv_video_mode_name, tv_folder_mode_name;
    private ImageView iv_video_mode, iv_folder_mode;
    private RelativeLayout ll_video_mode, ll_folder_mode;

    /**
     * 用来表示该组件在整个屏幕内的绝对坐标，其中 m position[0] 代表X坐标,m position[1] 代表Y坐标。
     */
    private final int[] mPosition;

    public VideoModePopupWindow(Context context) {
        this.mContext = context;
        mPosition = new int[2];
        initView();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        Log.d(TAG, "initView: ");
        mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_mode_popupwindow, null);
        tv_video_mode_name = mView.findViewById(R.id.tv_video_mode_name);
        tv_folder_mode_name = mView.findViewById(R.id.tv_folder_mode_name);

        iv_video_mode = mView.findViewById(R.id.iv_video_mode);
        iv_folder_mode = mView.findViewById(R.id.iv_folder_mode);

        ll_video_mode = mView.findViewById(R.id.ll_video_mode);
        ll_folder_mode = mView.findViewById(R.id.ll_folder_mode);
    }

    /**
     * @param currentView currentView
     */
    public void showPopupWindow(TextView currentView, VideoFileListTypeTool videoFileListTypeTool) {
        if (null == pwd) {
            pwd = new PopupWindow(mView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }
        selectMode(videoFileListTypeTool);
        // 测量 显示的 view
        mView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        currentView.getLocationInWindow(mPosition);

        Log.d(TAG, "showPopupWindow: mPosition = " + mPosition[0] + " " + mPosition[1]);
        Log.d(TAG, "showPopupWindow: " + mView.getMeasuredWidth() + "  " + mView.getMeasuredHeight());
        int intrinsicWidth = 0;
        //left top right Drawable
        Drawable[] compoundDrawables = currentView.getCompoundDrawables();
        if (null != compoundDrawables) {
            Drawable compoundDrawable = compoundDrawables[0];
            if (null != compoundDrawable) {
                intrinsicWidth = compoundDrawable.getIntrinsicWidth();
            }
        }
        Log.d(TAG, "showPopupWindow: intrinsicWidth = " + intrinsicWidth + " --- " + BuildConfig.FLAVOR);
        // 计算x, y
        int x = mPosition[0] - mView.getMeasuredWidth() + intrinsicWidth;
        int y = mPosition[1] + currentView.getHeight();
        if(Constant.isT22Flavor()){
            // 获取系统当前使用的语言
            String currentLanguage = Locale.getDefault().getLanguage();
// 判断是否为阿拉伯语
            boolean isArabic = currentLanguage.equals("ar");
            if(isArabic){
                y = y - 24;
            }else{
                y = y - 28;
            }
        }
        //T19C 显示的位置和T22不同，需要重新计算
        if (Constant.isT19CFlavor()) {
            x += 68;
            y -= currentView.getPaddingBottom();
        }
        pwd.showAtLocation(currentView, Gravity.NO_GRAVITY, x, y);
    }

    /**
     * 隐藏PopupWindow
     */
    public void dismiss() {
        if (pwd != null && pwd.isShowing()) {
            pwd.dismiss();
        }
    }

    public void setOnclickListener(final View.OnClickListener onClickListener) {
        View.OnClickListener currentOnclick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onClickListener.onClick(v);
            }
        };
        ll_video_mode.setOnClickListener(currentOnclick);
        ll_folder_mode.setOnClickListener(currentOnclick);
    }

    private void selectMode(VideoFileListTypeTool videoFileListTypeTool) {
        Log.d(TAG, "selectMode: videoFileListTypeTool = " + videoFileListTypeTool);
        if (null == videoFileListTypeTool) {
            return;
        }
        if (VideoFileListTypeTool.STYLE_TYPE_ALL == videoFileListTypeTool.getStyleType()) {
            tv_video_mode_name.setTextColor(mContext.getColor(R.color.select_mode_color));
            tv_folder_mode_name.setTextColor(mContext.getColor(R.color.tab_text_color));
            iv_video_mode.setVisibility(View.VISIBLE);
            iv_folder_mode.setVisibility(View.INVISIBLE);
        } else if (VideoFileListTypeTool.STYLE_TYPE_FOLDER == videoFileListTypeTool.getStyleType()) {
            tv_video_mode_name.setTextColor(mContext.getColor(R.color.tab_text_color));
            tv_folder_mode_name.setTextColor(mContext.getColor(R.color.select_mode_color));
            iv_video_mode.setVisibility(View.INVISIBLE);
            iv_folder_mode.setVisibility(View.VISIBLE);
        }
    }

}
