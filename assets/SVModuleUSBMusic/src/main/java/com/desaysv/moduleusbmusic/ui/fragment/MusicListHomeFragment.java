package com.desaysv.moduleusbmusic.ui.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduleusbmusic.BuildConfig;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.adapter.ScreenSlidePagerAdapter;
import com.desaysv.moduleusbmusic.bean.FolderItem;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.ui.view.ScrollConfigViewPager;
import com.desaysv.moduleusbmusic.utils.LocaleUtils;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author uidq1846
 * @desc 媒体列表主界面
 * @time 2022-11-16 20:15
 */
public class MusicListHomeFragment extends BaseFragment implements View.OnScrollChangeListener, ViewPager.OnPageChangeListener, IFragmentActionListener {
    private ScrollConfigViewPager vpMusicSwitch;
    private List<Fragment> fragments;
    private String[] titles;
    private ScreenSlidePagerAdapter pagerAdapter;
    private TabLayout tlMusicSwitch;
    private IFragmentActionListener listener;
    private MusicLocalFragment localFragment;
    private MusicUsbFragment musicUsbFragment;
    private MusicRecentFragment musicRecentFragment;
    //当前是否显示了文件夹返回文本
    private int POSITION_LOCAL = 0;
    private int POSITION_USB = 1;
    private int POSITION_RECENT = 2;
    private boolean isShowBackText = false;

    public MusicListHomeFragment() {
    }

    public MusicListHomeFragment(IFragmentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.music_list_home_fragment;
    }

    @Override
    public void initView(View view) {
        // Instantiate a ViewPager and a PagerAdapter.
        vpMusicSwitch = view.findViewById(R.id.vp_music_switch);
        tlMusicSwitch = view.findViewById(R.id.tl_music_switch);
    }

