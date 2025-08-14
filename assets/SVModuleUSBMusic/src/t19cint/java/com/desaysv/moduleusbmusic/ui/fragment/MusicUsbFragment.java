package com.desaysv.moduleusbmusic.ui.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.libusbmedia.bean.CurrentPlayInfo;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.adapter.MusicFolderListAdapter;
import com.desaysv.moduleusbmusic.adapter.MusicListAdapter;
import com.desaysv.moduleusbmusic.bean.FolderItem;
import com.desaysv.moduleusbmusic.businesslogic.control.CopyDeleteControl;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.listener.ProgressListener;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.ui.dialog.DownloadLimitDialog;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author uidq1846
 * @desc USB主界面
 * @time 2022-11-16 20:15
 */
public class MusicUsbFragment extends MusicBaseFragment implements View.OnScrollChangeListener, ViewPager.OnPageChangeListener, View.OnClickListener {
    private final static int UPDATE_LIST = UPDATE_CYCLE_TYPE + 1;
    private final static int UPDATE_POSITION = UPDATE_LIST + 1;
    private final static int UPDATE_DEVICE_STATE = UPDATE_POSITION + 1;
    private final static int UPDATE_FOLDER_LIST = UPDATE_DEVICE_STATE + 1;
    private IFragmentActionListener listener;
    private RecyclerView rvMusicList;
    private MusicListAdapter musicListAdapter;
    private RelativeLayout rlUsbCheck;
    //当前是否文件夹列表
    private boolean isFolderType = false;
    private MusicFolderListAdapter musicFolderListAdapter;
    private RelativeLayout rlFolderBack;
    private ImageButton ibFolderBack;
    private TextView tvFolderName;
    private RelativeLayout rlBottomDownload;
    private Button btAllSelect;
    private TextView tvSelectNumber;
    private Button btSelectCancel;
    private ImageButton ibSwitchFileMode;
    private ImageView ibDownloadSelect;
    private RelativeLayout rlSwitchFileMode;
    private Button btAllMusic;
    private Button btFolder;
    private DownloadLimitDialog limitDialog;
    private RelativeLayout rl_no_content;
    private AnimationDrawable downLoadAnimation;
    public MusicUsbFragment() {
    }

    public MusicUsbFragment(IFragmentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.music_usb_fragment;
    }

