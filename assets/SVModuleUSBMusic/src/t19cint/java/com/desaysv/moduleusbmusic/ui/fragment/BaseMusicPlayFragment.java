package com.desaysv.moduleusbmusic.ui.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.VDValueVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.mediacommonlib.utils.TimeUtils;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.adapter.MusicPlayListAdapter;
import com.desaysv.moduleusbmusic.businesslogic.control.CopyDeleteControl;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.listener.ProgressListener;
import com.desaysv.moduleusbmusic.ui.dialog.DownloadLimitDialog;
import com.desaysv.moduleusbmusic.ui.view.LyricsView;
import com.desaysv.moduleusbmusic.utils.ImageUtils;
import com.desaysv.moduleusbmusic.utils.LyricsUtil;
import com.desaysv.moduleusbmusic.vr.MusicVrManager;
import com.desaysv.svliblyrics.lyrics.LyricsRow;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @author uidq1846
 * @desc 媒体全屏播放界面
 * @time 2022-11-16 20:15
 */
public abstract class BaseMusicPlayFragment extends MusicBaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnScrollChangeListener, ViewPager.OnPageChangeListener {
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
    private byte[] lastAlbumPic;
    private TabLayout tlListLrcSwitch;
    private ImageView ivLrcBg;
    private FrameLayout flDefaultView;
    private ImageView ivPlaySingArm;
    @SuppressLint("Recycle")
    private ObjectAnimator albumRotation;
    private ObjectAnimator armRotationStart;
    private DownloadLimitDialog limitDialog;
    private ImageButton ibSwitchLeft;
    private ImageButton ibSwitchRight;
    private static final int PAGE_SELECTED = UPDATE_LIST_NUMBER + 1;
    private FileMessage lastPlayItem;
    //是否在播放
    private boolean mIsPlaying = false;

    private AnimationDrawable downLoadAnimation;

    public BaseMusicPlayFragment() {
    }

    public BaseMusicPlayFragment(IFragmentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.music_play_fragment;
    }

    @Override
    public void initView(View view) {
        //返回按钮
        ibClickBack = view.findViewById(R.id.ib_click_back);
        ivAlbumCover = view.findViewById(R.id.iv_album_cover);
        ibPlayControl = view.findViewById(R.id.ib_play_control);
        tvSongName = view.findViewById(R.id.tv_song_name);
        tvArtistName = view.findViewById(R.id.tv_artist_name);
        tvPosition = view.findViewById(R.id.tv_song_time_position);
        tvDuration = view.findViewById(R.id.tv_song_time_duration);
        sbTime = view.findViewById(R.id.sb_time);
        ibClickPre = view.findViewById(R.id.ib_click_pre);
        ibClickNext = view.findViewById(R.id.ib_click_next);
        ibClickCycle = view.findViewById(R.id.ib_click_cycle);
        ibClickDownload = view.findViewById(R.id.ib_click_download);
        vpSwitchListLrc = view.findViewById(R.id.vp_music_switch_list_lrc);
        tlListLrcSwitch = view.findViewById(R.id.tl_list_lrc_switch);
        ivPlaySingArm = view.findViewById(R.id.iv_play_sing_arm);
        ibSwitchLeft = view.findViewById(R.id.ib_switch_left);
        ibSwitchRight = view.findViewById(R.id.ib_switch_right);
        initListAndLrcView();
        if (getActivity() != null) {
            //WindowUtils.transparent(getActivity().getWindow());
        }
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
        ivLrcBg = lrcView.findViewById(R.id.iv_lrc_bg);
        flDefaultView = lrcView.findViewById(R.id.fl_default_view);
        List<View> listLrcViews = new ArrayList<>();
        listLrcViews.add(listView);
        listLrcViews.add(lrcView);
        pagerAdapter = new ListLrcPageAdapter(listLrcViews);
        vpSwitchListLrc.setAdapter(pagerAdapter);
        vpSwitchListLrc.setOnScrollChangeListener(this);
        vpSwitchListLrc.addOnPageChangeListener(this);
        tlListLrcSwitch.setupWithViewPager(vpSwitchListLrc);
    }

