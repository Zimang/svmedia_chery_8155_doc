package com.desaysv.moduleusbmusic.ui.fragment;

import static com.desaysv.moduleusbmusic.listener.FragmentAction.EXIT_PLAY_FRAGMENT;
import static com.desaysv.moduleusbmusic.listener.FragmentAction.TO_PLAY_FRAGMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.mediacommonlib.utils.ProductConfig;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.listener.FragmentAction;
import com.desaysv.moduleusbmusic.listener.IFragmentActionListener;
import com.desaysv.moduleusbmusic.vr.MusicVrManager;

/**
 * @author uidq1846
 * @desc 媒体主界面
 * @time 2022-11-16 20:15
 */
public class MusicMainFragment extends BaseFragment implements IFragmentActionListener {
    private BaseFragment musicListHomeFragment;
    private MusicPlayHomeFragment musicPlayHomeFragment;
    private IFragmentActionListener listener;
    private boolean isRootPath = true;
    private boolean isPlayPage = false;
    //用于传输页面切换意图
    public static final String ARG_TO_PAGE_KEY = "ARG_TO_PAGE_KEY";
    public static final String ARG_FROM_PAGE_KEY = "ARG_FROM_PAGE_KEY";
    public static final String MUSIC_PAGE = "MUSIC_PAGE";
    public static final String MUSIC_FG_FLAG = "MUSIC_FG_FLAG";
    //给音源页面进行编排位置0给了最主页的位置
    //本地列表页
    public static final int LOCAL_LIST_PAGE_POSITION = 1;
    //USB列表页
    public static final int USB0_LIST_PAGE_POSITION = LOCAL_LIST_PAGE_POSITION + 1;
    //最近列表页
    public static final int RECENT_LIST_PAGE_POSITION = USB0_LIST_PAGE_POSITION + 1;
    //本地播放页
    public static final int LOCAL_PLAY_PAGE_POSITION = RECENT_LIST_PAGE_POSITION + 1;
    //USB播放页
    public static final int USB0_PLAY_PAGE_POSITION = LOCAL_PLAY_PAGE_POSITION + 1;
    //最近播放页
    public static final int RECENT_PLAY_PAGE_POSITION = USB0_PLAY_PAGE_POSITION + 1;

    /**
     * 需提供无参构造，避免切换主题时，报异常
     */
    public MusicMainFragment() {
    }

