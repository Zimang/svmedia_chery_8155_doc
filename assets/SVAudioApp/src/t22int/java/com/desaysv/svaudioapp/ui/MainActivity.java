package com.desaysv.svaudioapp.ui;

import static com.desaysv.moduleusbmusic.ui.fragment.MusicMainFragment.MUSIC_PAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.desaysv.moduledab.utils.ProductUtils;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.manager.BTMusicManager;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.localmediasdk.sdk.bean.Constant;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.modulebtmusic.fragment.BTMusicMainFragment;
import com.desaysv.modulebtmusic.utils.FragmentSwitchUtil;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.ui.RadioMainFragment;
import com.desaysv.moduleradio.vr.SVRadioVRStateUtil;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.ui.fragment.MusicMainFragment;
import com.desaysv.svaudioapp.R;
import com.desaysv.svlibmediaobserver.bean.MediaInfoBean;
import com.desaysv.svlibmediaobserver.iiterface.IMediaObserver;
import com.desaysv.svlibmediaobserver.manager.MediaObserverManager;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * created by ZNB on 2022-10-14
 * 一个用来装载各个 Module的 壳
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        RadioMainFragment.IGotoRadioPlayListener, IFragmentActionListener, FragmentSwitchUtil.OnFragmentSwitchListener {

    private static final String TAG = "MediaMainActivity";

    private FrameLayout flContent;//用来替换各个Module的内容
    private FragmentManager fragmentManager;
    private RelativeLayout rlPop;//左上角的弹窗按钮
    private ImageView ivPop;//左上角的弹窗图标
    private PopupWindow popupWindow;//左侧弹窗
    //弹窗的Radio项
    private RelativeLayout rlPopRadio;
    private TextView tvPopRadio;
    //弹窗的蓝牙项
    private RelativeLayout rlPopBTMusic;
    private TextView tvPopBTMusic;
    //弹窗的本地项
    private RelativeLayout rlPopLocalMusic;
    private TextView tvPopLocalMusic;
    //应该实现初始化Fragment，避免切换的时候每次都new
    private BaseFragment radioMainFragment;
    private BaseFragment localMainFragment;
    private BaseFragment btMainFragment;

    private String source = Constants.Source.SOURCE_RADIO;//打开哪个音源
    private int navigation = Constants.NavigationFlag.FLAG_MAIN;//打开音源的哪个界面
    private Intent intent;

    private boolean hasMulti = false;//用本地变量获取一次就好了，避免多次获取

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentSwitchUtil.getInstance().registerListener(this);
        initView();
        initViewListener();
        Log.d(TAG, "onCreate");
        intent = getIntent();
        if (intent != null) {
            source = intent.getStringExtra(Constants.Source.SOURCE_KEY);
            Log.d(TAG, "onCreate,source:" + source);
            navigation = intent.getIntExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_MAIN);
        }
        gotoFragment(source, navigation);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        if (intent != null) {
            source = intent.getStringExtra(Constants.Source.SOURCE_KEY);
            navigation = intent.getIntExtra(Constants.NavigationFlag.KEY, -1);
            if (source != null && navigation != -1) {
                gotoFragment(source, navigation);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        boolean audioIsPlaying = false;
        Log.d(TAG, "onStop,source="+source);
        if (source != null && source.equals(Constants.Source.SOURCE_BT)){
            SVMusicInfo musicPlayInfo = BTMusicManager.getInstance().getMusicPlayInfo();
            if (musicPlayInfo != null && musicPlayInfo.getPlayState() == BaseConstants.PlayState.STATE_PLAYING) {
                audioIsPlaying = true;
            }
        }else if (source != null &&(source.equals(Constants.Source.SOURCE_MUSIC) || source.equals(DsvAudioSDKConstants.USB0_MUSIC_SOURCE) || source.equals(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE))){
            audioIsPlaying = ModuleUSBMusicTrigger.getInstance().isPlaying();
        }else {
            audioIsPlaying = ModuleRadioTrigger.getInstance().isPlaying();
        }
        if (!audioIsPlaying) {
            PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
        }
        if (null != popupWindow && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FragmentSwitchUtil.getInstance().unregisterListener(this);
        MediaObserverManager.getInstance().removeObserver(TAG);
        Log.d(TAG, "onDestroy");
        if (intent != null){
            Log.d(TAG,"removeExtra begin");
            if (intent.hasExtra(Constants.Source.SOURCE_KEY)){
                Log.d(TAG,"removeExtra Source");
                intent.removeExtra(Constants.Source.SOURCE_KEY);
            }
            if (intent.hasExtra(Constants.NavigationFlag.KEY)){
                Log.d(TAG,"removeExtra NavigationFlag");
                intent.removeExtra(Constants.NavigationFlag.KEY);
            }
            Log.d(TAG,"removeExtra end");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume,source:" + source);
        //更新选项
        if(source != null) {
            updatePopItemCheck(source);
        }
        PointTrigger.getInstance().trackEvent(Point.KeyName.OpenAudioClick,Point.Field.OPENTYPE,Point.FieldValue.CLICK);
        updateMiniPlayer();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        int id = v.getId();
        if (id == R.id.rlPop) {
            showPopUp();
        } else if (id == R.id.rlPopRadio) {
            handleCheck(Constants.Source.SOURCE_RADIO);
        } else if (id == R.id.rlPopBTMusic) {
            handleCheck(Constants.Source.SOURCE_BT);
        } else if (id == R.id.rlPopLocalMusic) {
            handleCheck(Constants.Source.SOURCE_MUSIC);
        } else if (id == R.id.rvMiniPlayer) {
            miniGoToPlayPage();
        }
    }

    private void initView() {
        hasMulti = ProductUtils.hasMulti();
        ivPop = findViewById(R.id.ivPop);
        rlPop = findViewById(R.id.rlPop);
        flContent = findViewById(R.id.flContent);
        fragmentManager = getSupportFragmentManager();
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        View popLayout = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);
        popupWindow.setContentView(popLayout);
        popLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.setWidth(popLayout.getMeasuredWidth());//这个10是用来增加padding的
        rlPopRadio = popLayout.findViewById(R.id.rlPopRadio);
        tvPopRadio = popLayout.findViewById(R.id.tvPopRadio);
        rlPopBTMusic = popLayout.findViewById(R.id.rlPopBTMusic);
        tvPopBTMusic = popLayout.findViewById(R.id.tvPopBTMusic);
        rlPopLocalMusic = popLayout.findViewById(R.id.rlPopLocalMusic);
        tvPopLocalMusic = popLayout.findViewById(R.id.tvPopLocalMusic);
        //todo 待各个模块提供
        radioMainFragment = new RadioMainFragment();
        ((RadioMainFragment) radioMainFragment).setGotoRadioPlayListener(this);
        btMainFragment = new BTMusicMainFragment();
        localMainFragment = new MusicMainFragment(this);

        rvMiniPlayer = findViewById(R.id.rvMiniPlayer);
        ivMiniAlbum = findViewById(R.id.ivMiniAlbum);
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        mHandler = new MyHandler(this);
        if (!hasMulti){
            rvMiniPlayer.setVisibility(View.GONE);
        }
    }

    private void initViewListener() {
        rlPop.setOnClickListener(this);
        rlPopRadio.setOnClickListener(this);
        rlPopBTMusic.setOnClickListener(this);
        rlPopLocalMusic.setOnClickListener(this);
        rvMiniPlayer.setOnClickListener(this);
        MediaObserverManager.getInstance().init(this);
        MediaObserverManager.getInstance().addObserver(TAG, observer);
    }

    /**
     * 显示弹窗
     */
    private void showPopUp() {
        popupWindow.showAtLocation(rlPop, Gravity.START, getResources().getInteger(R.integer.pop_x), getResources().getInteger(R.integer.pop_y));
    }

    /**
     * 处理弹窗某项的选中
     *
     * @param source 选中的位置
     */
    private void handleCheck(String source) {
        Log.d(TAG, "handleCheck:" + source);
        gotoFragment(source, Constants.NavigationFlag.FLAG_MAIN);
    }

    /**
     * 打开对应Fragment
     *
     * @param source 对应音源Fragment的位置
     * @param flag   对应Fragment的类型：首页或播放页
     */
    private void gotoFragment(String source, int flag) {
        Log.d(TAG, "gotoFragment: source = " + source + " flag = " + flag);
        //切换页面，这个选项都需消失
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        if (source == null) {
            //这里说明没有特定打开的音源，则根据当前的音源分配
            String sourceName = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(getApplicationContext());
            switch (sourceName) {
                case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                    //这里应该根据最后所在的页面，进行启动
                    int page = MusicSetting.getInstance().getInt(MUSIC_PAGE, flag);
                    if (page >= MusicMainFragment.LOCAL_PLAY_PAGE_POSITION) {
                        flag = Constant.OpenSourceViewType.PLAY_VIEW;
                    }
                    source = sourceName;
                    Log.d(TAG, "gotoFragment: page = " + page);
                    break;
                case DsvAudioSDKConstants.BT_MUSIC_SOURCE:
                    flag = MusicSetting.getInstance().getInt(com.desaysv.modulebtmusic.Constants.BT_MUSIC_PAGE, flag);
                    source = Constants.Source.SOURCE_BT;
                    break;
                default:
                    int radioPage = MusicSetting.getInstance().getInt(RadioConstants.RADIO_PAGE, flag);
                    Log.d(TAG, "gotoFragment: radioPage = " + radioPage);
                    flag = radioPage;
                    source = Constants.Source.SOURCE_RADIO;
                    break;
            }
        }
        BaseFragment targetFragment = null;
        //todo 待各模块提供
        switch (source) {
            case Constants.Source.SOURCE_RADIO:
            default:
                targetFragment = radioMainFragment;
                ((RadioMainFragment) targetFragment).setNavigation(flag);
                PointTrigger.getInstance().trackEvent(Point.KeyName.OpenAudioRadioClick,Point.Field.OPENTYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioBTClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioMusicClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                break;
            case Constants.Source.SOURCE_BT:
            case "bt_music":
                targetFragment = btMainFragment;
                ((BTMusicMainFragment) targetFragment).setNavigation(flag);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioRadioClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.OpenAudioBTClick,Point.Field.OPENTYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioMusicClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                break;
            case Constants.Source.SOURCE_MUSIC:
                //这个是做菜单栏点击的进入的
                targetFragment = localMainFragment;
                Bundle bundle = new Bundle();
                bundle.putInt(MusicMainFragment.ARG_TO_PAGE_KEY, MusicMainFragment.LOCAL_LIST_PAGE_POSITION);
                bundle.putInt(MusicMainFragment.ARG_FROM_PAGE_KEY, 0);
                targetFragment.setArguments(bundle);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioRadioClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioBTClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.OpenAudioMusicClick,Point.Field.OPENTYPE,Point.FieldValue.CLICK);
                UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperType, PointValue.OperStyleValue.Click));
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                targetFragment = localMainFragment;
                Bundle usb0Bundle = new Bundle();
                if (flag == Constant.OpenSourceViewType.PLAY_VIEW) {
                    usb0Bundle.putInt(MusicMainFragment.ARG_TO_PAGE_KEY, MusicMainFragment.USB0_PLAY_PAGE_POSITION);
                } else {
                    usb0Bundle.putInt(MusicMainFragment.ARG_TO_PAGE_KEY, MusicMainFragment.USB0_LIST_PAGE_POSITION);
                }
                usb0Bundle.putInt(MusicMainFragment.ARG_FROM_PAGE_KEY, 0);
                targetFragment.setArguments(usb0Bundle);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioRadioClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioBTClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.OpenAudioMusicClick,Point.Field.OPENTYPE,Point.FieldValue.CLICK);
                UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperType, PointValue.OperStyleValue.Click));
                break;
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                targetFragment = localMainFragment;
                //配置需要启动的信息
                Bundle localBundle = new Bundle();
                if (flag == Constant.OpenSourceViewType.PLAY_VIEW) {
                    localBundle.putInt(MusicMainFragment.ARG_TO_PAGE_KEY, MusicMainFragment.LOCAL_PLAY_PAGE_POSITION);
                } else {
                    localBundle.putInt(MusicMainFragment.ARG_TO_PAGE_KEY, MusicMainFragment.LOCAL_LIST_PAGE_POSITION);
                }
                localBundle.putInt(MusicMainFragment.ARG_FROM_PAGE_KEY, 0);
                targetFragment.setArguments(localBundle);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioRadioClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.CloseAudioBTClick,Point.Field.CLOSETYPE,Point.FieldValue.CLICK);
                PointTrigger.getInstance().trackEvent(Point.KeyName.OpenAudioMusicClick,Point.Field.OPENTYPE,Point.FieldValue.CLICK);
                LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperType, PointValue.OperStyleValue.Click));
                break;
        }
        if (targetFragment == null) {
            Log.d(TAG, "gotoFragment: targetFragment is null");
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flContent, targetFragment);
        transaction.commit();
        this.source = source;
        updatePopItemCheck(source);
        //告知是新意图
        targetFragment.onFragmentNewIntent();
        SVRadioVRStateUtil.getInstance().setCurrentSource(source);
    }

    /**
     * 更新选中
     *
     * @param source source
     */
    private void updatePopItemCheck(String source) {
        if (source == null) {
            source = Constants.Source.SOURCE_RADIO;
        }
        switch (source) {
            case Constants.Source.SOURCE_RADIO:
            default:
                rlPopRadio.setSelected(true);
                rlPopBTMusic.setSelected(false);
                rlPopLocalMusic.setSelected(false);
                ivPop.setImageResource(R.mipmap.icon_radio);
                break;
            case Constants.Source.SOURCE_BT:
            case "bt_music":
                rlPopRadio.setSelected(false);
                rlPopBTMusic.setSelected(true);
                rlPopLocalMusic.setSelected(false);
                ivPop.setImageResource(R.mipmap.icon_bt);
                break;
            case Constants.Source.SOURCE_MUSIC:
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                rlPopRadio.setSelected(false);
                rlPopBTMusic.setSelected(false);
                rlPopLocalMusic.setSelected(true);
                ivPop.setImageResource(R.mipmap.icon_music);
                break;
        }
    }

    @Override
    public void gotoPlayPage(boolean isPlayPage) {
        if (isPlayPage) {
            rlPop.setVisibility(View.GONE);
            rvMiniPlayer.setVisibility(View.GONE);
        } else {
            rlPop.setVisibility(View.VISIBLE);
            if (hasMulti) {
                rvMiniPlayer.setVisibility(View.VISIBLE);
            }
            updateMiniPlayer();
        }
    }

    @Override
    public void onActionChange(FragmentAction action, int targetPage, int fromPage) {
        Log.d(TAG, "onActionChange: action = " + action.name());
        switch (action) {
            case TO_PLAY_FRAGMENT:
            case TO_USB_FOLDER_VIEW:
                //这里通知main当前进入了播放页面
                gotoPlayPage(true);
                break;
            case EXIT_PLAY_FRAGMENT:
            case EXIT_USB_FOLDER_VIEW:
                gotoPlayPage(false);
                break;
        }
    }

    @Override
    public void onActionChange(FragmentAction action, int targetPage, int fromPage, Object data) {
        Log.d(TAG, "onActionChange: action = " + action.name());
    }

    @Override
    public void onFragmentSwitch(int type) {//蓝牙音乐界面切换的回调通知
        switch (type) {
            case FragmentSwitchUtil.BT_MUSIC_HOME:
                gotoPlayPage(false);
                break;
            case FragmentSwitchUtil.BT_MUSIC_PLAY:
                //这里通知main当前进入了播放页面
                gotoPlayPage(true);
                break;
        }
    }


    /**
     * 新增需求，右上角实现mini播放器
     */
    private String audioSource;
    private RelativeLayout rvMiniPlayer;
    private ImageView ivMiniAlbum;
    private TextView tvMiniTitle;
    private IMediaObserver observer = new IMediaObserver() {
        @Override
        public void onPageChanged(int pageFlag) {
            Log.d(TAG, "onPageChanged:" + pageFlag);
        }

        @Override
        public void onPlayStatusChanged(String source, boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChanged,source:" + source + ",isPlaying:" + isPlaying);
            mHandler.sendEmptyMessage(0);
        }

        @Override
        public void onAlbumChanged(String source, String uri) {
            Log.d(TAG, "onAlbumChanged,source:" + source + ",uri:" + uri);
            mHandler.sendEmptyMessage(0);
        }

        @Override
        public void onAlbumChanged(String source, byte[] bytes) {
            Log.d(TAG, "onAlbumChanged,source:" + source + ",bytes:" + bytes);
            mHandler.sendEmptyMessage(0);
        }

        @Override
        public void onMediaInfoChanged(String source, MediaInfoBean mediaInfoBean) {
            Log.d(TAG, "onMediaInfoChanged,source:" + source + ",mediaInfoBean:" + mediaInfoBean);
            mHandler.sendEmptyMessage(0);
        }
    };



    protected void updateMiniPlayer() {
        MediaInfoBean mediaInfoBean = MediaObserverManager.getInstance().getCurrentMediaInfo();
        if (mediaInfoBean != null) {
            RequestOptions option = RequestOptions
                    .bitmapTransform(new RoundedCorners(8));
            if (Objects.equals(mediaInfoBean.getSource(), "fm") || Objects.equals(mediaInfoBean.getSource(), "am") || Objects.equals(mediaInfoBean.getSource(), "dab")) {
                option = option.error(R.mipmap.icon_miniradio);
            } else if (Objects.equals(mediaInfoBean.getSource(), Constants.Source.SOURCE_BT) || Objects.equals(mediaInfoBean.getSource(), "bt_music")) {
                if (BTMusicManager.getInstance().isA2DPConnected()) {
                    option = option.error(R.mipmap.icon_bt);
                } else {
                    option = option.error(R.mipmap.icon_bt_n);
                }
            } else if (Objects.equals(mediaInfoBean.getSource(), Constants.Source.SOURCE_MUSIC) || Objects.equals(mediaInfoBean.getSource(), DsvAudioSDKConstants.USB0_MUSIC_SOURCE)
                    || Objects.equals(mediaInfoBean.getSource(), DsvAudioSDKConstants.USB1_MUSIC_SOURCE) || Objects.equals(mediaInfoBean.getSource(), DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE)) {
                option = option.error(R.mipmap.icon_music);
                Log.i(TAG, "updateMiniPlayer,set option.error icon");
            } else {
                option = option.error(R.mipmap.icon_radio);
            }
            //界面销毁了就不再更新小图标
            Log.i(TAG, "updateMiniPlayer,before glide load");
            if (!isDestroyed()) {
                Log.i(TAG, "updateMiniPlayer,mediaInfoBean.getBytes != null:" + (mediaInfoBean.getBytes() != null));
                Log.i(TAG, "updateMiniPlayer,mediaInfoBean.getAlbumUri :" + mediaInfoBean.getAlbumUri());
                Glide.with(this).load(mediaInfoBean.getBytes() != null ? mediaInfoBean.getBytes() : mediaInfoBean.getAlbumUri())
                        .apply(option)
                        .into(ivMiniAlbum);
            }
            tvMiniTitle.setText(mediaInfoBean.getTitle());
            Log.i(TAG, "updateMiniPlayer,mediaInfoBean:" + mediaInfoBean);
            startMiniAni(mediaInfoBean.getPlaying());
            audioSource = mediaInfoBean.getSource();
        }
    }


    private void miniGoToPlayPage() {
        MediaInfoBean mediaInfoBean = MediaObserverManager.getInstance().getCurrentMediaInfo();
        Log.d(TAG, "miniGoToPlayPage:" + mediaInfoBean);
        if (mediaInfoBean != null) {
            String sourceName = mediaInfoBean.getSource();
            int naviFlag = 0;
            switch (sourceName) {
                case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                    naviFlag = Constant.OpenSourceViewType.PLAY_VIEW;
                    break;
                case DsvAudioSDKConstants.BT_MUSIC_SOURCE:
                    if (!BTMusicManager.getInstance().isA2DPConnected()) {
                        Log.w(TAG, "miniGoToPlayPage: BT Music disconnected");
                        return;
                    }
                    FragmentSwitchUtil.getInstance().requestSwitchFragment(FragmentSwitchUtil.BT_MUSIC_PLAY);
                    naviFlag = Constants.NavigationFlag.FLAG_PLAY;
                    break;
                case DsvAudioSDKConstants.DAB_SOURCE:
                    naviFlag = Constants.NavigationFlag.FLAG_DAB_PLAY;
                    break;
                default:
                    naviFlag = Constants.NavigationFlag.FLAG_PLAY;
                    break;
            }
            gotoFragment(sourceName, naviFlag);
            gotoPlayPage(true);
        }
    }


    private ObjectAnimator loadingAnimator;

    protected void startMiniAni(boolean start) {
        Log.d(TAG, "startMiniAni: start = " + start);
        if (loadingAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingAnimator = ObjectAnimator.ofFloat(ivMiniAlbum, "rotation", 0f, 360f);
            loadingAnimator.setDuration(3000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (start) {
            if (!loadingAnimator.isStarted()) {
                Log.d(TAG, "startMiniAni: start");
                loadingAnimator.start();
            } else {
                loadingAnimator.resume();
            }
        } else {
            Log.d(TAG, "startMiniAni: pause");
            loadingAnimator.pause();
        }
    }

    public MyHandler mHandler;

    private static class MyHandler extends Handler {
        WeakReference<MainActivity> weakReference;

        MyHandler(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            weakReference.get().updateMiniPlayer();
        }
    }
}