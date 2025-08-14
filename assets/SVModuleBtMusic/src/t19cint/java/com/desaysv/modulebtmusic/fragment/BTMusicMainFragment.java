package com.desaysv.modulebtmusic.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.modulebtmusic.R;
import com.desaysv.modulebtmusic.manager.BTMusicManager;
import com.desaysv.modulebtmusic.manager.BTMusicVRAdapterManager;
import com.desaysv.modulebtmusic.utils.FragmentSwitchUtil;

public class BTMusicMainFragment extends BaseFragment {
    private static final String TAG = "BTMusicMainFragment";

    private BTMusicHomeFragment btMusicHomeFragment;
    private BTMusicPlayFragment btMusicPlayFragment;

    private int navigation = Constants.NavigationFlag.FLAG_MAIN;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        FragmentSwitchUtil.getInstance().registerListener(fragmentSwitchListener);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        BTMusicVRAdapterManager.getInstance().setBTFragmentForeground(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        initShowFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        BTMusicVRAdapterManager.getInstance().setBTFragmentForeground(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentSwitchUtil.getInstance().unregisterListener(fragmentSwitchListener);
    }

    @Override
    public int getLayoutResID() {
        return R.layout.layout_bt_music_main_fg;
    }

    @Override
    public void initView(View view) {
    }

    @Override
    public void initData() {
        btMusicHomeFragment = new BTMusicHomeFragment();
        btMusicPlayFragment = new BTMusicPlayFragment();
    }

    @Override
    public void initViewListener() {
    }

    public void setNavigation(int navigation) {
        this.navigation = navigation;
        MusicSetting.getInstance().putInt(com.desaysv.modulebtmusic.Constants.BT_MUSIC_PAGE, navigation);
    }

    private void initShowFragment() {
        if (BTMusicManager.getInstance().isA2DPConnected() && navigation == Constants.NavigationFlag.FLAG_PLAY) {
            toFragment(btMusicPlayFragment, FragmentSwitchUtil.BT_MUSIC_PLAY);
            MusicSetting.getInstance().putInt(com.desaysv.modulebtmusic.Constants.BT_MUSIC_PAGE, Constants.NavigationFlag.FLAG_PLAY);
        } else {
            toFragment(btMusicHomeFragment, FragmentSwitchUtil.BT_MUSIC_HOME);
            MusicSetting.getInstance().putInt(com.desaysv.modulebtmusic.Constants.BT_MUSIC_PAGE, Constants.NavigationFlag.FLAG_MAIN);
        }
    }

    /**
     * 切换到其它页面
     *
     * @param fragment fragment
     */
    private void toFragment(Fragment fragment, int type) {
        if (fragment == null) {
            Log.w(TAG, "toFragment: fragment is null");
            return;
        }
        Log.i(TAG, "toFragment: type = " + type);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.fl_fragment_container_view, fragment);
        transaction.commit();
//        //如果没添加则添加到栈当中
//        if (!fragment.isAdded()) {
//            transaction.add(R.id.fl_fragment_container_view, fragment);
//        }
//        //当前打开的是播放页，则隐藏home
//        if (fragment != btMusicHomeFragment) {
//            transaction.hide(btMusicHomeFragment);
//        }
//        //当前打开的是home页，则隐藏播放页
//        if (fragment != btMusicPlayFragment) {
//            transaction.hide(btMusicPlayFragment);
//        }
//        transaction.show(fragment);
//        transaction.addToBackStack(fragment.getClass().getSimpleName());
//        transaction.commit();
        FragmentSwitchUtil.getInstance().notifyFragmentSwitch(type);
    }

    private FragmentSwitchUtil.OnFragmentSwitchListener fragmentSwitchListener = new FragmentSwitchUtil.OnFragmentSwitchListener() {
        @Override
        public void onFragmentSwitch(int type) {
        }

        @Override
        public void switchFragment(int type) {
            switch (type) {
                case FragmentSwitchUtil.BT_MUSIC_HOME:
                    toFragment(btMusicHomeFragment, type);
                    setNavigation(Constants.NavigationFlag.FLAG_MAIN);
                    break;
                case FragmentSwitchUtil.BT_MUSIC_PLAY:
                    toFragment(btMusicPlayFragment, type);
                    setNavigation(Constants.NavigationFlag.FLAG_PLAY);
                    break;
            }
        }
    };
}
