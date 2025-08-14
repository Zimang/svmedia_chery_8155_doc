package com.desaysv.moduleradio.ui;

import android.content.Context;
import android.hardware.radio.RadioManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduledab.fragment.DABEPGFragment;
import com.desaysv.moduledab.fragment.DABListMainFragment;
import com.desaysv.moduledab.fragment.DABPlayFragment;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.vr.SVRadioVRControl;

public class RadioMainFragment extends BaseFragment implements RadioHomeFragment.IOnItemClickListener, RadioPlayFragment.IOnBackClickListener, DABPlayFragment.IOnBackClickListener,DABListMainFragment.IOnBackClickListener,DABEPGFragment.IOnBackClickListener, MultiPlayFragment.IOnBackClickListener {

    private RadioHomeFragment radioHomeFragment;
    private RadioPlayFragment radioPlayFragment;

    private MultiPlayFragment multiPlayFragment;
    private DABPlayFragment dabPlayFragment;
    private DABListMainFragment dabListMainFragment;
    private FragmentManager fragmentManager;
    private DABEPGFragment dabepgFragment;
    //启动到首页或者播放页的标志
    private int navigation = Constants.NavigationFlag.FLAG_MAIN;

    private boolean hasDAB = false;//用本地变量获取一次就好了，避免多次获取

    private boolean hasAM = false;//用本地变量获取一次就好了，避免多次获取

    private boolean hasMulti = false;//用本地变量获取一次就好了，避免多次获取

