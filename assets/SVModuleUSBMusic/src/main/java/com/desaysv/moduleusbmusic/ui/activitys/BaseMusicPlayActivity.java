package com.desaysv.moduleusbmusic.ui.activitys;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaPlayType;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.mediacommonlib.utils.TimeUtils;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.adapter.MusicPlayListAdapter;
import com.desaysv.moduleusbmusic.businesslogic.control.CopyDeleteControl;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.listener.ProgressListener;
import com.desaysv.moduleusbmusic.ui.fragment.MusicMainFragment;
import com.desaysv.moduleusbmusic.ui.view.LyricsView;
import com.desaysv.moduleusbmusic.utils.ImageUtils;
import com.desaysv.moduleusbmusic.utils.LyricsUtil;
import com.desaysv.svliblyrics.lyrics.LyricsRow;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author uidq1846
 * @desc 基本播放activity, 先保留，解决Fragment切换卡顿问题
 * @time 2023-1-4 21:32
 */
public abstract class BaseMusicPlayActivity extends BaseMusicActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private IFragmentActionListener listener;
    private ImageButton ibClickBack;
    private ImageView ivAlbumCover;
    private ImageButton ibPlayControl;
    private ViewPager vpSwitchListLrc;
    private TextView tvSongName;
    private TextView tvArtistName;
    private TextView tvPosition;
    private TextView tvDuration;
    private ImageButton ibClickPre;
    private ImageButton ibClickNext;
    private ImageButton ibClickCycle;
    private ImageButton ibClickDownload;
    private SeekBar sbTime;
    private ListLrcPageAdapter pagerAdapter;
    private TextView tvListNumber;
    private RecyclerView rvPlayList;
    private LyricsView lvLrc;
    private MusicPlayListAdapter playListAdapter;
    private static final int UPDATE_POSITION = UPDATE_CYCLE_TYPE + 1;
    private static final int UPDATE_LRC_LINE = UPDATE_POSITION + 1;
    private static final int UPDATE_LIST_NUMBER = UPDATE_LRC_LINE + 1;
    private boolean isSeekBarTouch = false;

    @Override
    public int getLayoutResID() {
        return R.layout.music_play_fragment;
    }

    @Override
    public void initView() {
        //返回按钮
        ibClickBack = findViewById(R.id.ib_click_back);
        ivAlbumCover = findViewById(R.id.iv_album_cover);
        ibPlayControl = findViewById(R.id.ib_play_control);
        tvSongName = findViewById(R.id.tv_song_name);
        tvArtistName = findViewById(R.id.tv_artist_name);
        tvPosition = findViewById(R.id.tv_song_time_position);
        tvDuration = findViewById(R.id.tv_song_time_duration);
        sbTime = findViewById(R.id.sb_time);
        ibClickPre = findViewById(R.id.ib_click_pre);
        ibClickNext = findViewById(R.id.ib_click_next);
        ibClickCycle = findViewById(R.id.ib_click_cycle);
        ibClickDownload = findViewById(R.id.ib_click_download);
        vpSwitchListLrc = findViewById(R.id.vp_music_switch_list_lrc);
        initListAndLrcView();
    }

    /**
     * 初始化右侧歌词和播放列表
     */
    private void initListAndLrcView() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View listView = inflater.inflate(R.layout.item_music_play_fragment_list, null);
        tvListNumber = listView.findViewById(R.id.tv_play_list_number);
        rvPlayList = listView.findViewById(R.id.rv_play_list);
        @SuppressLint("InflateParams")
        View lrcView = inflater.inflate(R.layout.item_music_play_fragment_lrc, null);
        lvLrc = lrcView.findViewById(R.id.lv_lrc);
        lvLrc.setCanSliding(true);
        List<View> listLrcViews = new ArrayList<>();
        listLrcViews.add(listView);
        listLrcViews.add(lrcView);
        pagerAdapter = new ListLrcPageAdapter(listLrcViews);
        vpSwitchListLrc.setAdapter(pagerAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        USBMusicDate.getInstance().setMusicListChangeListener(iMusicListChange);
        LyricsUtil.getInstance().setMusicLyricChangeListener(iMusicLyricChange);
        CopyDeleteControl.getInstance().getCopyControl().registerCopyProgressListener(TAG, progressListener);
        updateAllState();
    }

    @Override
    public void onStop() {
        super.onStop();
        USBMusicDate.getInstance().removeMusicListChangeListener(iMusicListChange);
        LyricsUtil.getInstance().removeMusicLyricChangeListener(iMusicLyricChange);
        CopyDeleteControl.getInstance().getCopyControl().unRegisterCopyProgressListener(TAG);
    }

    /**
     * 更新所有状态
     */
    private void updateAllState() {
        updateInfo();
        updateAlbum();
        updateTime();
        updatePlayState();
        updateCycleType();
        updateListNumber();
        updatePlayList();
        //重新赋值，避免已经切换了曲目
    }

    @Override
    public void initData() {
        super.initData();
        initPlayListAdapter();
    }

    /**
     * 每句歌词变化回调
     */
    private final LyricsUtil.IMusicLyricChange iMusicLyricChange = new LyricsUtil.IMusicLyricChange() {
        @Override
        public void onMusicLyricChange(String lyrics, int lyricsIndex) {
            handler.sendMessage(handler.obtainMessage(UPDATE_LRC_LINE, lyricsIndex));
        }
    };

    /**
     * 数据变化监听
     */
    private final USBMusicDate.IListDataChange iMusicListChange = new USBMusicDate.IListDataChange() {
        @Override
        public void onUSB1MusicAllFolderMapChange() {

        }

        @Override
        public void onUSB2MusicAllFolderMapChange() {

        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onUSB1MusicAllListChange() {
            Log.d(TAG, "onUSB1MusicAllListChange: " + getPagePosition());
            //通知主线程刷新
            if (getPagePosition() == MusicMainFragment.USB0_PLAY_PAGE_POSITION) {
                updateAllState();
            }
        }

        @Override
        public void onUSB2MusicAllListChange() {
            Log.d(TAG, "onUSB2MusicAllListChange: " + getPagePosition());
            if (getPagePosition() == 3) {
                updateAllState();
            }
        }

        @Override
        public void onLocalMusicListChange() {
            Log.d(TAG, "onLocalMusicListChange: " + getPagePosition());
            if (getPagePosition() == MusicMainFragment.LOCAL_PLAY_PAGE_POSITION) {
                updateAllState();
            }
        }

        @Override
        public void onRecentMusicListChange() {
            Log.d(TAG, "onRecentMusicListChange: " + getPagePosition());
        }
    };

    /**
     * 初始化适配器列表
     */
    private void initPlayListAdapter() {
        List<FileMessage> playList = getMusicControlTool().getStatusTool().getPlayList();
        if (playList == null) {
            playList = getMusicList();
            getMusicControlTool().getControlTool().setPlayList(getMusicList(), CurrentPlayListType.ALL);
        }
        playListAdapter = new MusicPlayListAdapter(this, new ArrayList<>(playList));
        playListAdapter.setItemClickListener(itemClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvPlayList.setLayoutManager(linearLayoutManager);
        rvPlayList.setAdapter(playListAdapter);
    }

    /**
     * 更新右侧列表选项
     */
    @SuppressLint("SetTextI18n")
    private void updateListNumber() {
        Log.d(TAG, "updateListNumber: ");
        handler.removeMessages(UPDATE_LIST_NUMBER);
        handler.sendEmptyMessage(UPDATE_LIST_NUMBER);
    }

    @Override
    public void initViewListener() {
        ibClickBack.setOnClickListener(this);
        ibPlayControl.setOnClickListener(this);
        ibClickPre.setOnClickListener(this);
        ibClickNext.setOnClickListener(this);
        ibClickCycle.setOnClickListener(this);
        ibClickDownload.setOnClickListener(this);
        sbTime.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_click_back) {
            listener.onActionChange(FragmentAction.EXIT_PLAY_FRAGMENT, getExitToPagePosition(), getPagePosition());
        } else if (v.getId() == R.id.ib_play_control) {
            getMusicControlTool().getControlTool().processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.CLICK);
        } else if (v.getId() == R.id.ib_click_pre) {
            getMusicControlTool().getControlTool().processCommand(MediaAction.PRE, ChangeReasonData.CLICK);
        } else if (v.getId() == R.id.ib_click_next) {
            getMusicControlTool().getControlTool().processCommand(MediaAction.NEXT, ChangeReasonData.CLICK);
        } else if (v.getId() == R.id.ib_click_cycle) {
            getMusicControlTool().getControlTool().processCommand(MediaAction.CHANGE_LOOP_TYPE, ChangeReasonData.CLICK);
        } else if (v.getId() == R.id.ib_click_download) {
            Log.d(TAG, "onClick: download.");
            FileMessage playItem = getMusicControlTool().getStatusTool().getCurrentPlayItem();
            if (CopyDeleteControl.getInstance().getCopyControl().isCopied(playItem)) {
                Log.d(TAG, "onClick: already download");
                return;
            }
            ibClickDownload.setEnabled(false);
            CopyDeleteControl.getInstance().getCopyControl().copyFile(playItem, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            tvPosition.setText(TimeUtils.longToTimeStr(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStartTrackingTouch: ");
        isSeekBarTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch: ");
        isSeekBarTouch = false;
        getMusicControlTool().getControlTool().processCommand(MediaAction.SEEKTO, ChangeReasonData.CLICK, seekBar.getProgress());
    }

    /**
     * 文件拷贝进度
     */
    private final ProgressListener progressListener = new ProgressListener() {
        @Override
        public void onProgressChange(long progress, long total) {
            Log.d(TAG, "onProgressChange: progress = " + progress + " total = " + total + " " + (int) (progress * 1.0 / total * 100) + "%");
        }

        @Override
        public void onSuccess(FileMessage fileMessage) {
            Log.d(TAG, "onSuccess: fileMessage = " + fileMessage);
        }

        @Override
        public void onFailed(FileMessage fileMessage) {
            Log.d(TAG, "onFailed: fileMessage = " + fileMessage);
        }

        @Override
        public void onSizeLimit(FileMessage fileMessage) {
            Log.d(TAG, "onSizeLimit: ");
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish: ");
            //真正的下载结束
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateDownloadState();
                }
            });
        }
    };

    /**
     * 更新当前条目下载状态
     */
    protected void updateDownloadState() {
        FileMessage playItem = getMusicControlTool().getStatusTool().getCurrentPlayItem();
        //如果当前在拷贝当中
        if (CopyDeleteControl.getInstance().getCopyControl().isCopied(playItem)) {
            ibClickDownload.setEnabled(false);
            ibClickDownload.setImageResource(R.drawable.music_play_downloaded_select_icon_selector);
        } else {
            ibClickDownload.setEnabled(true);
            ibClickDownload.setImageResource(R.drawable.music_play_download_select_icon_selector);
        }
    }

    /**
     * 当前列表条目点击配置
     */
    private final MusicPlayListAdapter.MusicListItemClickListener itemClickListener = new MusicPlayListAdapter.MusicListItemClickListener() {
        @Override
        public void onItemClick(int position, FileMessage fileMessage) {
            Log.d(TAG, "onItemClick: position = " + position + " fileMessage = " + fileMessage);
            getMusicControlTool().getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.CLICK_ITEM, position);
        }
    };

    /**
     * page 切换需要的内部类
     */
    private static class ListLrcPageAdapter extends PagerAdapter {
        private List<View> listLrcViews;

        public ListLrcPageAdapter(List<View> listLrcViews) {
            this.listLrcViews = listLrcViews;
        }

        @Override
        public int getCount() {
            return listLrcViews.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(listLrcViews.get(position));
            //每次滑动把视图添加到viewpager
            return listLrcViews.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //移除当前位置
            container.removeView(listLrcViews.get(position));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void dispatchMessage(Message msg, BaseMusicActivity activity) {
        final BaseMusicPlayActivity playActivity = (BaseMusicPlayActivity) activity;
        switch (msg.what) {
            case UPDATE_TIME:
                IStatusTool statusTool = getMusicControlTool().getStatusTool();
                int playTime = 0;
                int duration = 0;
                if (statusTool != null) {
                    playTime = statusTool.getCurrentPlayTime();
                    duration = statusTool.getDuration();
                }
                LyricsUtil.getInstance().start(playTime);
                playActivity.tvDuration.setText(TimeUtils.longToTimeStr(duration));
                if (playActivity.sbTime.getMax() != duration) {
                    playActivity.sbTime.setMax(duration);
                }
                //如果在拖动的过程当中，不能通过回调设置时间和进度情况
                if (isSeekBarTouch) {
                    return;
                }
                playActivity.tvPosition.setText(TimeUtils.longToTimeStr(playTime));
                playActivity.sbTime.setProgress(playTime);
                break;
            case UPDATE_ALBUM:
                byte[] albumPic = getMusicControlTool().getStatusTool().getAlbumPic();
                ImageUtils.getInstance().showImage(playActivity.ivAlbumCover, albumPic, R.mipmap.img_cover_player_music, R.mipmap.img_cover_player_music);
                break;
            case UPDATE_SONG_INFO:
                updateDownloadState();
                FileMessage playItem = getMusicControlTool().getStatusTool().getCurrentPlayItem();
                playActivity.tvSongName.setText(TextUtils.isEmpty(playItem.getName()) ? getString(R.string.usb_music_unknown_song_name) : playItem.getName());
                playActivity.tvArtistName.setText(TextUtils.isEmpty(playItem.getAuthor()) ? getString(R.string.usb_music_unknown_album) : playItem.getAuthor());
                MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        //更新条目状态
                        int playPosition = getMusicControlTool().getStatusTool().getCurrentItemPosition();
                        playActivity.handler.removeMessages(UPDATE_POSITION);
                        Message obtain = Message.obtain();
                        obtain.what = UPDATE_POSITION;
                        obtain.arg1 = playPosition;
                        playActivity.handler.sendMessage(obtain);
                    }
                });
                break;
            case UPDATE_POSITION:
                //更新旧的和新的位置，无需整体刷新
                //避免点击动画执行
                if (msg.arg1 == playActivity.playListAdapter.getCurrentHighLightPosition()) {
                    Log.d(TAG, "dispatchMessage: is the same item");
                    playActivity.rvPlayList.scrollToPosition(msg.arg1);
                    return;
                }
                playActivity.playListAdapter.notifyItemChanged(playActivity.playListAdapter.getCurrentHighLightPosition());
                playActivity.playListAdapter.notifyItemChanged(msg.arg1);
                playActivity.rvPlayList.scrollToPosition(msg.arg1);
                break;
            case UPDATE_PLAY_STATE:
                boolean playing = getMusicControlTool().getStatusTool().isPlaying();
                if (playing) {
                    playActivity.ibPlayControl.setImageResource(R.drawable.music_usb_play_play_icon_selector);
                } else {
                    playActivity.ibPlayControl.setImageResource(R.drawable.music_usb_play_pause_icon_selector);
                }
                playActivity.lvLrc.setPlayStatus(playing);
                break;
            case UPDATE_LRC:
                List<LyricsRow> playLyrics = getMusicControlTool().getStatusTool().getCurrentPlayLyrics();
                LyricsUtil.getInstance().setLyricList(playLyrics);
                playActivity.lvLrc.setLyricsRows(playLyrics);
                break;
            case UPDATE_LRC_LINE:
                playActivity.lvLrc.updateLyrics((Integer) msg.obj, false);
                break;
            case UPDATE_PLAY_LIST:
                playActivity.playListAdapter.setFileMessageList(new ArrayList<>(getMusicControlTool().getStatusTool().getPlayList()));
                playActivity.updateListNumber();
                break;
            case UPDATE_LIST_NUMBER:
                //这里还是根据当前的type来确定使用哪个控制器
                List<FileMessage> playList = getMusicControlTool().getStatusTool().getPlayList();
                if (playList == null) {
                    playActivity.tvListNumber.setText(getString(R.string.usb_music_play_list_number_text) + "(" + 0 + ")");
                } else {
                    playActivity.tvListNumber.setText(getString(R.string.usb_music_play_list_number_text) + "(" + playList.size() + ")");
                }
                break;
            case UPDATE_CYCLE_TYPE:
                String loopType = getMusicControlTool().getStatusTool().getLoopType();
                Log.d(TAG, "dispatchMessage: loopType = " + loopType);
                if (USBConstants.LoopType.RANDOM.equals(loopType)) {
                    playActivity.ibClickCycle.setImageResource(R.drawable.music_usb_play_random_icon_selector);
                } else if (USBConstants.LoopType.SINGLE.equals(loopType)) {
                    playActivity.ibClickCycle.setImageResource(R.drawable.music_usb_play_single_icon_selector);
                } else {
                    playActivity.ibClickCycle.setImageResource(R.drawable.music_usb_play_cycle_icon_selector);
                }
                break;
        }
    }

    /**
     * 音乐状态监听
     * 当前状态是在子线程刷新的，所以需要变更成为主线程
     */
    private final IMediaStatusChange iMediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {
            updateInfo();
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            updatePlayState();
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {
            updateTime();
        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {
            handler.removeMessages(UPDATE_MEDIA_TYPE);
            Message obtain = Message.obtain();
            obtain.what = UPDATE_MEDIA_TYPE;
            obtain.arg1 = mediaPlayType.ordinal();
            handler.sendMessage(obtain);
        }

        @Override
        public void onAlbumPicDataChange() {
            updateAlbum();
        }

        @Override
        public void onLoopTypeChange() {
            updateCycleType();
        }

        @Override
        public void onLyricsChange() {
            handler.removeMessages(UPDATE_LRC);
            handler.sendEmptyMessage(UPDATE_LRC);
        }

        @Override
        public void onPlayListChange() {
            updatePlayList();
        }
    };

    /**
     * 获取当前的页面位置
     *
     * @return int
     */
    protected abstract int getPagePosition();

    /**
     * 具体退出的后要到的页面
     *
     * @return int
     */
    protected abstract int getExitToPagePosition();

    /**
     * 获取相应页面的列表
     *
     * @return List<FileMessage>
     */
    protected abstract List<FileMessage> getMusicList();
}
