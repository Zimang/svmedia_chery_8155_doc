package com.desaysv.modulebtmusic.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.R;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.manager.BTMusicManager;
import com.desaysv.modulebtmusic.util.ThemeUtil;
import com.desaysv.modulebtmusic.utils.AnimationUtils;
import com.desaysv.modulebtmusic.utils.FragmentSwitchUtil;
import com.desaysv.modulebtmusic.utils.MultiTaskTimer;
import com.desaysv.modulebtmusic.utils.TimeUtils;
import com.desaysv.modulebtmusic.view.MarqueeTextView;
import com.desaysv.modulebtmusic.view.MyProgressBar;

import java.util.List;
import java.util.Locale;

//todo 按键防抖
public class BTMusicPlayFragment extends BTMusicBaseFragment {
    private static final String TAG = "BTMusicPlayFragment";

    private RelativeLayout rl_root;
    private RelativeLayout rl_artist_area;
    private FrameLayout fl_progress_and_duration;
    private ImageView iv_progress_bar_bg;
    private TextView tv_progress;
    private TextView tv_duration;
    private MyProgressBar progress_bar;
    private ImageView iv_album_cover_art;
    private ImageView iv_play_or_pause;
    private ImageView iv_back;
    private MarqueeTextView tv_music_title;
    private TextView tv_music_artist_name;
    private ImageView iv_previous;
    private ImageView iv_next;
    private ImageView iv_like;
    private TextView tv_music_play_state;

    private int mCurrentPlayState = -1;
    private int mTheme = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        if (BTMusicManager.getInstance().getReplayBtMusicFlag()) {
            initPlayState();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
//        if (!isHidden()) {
//        initPlayState();
//        }
        updateUIByMusicInfoChanged(BTMusicManager.getInstance().getMusicPlayInfo());
    }

    @Override
    public void onResume() {
        super.onResume();
        initProgressBar(mTheme);
        initDirection();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        mCurrentPlayState = -1;
        AnimationUtils.stopRotateAnim();
    }

//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (!hidden) {
//            initPlayState();
//        }
//    }

    @Override
    public int getLayoutResID() {
        //主题二适配部分
        mTheme = ThemeUtil.getTheme(getContext());
        if (ThemeUtil.THEME_ONE == mTheme) {
            return R.layout.layout_bt_music_play_fg;
        } else if (ThemeUtil.THEME_SECOND == mTheme) {
            return R.layout.layout_bt_music_play_fg2;
        }
        return R.layout.layout_bt_music_play_fg;
    }

    @Override
    public void initView(View view) {
        rl_root = view.findViewById(R.id.rl_root);
        rl_artist_area = view.findViewById(R.id.rl_artist_area);
        fl_progress_and_duration = view.findViewById(R.id.fl_progress_and_duration);
        iv_progress_bar_bg = view.findViewById(R.id.iv_progress_bar_bg);
        tv_progress = view.findViewById(R.id.tv_progress);
        tv_duration = view.findViewById(R.id.tv_duration);
        progress_bar = view.findViewById(R.id.progress_bar);
        iv_album_cover_art = view.findViewById(R.id.iv_album_cover_art);
        iv_play_or_pause = view.findViewById(R.id.iv_play_or_pause);
        iv_back = view.findViewById(R.id.iv_back);
        tv_music_title = view.findViewById(R.id.tv_music_title);
        tv_music_artist_name = view.findViewById(R.id.tv_music_artist_name);
        iv_previous = view.findViewById(R.id.iv_previous);
        iv_next = view.findViewById(R.id.iv_next);
        iv_like = view.findViewById(R.id.iv_like);
        tv_music_play_state = view.findViewById(R.id.tv_music_play_state);
    }

    @Override
    public void initData() {
//        progress_bar.setTheme(mTheme);
    }

    @Override
    public void initViewListener() {
        iv_back.setOnClickListener(v -> {
            FragmentSwitchUtil.getInstance().requestSwitchFragment(FragmentSwitchUtil.BT_MUSIC_HOME);
        });
        iv_play_or_pause.setOnClickListener(v -> {
            boolean isExecute = BTMusicManager.getInstance().setTimeTask(MultiTaskTimer.TASK_SWITCH_PLAY_STATE, 500);
            if (isExecute) {
                switchPlayState();
            }
        });
        iv_previous.setOnClickListener(v -> {
            boolean isExecute = BTMusicManager.getInstance().setTimeTask(MultiTaskTimer.TASK_SKIP_TO_PREVIOUS, 500);
            if (isExecute) {
                skipToPrevious();
            }
        });
        iv_next.setOnClickListener(v -> {
            boolean isExecute = BTMusicManager.getInstance().setTimeTask(MultiTaskTimer.TASK_SKIP_TO_NEXT, 500);
            if (isExecute) {
                skipToNext();
            }
        });
        iv_like.setOnClickListener(v -> {
            //do something
        });
    }

