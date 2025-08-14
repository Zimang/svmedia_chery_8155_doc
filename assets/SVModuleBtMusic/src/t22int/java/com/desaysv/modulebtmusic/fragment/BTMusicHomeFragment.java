package com.desaysv.modulebtmusic.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.R;
import com.desaysv.modulebtmusic.adapter.CollectionListAdapter;
import com.desaysv.modulebtmusic.bean.SVDevice;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.manager.BTMusicManager;
import com.desaysv.modulebtmusic.utils.FragmentSwitchUtil;
import com.desaysv.svlibmediaobserver.bean.AppConstants;
import com.desaysv.svlibmediaobserver.manager.MediaObserverManager;

import java.util.List;
import java.util.Locale;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class BTMusicHomeFragment extends BTMusicBaseFragment {
    private static final String TAG = "BTMusicHomeFragment";
    private static final String ACTION_BLUETOOTH_SETTING = "com.desaysv.setting.ACTION_BLUETOOTH_SETTING";

    private CollectionListAdapter mCollectionListAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private List<SVMusicInfo> mCollectionList;//蓝牙音乐收藏列表

    private RelativeLayout rl_bt_music_home;
    private RelativeLayout rl_music_state_card;
    private TextView tv_card1_title;
    private TextView tv_bt_music_title;
    private AudioWaveView av_music_play_state_icon;
    private ImageView iv_bt_music_icon_bg;
    private ImageView iv_bt_music_icon;
    private RelativeLayout rl_bt_music_btn;
    private TextView tv_btn;
    private RelativeLayout rl_music_collection_card;
    private TextView tv_card2_title;
    private ImageView iv_collection_play_icon;
    private RecyclerView rv_collection_list;
    private ImageView iv_collection_list_mask;
    private ImageView iv_music_collection_icon_bg;
    private ImageView iv_music_collection_icon;
    private TextView tv_no_collection_notice;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        initUI();
        initDirection();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCollectionListAdapter != null) {
            mCollectionListAdapter = null;
        }
    }

    @Override
    public int getLayoutResID() {
        return R.layout.layout_bt_music_home_fg;
    }

    @Override
    public void initView(View view) {
        rl_bt_music_home = view.findViewById(R.id.rl_bt_music_home);
        //卡片1
        rl_music_state_card = view.findViewById(R.id.rl_music_state_card);
        tv_card1_title = view.findViewById(R.id.tv_card1_title);
        tv_bt_music_title = view.findViewById(R.id.tv_bt_music_title);
        av_music_play_state_icon = view.findViewById(R.id.av_music_play_state_icon);
        iv_bt_music_icon_bg = view.findViewById(R.id.iv_bt_music_icon_bg);
        iv_bt_music_icon = view.findViewById(R.id.iv_bt_music_icon);
        rl_bt_music_btn = view.findViewById(R.id.rl_bt_music_btn);
        tv_btn = view.findViewById(R.id.tv_btn);
        //卡片2
        rl_music_collection_card = view.findViewById(R.id.rl_music_collection_card);
        tv_card2_title = view.findViewById(R.id.tv_card2_title);
        iv_collection_play_icon = view.findViewById(R.id.iv_collection_play_icon);
        rv_collection_list = view.findViewById(R.id.rv_collection_list);
        iv_collection_list_mask = view.findViewById(R.id.iv_collection_list_mask);
        iv_music_collection_icon_bg = view.findViewById(R.id.iv_music_collection_icon_bg);
        iv_music_collection_icon = view.findViewById(R.id.iv_music_collection_icon);
        tv_no_collection_notice = view.findViewById(R.id.tv_no_collection_notice);

        initRecycleView();
    }

    private void initRecycleView() {
        mLinearLayoutManager = new LinearLayoutManager(AppBase.mContext, LinearLayoutManager.VERTICAL, false);
        rv_collection_list.setLayoutManager(mLinearLayoutManager);
        OverScrollDecoratorHelper.setUpOverScroll(rv_collection_list, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    @Override
    public void initData() {
    }

    private void initUI() {//初始化UI
        if (!BTMusicManager.getInstance().isA2DPConnected()) {
            updateUIByConnectionChanged(BaseConstants.ProfileConnectionState.STATE_DISCONNECTED);
        } else {
            updateUIByConnectionChanged(BaseConstants.ProfileConnectionState.STATE_CONNECTED);
            SVMusicInfo musicPlayInfo = BTMusicManager.getInstance().getMusicPlayInfo();
            updateUIByMusicInfoChanged(musicPlayInfo);
        }
    }

    @Override
    public void initViewListener() {
        rl_music_state_card.setOnClickListener(v -> {
            if (BTMusicManager.getInstance().isA2DPConnected()) {
                FragmentSwitchUtil.getInstance().requestSwitchFragment(FragmentSwitchUtil.BT_MUSIC_PLAY);
            }
        });

        iv_bt_music_icon_bg.setOnClickListener(v -> {
            if (BTMusicManager.getInstance().isA2DPConnected()) {
                FragmentSwitchUtil.getInstance().requestSwitchFragment(FragmentSwitchUtil.BT_MUSIC_PLAY);
            }
        });

        rl_bt_music_btn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(ACTION_BLUETOOTH_SETTING);
            startActivity(intent);
        });

        iv_collection_play_icon.setOnClickListener(v -> {
            //do nothing
        });
    }

    @Override
    protected void updateA2DPConnectionState(String address, int state) {
        updateUIByConnectionChanged(state);
    }

    private void updateUIByConnectionChanged(int state) {
        if (state == BaseConstants.ProfileConnectionState.STATE_CONNECTED) {//A2DP连接
            rl_music_state_card.setBackgroundResource(R.mipmap.img_bg_card_720_380_f);
            iv_bt_music_icon_bg.setImageResource(R.drawable.drawable_round_icon_connected_bg);
            rl_bt_music_btn.setBackgroundResource(R.drawable.drawable_bt_device_button_bg);
            tv_btn.setTextColor(getResources().getColor(R.color.color_card_device_name));
            SVDevice connectedDevice = BTMusicManager.getInstance().getConnectedDevice();
            if (connectedDevice != null) {
                tv_btn.setText(connectedDevice.getName());
            }
            iv_music_collection_icon_bg.setImageResource(R.drawable.drawable_round_icon_connected_bg);

            updateCollectionCardUI();
        } else if (state == BaseConstants.ProfileConnectionState.STATE_DISCONNECTED) {//A2DP断开
            rl_music_state_card.setBackgroundResource(R.mipmap.img_bg_card_720_380_n);
            iv_bt_music_icon_bg.setImageResource(R.drawable.drawable_round_icon_bg);
            rl_bt_music_btn.setBackgroundResource(R.drawable.drawable_bt_setting_button_bg);
            tv_btn.setTextColor(getResources().getColor(R.color.color_card_setting));
            tv_btn.setText(R.string.string_bt_setting);
            iv_music_collection_icon_bg.setImageResource(R.drawable.drawable_round_icon_bg);
            av_music_play_state_icon.setVisibility(View.GONE);
            tv_bt_music_title.setVisibility(View.GONE);

            mCollectionList = null;
            updateCollectionCardUI();
        }
    }

    @Override
    protected void updateAVRCPConnectionState(String address, int state) {
    }

    @Override
    protected void updateMusicPlayInfo(SVMusicInfo musicInfo) {
        updateUIByMusicInfoChanged(musicInfo);
    }

    @Override
    protected void updateMusicPlayState(int state) {
        //do something
    }

    @Override
    protected void updateMusicPlayProgress(long progress, long duration) {
    }

    @Override
    protected void updateMusicPlayList(List<SVMusicInfo> list) {
    }

    private com.desaysv.svlibmediaobserver.bean.MediaInfoBean changeRadioMessageToAppMedia(String currentSource, SVMusicInfo musicInfo) {
        com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder appBuilder = new com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder();
        appBuilder.setSource(currentSource);
        appBuilder.setPlaying(musicInfo.getPlayState() == BaseConstants.PlayState.STATE_PLAYING);
        appBuilder.setTitle(musicInfo.getMediaTitle());
//        appBuilder.setPath(musicInfo.getMediaUri().toString());
        return appBuilder.Build();
    }
    private void updateUIByMusicInfoChanged(SVMusicInfo musicInfo) {
        if (musicInfo == null) {
            Log.w(TAG, "updateUIByMusicInfoChanged: musicInfo is null");
            return;
        }
        if (BTMusicManager.getInstance().isA2DPConnected()) {
            MediaObserverManager.getInstance().setMediaInfo(AppConstants.Source.BT_MUSIC_SOURCE,
                    changeRadioMessageToAppMedia(AppConstants.Source.BT_MUSIC_SOURCE, musicInfo), false);
        }
        if (musicInfo.getPlayState() == BaseConstants.PlayState.STATE_PLAYING) {
            av_music_play_state_icon.start();
            av_music_play_state_icon.setVisibility(View.VISIBLE);
            tv_bt_music_title.setText(musicInfo.getMediaTitle());
            tv_bt_music_title.setVisibility(View.VISIBLE);
        } else {
            av_music_play_state_icon.setVisibility(View.GONE);
            av_music_play_state_icon.stop();
            tv_bt_music_title.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateCollectionCardUI() {
        updateCollectionList(mCollectionList);
        if (mCollectionList == null || mCollectionList.isEmpty()) {
            tv_card2_title.setText(getString(R.string.string_bt_music_collection_list));
            iv_music_collection_icon_bg.setVisibility(View.VISIBLE);
            iv_music_collection_icon.setVisibility(View.VISIBLE);
            iv_collection_list_mask.setVisibility(View.GONE);
            tv_no_collection_notice.setVisibility(View.VISIBLE);
        } else {
            tv_card2_title.setText(getString(R.string.string_bt_music_collection_list) + "(" + mCollectionList.size() + ")");
            iv_music_collection_icon_bg.setVisibility(View.GONE);
            iv_music_collection_icon.setVisibility(View.GONE);
            iv_collection_list_mask.setVisibility(View.VISIBLE);
            tv_no_collection_notice.setVisibility(View.GONE);
        }
    }

    private void updateCollectionList(List<SVMusicInfo> list) {
        if (list == null || list.isEmpty()) {
            rv_collection_list.setVisibility(View.GONE);
        } else {
            rv_collection_list.setVisibility(View.VISIBLE);
        }
        if (mCollectionListAdapter == null) {
            mCollectionListAdapter = new CollectionListAdapter(getContext().getApplicationContext(), list);
        } else {
            mCollectionListAdapter.updateList(list);
        }
        if (rv_collection_list.getAdapter() instanceof CollectionListAdapter) {
            Log.d(TAG, "updateCollectionList: do not need to setAdapter again");
            return;
        }
        rv_collection_list.setAdapter(mCollectionListAdapter);
        mCollectionListAdapter.setOnItemClickListener((view, position) -> {
            //do something
        });
    }

    private void initDirection() {
        String language = Locale.getDefault().getLanguage();
        if (language.endsWith("ar") || language.endsWith("fa") || language.endsWith("iw")) {
            rl_music_state_card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            rl_music_state_card.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }
}