    @Override
    public void initData() {
        fragments = new ArrayList<>();
        localFragment = new MusicLocalFragment(this);
        musicUsbFragment = new MusicUsbFragment(this);
        musicRecentFragment = new MusicRecentFragment(this);
        Configuration config = getResources().getConfiguration();
        int direction = config.getLayoutDirection();
        String language = config.locale.getLanguage();
        Log.i(TAG, "initData: direction = " + direction + " language = " + language);
        if (BuildConfig.FLAVOR.equals("chery_8155_int_t19c")) {
            //非阿拉伯语等的右舵不翻转
            if (direction == View.LAYOUT_DIRECTION_RTL && !isRTLLanguage(language)) {
                // 当前语言环境是RTL，需要设置TabLayout的layout_direction属性为rtl
                tlMusicSwitch.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
            fragments.add(localFragment);
            fragments.add(musicUsbFragment);
            fragments.add(musicRecentFragment);
            titles = new String[]{getString(R.string.usb_music_tab_local_text), getString(R.string.usb_music_tab_usb_text), getString(R.string.usb_music_tab_recent_text)};
        } else {//其余项目保持镜像原有逻辑
            if (direction == View.LAYOUT_DIRECTION_RTL && !isRTLLanguage(language)) {
                // 当前语言环境是RTL，需要设置TabLayout的layout_direction属性为rtl
                tlMusicSwitch.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                POSITION_RECENT = 0;
                POSITION_USB = 1;
                POSITION_LOCAL = 2;
                fragments.add(musicRecentFragment);
                fragments.add(musicUsbFragment);
                fragments.add(localFragment);
                titles = new String[]{getString(R.string.usb_music_tab_recent_text), getString(R.string.usb_music_tab_usb_text), getString(R.string.usb_music_tab_local_text)};
            } else {
                fragments.add(localFragment);
                fragments.add(musicUsbFragment);
                fragments.add(musicRecentFragment);
                titles = new String[]{getString(R.string.usb_music_tab_local_text), getString(R.string.usb_music_tab_usb_text), getString(R.string.usb_music_tab_recent_text)};
            }
        }
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), fragments, titles);
        vpMusicSwitch.setOffscreenPageLimit(2);
        vpMusicSwitch.setAdapter(pagerAdapter);
        tlMusicSwitch.setupWithViewPager(vpMusicSwitch);
        //去除顶部长按出现的工具集
        for (int i = 0; i < tlMusicSwitch.getTabCount(); i++) {
            TabLayout.Tab tab = tlMusicSwitch.getTabAt(i);
            if (tab != null) {
                TooltipCompat.setTooltipText(tab.view, null);
                TextView textView = (TextView) tab.view.getChildAt(1);
                textView.setMaxLines(1);
            }
        }
    }

    /**
     * 是否默认镜像翻转的语言
     *
     * @param language language
     * @return T F
     */
    private boolean isRTLLanguage(String language) {
        return language.equals("ar") || language.equals("fa") || language.equals("iw");
    }

    private void adaptiveLanguage() {
        ViewGroup.LayoutParams tlMusicSwitchLP = tlMusicSwitch.getLayoutParams();
        if (LocaleUtils.getInstance().isHebrewLocale()) {
            Log.d(TAG, "adaptiveLanguage: hebrew");
            tlMusicSwitchLP.width = 800;
        }
        tlMusicSwitch.setLayoutParams(tlMusicSwitchLP);
    }

    @Override
    public void initViewListener() {
        vpMusicSwitch.addOnPageChangeListener(this);
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
        MusicSetting.getInstance().putInt(TAG, position);
        if (position == POSITION_LOCAL) {
            showLocalSelectState();
            localFragment.onPageSelected(position);
        } else if (position == POSITION_USB) {
            showUsbSelectState();
            musicUsbFragment.onPageSelected(position);
        } else if (position == POSITION_RECENT) {
            showRecentlySelectState();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(TAG, "onPageScrollStateChanged: state = " + state);
        //这里用于感知状态
        //SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
        musicUsbFragment.onPageScrollStateChanged(state);
        localFragment.onPageScrollStateChanged(state);
    }

    /**
     * currentPage
     *
     * @param currentPage currentPage
     */
    private void toPage(int currentPage) {
        Log.d(TAG, "toPage: currentPage = " + currentPage);
        MusicSetting.getInstance().putInt(TAG, currentPage);
        vpMusicSwitch.setCurrentItem(currentPage, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //adaptiveLanguage();
        //当前在列表页，通知刷新主页面
        listener.onActionChange(FragmentAction.EXIT_PLAY_FRAGMENT, -1, -1);
        onFragmentNewIntent();
    }

    @Override
    public void onFragmentNewIntent() {
        super.onFragmentNewIntent();
        if (!isAdded()) {
            Log.d(TAG, "onNewIntent: is no add");
            return;
        }
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.w(TAG, "onNewIntent: arguments is null");
            toPage(MusicSetting.getInstance().getInt(TAG, POSITION_LOCAL));
            return;
        }
        //默认去全部列表页
        int page = arguments.getInt(MusicMainFragment.ARG_TO_PAGE_KEY, MusicMainFragment.LOCAL_LIST_PAGE_POSITION);
        switch (page) {
            case MusicMainFragment.LOCAL_LIST_PAGE_POSITION:
                toPage(POSITION_LOCAL);
                break;
            case MusicMainFragment.USB0_LIST_PAGE_POSITION:
                toPage(POSITION_USB);
                break;
            case MusicMainFragment.RECENT_LIST_PAGE_POSITION:
                toPage(POSITION_RECENT);
                break;
            default:
                toPage(MusicSetting.getInstance().getInt(TAG, POSITION_LOCAL));
                break;
        }
        setArguments(null);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * 显示USB页面右上角选项
     */
    private void showUsbSelectState() {
        //如果当前是根目录的话则显示，反之只显示视图
        if (isShowBackText) {
            tlMusicSwitch.setVisibility(View.INVISIBLE);
        } else {
            tlMusicSwitch.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示本地页面右上角选项
     */
    private void showLocalSelectState() {
        tlMusicSwitch.setVisibility(View.VISIBLE);
    }

    /**
     * 显示最近页面右上角选项
     */
    private void showRecentlySelectState() {
        tlMusicSwitch.setVisibility(View.VISIBLE);
    }

    /**
     * 设置显示的文本
     *
     * @param text text
     */
    private void showBackText(String text) {
        //进入子目录不让滑动
        vpMusicSwitch.setCanScroll(false);
        isShowBackText = true;
        tlMusicSwitch.setVisibility(View.GONE);
    }

    /**
     * 隐藏返回按钮
     */
    private void hideBackText() {
        //恢复滑动
        vpMusicSwitch.setCanScroll(true);
        isShowBackText = false;
        showUsbSelectState();
    }

    @Override
    public void onActionChange(FragmentAction action, int targetPage, int fromPage) {
        Log.d(TAG, "onActionChange: action = " + action.name());
        listener.onActionChange(action, targetPage, fromPage);
    }

    @Override
    public void onActionChange(FragmentAction action, int targetPage, int fromPage, Object data) {
        Log.d(TAG, "onActionChange: action = " + action.name());
        if (action == FragmentAction.TO_USB_FOLDER_VIEW) {
            FolderItem folderItem = (FolderItem) data;
            Log.d(TAG, "onActionChange: " + folderItem.getNotePath());
            //如果是根目录则隐藏
            if (folderItem.getNotePath().equals("/")) {
                hideBackText();
                listener.onActionChange(FragmentAction.EXIT_USB_FOLDER_VIEW, targetPage, fromPage);
            } else {
                listener.onActionChange(FragmentAction.TO_USB_FOLDER_VIEW, targetPage, fromPage);
                showBackText(folderItem.getNoteName());
            }
        }
    }
}