    @Override
    protected void updateA2DPConnectionState(String address, int state) {
        if (state == BaseConstants.ProfileConnectionState.STATE_DISCONNECTED) {
            FragmentSwitchUtil.getInstance().requestSwitchFragment(FragmentSwitchUtil.BT_MUSIC_HOME);
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
        updateUIByMusicPlayStateChanged(state);
    }

    @Override
    protected void updateMusicPlayProgress(long progress, long duration) {
        updateUIByMusicPlayProgressChanged(progress, duration);
    }

    @Override
    protected void updateMusicPlayList(List<SVMusicInfo> list) {
    }

    private void initPlayState() {
        Log.i(TAG, "initPlayState: play");
        BTMusicManager.getInstance().play();
    }

    private void updateUIByMusicInfoChanged(SVMusicInfo musicInfo) {
        if (musicInfo == null) {
            Log.w(TAG, "updateUIByMusicInfoChanged: musicInfo is null");
            return;
        }
        tv_music_title.setText(TextUtils.isEmpty(musicInfo.getMediaTitle()) ? getString(R.string.string_unknown_song) : musicInfo.getMediaTitle());
        String artistName = TextUtils.isEmpty(musicInfo.getArtistName()) ? getString(R.string.string_unknown_artist) : musicInfo.getArtistName();
        if (!TextUtils.equals(tv_music_artist_name.getText(), artistName)) {
            tv_music_artist_name.setText(artistName);
        }
        updateAlbumCoverArt(musicInfo.getAlbumCoverArt());
        updateMusicPlayState(musicInfo.getPlayState());
        updateMusicPlayProgress(musicInfo.getProgress(), musicInfo.getDuration());
    }

    private void updateUIByMusicPlayStateChanged(int state) {
        if (mCurrentPlayState == state) {
            return;
        }
        mCurrentPlayState = state;
        if (state == BaseConstants.PlayState.STATE_PLAYING) {
            //主题二适配部分
            if (ThemeUtil.THEME_ONE == mTheme) {
                AnimationUtils.startRotateAnim(iv_album_cover_art);
            }
            iv_play_or_pause.setBackgroundResource(R.drawable.drawable_btn_pause_bg);//播放时显示暂停按钮
            tv_music_play_state.setVisibility(View.VISIBLE);
        } else {
            AnimationUtils.pauseRotateAnim();
            iv_play_or_pause.setBackgroundResource(R.drawable.drawable_btn_play_bg);//暂停时显示播放按钮
            tv_music_play_state.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUIByMusicPlayProgressChanged(long progress, long duration) {
        if (duration <= 0) {
            Log.w(TAG, "updateMusicPlayProgress: duration=" + duration);
            tv_progress.setText("00:00");
            tv_duration.setText("00:00");
            progress_bar.setProgress(0);
            return;
        }
        tv_progress.setText(TimeUtils.millisecondToTime(progress));
        tv_duration.setText(TimeUtils.millisecondToTime(duration));
        progress_bar.setProgress((int) (progress * progress_bar.getMax() / duration));
    }

    private void updateAlbumCoverArt(Bitmap bitmap) {
        //20230228 显示默认图片
//        if (bitmap != null) {
//            iv_album_cover_art.setImageBitmap(bitmap);
//        } else {
//            iv_album_cover_art.setImageResource(R.mipmap.img_cover_player_bluetooth);
//        }
    }

    private void initDirection() {
        if (CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_LEFT_RIGHT_RUDDER_TYPE) == 1) {
            rl_root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            if (ThemeUtil.THEME_SECOND != mTheme) {
                RelativeLayout.LayoutParams lp_progress_bar = new RelativeLayout.LayoutParams(progress_bar.getLayoutParams());
                RelativeLayout.LayoutParams lp_fl_progress_and_duration = new RelativeLayout.LayoutParams(fl_progress_and_duration.getLayoutParams());
                lp_fl_progress_and_duration.setMargins(0, 0, 250, 0);
                lp_progress_bar.setMargins(0, 197, 270, 0);
                progress_bar.setLayoutParams(lp_progress_bar);
                fl_progress_and_duration.setLayoutParams(lp_fl_progress_and_duration);
            }
        } else {
            rl_root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        if (isArSeries()) {
            rl_artist_area.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            if (ThemeUtil.THEME_SECOND != mTheme) {
                RelativeLayout.LayoutParams lp_progress_bar = new RelativeLayout.LayoutParams(progress_bar.getLayoutParams());
                RelativeLayout.LayoutParams lp_fl_progress_and_duration = new RelativeLayout.LayoutParams(fl_progress_and_duration.getLayoutParams());
                lp_fl_progress_and_duration.setMargins(0, 0, 250, 0);
                lp_progress_bar.setMargins(0, 197, 270, 0);
                progress_bar.setLayoutParams(lp_progress_bar);
                fl_progress_and_duration.setLayoutParams(lp_fl_progress_and_duration);
            }
        } else {
            rl_artist_area.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    private boolean isArSeries() {
        String language = Locale.getDefault().getLanguage();
        return language.endsWith("ar") || language.endsWith("fa") || language.endsWith("iw");
    }

    private void initProgressBar(int currentTheme) {
        progress_bar.setTheme(currentTheme);
    }
}
