package com.desaysv.moduleradio.ui;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.radio.RadioManager;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.textclassifier.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.dialog.DABAnnSettingsDialog;
import com.desaysv.moduledab.dialog.DABSFSettingsDialog;
import com.desaysv.moduledab.fragment.DABPlayFragment;
import com.desaysv.moduledab.fragment.EPGFragment;
import com.desaysv.moduledab.fragment.list.DABListAllFragment;
import com.desaysv.moduledab.fragment.DABListCollectFragment;
import com.desaysv.moduledab.fragment.list.DABListEnsembleFragment;
import com.desaysv.moduledab.fragment.list.DABListPlayFragment;
import com.desaysv.moduledab.fragment.list.DABListTypeFragment;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioFragmentAdapter;
import com.desaysv.moduleradio.adapter.RadioFragmentListAdapter;
import com.desaysv.moduleradio.adapter.RadioPlayListAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.ui.list.AMCollectListFragment;
import com.desaysv.moduleradio.ui.list.AMListFragment;
import com.desaysv.moduleradio.ui.list.FMCollectListFragment;
import com.desaysv.moduleradio.ui.list.FMListFragment;
import com.desaysv.moduleradio.view.NoSmoothViewPager;
import com.desaysv.moduleradio.view.RDSSettingsDialog;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RadioPlayFragment extends BaseFragment implements View.OnClickListener,RadioPlayListAdapter.OnItemClickListener {

    private boolean hasDAB = false;
    private NoSmoothViewPager vpRadioMain;
    private TabLayout tbRadioMain;
    private DABPlayFragment dabFragment;
    private FMFragment fmFragment;
    private AMFragment amFragment;

    private int currentTab;

    private int currentList = -1;

    private MyHandler mHandler;
    //收音的控制器
    protected IControlTool mRadioControl;

    //获取收音控制器和状态获取器的对象
    protected IStatusTool mGetRadioStatusTool;

    //收音的状态获取器
    protected IGetControlTool mGetControlTool;

    private ImageView ivRadioList;
    private ImageView ivRadioScan;
    private ImageView ivRadioPre;
    private ImageView ivRadioPlay;
    private ImageView ivRadioNext;
    //rds
    private ImageView ivRadioRt;
    private ImageView ivRadioRDS;
    //dab
    private ImageView ivRadioMore;

    //以下为播放列表相关
    private RelativeLayout rlPlayList;
    private TabLayout tbRadioList;
    private RadioFragmentListAdapter fragmentListAdapter;
    private NoSmoothViewPager vpRadioList;
    //以上为播放列表相关

    private RDSSettingsDialog rdsSettingsDialog;

    private DABAnnSettingsDialog dabAnnSettingsDialog;

    private DABSFSettingsDialog dabSFSettingsDialog;

    private RelativeLayout rlAll;

    @Override
    public int getLayoutResID() {
        return R.layout.radio_fragment_main;
    }

    @Override
    public void initView(View view) {
        Log.d(TAG, "initView");

        hasDAB = ProductUtils.hasDAB();
        mHandler = new MyHandler(this);

        mRadioControl = ModuleRadioTrigger.getInstance().mRadioControl;
        mGetRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        mGetControlTool = ModuleRadioTrigger.getInstance().mGetControlTool;

        //fragment里面嵌套fragment，所以是child
        FragmentManager fragmentManager = getChildFragmentManager();

        tbRadioMain = view.findViewById(R.id.tbRadioMain);
        vpRadioMain = view.findViewById(R.id.vpRadioMain);

        ivRadioList = view.findViewById(R.id.ivRadioList);
        ivRadioScan = view.findViewById(R.id.ivRadioScan);
        ivRadioPre = view.findViewById(R.id.ivRadioPre);
        ivRadioPlay = view.findViewById(R.id.ivRadioPlay);
        ivRadioNext = view.findViewById(R.id.ivRadioNext);
        //rds
        ivRadioRt = view.findViewById(R.id.ivRadioRt);
        ivRadioRDS = view.findViewById(R.id.ivRadioRDS);
        //dab
        ivRadioMore = view.findViewById(R.id.ivRadioMore);
        //播放列表
        rlPlayList = view.findViewById(R.id.rlPlayList);

        rdsSettingsDialog = new RDSSettingsDialog(getContext(), R.style.radio_dialogstyle);

        dabAnnSettingsDialog = new DABAnnSettingsDialog(getContext(), R.style.radio_dialogstyle);

        dabSFSettingsDialog = new DABSFSettingsDialog(getContext(), R.style.radio_dialogstyle);

        rlAll = view.findViewById(R.id.rlAll);

        tbRadioList = view.findViewById(R.id.tbRadioList);
        vpRadioList = view.findViewById(R.id.vpRadioList);
        fragmentListAdapter = new RadioFragmentListAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        tbRadioList.setupWithViewPager(vpRadioList, true);
        vpRadioList.setAdapter(fragmentListAdapter);
        RadioFragmentAdapter mainFragmentAdapter = new RadioFragmentAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        //这一部分很重要，用于重载之后，从系统保存的对象取出 fragment(例如白天黑夜切换)
        //否则因为系统默认恢复的是保存的fragment，走的是恢复对象的生命周期，
        //而这里又new了新的fragment对象，就会导致new的对象里面的参数实际并没有初始化，因为new这个fragment不会被add进去，不会执行相应的生命周期
        //就会导致各种空指针异常
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null || fragments.size() == 0) {
            if (hasDAB) {
                dabFragment = new DABPlayFragment();
            }
            fmFragment = new FMFragment();
            amFragment = new AMFragment();

        } else {
            for (Fragment fragment : fragments) {
                Log.d(TAG,"fragments test");
                if (fragment instanceof DABPlayFragment) {
                    dabFragment = (DABPlayFragment) fragment;
                } else if (fragment instanceof FMFragment) {
                    fmFragment = (FMFragment) fragment;
                } else if (fragment instanceof AMFragment) {
                    amFragment = (AMFragment) fragment;
                }
            }
            //未执行到的fragment不会被保存，所以需要进行判空
            if (dabFragment == null){
                if (hasDAB) {
                    dabFragment = new DABPlayFragment();
                }
            }
            if (fmFragment == null){
                fmFragment = new FMFragment();
            }
            if (amFragment == null){
                amFragment = new AMFragment();
            }

        }
        mainFragmentAdapter.setFragments(fmFragment,amFragment);
        if (hasDAB) {
            mainFragmentAdapter.setDABFragment(dabFragment);
            mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio_dab));
        } else {
            mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio));
        }
        tbRadioMain.setupWithViewPager(vpRadioMain, true);

        vpRadioMain.setAdapter(mainFragmentAdapter);

        for (int i = 0; i < tbRadioMain.getTabCount(); i++) {
            TabLayout.Tab tab = tbRadioMain.getTabAt(i);
            if (tab != null) {
                tab.view.setLongClickable(false);
                tab.view.setTooltipText(null);
            }
        }
    }

    @Override
    public void initData() {
        Log.d(TAG, "initData,currentTab:"+currentTab);
        RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "initData: currentRadioMessage = " + currentRadioMessage);

        if (currentTab == 0) {//不等于0 表示这个值已经改变过了，表示有其他地方设置了，需要优先考虑
            if (currentRadioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
                currentTab = RadioConstants.TABWithDAB.POSITION_DAB;
            } else {
                int radioBand = currentRadioMessage.getRadioBand();
                if (radioBand == RadioManager.BAND_FM || radioBand == RadioManager.BAND_FM_HD) {
                    if (hasDAB) {
                        currentTab = RadioConstants.TABWithDAB.POSITION_FM;
                    }else {
                        currentTab = RadioConstants.TABWithoutDAB.POSITION_FM;
                    }
                } else {
                    if (hasDAB) {
                        currentTab = RadioConstants.TABWithDAB.POSITION_AM;
                    }else {
                        currentTab = RadioConstants.TABWithoutDAB.POSITION_AM;
                    }
                }
            }
        }
        vpRadioMain.setCurrentItem(currentTab, false);
        updateViewWithCurrentTab();
        Log.d(TAG, "initData: end");
    }

    @Override
    public void initViewListener() {
        ivRadioList.setOnClickListener(this);
        ivRadioScan.setOnClickListener(this);
        ivRadioPre.setOnClickListener(this);
        ivRadioPlay.setOnClickListener(this);
        ivRadioNext.setOnClickListener(this);
        //rds
        ivRadioRt.setOnClickListener(this);
        ivRadioRDS.setOnClickListener(this);
        //dab
        ivRadioMore.setOnClickListener(this);

        vpRadioMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected");
                currentTab = position;
                if (hasDAB){
                    if (position == RadioConstants.TABWithDAB.POSITION_FM){
                        mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, mGetRadioStatusTool.getFMRadioMessage());
                    }else if (position == RadioConstants.TABWithDAB.POSITION_AM){
                        mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, mGetRadioStatusTool.getAMRadioMessage());
                    }else if (position == RadioConstants.TABWithDAB.POSITION_DAB){
                        mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, mGetRadioStatusTool.getDABRadioMessage());
                    }
                }else {
                    if (position == RadioConstants.TABWithoutDAB.POSITION_FM){
                        mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, mGetRadioStatusTool.getFMRadioMessage());
                    }else if (position == RadioConstants.TABWithoutDAB.POSITION_AM){
                        mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, mGetRadioStatusTool.getAMRadioMessage());
                    }
                }

                updateViewWithCurrentTab();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged");
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        updateCurrentInfo();
        updatePlayStatues();
        updateList();
        if (currentList != -1){
            openListWithVR(currentList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePlayStatues();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateSearchStatues(false);
        mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
    }

    @Override
    public void onStop() {
        super.onStop();
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
    }


    IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG, "onFMCollectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST);
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG, "onAMCollectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST);
        }

        @Override
        public void onDABCollectListChange() {

        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG, "onFMEffectListChange: ");
