package com.desaysv.moduledab.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.common.DABMsg;
import com.desaysv.moduledab.fragment.DABFragment;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.FastBlur;
import com.desaysv.moduledab.utils.EPGUtils;
import java.lang.ref.WeakReference;


public class DABEPGDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "DABEPGDialog";
    private RelativeLayout rlDABEPGDialog;
    private TextView tvDABEPGTitle;
    private TextView tvDABEPGContent;
    private TextView tvEPGListen;
    private TextView tvEPGCancel;
    private TimeoutHandler timeoutHandler;

    private DABEPGSchedule dabepgSchedule;

    public DABEPGDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public DABEPGDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected DABEPGDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init(){
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.dab_epg_dialog);

        rlDABEPGDialog = findViewById(R.id.rlDABEPGDialog);
        tvDABEPGTitle = findViewById(R.id.tvDABEPGTitle);
        tvDABEPGContent = findViewById(R.id.tvDABEPGContent);
        tvEPGListen = findViewById(R.id.tvEPGListen);
        tvEPGCancel = findViewById(R.id.tvEPGCancel);
        tvDABEPGTitle.setOnClickListener(this);
        tvEPGListen.setOnClickListener(this);
        tvEPGCancel.setOnClickListener(this);
        timeoutHandler = new TimeoutHandler(this);
    }


    public void updateListenContent(DABEPGSchedule dabepgSchedule){
        Log.d(TAG,"updateListenContent");
        this.dabepgSchedule = dabepgSchedule;
        tvDABEPGContent.setText(String.format(getContext().getResources().getString(R.string.epg_arrival_content),dabepgSchedule.getProgramName()));
    }

    /**
     *
     * @param canClickTitle
     */
    public void updateShowStyle(boolean canClickTitle){
        tvDABEPGTitle.setSelected(canClickTitle);
    }


    public void updateTimeout(int time){
        tvEPGCancel.setText(String.format(getContext().getResources().getString(R.string.epg_arrival_cancel),time));
        if (time <= 0){
            dismiss();
        }
    }
    /**
     * 通知类型 int 0:无效; 1:alarm; 2:traffic; 3:other;
     * @return
     */
    private String changeTypeToString(int announcementType){
        String s = "";
        switch (announcementType){
            case 0:
                break;
            case 1:
                s = getContext().getResources().getString(R.string.dab_ann_alarm);
                break;
            case 2:
                s = getContext().getResources().getString(R.string.dab_ann_ta);
                break;
            case 3:
                s = getContext().getResources().getString(R.string.dab_ann_other);
                break;

        }

        return s;
    }



    /**
     * 设置为模糊背景
     */
    public void setBlurBg(){
        //截图模糊处理用作背景
        if (rlDABEPGDialog != null) {//t26没有这个
            ((Runnable) () -> { //在子线程处理完毕再回到主线程设置
                BitmapDrawable blurDrawable = FastBlur.shotScreenBlurAndDimDrawable(getContext(), 5, 0.3f);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (blurDrawable != null) {
                        rlDABEPGDialog.setBackground(blurDrawable);
                    }
                });
            }).run();
        }
    }

    @Override
    public void show() {
        if (!isShowing()) {
            setBlurBg();
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = WindowManager.LayoutParams.MATCH_PARENT;
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            super.show();
            // 在show之后设置宽高才有效
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = width;
            lp.height = height;
            getWindow().setAttributes(lp);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            timeoutHandler.sendEmptyMessage(0);
        }
    }

    @Override
    public void dismiss() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        timeoutHandler.removeMessages(0);
        timeoutHandler.setCount(10);
        super.dismiss();
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float screenWidth = dm.widthPixels;
        float screenHeight = dm.heightPixels;
        float dialogWidth = resources.getDimensionPixelSize(R.dimen.dialog_bg_width);
        float dialogHeight = resources.getDimensionPixelSize(R.dimen.dialog_bg_height);
        Log.d(TAG, "onTouchEvent: screenWidth: " + screenWidth +" height: " +  screenHeight
                + " dialogWidth: " + dialogWidth +" dialogHeight: " + dialogHeight);
        //点击弹框范围外的区域，弹窗消失
        if ((((screenHeight/2 - dialogHeight/2) < event.getY()) && (event.getY() < (screenHeight/2 + dialogHeight/2))
                && ((screenWidth/2 - dialogWidth/2 < event.getX())) && (event.getX() < (screenWidth/2 + dialogWidth/2))) != true){
            Log.d(TAG, "onTouchEvent: dialogDismiss");
            this.dismiss();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        if (id == R.id.tvDABEPGTitle){
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                    ChangeReasonData.EPG_CLICK, EPGUtils.convertToDABMessage(dabepgSchedule));

            Intent intent = new Intent();
            intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.Source.SOURCE_KEY, DsvAudioSDKConstants.DAB_SOURCE);
            intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_DAB_PLAY);
            AppBase.mContext.startActivity(intent);
            //主动获取底层播放信息
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.GET_DAB_INFO, ChangeReasonData.EPG_CLICK);
        }else if (id == R.id.tvEPGListen){
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                    ChangeReasonData.EPG_CLICK, EPGUtils.convertToDABMessage(dabepgSchedule));
            //主动获取底层播放信息
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.GET_DAB_INFO, ChangeReasonData.EPG_CLICK);

        }else if (id == R.id.tvEPGCancel){

        }
        dismiss();
    }


    private static class TimeoutHandler extends Handler {
        private int count = 10;
        private WeakReference<DABEPGDialog> weakReference;

        public TimeoutHandler(DABEPGDialog dabepgDialog) {
            weakReference = new WeakReference<>(dabepgDialog);
        }
        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final DABEPGDialog dabepgDialog = weakReference.get();
            count--;
            Log.d(TAG, "handleMessage,count：" + count);
            if (count < 0) {
            }else {
                sendEmptyMessageDelayed(0, 1000);
                dabepgDialog.updateTimeout(count);
            }
        }
    }


}
