package com.desaysv.moduleusbvideo.view;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.util.Constant;

/**
 * 新手引导
 * 用于 区分新手 引导方式：T19C/T18FL3 默认一个界面的引导方式；
 * T22 主题一，一个界面的引导方式；T22 主题二，使用点击三次界面，关闭引导方式
 * <p>
 * Create by extodc87 on 2023-11-7
 * Author: extodc87
 */
public class NoviceGuidanceView extends RelativeLayout implements View.OnClickListener {
    private static final String TAG = "NoviceGuidanceView";


    public NoviceGuidanceView(Context context) {
        this(context, null);
    }

    public NoviceGuidanceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoviceGuidanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NoviceGuidanceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private static final int DEFAULT_STYLE = 1;

    private LinearLayout ll_left;
    private LinearLayout ll_content;
    private LinearLayout ll_right;
    private Button btn_got_it;

    private boolean isOverlay2 = false;

    private void initialize(Context context) {
        int integer = context.getResources().getInteger(R.integer.novice_guidance);
        boolean t22Flavor = Constant.isT22Flavor();
        isOverlay2 = t22Flavor && DEFAULT_STYLE != integer;
        Log.d(TAG, "initialize() called with: context = [" + context + "], integer = [" + integer + "], isOverlay2 = [" + isOverlay2 + "], t22Flavor = [" + t22Flavor + "]");
        View rootView;
        boolean isRtl = Constant.isRtl();
        Log.d(TAG, "initialize() isRtl：" + isRtl);
        if (isOverlay2) {
            rootView = LayoutInflater.from(context).inflate(R.layout.include_novice_guidance_overlay2, this);
            //阿语镜像图片开口方向没变需要反过来
            if(isRtl){
                rootView.findViewById(R.id.llLeftContent).setBackgroundResource(R.drawable.icon_novice_guidance_bg3);
                rootView.findViewById(R.id.llRightContent).setBackgroundResource(R.drawable.icon_novice_guidance_bg1);
            }
        } else {
            rootView = LayoutInflater.from(context).inflate(R.layout.include_novice_guidance, this);
        }
        //右舵，左英保持LTR
        if(!isRtl){
            rootView.findViewById(R.id.rl_novice_guidance).setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        ll_left = rootView.findViewById(R.id.ll_left);
        ll_content = rootView.findViewById(R.id.ll_content);
        ll_right = rootView.findViewById(R.id.ll_right);
        btn_got_it = rootView.findViewById(R.id.btn_got_it);

        initDefault();

        btn_got_it.setOnClickListener(this);
        setOnClickListener(this);

    }

    private void initDefault() {
        if (isOverlay2) {
            ll_left.setVisibility(View.VISIBLE);
            ll_content.setVisibility(View.GONE);
            ll_right.setVisibility(View.GONE);
        } else {
            ll_left.setVisibility(View.VISIBLE);
            ll_content.setVisibility(View.VISIBLE);
            ll_right.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Log.i(TAG, "setVisibility: visibility: " + visibility);
        if (View.VISIBLE == visibility) {
            initDefault();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.i(TAG, "onClick: id ： " + id);
        int btnGotItId = null == btn_got_it ? 0 : btn_got_it.getId();
        if (id == btnGotItId) {
            Log.i(TAG, "onClick: btn_got_it ");
            setVisibility(View.GONE);
            Settings.System.putInt(getContext().getContentResolver(), Constant.VIDEO_NOVICE_GUIDANCE, Constant.VIDEO_NOVICE_GUIDANCE_SKILLED);
        } else if (isOverlay2) {
            Log.i(TAG, "onClick: isOverlay2: ll_left: " + ll_left.getVisibility() + " , ll_content: " + ll_content.getVisibility() + " , ll_right: " + ll_right.getVisibility());
            if (View.VISIBLE == ll_left.getVisibility()) {
                ll_left.setVisibility(View.GONE);
                ll_content.setVisibility(View.VISIBLE);
                ll_right.setVisibility(View.GONE);
            } else if (View.VISIBLE == ll_content.getVisibility()) {
                ll_left.setVisibility(View.GONE);
                ll_content.setVisibility(View.GONE);
                ll_right.setVisibility(View.VISIBLE);
            } /*else if (View.VISIBLE == ll_right.getVisibility()) {

            }*/
        }
    }
}