    /**
     * Called when the scroll position of a view changes.
     *
     * @param view       The view whose scroll position has changed.
     * @param scrollX    Current horizontal scroll origin.
     * @param scrollY    Current vertical scroll origin.
     * @param oldScrollX Previous horizontal scroll origin.
     * @param oldScrollY Previous vertical scroll origin.
     */
    @Override
    public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        Log.d(TAG, "onScrollChange: scrollX = " + scrollX + " oldScrollX = " + oldScrollX + " scrollY = " + scrollY + " oldScrollY = " + oldScrollY);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d(TAG, "onPageScrolled: position = " + position + " positionOffset = " + positionOffset + " positionOffsetPixels = " + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: position = " + position);
        showPagePoint(position);
    }

    /**
     * 显示歌词列表页面选中点高亮显示
     *
     * @param position position页面
     */
    private void showPagePoint(int position) {
        Log.d(TAG, "showPagePoint: position = " + position);
        handler.removeMessages(PAGE_SELECTED);
        Message obtain = Message.obtain();
        obtain.what = PAGE_SELECTED;
        obtain.arg1 = position;
        handler.sendMessage(obtain);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(TAG, "onPageScrollStateChanged: state = " + state);
        //当前在拖动
        /*switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING://ViewPage 处于拖动状态
            case ViewPager.SCROLL_STATE_SETTLING://ViewPage 处于正在找寻最后位置阶段
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ivLrcBg.setVisibility(View.INVISIBLE);
                    }
                });
                break;
            case ViewPager.SCROLL_STATE_IDLE://ViewPage已经停下来了
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ivLrcBg.setVisibility(View.VISIBLE);
                    }
                });
                break;
        }*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (armRotationStart != null) {
            armRotationStart.cancel();
            armRotationStart.end();
        }
        if (albumRotation != null) {
            albumRotation.cancel();
            albumRotation.end();
        }
        armRotationStart = null;
        albumRotation = null;
        if (getActivity() != null) {
            //WindowUtils.exitTransparent(getActivity().getWindow());
        }
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
        CopyDeleteControl.getInstance().getDeleteControl().registerDeleteProgressListener(TAG, deleteProgressListener);
        updateAllState();
    }

    @Override
    public void onStop() {
        super.onStop();
        lastPlayItem = null;
        lastAlbumPic = null;
        stopDownloadAnim();
        USBMusicDate.getInstance().removeMusicListChangeListener(iMusicListChange);
        LyricsUtil.getInstance().removeMusicLyricChangeListener(iMusicLyricChange);
        CopyDeleteControl.getInstance().getCopyControl().unRegisterCopyProgressListener(TAG);
        CopyDeleteControl.getInstance().getDeleteControl().unRegisterDeleteProgressListener(TAG);
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
        updateLrc();
    }

    @Override
    public void initData() {
        super.initData();
        //默认是音乐列表
        showPagePoint(0);
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
        playListAdapter = new MusicPlayListAdapter(requireContext(), new ArrayList<>(playList));
        playListAdapter.setItemClickListener(itemClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
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
        ibPlayControl.setAccessibilityDelegate(playControl);
        ibPlayControl.setOnClickListener(this);
        ibClickPre.setOnClickListener(this);
        ibClickNext.setOnClickListener(this);
        ibClickCycle.setOnClickListener(this);
        ibClickDownload.setOnClickListener(this);
        sbTime.setOnSeekBarChangeListener(this);
    }

    private final View.AccessibilityDelegate playControl = new View.AccessibilityDelegate() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            Log.d(TAG, "onInitializeAccessibilityNodeInfo ibPlayControl");
            info.setChecked(mIsPlaying);
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_click_back) {
            exitUI();
        } else if (v.getId() == R.id.ib_play_control) {
            if (isLocalPoint()) {
                if (getMusicControlTool().getStatusTool().isPlaying()) {
                    LocalMusicPoint.getInstance().pause(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
                } else {
                    LocalMusicPoint.getInstance().play(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
                }
            } else {
                if (getMusicControlTool().getStatusTool().isPlaying()) {
                    UsbMusicPoint.getInstance().pause(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
                } else {
                    UsbMusicPoint.getInstance().play(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
                }
            }
            getMusicControlTool().getControlTool().processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.CLICK);
        } else if (v.getId() == R.id.ib_click_pre) {
            getMusicControlTool().getControlTool().processCommand(MediaAction.PRE, ChangeReasonData.CLICK);
            if (isLocalPoint()) {
                LocalMusicPoint.getInstance().pre(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
            } else {
                UsbMusicPoint.getInstance().pre(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
            }
        } else if (v.getId() == R.id.ib_click_next) {
            getMusicControlTool().getControlTool().processCommand(MediaAction.NEXT, ChangeReasonData.CLICK);
            if (isLocalPoint()) {
                LocalMusicPoint.getInstance().next(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
            } else {
                UsbMusicPoint.getInstance().next(getContentData(getMusicControlTool().getStatusTool().getCurrentPlayItem()));
            }
        } else if (v.getId() == R.id.ib_click_cycle) {
            String loopType = getMusicControlTool().getStatusTool().getLoopType();
            if (USBConstants.LoopType.CYCLE.equals(loopType)) {
                if (isLocalPoint()) {
                    LocalMusicPoint.getInstance().singleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                } else {
                    UsbMusicPoint.getInstance().singleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                }
            } else if (USBConstants.LoopType.SINGLE.equals(loopType)) {
                if (isLocalPoint()) {
                    LocalMusicPoint.getInstance().randomMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                } else {
                    UsbMusicPoint.getInstance().randomMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                }
            } else if (USBConstants.LoopType.RANDOM.equals(loopType)) {
                if (isLocalPoint()) {
                    LocalMusicPoint.getInstance().cycleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                } else {
                    UsbMusicPoint.getInstance().cycleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                }
            }
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
            startDownloadAnim();
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
     * 文件删除进度
     */
    private final ProgressListener deleteProgressListener = new ProgressListener() {
        @Override
        public void onProgressChange(long progress, long total) {
            Log.d(TAG, "onProgressChange: deleteProgressListener = " + progress + " total = " + total + " " + (int) (progress * 1.0 / total * 100) + "%");
        }

        @Override
        public void onSuccess(FileMessage fileMessage) {
            Log.d(TAG, "onSuccess: deleteProgressListener fileMessage = " + fileMessage);
        }

        @Override
        public void onFailed(FileMessage fileMessage) {
            Log.d(TAG, "onFailed: deleteProgressListener fileMessage = " + fileMessage);
        }

        @Override
        public void onSizeLimit(FileMessage fileMessage) {

        }

        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish: deleteProgressListener");
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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showLimitDialog();
                }
            });
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish: ");
            //真正的下载结束
            handler.post(new Runnable() {
                @Override
                public void run() {
                    stopDownloadAnim();
                    updateDownloadState();
                }
            });
        }
    };

    /**
     * 开始下载动画
     */
    private void startDownloadAnim() {
        ibClickDownload.setImageResource(R.drawable.usb_music_download_anim_list);
        downLoadAnimation = (AnimationDrawable) ibClickDownload.getDrawable();
        downLoadAnimation.start();
    }

    private void stopDownloadAnim() {
        if (downLoadAnimation != null && downLoadAnimation.isRunning()) {
            downLoadAnimation.stop();
        }
    }

    /**
     * 退出当前界面
     */
    protected void exitUI() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onActionChange(FragmentAction.EXIT_PLAY_FRAGMENT, getExitToPagePosition(), getPagePosition());
            }
        });
    }

    /**
     * 容量限制弹窗
     */
    private void showLimitDialog() {
        if (limitDialog == null) {
            limitDialog = new DownloadLimitDialog(TAG);
            limitDialog.setNeedBlur(false);
        }
        limitDialog.show(AppBase.mContext);
    }

    /**
     * 更新当前条目下载状态
     */
    protected void updateDownloadState() {
        FileMessage playItem = getMusicControlTool().getStatusTool().getCurrentPlayItem();
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
            if (fileMessage.getPath() != null && fileMessage.getPath().startsWith(USBConstants.USBPath.LOCAL_PATH)) {
                LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
            } else {
                UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
            }
        }
    };

    /**
     * 封装曲目信息
     *
     * @param currentPlayItem currentPlayItem
     * @return ContentData[]
     */
    private ContentData[] getContentData(FileMessage currentPlayItem) {
        ContentData[] contentData = new ContentData[4];
        contentData[0] = new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click);
        contentData[1] = new ContentData(PointValue.Field.ProgramName, currentPlayItem.getName());
        contentData[2] = new ContentData(PointValue.Field.Author, currentPlayItem.getAuthor());
        contentData[3] = new ContentData(PointValue.Field.Album, currentPlayItem.getAlbum());
        return contentData;
    }

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

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void dispatchMessage(Message msg, MusicBaseFragment fragment) {
        final BaseMusicPlayFragment playFragment = (BaseMusicPlayFragment) fragment;
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
                playFragment.tvDuration.setText(TimeUtils.longToTimeStr(duration));
                if (playFragment.sbTime.getMax() != duration) {
                    playFragment.sbTime.setMax(duration);
                }
                //如果在拖动的过程当中，不能通过回调设置时间和进度情况
                if (isSeekBarTouch) {
                    return;
                }
                playFragment.tvPosition.setText(TimeUtils.longToTimeStr(playTime));
                playFragment.sbTime.setProgress(playTime);
                break;
            case UPDATE_ALBUM:
                byte[] albumPic = getMusicControlTool().getStatusTool().getAlbumPic();
                if (albumPic != lastAlbumPic || lastAlbumPic == null) {
                    Glide.with(requireContext()).clear(playFragment.ivAlbumCover);
                    ImageUtils.getInstance().showImage(playFragment.ivAlbumCover, albumPic, R.mipmap.img_cover_player_music, R.mipmap.img_cover_player_music);
                } else {
                    Log.d(TAG, "dispatchMessage: albumPic is same");
                }
                lastAlbumPic = albumPic;
                break;
            case UPDATE_SONG_INFO:
                updateDownloadState();
                FileMessage playItem = getMusicControlTool().getStatusTool().getCurrentPlayItem();
                if (lastPlayItem == null || !lastPlayItem.getPath().equals(playItem.getPath())) {
                    Log.d(TAG, "dispatchMessage: UPDATE_SONG_INFO lastPlayItem = " + lastPlayItem + " playItem = " + playItem);
                    //如果当前的名称和新设置的名称一样的话，则不重新设置
                    playFragment.tvSongName.setText(TextUtils.isEmpty(playItem.getName()) ? getString(R.string.usb_music_unknown_song_name) : playItem.getName());
                    playFragment.tvArtistName.setText(TextUtils.isEmpty(playItem.getAuthor()) ? getString(R.string.usb_music_unknown_singer) : playItem.getAuthor());
                }
                lastPlayItem = playItem;
                int playPosition = getMusicControlTool().getStatusTool().getCurrentItemPosition();
                playFragment.rvPlayList.scrollToPosition(playPosition);
                if (playPosition == playFragment.playListAdapter.getCurrentHighLightPosition()) {
                    Log.d(TAG, "dispatchMessage: UPDATE_SONG_INFO is the same item");
                    return;
                }
                playFragment.playListAdapter.notifyDataSetChanged();
                /*MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        //更新条目状态
                        int playPosition = getMusicControlTool().getStatusTool().getCurrentItemPosition();
                        playFragment.handler.removeMessages(UPDATE_POSITION);
                        Message obtain = Message.obtain();
                        obtain.what = UPDATE_POSITION;
                        obtain.arg1 = playPosition;
                        playFragment.handler.sendMessage(obtain);
                    }
                });*/
                break;
            case UPDATE_POSITION:
                //更新旧的和新的位置，无需整体刷新
                //避免点击动画执行
                if (msg.arg1 == playFragment.playListAdapter.getCurrentHighLightPosition()) {
                    Log.d(TAG, "dispatchMessage: is the same item");
                    playFragment.rvPlayList.scrollToPosition(msg.arg1);
                    return;
                }
                playFragment.playListAdapter.notifyItemChanged(playFragment.playListAdapter.getCurrentHighLightPosition());
                playFragment.playListAdapter.notifyItemChanged(msg.arg1);
                playFragment.rvPlayList.scrollToPosition(msg.arg1);
                break;
            case UPDATE_PLAY_STATE:
                final boolean playing = getMusicControlTool().getStatusTool().isPlaying();
                mIsPlaying = playing;
                //跑马灯状态
                tvSongName.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSongName.setSelected(playing);
                        tvArtistName.setSelected(playing);
                    }
                });
                initAnim();
                //获取当前的动画位置
                float animatedValue = (float) armRotationStart.getAnimatedValue();
                if (playing) {
                    armRotationStart.setFloatValues(animatedValue, -20);
                    armRotationStart.start();
                    if (albumRotation.isStarted()) {
                        albumRotation.resume();
                    } else {
                        albumRotation.start();
                    }
                    playFragment.ibPlayControl.setImageResource(R.drawable.music_usb_play_play_icon_selector);
                } else {
                    armRotationStart.setFloatValues(animatedValue, 0);
                    armRotationStart.start();
                    albumRotation.pause();
                    playFragment.ibPlayControl.setImageResource(R.drawable.music_usb_play_pause_icon_selector);
                }
                playFragment.lvLrc.setPlayStatus(playing);
                //刷新列表
                playFragment.playListAdapter.notifyDataSetChanged();
                break;
            case UPDATE_LRC:
                List<LyricsRow> playLyrics = getMusicControlTool().getStatusTool().getCurrentPlayLyrics();
                LyricsUtil.getInstance().setLyricList(playLyrics);
                playFragment.flDefaultView.setVisibility(playLyrics.isEmpty() ? View.VISIBLE : View.GONE);
                playFragment.lvLrc.setLyricsRows(playLyrics);
                break;
            case UPDATE_LRC_LINE:
                playFragment.lvLrc.updateLyrics((Integer) msg.obj, false);
                break;
            case UPDATE_PLAY_LIST:
                playFragment.playListAdapter.setFileMessageList(new ArrayList<>(getMusicControlTool().getStatusTool().getPlayList()));
                playFragment.updateListNumber();
                break;
            case UPDATE_LIST_NUMBER:
                //这里还是根据当前的type来确定使用哪个控制器
                List<FileMessage> playList = getMusicControlTool().getStatusTool().getPlayList();
                if (playList == null) {
                    playFragment.tvListNumber.setText("(" + 0 + ")");
                } else {
                    playFragment.tvListNumber.setText("(" + playList.size() + ")");
                }
                break;
            case UPDATE_CYCLE_TYPE:
                String loopType = getMusicControlTool().getStatusTool().getLoopType();
                Log.d(TAG, "dispatchMessage: loopType = " + loopType);
                if (USBConstants.LoopType.RANDOM.equals(loopType)) {
                    playFragment.ibClickCycle.setImageResource(R.drawable.music_usb_play_random_icon_selector);
                } else if (USBConstants.LoopType.SINGLE.equals(loopType)) {
                    playFragment.ibClickCycle.setImageResource(R.drawable.music_usb_play_single_icon_selector);
                } else {
                    playFragment.ibClickCycle.setImageResource(R.drawable.music_usb_play_cycle_icon_selector);
                }
                break;
            case PAGE_SELECTED:
                //这里判断是不是右布局，进行反向切换
                if (isRtlView()) {
                    playFragment.ibSwitchLeft.setImageResource(msg.arg1 == 0 ? R.mipmap.img_slide_point_n : R.mipmap.img_slide_point_p);
                    playFragment.ibSwitchRight.setImageResource(msg.arg1 == 0 ? R.mipmap.img_slide_point_p : R.mipmap.img_slide_point_n);
                } else {
                    playFragment.ibSwitchLeft.setImageResource(msg.arg1 == 0 ? R.mipmap.img_slide_point_p : R.mipmap.img_slide_point_n);
                    playFragment.ibSwitchRight.setImageResource(msg.arg1 == 0 ? R.mipmap.img_slide_point_n : R.mipmap.img_slide_point_p);
                }
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //播放页面和main页面是显示隐藏的，需要更新状态
        if (!hidden) {
            updateAllState();
        }
    }

    /**
     * 初始化专辑动画
     */
    private void initAnim() {
        //专辑图旋转
        if (albumRotation == null) {
            albumRotation = ObjectAnimator.ofFloat(ivAlbumCover, "rotation", 0f, 360.0f);
            albumRotation.setDuration(10000);
            albumRotation.setInterpolator(new LinearInterpolator());
            albumRotation.setRepeatCount(ObjectAnimator.INFINITE);
        }
        //把手旋转
        if (armRotationStart == null) {
            armRotationStart = ObjectAnimator.ofFloat(ivPlaySingArm, "rotation", 0f, -20.0f);
            armRotationStart.setDuration(500);
            armRotationStart.setInterpolator(new LinearInterpolator());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        vrToPlayList();
    }

    /**
     * 配合VR动作
     */
    private void vrToPlayList() {
        int state = MusicSetting.getInstance().getInt(MusicVrManager.VR_PLAY_LIST_TAG, MusicVrManager.VR_PLAY_LIST_NULL);
        Log.d(TAG, "vrToPlayList: state = " + state);
        //重置状态
        switch (state) {
            case MusicVrManager.VR_PLAY_LIST_OPEN:
                MusicSetting.getInstance().putInt(MusicVrManager.VR_PLAY_LIST_TAG, MusicVrManager.VR_PLAY_LIST_NULL);
                if (vpSwitchListLrc.getCurrentItem() == 0) {
                    postToVrTts(getString(R.string.media_music_openplaylist_opened));
                } else {
                    postToVrTts(getString(R.string.media_music_openplaylist_open));
                    vpSwitchListLrc.setCurrentItem(0);
                }
                break;
            case MusicVrManager.VR_PLAY_LIST_CLOSE:
                MusicSetting.getInstance().putInt(MusicVrManager.VR_PLAY_LIST_TAG, MusicVrManager.VR_PLAY_LIST_NULL);
                if (vpSwitchListLrc.getCurrentItem() == 0) {
                    postToVrTts(getString(R.string.media_music_closeplaylist_close));
                    vpSwitchListLrc.setCurrentItem(1);
                } else {
                    postToVrTts(getString(R.string.media_music_closeplaylist_closed));
                }
                break;
        }
    }

    /**
     * 播报tts文本方法
     *
     * @param ttsText 需要播报的tts文本
     */
    private void postToVrTts(String ttsText) {
        Log.d(TAG, "postToVrTts: ttsText = " + ttsText);
        // TTS播报（set）
        VDVRPipeLine param = new VDVRPipeLine();
        param.setKey(VDValueVR.VRSemanticKey.VR_CONTROL_RESPONSE);
        param.setValue(ttsText);
        param.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_SUCCEED_AND_TTS); //根据执行结果反馈
        VDEvent event = VDVRPipeLine.createEvent(VDEventVR.VR_AIRCONDITION, param);
        VDBus.getDefault().set(event);
    }

    /**
     * 是否本地埋点
     *
     * @return T F
     */
    private boolean isLocalPoint() {
        int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());
        return MediaType.LOCAL_MUSIC.ordinal() == mediaType;
    }

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
