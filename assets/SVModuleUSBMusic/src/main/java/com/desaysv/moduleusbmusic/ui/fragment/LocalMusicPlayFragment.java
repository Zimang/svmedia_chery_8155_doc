package com.desaysv.moduleusbmusic.ui.fragment;

import android.view.View;

import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 媒体全屏播放界面
 * @time 2022-11-16 20:15
 */
public class LocalMusicPlayFragment extends BaseMusicPlayFragment {
    
    public LocalMusicPlayFragment() {
    }

    public LocalMusicPlayFragment(IFragmentActionListener listener) {
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
        return ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool;
    }

    @Override
    protected int getPagePosition() {
        return MusicMainFragment.LOCAL_PLAY_PAGE_POSITION;
    }

    @Override
    protected int getExitToPagePosition() {
        return MusicMainFragment.LOCAL_LIST_PAGE_POSITION;
    }

    @Override
    protected List<FileMessage> getMusicList() {
        return USBMusicDate.getInstance().getLocalMusicAllList();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalMusicPoint.getInstance().close(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
    }
}