    @Override
    public int getLayoutResID() {
        return R.layout.radio_main;
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void initData() {
        init();
    }

    @Override
    public void initViewListener() {

    }

    //设置当前需要跳转的接口
    public void setNavigation(int navigation) {
        this.navigation = navigation;
        MusicSetting.getInstance().putInt(RadioConstants.RADIO_PAGE, navigation);
    }

    @Override
    public void onStart() {
        super.onStart();
        SVRadioVRControl.handleRequestStatus(true);
    }

    @Override
    public void onFragmentNewIntent() {
        super.onFragmentNewIntent();
        if (isAdded() && fragmentManager !=null) {
            setTargetFragment(navigation);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTargetFragment(navigation);
    }

    @Override
    public void onStop() {
        super.onStop();
        SVRadioVRControl.handleRequestStatus(false);
    }

    public void init() {
        radioHomeFragment = new RadioHomeFragment();
        radioPlayFragment = new RadioPlayFragment();
        dabPlayFragment = new DABPlayFragment();
        dabListMainFragment = new DABListMainFragment();
        dabepgFragment = new DABEPGFragment();
        multiPlayFragment = new MultiPlayFragment();
        dabListMainFragment.setBackClickListener(this);
        dabepgFragment.setBackClickListener(this);
        dabPlayFragment.setBackClickListener(this);
        radioHomeFragment.setOnItemClickListener(this);
        radioPlayFragment.setBackClickListener(this);
        multiPlayFragment.setBackClickListener(this);
        fragmentManager = getChildFragmentManager();
        hasDAB = ProductUtils.hasDAB();
        hasAM = ProductUtils.hasAM();
        hasMulti = ProductUtils.hasMulti();
    }


    public void setTargetFragment(int flag) {
        Log.d(TAG, "setTargetFragment: " + flag);
        setNavigation(flag);
        fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentPreAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
                super.onFragmentPreAttached(fm, f, context);
                if (f instanceof RadioHomeFragment) {
                    iGotoRadioPlayListener.gotoPlayPage(false);//T19C的播放界面需要做成沉浸式，会导致切换回来时，闪现界面，因此在这里打个时间差，让沉浸式切换回来后再显示
                }
            }

            @Override
            public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
                super.onFragmentAttached(fm, f, context);
                if (f instanceof RadioHomeFragment) {
                    iGotoRadioPlayListener.gotoPlayPage(false);//T19C的播放界面需要做成沉浸式，会导致切换回来时，闪现界面，因此在这里打个时间差，让沉浸式切换回来后再显示
                }
            }
        },false);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (flag == Constants.NavigationFlag.FLAG_PLAY) {
            //这里需要区分播放的是不是DAB，是的话要切到DAB的播放页，例如外部卡片跳转因为不会区分是否DAB
            RadioMessage current = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            if (hasMulti){
                if (current != null && (current.getRadioType() == RadioMessage.DAB_TYPE || current.getRadioBand() == RadioManager.BAND_FM)) {
                    transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, multiPlayFragment);
                } else {
                    transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, radioPlayFragment);
                }
            }else {
                if (current != null && current.getRadioType() == RadioMessage.DAB_TYPE) {
                    transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, dabPlayFragment);
                } else {
                    transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, radioPlayFragment);
                }
            }
            transaction.commitNowAllowingStateLoss();
            iGotoRadioPlayListener.gotoPlayPage(true);
        } else if (flag == Constants.NavigationFlag.FLAG_DAB_PLAY) {
            if (hasMulti){
                transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, multiPlayFragment);
            }else {
                transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent,dabPlayFragment);
            }

            transaction.commitNowAllowingStateLoss();
            iGotoRadioPlayListener.gotoPlayPage(true);
        } else if (flag == Constants.NavigationFlag.FLAG_DAB_PLAY_LIST) {
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, dabListMainFragment);
            transaction.commitNowAllowingStateLoss();
            iGotoRadioPlayListener.gotoPlayPage(true);
        }else if (flag == Constants.NavigationFlag.FLAG_DAB_PLAY_EPG){
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, dabepgFragment);
            transaction.commitNowAllowingStateLoss();
            iGotoRadioPlayListener.gotoPlayPage(true);
        }
        else if (flag == Constants.NavigationFlag.FLAG_RADIO_COLLECT || flag == Constants.NavigationFlag.FLAG_VR_MAIN_COLLECT){
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, radioHomeFragment);
            transaction.commitNowAllowingStateLoss();
            if (hasMulti){
                if (hasAM){
                    radioHomeFragment.setCurrentTab(RadioConstants.TABWithMultiAM.POSITION_COLLECT);
                }else {
                    radioHomeFragment.setCurrentTab(RadioConstants.TABWithoutAM.POSITION_COLLECT);
                }
            }else {
                if (hasDAB){
                    radioHomeFragment.setCurrentTab(RadioConstants.TABWithDAB.POSITION_COLLECT);
                }else {
                    radioHomeFragment.setCurrentTab(RadioConstants.TABWithoutDAB.POSITION_COLLECT);
                }
            }
            iGotoRadioPlayListener.gotoPlayPage(false);
        }else if (flag == Constants.NavigationFlag.FLAG_MULTI_PLAY) {
            //这里需要区分播放的是不是DAB，是的话要切到DAB的播放页，例如外部卡片跳转因为不会区分是否DAB
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, multiPlayFragment);
            transaction.commitNowAllowingStateLoss();
            iGotoRadioPlayListener.gotoPlayPage(true);
        }
        else {
            transaction.setTransition(FragmentTransaction.TRANSIT_NONE).replace(R.id.flRadioContent, radioHomeFragment);
            transaction.commitNowAllowingStateLoss();
            if (flag == Constants.NavigationFlag.FLAG_VR_MAIN_FM){
                if(hasMulti){
                    radioHomeFragment.setCurrentTab(RadioConstants.TABWithoutAM.POSITION_MULTI);
                }else {
                    if (hasDAB){
                        radioHomeFragment.setCurrentTab(RadioConstants.TABWithDAB.POSITION_FM);
                    }else {
                        radioHomeFragment.setCurrentTab(RadioConstants.TABWithoutDAB.POSITION_FM);
                    }
                }
            }else if (flag == Constants.NavigationFlag.FLAG_VR_MAIN_AM){
                if (hasMulti){
                    radioHomeFragment.setCurrentTab(RadioConstants.TABWithMultiAM.POSITION_AM);
                }else {
                    if (hasDAB){
                        radioHomeFragment.setCurrentTab(RadioConstants.TABWithDAB.POSITION_AM);
                    }else {
                        radioHomeFragment.setCurrentTab(RadioConstants.TABWithoutDAB.POSITION_AM);
                    }
                }

            }else if (flag == Constants.NavigationFlag.FLAG_VR_MAIN_DAB){
                if (hasMulti){
                    radioHomeFragment.setCurrentTab(RadioConstants.TABWithoutAM.POSITION_MULTI);
                }else {
                    if (hasDAB){
                        radioHomeFragment.setCurrentTab(RadioConstants.TABWithDAB.POSITION_DAB);
                    }
                }
            }
        }
    }


    @Override
    public void onItemCLick(int flag) {
        setTargetFragment(flag);
    }

    @Override
    public void gotoSearchView(boolean isSearchView) {
        iGotoRadioPlayListener.gotoPlayPage(isSearchView);
    }

    @Override
    public void onBackClick() {
        setTargetFragment(Constants.NavigationFlag.FLAG_MAIN);
    }

    @Override
    public void onDABBackClick() {
        setTargetFragment(Constants.NavigationFlag.FLAG_MAIN);
    }

    @Override
    public void onDABPlayEnterListClick() {
        setTargetFragment(Constants.NavigationFlag.FLAG_DAB_PLAY_LIST);
    }

    @Override
    public void onDABPlayEnterEPGClick() {
        setTargetFragment(Constants.NavigationFlag.FLAG_DAB_PLAY_EPG);
    }

    @Override
    public void onDABListBackClick() {
        setTargetFragment(hasAM ? Constants.NavigationFlag.FLAG_DAB_PLAY : Constants.NavigationFlag.FLAG_MULTI_PLAY);
    }

    @Override
    public void onEPGBackClick() {
        setTargetFragment(hasAM ? Constants.NavigationFlag.FLAG_DAB_PLAY : Constants.NavigationFlag.FLAG_MULTI_PLAY);
    }

    private IGotoRadioPlayListener iGotoRadioPlayListener;

    public void setGotoRadioPlayListener(IGotoRadioPlayListener iGotoRadioPlayListener) {
        this.iGotoRadioPlayListener = iGotoRadioPlayListener;
    }

    public interface IGotoRadioPlayListener {

        void gotoPlayPage(boolean isPlayPage);
    }
}