    public MusicMainFragment(IFragmentActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResID() {
        return R.layout.music_main_fragment;
    }

    @Override
    public void initView(View view) {
        // Instantiate a ViewPager and a PagerAdapter.
    }

    @Override
    public void initData() {
        if (ProductConfig.isTheme2(getContext())) {
            musicListHomeFragment = new MusicListHomeFragmentTheme2(this);
        } else {
            musicListHomeFragment = new MusicListHomeFragment(this);
        }
        musicPlayHomeFragment = new MusicPlayHomeFragment(this);
    }

    @Override
    public void initViewListener() {

    }

    @Override
    public void onStart() {
        super.onStart();
        onFragmentNewIntent();
        MusicVrManager.getInstance().getResponse().uploadActiveStatus(Constants.Source.SOURCE_MUSIC,true);
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicSetting.getInstance().putInt(MUSIC_FG_FLAG, 1);
    }

    @Override
    public void onPause() {
        super.onPause();
        MusicSetting.getInstance().putInt(MUSIC_FG_FLAG, 0);
        MusicVrManager.getInstance().getResponse().uploadActiveStatus(Constants.Source.SOURCE_MUSIC,false);
    }

    /**
     * 增加类似activity的新意图方法，避免设置属性值后，因onState
     */
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActionChange(FragmentAction action, int targetPage, int fromPage) {
        Log.d(TAG, "onActionChange: action = " + action.name() + " targetPage = " + targetPage);
        if (targetPage > 0) {
            MusicSetting.getInstance().putInt(MUSIC_PAGE, targetPage);
        }
        switch (action) {
            case TO_PLAY_FRAGMENT:
                //这里通知main当前进入了播放页面
                isPlayPage = true;
                listener.onActionChange(action, targetPage, fromPage);
                //如果targetPage小于0，说明是自恢复的，不是主动设置进入的，只需通知主页面刷新左侧按钮
                if (targetPage >= 0) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ARG_TO_PAGE_KEY, targetPage);
                    bundle.putInt(ARG_FROM_PAGE_KEY, fromPage);
                    musicPlayHomeFragment.setArguments(bundle);
                    toFragment(musicPlayHomeFragment);
                }
                break;
            case EXIT_PLAY_FRAGMENT:
                isPlayPage = false;
                if (targetPage >= 0) {
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt(ARG_TO_PAGE_KEY, targetPage);
                    bundle2.putInt(ARG_FROM_PAGE_KEY, fromPage);
                    musicListHomeFragment.setArguments(bundle2);
                    toFragment(musicListHomeFragment);
                }
                //等页面切换后再显示
                if (isRootPath) {
                    listener.onActionChange(action, targetPage, fromPage);
                }
                break;
            case EXIT_USB_FOLDER_VIEW:
                isRootPath = true;
                //这里要判断是不是在播放页
                if (!isPlayPage) {
                    listener.onActionChange(action, targetPage, fromPage);
                }
                break;
            case TO_USB_FOLDER_VIEW:
                isRootPath = false;
                listener.onActionChange(action, targetPage, fromPage);
                break;
            default:
                listener.onActionChange(action, targetPage, fromPage);
                break;
        }
    }

    @Override
    public void onActionChange(FragmentAction action, int targetPage, int fromPage, Object data) {
        Log.d(TAG, "onActionChange: action = " + action.name());
        listener.onActionChange(action, targetPage, fromPage, data);
    }

    /**
     * 切换到对应的页面
     */
    private void toPage() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.w(TAG, "toPage: arguments is null");
            return;
        }
        //默认去全部列表页
        int page = arguments.getInt(MusicMainFragment.ARG_TO_PAGE_KEY, LOCAL_LIST_PAGE_POSITION);
        int from = arguments.getInt(MusicMainFragment.ARG_FROM_PAGE_KEY, 0);
        int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());
        Log.d(TAG, "toPage: page = " + page + " mediaType = " + mediaType);
        //这里还是需注意的是，如果当前是最近播放，但是音源是USB0或者本地，则需回到最近播放列表
        switch (page) {
            case LOCAL_LIST_PAGE_POSITION:
            case USB0_LIST_PAGE_POSITION:
                if (MediaType.RECENT_MUSIC.ordinal() == mediaType) {
                    page = RECENT_LIST_PAGE_POSITION;
                }
            case RECENT_LIST_PAGE_POSITION:
                onActionChange(EXIT_PLAY_FRAGMENT, page, from);
                break;
            case LOCAL_PLAY_PAGE_POSITION:
            case USB0_PLAY_PAGE_POSITION:
                if (MediaType.RECENT_MUSIC.ordinal() == mediaType) {
                    page = RECENT_PLAY_PAGE_POSITION;
                }
            case RECENT_PLAY_PAGE_POSITION:
                onActionChange(TO_PLAY_FRAGMENT, page, from);
                break;
        }
        setArguments(null);
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
        /*//如果没添加则添加到栈当中
        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_container_view, fragment);
        }
        //当前打开的是播放页，则隐藏home
        if (fragment != musicListHomeFragment) {
            transaction.hide(musicListHomeFragment);
        }
        //当前打开的是home页，则隐藏播放页
        if (fragment != musicPlayHomeFragment) {
            transaction.hide(musicPlayHomeFragment);
        }
        transaction.show(fragment);*/
        //transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.replace(R.id.fragment_container_view, fragment);
        transaction.commitNow();
        fragment.onFragmentNewIntent();
    }
}