//            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG, "onAMEffectListChange: ");
//            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_AM_LIST);
        }

        @Override
        public void onDABEffectListChange() {

        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG, "onFMAllListChange: ");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG, "onAMAllListChange: ");
        }

        @Override
        public void onDABAllListChange() {

        }

        @Override
        public void onFMPlayListChange() {
            android.util.Log.d(TAG, "onFMPlayListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
        }

        @Override
        public void onAMPlayListChange() {
            android.util.Log.d(TAG, "onAMPlayListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_AM_LIST);
        }

        @Override
        public void onDABPlayListChange() {
            android.util.Log.d(TAG, "onDABPlayListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
        }
    };

    IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG, "onCurrentRadioMessageChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS);
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG, "onAstListChanged: ");
            if (band == RadioManager.BAND_FM) {
                mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
            } else if (band == RadioManager.BAND_AM) {
                mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_AM_LIST);
            }
        }

        @Override
        public void onSearchStatusChange(final boolean isSearching) {
            Log.d(TAG, "onSearchStatusChange: isSearching = " + isSearching);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS, isSearching));
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG, "onSeekStatusChange: isSeeking = " + isSeeking);
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {

        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG, "onRDSFlagInfoChange: RDSFlagInfo = " + info);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_RDS_FLAG, info));
        }

        @Override
        public void onRDSSettingsStatus(RDSSettingsSwitch rdsSettingsSwitch) {
            Log.d(TAG, "onRDSSettingsStatus: rdsSettingsSwitch = " + rdsSettingsSwitch);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RDS_SETTINGS, rdsSettingsSwitch));
        }
    };

    /**
     * 根据当前Tab，更新底部控制栏
     */
    public void updateViewWithCurrentTab(){
        if (hasDAB){
            ivRadioPre.setLongClickable(true);
            ivRadioNext.setLongClickable(true);
            if(currentTab == RadioConstants.TABWithDAB.POSITION_FM){//FM
                ivRadioRt.setVisibility(View.GONE);
                ivRadioRDS.setVisibility(View.VISIBLE);
                ivRadioMore.setVisibility(View.GONE);
            }else if (currentTab == RadioConstants.TABWithDAB.POSITION_AM){//AM
                ivRadioRt.setVisibility(View.GONE);
                ivRadioRDS.setVisibility(View.GONE);
                ivRadioMore.setVisibility(View.GONE);
            }else if (currentTab == RadioConstants.TABWithDAB.POSITION_DAB){//DAB
                ivRadioRt.setVisibility(View.GONE);
                ivRadioRDS.setVisibility(View.GONE);
                ivRadioMore.setVisibility(View.VISIBLE);
                ivRadioPre.setLongClickable(false);
                ivRadioNext.setLongClickable(false);
            }
        }else {
            ivRadioPre.setLongClickable(true);
            ivRadioNext.setLongClickable(true);
            if(currentTab == RadioConstants.TABWithoutDAB.POSITION_FM){//FM
                ivRadioRt.setVisibility(View.GONE);
                ivRadioRDS.setVisibility(View.VISIBLE);
                ivRadioMore.setVisibility(View.GONE);
            }else if (currentTab == RadioConstants.TABWithoutDAB.POSITION_AM){//AM
                ivRadioRt.setVisibility(View.GONE);
                ivRadioRDS.setVisibility(View.GONE);
                ivRadioMore.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 更新当前播放信息，由子类实现
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateCurrentInfo() {
        initData();
    }

    /**
     * 更新播放状态
     */
    public void updatePlayStatues() {
        ivRadioPlay.setSelected(!mGetRadioStatusTool.isPlaying());
    }

    /**
     * 根据搜索状态更新界面
     *
     * @param isSearching isSearching
     */
    public void updateSearchStatues(boolean isSearching) {
        Log.d(TAG,"updateSearchStatues:"+isSearching);
        updatePlayStatues();
        if (isSearching) {
            ivRadioScan.setSelected(true);
            ivRadioList.setEnabled(false);
            ivRadioPre.setEnabled(false);
            ivRadioPlay.setEnabled(false);
            ivRadioNext.setEnabled(false);
            ivRadioRt.setEnabled(false);
            ivRadioRDS.setEnabled(false);
        } else {
            ivRadioScan.setSelected(false);
            ivRadioList.setEnabled(true);
            ivRadioPre.setEnabled(true);
            ivRadioPlay.setEnabled(true);
            ivRadioNext.setEnabled(true);
            ivRadioRt.setEnabled(true);
            ivRadioRDS.setEnabled(true);
        }
    }

    /**
     * 更新列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList() {
        //不是列表界面
        if (!ivRadioList.isSelected()){
            return;
        }
        //主要是更新title
        if (hasDAB){
            if (currentTab == RadioConstants.TABWithDAB.POSITION_FM){
                String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getFMEffectRadioMessageList().size())
                        ,getResources().getString(R.string.radio_collect_list)};
                fragmentListAdapter.setTabTitles(titles);

            }else if (currentTab == RadioConstants.TABWithDAB.POSITION_AM){
                String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getAMEffectRadioMessageList().size())
                        ,getResources().getString(R.string.radio_collect_list)};
                fragmentListAdapter.setTabTitles(titles);
            }else if (currentTab == RadioConstants.TABWithDAB.POSITION_DAB){
                String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getDABEffectRadioMessageList().size())
                        ,getResources().getString(R.string.radio_collect_list),getResources().getString(R.string.radio_all_list)
                        ,getResources().getString(R.string.radio_type_list),getResources().getString(R.string.radio_ctg_list)};
                fragmentListAdapter.setTabTitles(titles);
            }
        }else {
            if (currentTab == RadioConstants.TABWithoutDAB.POSITION_FM){
                String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getFMEffectRadioMessageList().size())
                        ,getResources().getString(R.string.radio_collect_list)};
                fragmentListAdapter.setTabTitles(titles);
            }else if (currentTab == RadioConstants.TABWithoutDAB.POSITION_AM){
                String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getAMEffectRadioMessageList().size())
                        ,getResources().getString(R.string.radio_collect_list)};
                fragmentListAdapter.setTabTitles(titles);
            }
        }
        fragmentListAdapter.notifyDataSetChanged();
    }

    /**
     * 根据Tab，显示当前列表
     * @param show
     */
    public void showCurrentList(boolean show){
        Log.d(TAG,"showCurrentList,show: "+show);
        tbRadioList.setTabMode(TabLayout.MODE_AUTO);
        if (show) {
            rlAll.setBackgroundResource(R.drawable.radio_play_list_bg);
            rlPlayList.setVisibility(View.VISIBLE);
            ivRadioList.setSelected(true);
            if (hasDAB){
                if (currentTab == RadioConstants.TABWithDAB.POSITION_FM){
                    String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getFMEffectRadioMessageList().size())
                            ,getResources().getString(R.string.radio_collect_list)};
                    List<BaseFragment> list = new ArrayList<>();
                    list.add(new FMListFragment());
                    list.add(new FMCollectListFragment());
                    fragmentListAdapter.setTabTitles(titles);
                    fragmentListAdapter.setFragments(list);

                }else if (currentTab == RadioConstants.TABWithDAB.POSITION_AM){
                    String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getAMEffectRadioMessageList().size())
                            ,getResources().getString(R.string.radio_collect_list)};
                    List<BaseFragment> list = new ArrayList<>();
                    list.add(new AMListFragment());
                    list.add(new AMCollectListFragment());
                    fragmentListAdapter.setTabTitles(titles);
                    fragmentListAdapter.setFragments(list);
                }else if (currentTab == RadioConstants.TABWithDAB.POSITION_DAB){
                    tbRadioList.setTabMode(TabLayout.MODE_SCROLLABLE);
                    String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getDABEffectRadioMessageList().size())
                            ,getResources().getString(R.string.radio_collect_list),getResources().getString(R.string.radio_all_list)
                            ,getResources().getString(R.string.radio_type_list),getResources().getString(R.string.radio_ctg_list)};
                    List<BaseFragment> list = new ArrayList<>();

                    list.add(new DABListPlayFragment());
                    list.add(new DABListCollectFragment());
                    list.add(new DABListAllFragment());
                    list.add(new DABListEnsembleFragment());
                    list.add(new DABListTypeFragment());
                    fragmentListAdapter.setTabTitles(titles);
                    fragmentListAdapter.setFragments(list);
                }
            }else {
                if (currentTab == RadioConstants.TABWithoutDAB.POSITION_FM){
                    String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getFMEffectRadioMessageList().size())
                            ,getResources().getString(R.string.radio_collect_list)};
                    List<BaseFragment> list = new ArrayList<>();
                    list.add(new FMListFragment());
                    list.add(new FMCollectListFragment());
                    fragmentListAdapter.setTabTitles(titles);
                    fragmentListAdapter.setFragments(list);
                }else if (currentTab == RadioConstants.TABWithoutDAB.POSITION_AM){
                    String[] titles = new String[]{String.format(getResources().getString(R.string.radio_playlist_title2), RadioList.getInstance().getAMEffectRadioMessageList().size())
                            ,getResources().getString(R.string.radio_collect_list)};
                    List<BaseFragment> list = new ArrayList<>();
                    list.add(new AMListFragment());
                    list.add(new AMCollectListFragment());
                    fragmentListAdapter.setTabTitles(titles);
                    fragmentListAdapter.setFragments(list);
                }
            }

            for (int i = 0; i < tbRadioList.getTabCount(); i++) {
                TabLayout.Tab tab = tbRadioList.getTabAt(i);
                if (tab != null) {
                    tab.view.setLongClickable(false);
                    tab.view.setTooltipText(null);
                }
            }
        }else {
            rlPlayList.setVisibility(View.GONE);
            vpRadioList.removeAllViews();
            ivRadioList.setSelected(false);
            rlAll.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivRadioList){
            Log.d(TAG,"onClick ivRadioList");
            v.setSelected(!v.isSelected());
            showCurrentList(v.isSelected());
        }else if (id == R.id.ivRadioScan){
            if (mGetRadioStatusTool.isSearching()){
                mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.CLICK);
            }else {
                mRadioControl.processCommand(RadioAction.AST, ChangeReasonData.CLICK);
            }
        }else if (id == R.id.ivRadioPre){
            mRadioControl.processCommand(RadioAction.SEEK_BACKWARD, ChangeReasonData.CLICK);
        }else if (id == R.id.ivRadioPlay){
            mRadioControl.processCommand(RadioAction.PLAY_OR_PAUSE, ChangeReasonData.CLICK);
        }else if (id == R.id.ivRadioNext){
            mRadioControl.processCommand(RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
        }else if (id == R.id.ivRadioRt){
            //todo showRadioText
        }else if (id == R.id.ivRadioRDS){
            rdsSettingsDialog.show();
        }else if (id == R.id.ivRadioMore){
            showMoreIcon();
        }else if (id == R.id.rlDABAnnSettings){
            dabAnnSettingsDialog.show();
            windowManager.removeViewImmediate(viewMore);
        }else if (id == R.id.rlDABSet){
            dabSFSettingsDialog.show();
            windowManager.removeViewImmediate(viewMore);
        }else if (id == R.id.rlDABEPG){
            windowManager.removeViewImmediate(viewMore);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flEPG, new EPGFragment());
            transaction.commit();
        }
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG,"onItemClick,position:"+position);
        showCurrentList(false);
    }


    //设计增加了一个More的弹窗
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View viewMore;
    private RelativeLayout rlDABAnnSettings;//进入DAB SF的设置项
    private RelativeLayout rlDABEPG;
    private RelativeLayout rlDABSet;//
    private void showMoreIcon(){
        if (windowManager == null){
            windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            viewMore = inflater.inflate(R.layout.dab_more,null);
            rlDABAnnSettings = viewMore.findViewById(R.id.rlDABAnnSettings);
            rlDABEPG = viewMore.findViewById(R.id.rlDABEPG);
            rlDABSet = viewMore.findViewById(R.id.rlDABSet);
            rlDABAnnSettings.setOnClickListener(this);
            rlDABEPG.setOnClickListener(this);
            rlDABSet.setOnClickListener(this);
            viewMore.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    windowManager.removeViewImmediate(viewMore);
                    return false;
                }
            });
        }
        if (!viewMore.isAttachedToWindow()) {//如果在显示期间有新的弹窗，会自动更新内容，不需要继续add，否则会出现 “has already been added to the window manager” 的 RuntimeException
            windowManager.addView(viewMore, layoutParams);
        }
    }


    private static class MyHandler extends Handler {
        WeakReference<RadioPlayFragment> weakReference;

        MyHandler(RadioPlayFragment radioPlayFragment) {
            weakReference = new WeakReference<>(radioPlayFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final RadioPlayFragment radioPlayFragment = weakReference.get();
            Log.d(radioPlayFragment.TAG, "handleMessage:" + msg.what);
            if (radioPlayFragment.isDetached()){
                Log.d(radioPlayFragment.TAG, "radioPlayFragment.isDetach");
                return;
            }
            switch (msg.what) {
                case RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE:
                    radioPlayFragment.updateCurrentInfo();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS:
                    radioPlayFragment.updatePlayStatues();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_FM_LIST:
                //case RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST:
                case RadioConstants.MSG_UPDATE_RADIO_AM_LIST:
                    radioPlayFragment.updateList();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS:
                    radioPlayFragment.updateSearchStatues((Boolean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 响应语音打开对应Band的操作
      * @param navigationFlag
     */
   public void openBandWithVR(int navigationFlag){
       Log.d(TAG,"openBandWithVR: " + navigationFlag);
        if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_FM){//打开FM
            if (hasDAB) {
                currentTab = RadioConstants.TABWithDAB.POSITION_FM;
                vpRadioMain.setCurrentItem(RadioConstants.TABWithDAB.POSITION_FM);
            }else {
                currentTab = RadioConstants.TABWithoutDAB.POSITION_FM;
                vpRadioMain.setCurrentItem(RadioConstants.TABWithoutDAB.POSITION_FM);
            }
        }else if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_AM){//打开AM
            if (hasDAB) {
                currentTab = RadioConstants.TABWithDAB.POSITION_AM;
                vpRadioMain.setCurrentItem(RadioConstants.TABWithDAB.POSITION_AM);
            }else {
                currentTab = RadioConstants.TABWithoutDAB.POSITION_AM;
                vpRadioMain.setCurrentItem(RadioConstants.TABWithoutDAB.POSITION_AM);
            }
        }else if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_DAB){//打开AM
            if (hasDAB) {
                currentTab = RadioConstants.TABWithDAB.POSITION_DAB;
                vpRadioMain.setCurrentItem(RadioConstants.TABWithDAB.POSITION_DAB);
            }else {
                //do nothing
            }
        }else if (navigationFlag == Constants.NavigationFlag.FLAG_PLAY){//在这里表示的是Mode切换
            currentTab = 0;
        }
   }

    /**
     * 如果没拉起过Activity，需要走这里响应语音
     * @param navigationFlag
     */
    public void setBandWithVR(int navigationFlag){
        Log.d(TAG,"setBandWithVR: " + navigationFlag);
        if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_FM){//打开FM
            if (hasDAB) {
                currentTab = RadioConstants.TABWithDAB.POSITION_FM;
            }else {
                currentTab = RadioConstants.TABWithoutDAB.POSITION_FM;
            }
        }else if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_AM){//打开AM
            if (hasDAB) {
                currentTab = RadioConstants.TABWithDAB.POSITION_AM;
            }else {
                currentTab = RadioConstants.TABWithoutDAB.POSITION_AM;
            }
        }else if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_DAB){//打开AM
            if (hasDAB) {
                currentTab = RadioConstants.TABWithDAB.POSITION_DAB;
            }else {
                //do nothing
            }
        }else if (navigationFlag == Constants.NavigationFlag.FLAG_PLAY){//在这里表示的是Mode切换
            currentTab = 0;
        }
    }

   public void openListWithVR(int navigationFlag){
       Log.d(TAG,"openListWithVR: " + navigationFlag);
       if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_LIST){//打开播放列表
           if (ivRadioList.isSelected()) {//已经打开
           }else {
               ivRadioList.performClick();
           }
           vpRadioList.setCurrentItem(0);

       }else if (navigationFlag == Constants.NavigationFlag.FLAG_MAIN){//打开主页或者关闭播放列表
           if (ivRadioList.isSelected()) {//已经打开
               ivRadioList.performClick();
           }else {
           }
       }else if (navigationFlag == Constants.NavigationFlag.FLAG_VR_MAIN_COLLECT){//打开收藏列表
           if (ivRadioList.isSelected()) {//已经打开
           }else {
               ivRadioList.performClick();
           }
           vpRadioList.setCurrentItem(1);
       }
       currentList = -1;
   }

    /**
     * 如果没拉起过Activity，需要走这里响应语音
     * @param navigationFlag
     */
    public void setListWithVR(int navigationFlag){
        Log.d(TAG,"setListWithVR: " + navigationFlag);
        currentList = navigationFlag;
    }



















    /**
     * 对于 T1EJ国际 来讲，这个没有使用，只是为了编译
     * 左上角返回按钮的点击事件
     */
    private IOnBackClickListener backClickListener;

    public void setBackClickListener(IOnBackClickListener backClickListener) {
        this.backClickListener = backClickListener;
    }

    public interface IOnBackClickListener {

        void onBackClick();
    }
}
