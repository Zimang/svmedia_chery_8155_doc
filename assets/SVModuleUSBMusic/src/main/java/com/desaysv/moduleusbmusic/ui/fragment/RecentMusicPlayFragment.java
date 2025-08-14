package com.desaysv.moduleusbmusic.ui.fragment;

import android.util.Log;
import android.view.View;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 媒体全屏播放界面
 * @time 2022-11-16 20:15
 */
public class RecentMusicPlayFragment extends BaseMusicPlayFragment {

    public RecentMusicPlayFragment() {
    }

    public RecentMusicPlayFragment(IFragmentActionListener listener) {
        super(listener);
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        //返回按钮
        View ibClickDownload = view.findViewById(R.id.ib_click_download);
        //本地没有下载
        ibClickDownload.setVisibility(View.GONE);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    protected IGetControlTool getMusicControlTool() {
        //这里根据需要返回不同的控制器，毕竟是播放音乐
        return ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool;
    }

    @Override
    protected int getPagePosition() {
        return MusicMainFragment.RECENT_PLAY_PAGE_POSITION;
    }

    @Override
    protected int getExitToPagePosition() {
        return MusicMainFragment.RECENT_LIST_PAGE_POSITION;
    }

    @Override
    protected List<FileMessage> getMusicList() {
        return USBMusicDate.getInstance().getRecentMusicAllList();
    }

    @Override
    public void onStart() {
        super.onStart();
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
    }

    @Override
    public void onStop() {
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
        super.onStop();
        UsbMusicPoint.getInstance().closeRecently(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
    }

    /**
     * 设备状态的变化回调
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            String currentPath = getMusicControlTool().getStatusTool().getCurrentPlayItem().getPath();
            Log.i(TAG, " PlayFragment onDeviceStatusChange: path = " + path + " status = " + status + " currentPath = " + currentPath);
            if (!status
                    && DeviceConstants.DevicePath.USB0_PATH.equals(path)
                    && (currentPath.startsWith(DeviceConstants.DevicePath.USB0_PATH) || currentPath.isEmpty())) {
                exitUI();
            }
        }
    };
}
