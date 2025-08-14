package com.desaysv.moduleusbmusic.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;

/**
 * @author uidq1846
 * @desc 媒体全屏播放界面
 * @time 2022-11-16 20:15
 */
public class MusicPlayHomeFragment extends BaseFragment {
    private IFragmentActionListener listener;
    private USB1MusicPlayFragment usb1MusicPlayFragment;
    private LocalMusicPlayFragment localMusicPlayFragment;
    private RecentMusicPlayFragment recentMusicPlayFragment;

    public MusicPlayHomeFragment() {
    }

    public MusicPlayHomeFragment(IFragmentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.music_play_home_fragment;
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void initData() {
        usb1MusicPlayFragment = new USB1MusicPlayFragment(listener);
        localMusicPlayFragment = new LocalMusicPlayFragment(listener);
        recentMusicPlayFragment = new RecentMusicPlayFragment(listener);
    }

    @Override
    public void initViewListener() {

    }

    @Override
    public void onStart() {
        super.onStart();
        onFragmentNewIntent();
        listener.onActionChange(FragmentAction.TO_PLAY_FRAGMENT, -1, -1);
    }

    @Override
    public void onFragmentNewIntent() {
        super.onFragmentNewIntent();
        if (!isAdded()) {
            Log.d(TAG, "onNewIntent: is no add");
            return;
        }
        toPage();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //进入界面时
        if (!hidden) {
            toPage();
        }
    }

    /**
     * 启动进入相应页面
     */
    private void toPage() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.w(TAG, "toPage: arguments is null");
            return;
        }
        int page = arguments.getInt(MusicMainFragment.ARG_TO_PAGE_KEY, MusicMainFragment.USB0_PLAY_PAGE_POSITION);
        Log.d(TAG, "toPage: page = " + page);
        switch (page) {
            case MusicMainFragment.USB0_PLAY_PAGE_POSITION:
                toFragment(usb1MusicPlayFragment);
                break;
            case MusicMainFragment.LOCAL_PLAY_PAGE_POSITION:
                toFragment(localMusicPlayFragment);
                break;
            case MusicMainFragment.RECENT_PLAY_PAGE_POSITION:
                toFragment(recentMusicPlayFragment);
                break;
        }
    }

    /**
     * 切换到其它页面
     *
     * @param fragment fragment
     */
    private void toFragment(BaseFragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction;
        transaction = fragmentManager.beginTransaction();
        fragment.setArguments(getArguments());
        transaction.replace(R.id.fl_play_page, fragment);
        transaction.commit();
        fragment.onFragmentNewIntent();
    }
}
