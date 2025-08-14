package com.desaysv.moduleusbmusic.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.moduleusbmusic.BuildConfig;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.adapter.MusicListAdapter;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author uidq1846
 * @desc 最近播放界面
 * @time 2022-11-16 20:15
 */
public class MusicRecentFragment extends MusicBaseFragment implements MusicListAdapter.MusicListItemClickListener {

    private IFragmentActionListener listener;
    private RecyclerView rvMusicList;
    private RelativeLayout rlEmptyList;
    private MusicListAdapter musicListAdapter;
    private final static int UPDATE_LIST = UPDATE_CYCLE_TYPE + 1;
    private final static int UPDATE_POSITION = UPDATE_LIST + 1;

    public MusicRecentFragment() {
    }

    public MusicRecentFragment(IFragmentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.music_recent_play_fragment;
    }

    @Override
    public void initView(View view) {
        rvMusicList = view.findViewById(R.id.rv_music_list);
        rlEmptyList = view.findViewById(R.id.rl_empty_list);
    }

    @Override
    public void initData() {
        super.initData();
        initAdapter();
    }

    private void initAdapter() {
        musicListAdapter = new MusicListAdapter(requireContext(), new ArrayList<>(USBMusicDate.getInstance().getRecentMusicAllList()));
        //配置行数
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvMusicList.setLayoutManager(layoutManager);
        rvMusicList.setHasFixedSize(true);
        rvMusicList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //super.getItemOffsets(outRect, view, parent, state);
                //由于条目边距不平均，所以列表左列需加80
                int position = parent.getChildAdapterPosition(view);
                if (BuildConfig.FLAVOR.equals("chery_8155_int_t19c")) {
                    if (position % 2 == 0) {
                        if (isRtlView()) {
                            outRect.right = -16;
                        } else {
                            outRect.left = -16;
                        }
                    } else {
                        outRect.left = 0;
                    }
                    //这里需要看是两行还是单行
                    int itemCount = parent.getAdapter().getItemCount();
                    if (itemCount % 2 == 0) {
                        //最后一列抬高底部
                        if (position >= itemCount - 2) {
                            outRect.bottom = 102;
                        }
                    } else {
                        if (position >= itemCount - 1) {
                            outRect.bottom = 102;
                        }
                    }
                } else {
                    if (position % 2 == 0) {
                        outRect.left = 80;
                    } else {
                        outRect.left = -8;
                    }
                }
            }
        });
        rvMusicList.setAdapter(musicListAdapter);
        musicListAdapter.setItemClickListener(this);
        //避免设置notifyItemChanged会闪一下
        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) rvMusicList.getItemAnimator();
        if (itemAnimator != null) {
            itemAnimator.setSupportsChangeAnimations(false);
        }
    }

    @Override
    public void onStart() {
        //界面恢复时设置一下当前的资源获取器
        super.onStart();
        USBMusicDate.getInstance().setMusicListChangeListener(iMusicListChange);
        updateList();
    }

    @Override
    public void onStop() {
        super.onStop();
        USBMusicDate.getInstance().removeMusicListChangeListener(iMusicListChange);
    }

    @Override
    public void initViewListener() {

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void dispatchMessage(Message msg, MusicBaseFragment fragment) {
        final MusicRecentFragment recentPlayFragment = (MusicRecentFragment) fragment;
        switch (msg.what) {
            case UPDATE_SONG_INFO:
                MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        //更新条目状态、这里需要注意了，最近播放的列表需查找自己对应的
                        int playPosition = getMusicControlTool().getStatusTool().getCurrentItemPosition();
                        Log.d(TAG, "UPDATE_SONG_INFO: playPosition = " + playPosition);
                        if (playPosition == -1) {
                            return;
                        }
                        recentPlayFragment.handler.removeMessages(UPDATE_POSITION);
                        Message obtain = Message.obtain();
                        obtain.what = UPDATE_POSITION;
                        obtain.arg1 = playPosition;
                        recentPlayFragment.handler.sendMessage(obtain);
                    }
                });
                break;
            case UPDATE_POSITION:
                //更新旧的和新的位置，无需整体刷新
                recentPlayFragment.musicListAdapter.notifyItemChanged(recentPlayFragment.musicListAdapter.getCurrentHighLightPosition());
                //最近播放回到的都是第一项
                recentPlayFragment.musicListAdapter.notifyItemChanged(0);
                recentPlayFragment.rvMusicList.scrollToPosition(0);
                break;
            case UPDATE_LIST:
                //这个是数据更新，播放列表不一定更新
                recentPlayFragment.musicListAdapter.setFileMessageList(new ArrayList<>(USBMusicDate.getInstance().getRecentMusicAllList()));
                recentPlayFragment.updateListView();
                break;
        }
    }

    @Override
    protected IGetControlTool getMusicControlTool() {
        //由于存在切换，则默认使用本地
        return ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool;
    }

    @Override
    public void onItemClick(final int position, final FileMessage fileMessage) {
        Log.d(TAG, "onItemClick: fileMessage = " + fileMessage);
        //最近播放比较特殊，每次点击都需要重新配
        //播放页不动，只刷新这个页面，播放最近列表时，不存在列表删减情况，所以不用主动更新
        //最近播放列表会发生变化，所以每次重新配置下
        getMusicControlTool().getControlTool().setPlayList(new ArrayList<>(USBMusicDate.getInstance().getRecentMusicAllList()), CurrentPlayListType.ALL);
        getMusicControlTool().getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.CLICK_ITEM, position);
        //然后跳转到全屏页面
        listener.onActionChange(FragmentAction.TO_PLAY_FRAGMENT, MusicMainFragment.RECENT_PLAY_PAGE_POSITION, MusicMainFragment.RECENT_LIST_PAGE_POSITION);
        UsbMusicPoint.getInstance().openRecently(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
    }

    @Override
    public void onSelectItemClick(int selectPosition, int selectSize, FileMessage fileMessage) {

    }

    /**
     * 更新列表，适配器刷新
     */
    private void updateList() {
        handler.removeMessages(UPDATE_LIST);
        handler.sendEmptyMessage(UPDATE_LIST);
    }

    /**
     * 更新当前列表页面
     */
    private void updateListView() {
        List<FileMessage> musicList = USBMusicDate.getInstance().getRecentMusicAllList();
        if (musicList.isEmpty()) {
            rvMusicList.setVisibility(View.GONE);
            rlEmptyList.setVisibility(View.VISIBLE);
        } else {
            rvMusicList.setVisibility(View.VISIBLE);
            rlEmptyList.setVisibility(View.GONE);
        }
    }

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
        }

        @Override
        public void onUSB2MusicAllListChange() {
        }

        @Override
        public void onLocalMusicListChange() {
        }

        @Override
        public void onRecentMusicListChange() {
            Log.d(TAG, "onRecentMusicListChange: ");
            //通知主线程刷新
            handler.removeMessages(UPDATE_LIST);
            handler.sendEmptyMessage(UPDATE_LIST);
        }
    };
}
