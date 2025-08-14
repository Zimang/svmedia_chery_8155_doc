package com.desaysv.moduleusbmusic.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.libusbmedia.bean.CurrentPlayInfo;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduleusbmusic.BuildConfig;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.adapter.MusicListAdapter;
import com.desaysv.moduleusbmusic.businesslogic.control.CopyDeleteControl;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.listener.ProgressListener;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.ui.dialog.LocalMusicDeleteDialog;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author uidq1846
 * @desc 媒体主界面
 * @time 2022-11-16 20:15
 */
public class MusicLocalFragment extends MusicBaseFragment implements MusicListAdapter.MusicListItemClickListener, View.OnScrollChangeListener, ViewPager.OnPageChangeListener, View.OnClickListener {
    private IFragmentActionListener listener;
    private RecyclerView rvMusicList;
    private RelativeLayout rlEmptyList;
    private MusicListAdapter musicListAdapter;
    private final static int UPDATE_LIST = UPDATE_CYCLE_TYPE + 1;
    private final static int UPDATE_POSITION = UPDATE_LIST + 1;
    private RelativeLayout rlBottomDelete;
    private Button btAllSelect;
    private TextView tvSelectNumber;
    private Button btSelectCancel;
    private ImageButton ibDelete;
    private LocalMusicDeleteDialog deleteDialog;

    public MusicLocalFragment() {
    }

    public MusicLocalFragment(IFragmentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.music_local_fragment;
    }

    @Override
    public void initView(View view) {
        rvMusicList = view.findViewById(R.id.rv_music_list);
        rlEmptyList = view.findViewById(R.id.rl_empty_list);
        //底部栏
        rlBottomDelete = view.findViewById(R.id.rl_bottom_delete);
        btAllSelect = view.findViewById(R.id.bt_all_select);
        tvSelectNumber = view.findViewById(R.id.tv_select_number);
        btSelectCancel = view.findViewById(R.id.bt_cancel);
        //右上角选项
        ibDelete = view.findViewById(R.id.ib_delete);
    }

    @Override
    public void initData() {
        super.initData();
        initAdapter();
    }

    private void initAdapter() {
        musicListAdapter = new MusicListAdapter(requireContext(), new ArrayList<>(USBMusicDate.getInstance().getLocalMusicAllList()));
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
        // 在列表可见前，先定位到正在播放的音乐位置，避免在列表可见后，再定位到正在播放的音乐位置，导致闪烁，该现象在越靠后的播放位置越明显
        int playPosition = getMusicControlTool().getStatusTool().getCurrentItemPosition();
        layoutManager.scrollToPositionWithOffset(playPosition, 0);
        Log.d(TAG, "scrollToPositionWithOffset done, playPosition = " + playPosition);
    }

    @Override
    public void onStart() {
        super.onStart();
        USBMusicDate.getInstance().setMusicListChangeListener(iMusicListChange);
        CopyDeleteControl.getInstance().getDeleteControl().registerDeleteProgressListener(TAG, progressListener);
        updateList();
        updateInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
        USBMusicDate.getInstance().removeMusicListChangeListener(iMusicListChange);
        CopyDeleteControl.getInstance().getDeleteControl().unRegisterDeleteProgressListener(TAG);
    }

