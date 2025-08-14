package com.desaysv.moduleradio.ui;

import android.hardware.radio.RadioManager;
import android.view.View;
import android.view.textclassifier.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.fragment.DABFragment;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioMainFragmentAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.vr.SVRadioVRStateUtil;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * created by ZNB on 2022-10-26
 * Radio模块主界面，通过 Tab + ViewPager + Fragment 的形式，
 * 嵌入DAB、FM、AM、收藏等几个 Fragment
 * T1ej国际没有使用该文件
 */
public class RadioHomeFragment extends BaseFragment implements RadioBaseListFragment.IOnItemClickListener, DABFragment.IOnDABItemClickListener {

    private ViewPager vpRadioMain;

    //定义Tab对应的Fragment
    private FMListFragment fmListFragment;
    private AMListFragment amListFragment;
    private DABFragment dabFragment;
    private CollectListFragment collectListFragment;


    private int currentTab = 0;

    private boolean hasDAB = false;//用本地变量获取一次就好了，避免多次获取

    @Override
    public int getLayoutResID() {
        return R.layout.radio_fragment_main;
    }

    @Override
    public void initView(View view) {
        Log.d(TAG, "initView");

        hasDAB = ProductUtils.hasDAB();

        //fragment里面嵌套fragment，所以是child
        FragmentManager fragmentManager = getChildFragmentManager();

        TabLayout tbRadioMain = view.findViewById(R.id.tbRadioMain);
        vpRadioMain = view.findViewById(R.id.vpRadioMain);

        RadioMainFragmentAdapter mainFragmentAdapter = new RadioMainFragmentAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        //这一部分很重要，用于重载之后，从系统保存的对象取出 fragment(例如白天黑夜切换)
        //否则因为系统默认恢复的是保存的fragment，走的是恢复对象的生命周期，
        //而这里又new了新的fragment对象，就会导致new的对象里面的参数实际并没有初始化，因为new这个fragment不会被add进去，不会执行相应的生命周期
        //就会导致各种空指针异常
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null || fragments.size() == 0) {
            if (hasDAB) {
                dabFragment = new DABFragment();
            }
            fmListFragment = new FMListFragment();
            amListFragment = new AMListFragment();
            collectListFragment = new CollectListFragment();
        } else {
            for (Fragment fragment : fragments) {
                Log.d(TAG,"fragments test");
                if (fragment instanceof DABFragment) {
                    dabFragment = (DABFragment) fragment;
                } else if (fragment instanceof FMListFragment) {
                    fmListFragment = (FMListFragment) fragment;
                } else if (fragment instanceof AMListFragment) {
                    amListFragment = (AMListFragment) fragment;
                } else if (fragment instanceof CollectListFragment) {
                    collectListFragment = (CollectListFragment) fragment;
                }
            }
            //未执行到的fragment不会被保存，所以需要进行判空
            if (dabFragment == null){
                if (hasDAB) {
                    dabFragment = new DABFragment();
                }
            }
            if (fmListFragment == null){
                fmListFragment = new FMListFragment();
            }
            if (amListFragment == null){
                amListFragment = new AMListFragment();
            }
            if (collectListFragment == null){
                collectListFragment = new CollectListFragment();
            }
        }
        if (hasDAB) {
            dabFragment.setOnDABItemClickListener(this);
        }
        fmListFragment.setOnItemClickListener(this);
        amListFragment.setOnItemClickListener(this);
        collectListFragment.setOnItemClickListener(this);

        mainFragmentAdapter.setFragments(fmListFragment, amListFragment, collectListFragment);
        if (hasDAB) {
            mainFragmentAdapter.setDABFragment(dabFragment);
            mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio_dab));
        } else {
            mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio));
        }
        tbRadioMain.setupWithViewPager(vpRadioMain, true);
        vpRadioMain.setAdapter(mainFragmentAdapter);
    }

    //用于进入应用时，自动跳转对应界面
    public void setCurrentTab(int currentTab) {
        Log.d(TAG, "setCurrentTab:" + currentTab);
        this.currentTab = currentTab;
        SVRadioVRStateUtil.getInstance().setCurrentTab(currentTab);
    }

    @Override
    public void initData() {
        Log.d(TAG, "initData,currentTab:"+currentTab);
        RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "initData: currentRadioMessage = " + currentRadioMessage);

        if (currentTab == 0) {//不等于0 表示这个值已经改变过了，表示有其他地方设置了，需要优先考虑
            if (currentRadioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
                setCurrentTab(RadioConstants.TABWithDAB.POSITION_DAB);
            } else {
                int radioBand = currentRadioMessage.getRadioBand();
                if (radioBand == RadioManager.BAND_FM || radioBand == RadioManager.BAND_FM_HD) {
                    if (hasDAB) {
                        setCurrentTab(RadioConstants.TABWithDAB.POSITION_FM);
                    } else {
                        setCurrentTab(RadioConstants.TABWithoutDAB.POSITION_FM);
                    }
                } else {
                    if (hasDAB) {
                        setCurrentTab(RadioConstants.TABWithDAB.POSITION_AM);
                    } else {
                        setCurrentTab(RadioConstants.TABWithoutDAB.POSITION_AM);
                    }
                }
            }
        }
        vpRadioMain.setCurrentItem(currentTab, false);
        Log.d(TAG, "initData: end");
    }

    @Override
    public void initViewListener() {
        Log.d(TAG, "initViewListener");
        vpRadioMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: position = " + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected,position: " + position);
                currentTab = position;
                SVRadioVRStateUtil.getInstance().setCurrentTab(currentTab);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged: state = " + state);
            }
        });
        Log.d(TAG, "initViewListener: end");
    }

    @Override
    public void onItemCLick() {
        Log.d(TAG, "onItemCLick: to play page ");
        onItemClickListener.onItemCLick(Constants.NavigationFlag.FLAG_PLAY);
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
    }

    @Override
    public void onItemCLickDAB() {
        Log.d(TAG, "onItemCLickDAB: to play page ");
        onItemClickListener.onItemCLick(Constants.NavigationFlag.FLAG_DAB_PLAY);
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
    }

    @Override
    public void onDABItemCLick() {
        Log.d(TAG, "onItemCLick: to play page  or  dab ");
        onItemClickListener.onItemCLick(Constants.NavigationFlag.FLAG_DAB_PLAY);
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
    }

    @Override
    public void onFragmentNewIntent() {
        super.onFragmentNewIntent();
        if (isAdded() && vpRadioMain != null) {
            vpRadioMain.setCurrentItem(currentTab, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vpRadioMain != null) {
            vpRadioMain.setCurrentItem(currentTab, false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
    }

    /**
     * 列表项点击事件，透传给主Fragment进行界面替换
     */
    protected IOnItemClickListener onItemClickListener;

    public void setOnItemClickListener(IOnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface IOnItemClickListener {
        void onItemCLick(int flag);
    }
}