    @Override
    public void initView(View view) {
        rl_no_content = view.findViewById(R.id.rl_no_content);
        rlUsbCheck = view.findViewById(R.id.rl_usb_check);
        rvMusicList = view.findViewById(R.id.rv_music_list);
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
            }
        });
        //初始化适配器
        musicListAdapter = new MusicListAdapter(requireContext(), new ArrayList<>(USBMusicDate.getInstance().getUSB1MusicAllList()));
        rvMusicList.setAdapter(musicListAdapter);
        musicListAdapter.setItemClickListener(itemClickListener);
        //先把文件夹的适配器初始化了
        musicFolderListAdapter = new MusicFolderListAdapter(requireContext(), new HashMap<>(USBMusicDate.getInstance().getUSB1MusicAllMap()));
        musicFolderListAdapter.setItemClickListener(folderItemClickListener);
        //避免设置notifyItemChanged会闪一下
        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) rvMusicList.getItemAnimator();
        if (itemAnimator != null) {
            itemAnimator.setSupportsChangeAnimations(false);
        }
        //左上角文件夹返回和名称选项
        rlFolderBack = view.findViewById(R.id.rl_folder_back);
        ibFolderBack = view.findViewById(R.id.ib_folder_back);
        tvFolderName = view.findViewById(R.id.tv_folder_name);
        //右上角选项
        ibSwitchFileMode = view.findViewById(R.id.ib_switch_file_mode);
        ibDownloadSelect = view.findViewById(R.id.ib_download_select);
        rlSwitchFileMode = view.findViewById(R.id.rl_switch_file_mode);
        btAllMusic = view.findViewById(R.id.bt_all_music);
        btFolder = view.findViewById(R.id.bt_folder);
        //底部界面布局
        rlBottomDownload = view.findViewById(R.id.rl_bottom_download);
        btAllSelect = view.findViewById(R.id.bt_all_select);
        tvSelectNumber = view.findViewById(R.id.tv_select_number);
        btSelectCancel = view.findViewById(R.id.bt_cancel);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initViewListener() {
        ibSwitchFileMode.setOnClickListener(this);
        ibDownloadSelect.setOnClickListener(this);
        btAllMusic.setOnClickListener(this);
        btFolder.setOnClickListener(this);
        ibFolderBack.setOnClickListener(this);
        //底部栏点击
        btAllSelect.setOnClickListener(this);
        tvSelectNumber.setOnClickListener(this);
        btSelectCancel.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        USBMusicDate.getInstance().setMusicListChangeListener(iMusicListChange);
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
        CopyDeleteControl.getInstance().getCopyControl().registerCopyProgressListener(TAG, progressListener);
        updateInfo();
        updateList();
        updateUsbDeviceState();
        needToResumeFolderView();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDownloadAnim();
        USBMusicDate.getInstance().removeMusicListChangeListener(iMusicListChange);
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
        CopyDeleteControl.getInstance().getCopyControl().unRegisterCopyProgressListener(TAG);
    }

    /**
     * 是否需要恢复文件夹页面
     */
    private void needToResumeFolderView() {
        //查看当前是否播发的是文件夹列表
        if (CurrentPlayListType.FLODER == Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB1_MUSIC)).getCurrentPlayListType()) {
            switchToFolder();
        } else {
            switchToFile();
        }
        backToRootFolder();
    }

    /**
     * 更新当前开关状态
     */
    private void updateAllMusicAndFolderState() {
        if (isFolderType()) {
            btAllMusic.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_list_all_n, 0, 0, 0);
            btAllMusic.setTextColor(getResources().getColor(R.color.usb_music_switch_file_mode_text_n_color));
            btFolder.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_list_folder_p, 0, 0, 0);
            btFolder.setTextColor(getResources().getColor(R.color.usb_music_switch_file_mode_text_s_color));
            btFolder.setSelected(true);
            btAllMusic.setSelected(false);
        } else {
            btAllMusic.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_list_all_p, 0, 0, 0);
            btAllMusic.setTextColor(getResources().getColor(R.color.usb_music_switch_file_mode_text_s_color));
            btFolder.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_list_folder_n, 0, 0, 0);
            btFolder.setTextColor(getResources().getColor(R.color.usb_music_switch_file_mode_text_n_color));
            btFolder.setSelected(false);
            btAllMusic.setSelected(true);
        }
    }

    /**
     * 点击下载按钮
     */
    public void downloadFiles(boolean showSelectView) {
        Log.i(TAG, "downloadFiles: isFolderType = " + isFolderType());
        if (isFolderType()) {
            musicFolderListAdapter.showOrHideSelectView(showSelectView);
        } else {
            musicListAdapter.showOrHideSelectView(showSelectView);
        }
    }

    /**
     * 切换到文件列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void switchToFile() {
        Log.i(TAG, "switchToFile: isFolderType = " + isFolderType);
        if (isFolderType) {
            if (rvMusicList == null) {
                Log.i(TAG, "switchToFile: rvMusicList == null");
                return;
            }
            rvMusicList.setAdapter(musicListAdapter);
            musicListAdapter.notifyDataSetChanged();
            isFolderType = false;
            backToRootFolder();
            updateInfo();
        }
    }

    /**
     * 界面退出回到根层级
     */
    private void backToRootFolder() {
        musicFolderListAdapter.backToRootFolder();
    }

    /**
     * 切换到文件夹列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void switchToFolder() {
        Log.i(TAG, "switchToFolder: ");
        if (!isFolderType) {
            if (rvMusicList == null) {
                Log.i(TAG, "switchToFolder: rvMusicList == null");
                return;
            }
            rvMusicList.setAdapter(musicFolderListAdapter);
            musicFolderListAdapter.notifyDataSetChanged();
            isFolderType = true;
            updateInfo();
        }
    }

    /**
     * 提供当前文件夹位置
     *
     * @return isFolderType
     */
    public boolean isFolderType() {
        return isFolderType;
    }

    /**
     * 返回上一层级
     */
    public void backToParentFolder() {
        musicFolderListAdapter.backToParentFolder();
    }

    /**
     * 设备状态变化
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.i(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            if (DeviceConstants.DevicePath.USB0_PATH.equals(path)) {
                updateUsbDeviceState();
            }
        }
    };

    /**
     * 更新USB不存在的做法
     */
    private void updateUsbDeviceState() {
        Log.i(TAG, "updateUsbDeviceState: ");
        handler.removeMessages(UPDATE_DEVICE_STATE);
        handler.sendEmptyMessage(UPDATE_DEVICE_STATE);
    }

    /**
     * 更新列表数据
     */
    private void updateList() {
        handler.removeMessages(UPDATE_LIST);
        handler.sendEmptyMessage(UPDATE_LIST);
        handler.removeMessages(UPDATE_FOLDER_LIST);
        handler.sendEmptyMessage(UPDATE_FOLDER_LIST);
    }

    /**
     * 文件夹条目点击
     */
    private final MusicFolderListAdapter.MusicListItemClickListener folderItemClickListener = new MusicFolderListAdapter.MusicListItemClickListener() {
        @Override
        public void onItemClick(int position, List<FileMessage> fileMessages) {
            getMusicControlTool().getControlTool().setPlayList(fileMessages, CurrentPlayListType.FLODER);
            getMusicControlTool().getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.CLICK_ITEM, position);
            //然后跳转到全屏页面
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onActionChange(FragmentAction.TO_PLAY_FRAGMENT, MusicMainFragment.USB0_PLAY_PAGE_POSITION, MusicMainFragment.USB0_LIST_PAGE_POSITION);
                }
            });
            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onFolderClick(FolderItem folderItem) {
            //切换文件夹时需要归位
            rvMusicList.scrollToPosition(0);
            musicFolderListAdapter.notifyDataSetChanged();
            Log.i(TAG, "onFolderClick: " + folderItem.getNotePath());
            //如果是根目录则隐藏
            if (folderItem.getNotePath().equals("/")) {
                //先隐藏顶返回
                hideBackText();
                listener.onActionChange(FragmentAction.TO_USB_FOLDER_VIEW, -1, 2, folderItem);
            } else {
                //先隐藏顶部栏，再显示返回
                listener.onActionChange(FragmentAction.TO_USB_FOLDER_VIEW, -1, 2, folderItem);
                showBackText(folderItem.getNoteName());
            }
        }

        @Override
        public void onSelectItemClick(int selectPosition, int selectSize, FolderItem folderItem) {
            Log.i(TAG, "onSelectItemClick: selectPosition = " + selectPosition + " selectSize = " + selectSize + " folderItem = " + folderItem);
            setSelectNumberText(selectSize);
            updateAllSelectState();
        }
    };

    /**
     * 条目点击监听
     */
    private final MusicListAdapter.MusicListItemClickListener itemClickListener = new MusicListAdapter.MusicListItemClickListener() {
        @Override
        public void onItemClick(int position, FileMessage fileMessage) {
            Log.i(TAG, "onItemClick: fileMessage = " + fileMessage);
            //点击后播放
            if (!CurrentPlayListType.ALL.equals(Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB1_MUSIC)).getCurrentPlayListType())) {
                getMusicControlTool().getControlTool().setPlayList(USBMusicDate.getInstance().getUSB1MusicAllList(), CurrentPlayListType.ALL);
            }
            getMusicControlTool().getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.CLICK_ITEM, position);
            //然后跳转到全屏页面
            listener.onActionChange(FragmentAction.TO_PLAY_FRAGMENT, MusicMainFragment.USB0_PLAY_PAGE_POSITION, MusicMainFragment.USB0_LIST_PAGE_POSITION);
            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
        }

        @Override
        public void onSelectItemClick(int selectPosition, int selectSize, FileMessage fileMessage) {
            Log.i(TAG, "onSelectItemClick: selectPosition = " + selectPosition + " selectSize = " + selectSize + " fileMessage = " + fileMessage);
            setSelectNumberText(selectSize);
            updateAllSelectState();
        }
    };

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
        tvSelectNumber.setText(getString(R.string.usb_music_select_download) + "  (" + selectSize + ")");
    }

    /**
     * 数据变化监听
     */
    private final USBMusicDate.IListDataChange iMusicListChange = new USBMusicDate.IListDataChange() {
        @Override
        public void onUSB1MusicAllFolderMapChange() {
            Log.d(TAG, "onUSB1MusicAllFolderMapChange: ");
            handler.removeMessages(UPDATE_FOLDER_LIST);
            handler.sendEmptyMessage(UPDATE_FOLDER_LIST);
        }

        @Override
        public void onUSB2MusicAllFolderMapChange() {

        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onUSB1MusicAllListChange() {
            Log.d(TAG, "onUSB1MusicAllListChange: ");
            //通知主线程刷新
            handler.removeMessages(UPDATE_LIST);
            handler.sendEmptyMessage(UPDATE_LIST);
        }

        @Override
        public void onUSB2MusicAllListChange() {

        }

        @Override
        public void onLocalMusicListChange() {

        }

        @Override
        public void onRecentMusicListChange() {

        }
    };

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void dispatchMessage(Message msg, MusicBaseFragment fragment) {
        final MusicUsbFragment usbFragment = (MusicUsbFragment) fragment;
        switch (msg.what) {
            case UPDATE_LIST:
                //查看当前是哪个列表状态
                usbFragment.musicListAdapter.setFileMessageList(new ArrayList<>(USBMusicDate.getInstance().getUSB1MusicAllList()));
                showNoContentView();
                break;
            case UPDATE_FOLDER_LIST:
                //需要看当前是否为文件夹列表
                usbFragment.musicFolderListAdapter.setFolders(new HashMap<>(USBMusicDate.getInstance().getUSB1MusicAllMap()));
                break;
            case UPDATE_SONG_INFO:
                updateDownloadState();
                int positionInShowList = 0;
                if (isFolderType()) {
                    positionInShowList = musicFolderListAdapter.getPositionInShowList(getMusicControlTool().getStatusTool().getCurrentPlayItem());
                } else {
                    positionInShowList = musicListAdapter.getPositionInShowList(getMusicControlTool().getStatusTool().getCurrentPlayItem());
                }
                Log.d(TAG, "UPDATE_SONG_INFO: positionInShowList = " + positionInShowList);
                if (positionInShowList >= 0) {
                    usbFragment.rvMusicList.scrollToPosition(positionInShowList);
                }
                usbFragment.musicListAdapter.notifyDataSetChanged();
                /*MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        //更新条目状态
                        FileMessage playItem = Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB1_MUSIC)).getCurrentPlayItem();
                        // TODO: 2022-12-15 这里存在问题，如果播放的不是当前的列表，这个位置就对不上了
                        usbFragment.handler.removeMessages(UPDATE_POSITION);
                        Message obtain = Message.obtain();
                        obtain.what = UPDATE_POSITION;
                        int positionInShowList = 0;
                        if (isFolderType()) {
                            positionInShowList = musicFolderListAdapter.getPositionInShowList(playItem);
                            obtain.arg2 = 1;
                        } else {
                            positionInShowList = musicListAdapter.getPositionInShowList(playItem);
                            obtain.arg2 = 0;
                        }
                        Log.d(TAG, "run: positionInShowList = " + positionInShowList);
                        if (positionInShowList >= 0) {
                            obtain.arg1 = positionInShowList;
                            usbFragment.handler.sendMessage(obtain);
                        }
                    }
                });*/
                break;
            case UPDATE_POSITION:
                //更新旧的和新的位置，无需整体刷新
                Log.d(TAG, "dispatchMessage: UPDATE_POSITION isFolderType = " + msg.arg2);
                if (msg.arg2 == 1) {
                    usbFragment.musicFolderListAdapter.notifyItemChanged(usbFragment.musicFolderListAdapter.getCurrentHighLightPosition());
                    usbFragment.musicFolderListAdapter.notifyItemChanged(msg.arg1);
                } else {
                    usbFragment.musicListAdapter.notifyItemChanged(usbFragment.musicListAdapter.getCurrentHighLightPosition());
                    usbFragment.musicListAdapter.notifyItemChanged(msg.arg1);
                }
                usbFragment.rvMusicList.scrollToPosition(msg.arg1);
                break;
            case UPDATE_DEVICE_STATE:
                boolean usb1Connect = DeviceStatusBean.getInstance().isUSB1Connect();
                if (usb1Connect) {
                    rlUsbCheck.setVisibility(View.GONE);
                    usbFragment.rvMusicList.setVisibility(View.VISIBLE);
                } else {
                    //这里要隐藏掉无数据的缺省页面，和这个互斥
                    usbFragment.rvMusicList.setVisibility(View.GONE);
                    rlUsbCheck.setVisibility(View.VISIBLE);
                    //隐藏下载选项、文件选择框等
                    hideBottomDownloadView();
                    downloadFiles(false);
                    switchToFile();
                }
                showNoContentView();
                break;
        }
    }

    /**
     * 设置显示没有数据内容时的缺省页
     */
    private void showNoContentView() {
        boolean usb1Connect = DeviceStatusBean.getInstance().isUSB1Connect();
        Log.i(TAG, "showNoContentView: usb1Connect = " + usb1Connect);
        if (usb1Connect) {
            //获取当前列表，有就隐藏，没有数据则显示此
            List<FileMessage> usb1MusicAllList = USBMusicDate.getInstance().getUSB1MusicAllList();
            Log.i(TAG, "showNoContentView: usb1MusicAllList size = " + usb1MusicAllList.size());
            rl_no_content.setVisibility(usb1MusicAllList.isEmpty() ? View.VISIBLE : View.GONE);
            updateDownloadAndFileModeEnableStatus(!usb1MusicAllList.isEmpty());
        } else {
            //U盘没接入直接隐藏这个视图
            rl_no_content.setVisibility(View.GONE);
            updateDownloadAndFileModeEnableStatus(false);
        }
    }

    @Override
    protected IGetControlTool getMusicControlTool() {
        return ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool;
    }

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
        Log.i(TAG, "onPageScrollStateChanged: state = " + state);
        if (!isAdded()) {
            Log.i(TAG, "onPageScrollStateChanged: no add");
            return;
        }
        //当前在拖动
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING://ViewPage 处于拖动状态
            case ViewPager.SCROLL_STATE_SETTLING://ViewPage 处于正在找寻最后位置阶段
                rlSwitchFileMode.setVisibility(View.GONE);
                ibSwitchFileMode.setVisibility(View.INVISIBLE);
                ibDownloadSelect.setVisibility(View.INVISIBLE);
                break;
            case ViewPager.SCROLL_STATE_IDLE://ViewPage已经停下来了
                //如果此时返回不是可见状态则，恢复下载按钮和切换按钮可见
                showSwitchFileModeAndDownloadSelect();
                break;
        }
    }

    /**
     * 显示与隐藏文件切换或者下载按钮的选项方法
     */
    private void showSwitchFileModeAndDownloadSelect() {
        Log.i(TAG, "showSwitchFileModeAndDownloadSelect: ");
        //如果下载栏显示，则全部隐藏
        if (rlBottomDownload.getVisibility() != View.VISIBLE) {
            //如果返回状态显示，则只显示下载
            if (rlFolderBack.getVisibility() != View.VISIBLE) {
                ibSwitchFileMode.setVisibility(View.VISIBLE);
            } else {
                rlSwitchFileMode.setVisibility(View.GONE);
                ibSwitchFileMode.setVisibility(View.INVISIBLE);
            }
            ibDownloadSelect.setVisibility(View.VISIBLE);
        } else {
            rlSwitchFileMode.setVisibility(View.GONE);
            ibSwitchFileMode.setVisibility(View.INVISIBLE);
            ibDownloadSelect.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ib_switch_file_mode) {
            Log.i(TAG, "onClick: ib_switch_file_mode");
            updateAllMusicAndFolderState();
            showSwitchFileModeView();
        } else if (id == R.id.ib_download_select) {
            Log.i(TAG, "onClick: ib_download_select");
            downloadFiles(true);
            showBottomDownloadView();
        } else if (id == R.id.bt_all_music) {
            Log.i(TAG, "onClick: bt_all_music");
            switchToFile();
            updateAllMusicAndFolderState();
            hideSwitchFileModeView();
        } else if (id == R.id.bt_folder) {
            Log.i(TAG, "onClick: bt_folder");
            switchToFolder();
            updateAllMusicAndFolderState();
            hideSwitchFileModeView();
        } else if (id == R.id.ib_folder_back) {
            Log.i(TAG, "onClick: ib_folder_back");
            backToParentFolder();
            downloadFiles(false);
            hideBottomDownloadView();
        } else if (id == R.id.bt_all_select) {
            Log.i(TAG, "onClick: bt_all_select");
            if (isFolderType()) {
                if (!musicFolderListAdapter.isAllSelect()) {
                    musicFolderListAdapter.allSelect();
                } else {
                    musicFolderListAdapter.allCancel();
                }
            } else {
                if (!musicListAdapter.isAllSelect()) {
                    musicListAdapter.allSelect();
                } else {
                    musicListAdapter.allCancel();
                }
            }
            updateAllSelectState();
        } else if (id == R.id.tv_select_number) {
            Log.i(TAG, "onClick: tv_select_number");
            if (CopyDeleteControl.getInstance().getCopyControl().isCopying()) {
                Log.w(TAG, "onClick: is files copying");
                return;
            }
            if (isFolderType()) {
                Map<String, FileMessage> selectedMap = musicFolderListAdapter.getSelectedMap();
                if (selectedMap.isEmpty()) {
                    Log.w(TAG, "onClick: musicFolderListAdapter current do not select some tings");
                    return;
                }
            } else {
                Map<String, FileMessage> selectedMap = musicListAdapter.getSelectedMap();
                if (selectedMap.isEmpty()) {
                    Log.w(TAG, "onClick: musicListAdapter current do not select some tings");
                    return;
                }
            }
            ibDownloadSelect.setEnabled(false);
            //执行删除，
            hideBottomDownloadView();
            downloadFiles(false);
            doDownloads();
        } else if (id == R.id.bt_cancel) {
            Log.i(TAG, "onClick: bt_cancel");
            downloadFiles(false);
            hideBottomDownloadView();
        }
    }

    /**
     * 执行下载动作
     */
    private void doDownloads() {
        Map<String, FileMessage> selectedMap;
        if (isFolderType()) {
            selectedMap = musicFolderListAdapter.getSelectedMap();
        } else {
            selectedMap = musicListAdapter.getSelectedMap();
        }
        if (selectedMap == null || selectedMap.isEmpty()) {
            Log.w(TAG, "onClick: current do not select some tings");
            return;
        }
        List<FileMessage> fileMessages = new ArrayList<>();
        for (Map.Entry<String, FileMessage> map : selectedMap.entrySet()) {
            if (map.getValue() != null) {
                fileMessages.add(map.getValue());
            } else {
                //说明是文件夹里包含的文件
                Map<String, List<FolderItem>> folderMap = musicFolderListAdapter.getFolderMap();
                fileMessages.addAll(findFileMessageByFolder(map.getKey(), folderMap, new ArrayList<>()));
                UsbMusicPoint.getInstance().downLoadFolder(new ContentData(PointValue.Field.DocName, map.getKey()));
            }
        }
        Log.i(TAG, "doDownloads: fileMessages = " + fileMessages);
        CopyDeleteControl.getInstance().getCopyControl().copyFiles(fileMessages, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        startDownloadAnim();
    }

    /**
     * 把文件夹下边所有的内容都找出来
     *
     * @param mapKey    mapKey
     * @param folderMap folderMap
     * @return List<FileMessage>
     */
    private List<FileMessage> findFileMessageByFolder(String mapKey, Map<String, List<FolderItem>> folderMap, List<FileMessage> fileMessages) {
        List<FolderItem> folderItems = folderMap.get(mapKey);
        Log.i(TAG, "findFileMessageByFolder: mapKey = " + mapKey + " folderItems = " + folderItems);
        if (folderItems != null) {
            for (FolderItem f : folderItems) {
                if (f.getFileMessage() != null) {
                    fileMessages.add(f.getFileMessage());
                } else {
                    //这个说明当前是选中的是文件夹,这里应该是递归操作
                    String notePath = f.getNotePath();
                    Log.i(TAG, "findFileMessageByFolder: notePath = " + notePath);
                    findFileMessageByFolder(notePath, folderMap, fileMessages);
                }
            }
        }
        return fileMessages;
    }

    /**
     * 更新全部选项状态
     */
    private void updateAllSelectState() {
        boolean allSelect;
        if (isFolderType()) {
            allSelect = musicFolderListAdapter.isAllSelect();
        } else {
            allSelect = musicListAdapter.isAllSelect();
        }
        if (isRtlView()) {
            btAllSelect.setCompoundDrawablesWithIntrinsicBounds(0, 0, allSelect ? R.mipmap.icon_choice_on : R.mipmap.icon_choice_no, 0);
        } else {
            btAllSelect.setCompoundDrawablesWithIntrinsicBounds(allSelect ? R.mipmap.icon_choice_on : R.mipmap.icon_choice_no, 0, 0, 0);
        }
    }

    /**
     * 隐藏底部下载栏
     */
    private void hideBottomDownloadView() {
        Log.d(TAG, "hideBottomDownloadView: ");
        rlBottomDownload.setVisibility(View.GONE);
        btAllMusic.setSelected(false);
        //如果此时返回不是可见状态则，恢复下载按钮和切换按钮可见
        showSwitchFileModeAndDownloadSelect();
    }

    /**
     * 显示底部下载栏
     */
    private void showBottomDownloadView() {
        Log.d(TAG, "showBottomDownloadView: ");
        setSelectNumberText(0);
        updateAllSelectState();
        rlBottomDownload.setVisibility(View.VISIBLE);
        hideSwitchFileModeView();
        ibDownloadSelect.setVisibility(View.INVISIBLE);
        rlSwitchFileMode.setVisibility(View.GONE);
        ibSwitchFileMode.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示文件夹和文件切换布局
     */
    private void showSwitchFileModeView() {
        if (rlSwitchFileMode.getVisibility() == View.VISIBLE) {
            rlSwitchFileMode.setVisibility(View.GONE);
        } else {
            rlSwitchFileMode.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏布局
     */
    private void hideSwitchFileModeView() {
        if (rlSwitchFileMode.getVisibility() == View.VISIBLE) {
            rlSwitchFileMode.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置显示的文本
     *
     * @param text text
     */
    private void showBackText(String text) {
        Log.d(TAG, "showBackText: text = " + text);
        //进入子目录不让滑动
        tvFolderName.setText(text);
        rlSwitchFileMode.setVisibility(View.GONE);
        ibSwitchFileMode.setVisibility(View.INVISIBLE);
        rlFolderBack.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏返回按钮
     */
    private void hideBackText() {
        Log.d(TAG, "hideBackText: ");
        rlFolderBack.setVisibility(View.GONE);
        showSwitchFileModeAndDownloadSelect();
    }

    /**
     * 文件拷贝进度
     */
    private final ProgressListener progressListener = new ProgressListener() {
        @Override
        public void onProgressChange(long progress, long total) {
            Log.i(TAG, "onProgressChange: progress = " + progress + " total = " + total + " " + (int) (progress * 1.0 / total * 100) + "%");
        }

        @Override
        public void onSuccess(FileMessage fileMessage) {
            Log.i(TAG, "onSuccess: fileMessage = " + fileMessage);
        }

        @Override
        public void onFailed(FileMessage fileMessage) {
            Log.i(TAG, "onFailed: fileMessage = " + fileMessage);
        }

        @Override
        public void onSizeLimit(FileMessage fileMessage) {
            Log.i(TAG, "onSizeLimit: fileMessage = " + fileMessage);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showLimitDialog();
                }
            });
        }

        @Override
        public void onFinish() {
            Log.i(TAG, "onFinish: ");
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
        ibDownloadSelect.setImageResource(R.drawable.usb_music_list_page_download_anim);
        downLoadAnimation = (AnimationDrawable) ibDownloadSelect.getDrawable();
        downLoadAnimation.start();
    }

    private void stopDownloadAnim() {
        if (downLoadAnimation != null && downLoadAnimation.isRunning()) {
            downLoadAnimation.stop();
        }
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
     * 更新下载按钮状态
     */
    private void updateDownloadState() {
        boolean usb1Connect = DeviceStatusBean.getInstance().isUSB1Connect();
        ibDownloadSelect.setImageResource(R.drawable.music_usb_download_select_icon_selector);
        if (usb1Connect) {
            ibDownloadSelect.setEnabled(!CopyDeleteControl.getInstance().getCopyControl().isCopying());
        }
    }

    private void updateDownloadAndFileModeEnableStatus(boolean isEnable) {
        Log.i(TAG, "updateDownloadAndFileModeEnableStatus: isEnable = " + isEnable + " isCopying = " + CopyDeleteControl.getInstance().getCopyControl().isCopying());
        //考虑存在很多文件曲目时，刚好在下载当中，数据又更新过来的情况
        ibDownloadSelect.setEnabled(isEnable && !CopyDeleteControl.getInstance().getCopyControl().isCopying());
        if (!isEnable) {
            rlSwitchFileMode.setVisibility(View.GONE);
        } else {
            if (CopyDeleteControl.getInstance().getCopyControl().isCopying()) {
                startDownloadAnim();
            }
        }
        ibSwitchFileMode.setEnabled(isEnable);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter("USB_SWITCH_MODE_CLOSE"));
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // 定义广播接收器
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action = " + action);
            if ("USB_SWITCH_MODE_CLOSE".equals(action)) {
                hideSwitchFileModeView();
            }
        }
    };
}