    @Override
    public void initViewListener() {
        btAllSelect.setOnClickListener(this);
        tvSelectNumber.setOnClickListener(this);
        btSelectCancel.setOnClickListener(this);
        //右上角选项
        ibDelete.setOnClickListener(this);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void dispatchMessage(Message msg, MusicBaseFragment fragment) {
        final MusicLocalFragment localFragment = (MusicLocalFragment) fragment;
        switch (msg.what) {
            case UPDATE_SONG_INFO:
                updateDeleteState();
                int playPosition = getMusicControlTool().getStatusTool().getCurrentItemPosition();
                localFragment.rvMusicList.scrollToPosition(playPosition);
                if (playPosition == localFragment.musicListAdapter.getCurrentHighLightPosition()) {
                    Log.d(TAG, "dispatchMessage: UPDATE_SONG_INFO is the same item");
                    return;
                }
                localFragment.musicListAdapter.notifyDataSetChanged();
                /*MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        //更新条目状态
                        int playPosition = getMusicControlTool().getStatusTool().getCurrentItemPosition();
                        localFragment.handler.removeMessages(UPDATE_POSITION);
                        Message obtain = Message.obtain();
                        obtain.what = UPDATE_POSITION;
                        obtain.arg1 = playPosition;
                        localFragment.handler.sendMessage(obtain);
                    }
                });*/
                break;
            case UPDATE_POSITION:
                //更新旧的和新的位置，无需整体刷新
                localFragment.musicListAdapter.notifyItemChanged(localFragment.musicListAdapter.getCurrentHighLightPosition());
                localFragment.musicListAdapter.notifyItemChanged(msg.arg1);
                localFragment.rvMusicList.scrollToPosition(msg.arg1);
                break;
            case UPDATE_LIST:
                //这个是数据更新，播放列表不一定更新
                localFragment.musicListAdapter.setFileMessageList(new ArrayList<>(USBMusicDate.getInstance().getLocalMusicAllList()));
                localFragment.updateListView();
                break;
        }
    }

    @Override
    protected IGetControlTool getMusicControlTool() {
        return ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool;
    }

    @Override
    public void onItemClick(int position, FileMessage fileMessage) {
        Log.d(TAG, "onItemClick: fileMessage = " + fileMessage);
        //点击后播放
        if (!CurrentPlayListType.ALL.equals(Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.LOCAL_MUSIC)).getCurrentPlayListType())) {
            getMusicControlTool().getControlTool().setPlayList(USBMusicDate.getInstance().getLocalMusicAllList(), CurrentPlayListType.ALL);
        }
        getMusicControlTool().getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.CLICK_ITEM, position);
        //然后跳转到全屏页面
        listener.onActionChange(FragmentAction.TO_PLAY_FRAGMENT, MusicMainFragment.LOCAL_PLAY_PAGE_POSITION, MusicMainFragment.LOCAL_LIST_PAGE_POSITION);
        LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
    }

    @Override
    public void onSelectItemClick(int selectPosition, int selectSize, FileMessage fileMessage) {
        Log.d(TAG, "onSelectItemClick: selectPosition = " + selectPosition + " selectSize = " + selectSize + " fileMessage = " + fileMessage);
        setSelectNumberText(selectSize);
        updateAllSelectState();
    }

    /**
     * 配置选项数量
     *
     * @param selectSize selectSize
     */
    @SuppressLint("SetTextI18n")
    private void setSelectNumberText(int selectSize) {
        if (selectSize > 0) {
            tvSelectNumber.setTextColor(getResources().getColor(R.color.usb_music_select_bottom_number_no_zero_color));
        } else {
            tvSelectNumber.setTextColor(getResources().getColor(R.color.usb_music_select_bottom_number_zero_color));
        }
        tvSelectNumber.setText(getString(R.string.usb_music_select_delete) + "  (" + selectSize + ")");
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
        List<FileMessage> musicList = USBMusicDate.getInstance().getLocalMusicAllList();
        if (musicList.isEmpty()) {
            rvMusicList.setVisibility(View.GONE);
            rlEmptyList.setVisibility(View.VISIBLE);
        } else {
            rvMusicList.setVisibility(View.VISIBLE);
            rlEmptyList.setVisibility(View.GONE);
        }
        updateDeleteState();
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
            Log.d(TAG, "onLocalMusicListChange: ");
            //通知主线程刷新
            handler.removeMessages(UPDATE_LIST);
            handler.sendEmptyMessage(UPDATE_LIST);
        }

        @Override
        public void onRecentMusicListChange() {

        }
    };

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: position = " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(TAG, "onPageScrollStateChanged: state = " + state);
        if (!isAdded()) {
            Log.d(TAG, "onPageScrollStateChanged: no add");
            return;
        }
        //当前在拖动
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING://ViewPage 处于拖动状态
            case ViewPager.SCROLL_STATE_SETTLING://ViewPage 处于正在找寻最后位置阶段
                ibDelete.setVisibility(View.INVISIBLE);
                break;
            case ViewPager.SCROLL_STATE_IDLE://ViewPage已经停下来了
                showDeleteSelectButton();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bt_all_select) {
            Log.d(TAG, "onClick: bt_all_select");
            if (!musicListAdapter.isAllSelect()) {
                musicListAdapter.allSelect();
            } else {
                musicListAdapter.allCancel();
            }
        } else if (id == R.id.tv_select_number) {
            Log.d(TAG, "onClick: tv_select_number");
            if (CopyDeleteControl.getInstance().getDeleteControl().isDeleting()) {
                Log.w(TAG, "onClick: is files deleting");
                return;
            }
            Map<String, FileMessage> selectedMap = musicListAdapter.getSelectedMap();
            if (selectedMap.isEmpty()) {
                Log.w(TAG, "onClick: current do not select some tings");
                return;
            }
            showDeleteDialog();
        } else if (id == R.id.bt_cancel) {
            Log.d(TAG, "onClick: bt_cancel");
            deleteFilesView(false);
            hideBottomDeleteView();
        } else if (id == R.id.ib_delete) {
            Log.d(TAG, "onClick: ib_delete");
            deleteFilesView(true);
            showBottomDownloadView();
        }
    }

    /**
     * 显示是否删除弹窗
     */
    private void showDeleteDialog() {
        if (deleteDialog == null) {
            deleteDialog = new LocalMusicDeleteDialog(TAG);
            deleteDialog.setButtonClickListener(buttonClickListener);
            if (BuildConfig.FLAVOR.equals("chery_8155_int_t19c")) {
                deleteDialog.setNeedBlur(false);
            }
        }
        deleteDialog.show(AppBase.mContext);
    }

    /**
     * 弹窗按钮点击回调
     */
    private final LocalMusicDeleteDialog.ButtonClickListener buttonClickListener = new LocalMusicDeleteDialog.ButtonClickListener() {
        @Override
        public void onConfirm() {
            Log.d(TAG, "onConfirm: ");
            ibDelete.setEnabled(false);
            //执行删除，
            hideBottomDeleteView();
            deleteFilesView(false);
            doDeletes();
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "onCancel: ");
        }
    };

    /**
     * 执行删除操作
     */
    private void doDeletes() {
        Map<String, FileMessage> selectedMap = musicListAdapter.getSelectedMap();
        if (selectedMap.isEmpty()) {
            Log.w(TAG, "onClick: current do not select some tings");
            return;
        }
        List<FileMessage> fileMessages = new ArrayList<>();
        IGetControlTool getControlTool = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool();
        boolean foundNextPosition = false;
        //删除的是播放的曲目
        for (Map.Entry<String, FileMessage> map : selectedMap.entrySet()) {
            FileMessage fileMessage = map.getValue();
            fileMessages.add(fileMessage);
            //当删除的文件刚好在是播放的文件时，先停止播放
            String playPath = getControlTool.getStatusTool().getCurrentPlayItem().getPath();
            if (playPath.equals(fileMessage.getPath())) {
                //当前处于播放状态才需要下一曲播放
                if (getControlTool.getStatusTool().isPlaying()) {
                    getControlTool.getControlTool().processCommand(MediaAction.STOP, ChangeReasonData.NA);
                    List<FileMessage> playList = getControlTool.getStatusTool().getPlayList();
                    int nextComparePosition = -1;
                    int preComparePosition = -1;
                    for (int position = 0; position < playList.size(); position++) {
                        String path = playList.get(position).getPath();
                        boolean isDeletePath = selectedMap.containsKey(path);
                        if (playPath.equals(path)) {
                            nextComparePosition = position + 1;
                            foundNextPosition = true;
                        } else {
                            //查找当前的下一项，找到了就不再前行，当前播放列表的顺序
                            if (foundNextPosition) {
                                if (isDeletePath) {
                                    nextComparePosition = position + 1;
                                } else {
                                    foundNextPosition = false;
                                }
                            }
                            if (preComparePosition < 0 && !isDeletePath) {
                                //记忆前一项(其实就是第一项)
                                preComparePosition = position;
                            }
                        }
                    }
                    Log.i(TAG, "doDeletes: nextComparePosition = " + nextComparePosition + " preComparePosition = " + preComparePosition + " size = " + playList.size());
                    if (nextComparePosition >= playList.size() && preComparePosition > -1) {
                        getControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.NA, preComparePosition);
                    } else if (nextComparePosition < playList.size() && nextComparePosition > -1) {
                        getControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.NA, nextComparePosition);
                    }
                } else {
                    getControlTool.getControlTool().processCommand(MediaAction.STOP, ChangeReasonData.NA);
                }
                try {
                    //这里休息下，否则还未来得及释放，就到文件删除了
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i(TAG, "doDeletes: fileMessages = " + fileMessages);
        CopyDeleteControl.getInstance().getDeleteControl().deleteFile(fileMessages);
    }

    /**
     * 显示与隐藏删除按钮的选项方法
     */
    private void showDeleteSelectButton() {
        Log.d(TAG, "showDeleteSelectButton: ");
        if (rlBottomDelete.getVisibility() != View.VISIBLE) {
            ibDelete.setVisibility(View.VISIBLE);
            return;
        }
        ibDelete.setVisibility(View.INVISIBLE);
    }

    /**
     * 隐藏底部下载栏
     */
    private void hideBottomDeleteView() {
        Log.d(TAG, "hideBottomDeleteView: ");
        rlBottomDelete.setVisibility(View.GONE);
        //如果此时显示的是空数据页面，则隐藏
        showDeleteSelectButton();
    }

    /**
     * 显示底部下载栏
     */
    private void showBottomDownloadView() {
        Log.d(TAG, "showBottomDownloadView: ");
        setSelectNumberText(0);
        updateAllSelectState();
        rlBottomDelete.setVisibility(View.VISIBLE);
        ibDelete.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示选项选择
     *
     * @param isShowView isShowView
     */
    private void deleteFilesView(boolean isShowView) {
        musicListAdapter.showOrHideSelectView(isShowView);
    }

    /**
     * 更新全部选项状态
     */
    private void updateAllSelectState() {
        if (BuildConfig.FLAVOR.equals("chery_8155_int_t19c") && isRtlView()) {
            btAllSelect.setCompoundDrawablesWithIntrinsicBounds(0, 0, musicListAdapter.isAllSelect() ? R.mipmap.icon_choice_on : R.mipmap.icon_choice_no, 0);
        } else {
            btAllSelect.setCompoundDrawablesWithIntrinsicBounds(musicListAdapter.isAllSelect() ? R.mipmap.icon_choice_on : R.mipmap.icon_choice_no, 0, 0, 0);
        }
    }

    /**
     * 文件删除进度
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
                    updateDeleteState();
                }
            });
        }
    };

    /**
     * 更新删除状态，当前未删除时的按钮状态
     */
    private void updateDeleteState() {
        //如果当前是无数据界面，则设置不可用
        if (rlEmptyList.getVisibility() == View.VISIBLE) {
            ibDelete.setEnabled(false);
        } else {
            ibDelete.setEnabled(!CopyDeleteControl.getInstance().getDeleteControl().isDeleting());
        }
    }
}
