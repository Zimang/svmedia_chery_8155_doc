package com.desaysv.moduleradio.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.desaysv.moduledab.fragment.DABFragment;
import com.desaysv.moduledab.fragment.DABPlayFragment;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.ui.AMFragment;
import com.desaysv.moduleradio.ui.AMListFragment;
import com.desaysv.moduleradio.ui.CollectListFragment;
import com.desaysv.moduleradio.ui.FMFragment;
import com.desaysv.moduleradio.ui.FMListFragment;

public class RadioFragmentAdapter extends FragmentPagerAdapter {
    private static final String TAG = "RadioFragmentAdapter";
    private FMFragment fmFragment;
    private AMFragment amFragment;
    private DABPlayFragment dabFragment;

    private String[] tabTitles = new String[]{};

    private boolean hasDAB;//用本地变量获取一次就好了，避免多次获取

    public RadioFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        hasDAB = ProductUtils.hasDAB();
    }

    //Radio的和DAB分开，以便下线配置差异化
    public void setFragments(FMFragment fmFragment, AMFragment amFragment){
        this.fmFragment = fmFragment;
        this.amFragment = amFragment;
    }

    //Radio的和DAB分开，以便下线配置差异化
    public void setDABFragment(DABPlayFragment dabFragment){
        this.dabFragment = dabFragment;
    }


    public void setTabTitles(String[] tabTitles){
        this.tabTitles = tabTitles;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (hasDAB){
            switch (position){
                case RadioConstants.TABWithDAB.POSITION_FM:
                default:
                    return fmFragment;
                case RadioConstants.TABWithDAB.POSITION_AM:
                    return amFragment;
                case RadioConstants.TABWithDAB.POSITION_DAB:
                    return dabFragment;
            }
        }else {
            switch (position){
                case RadioConstants.TABWithoutDAB.POSITION_FM:
                default:
                    return fmFragment;
                case RadioConstants.TABWithoutDAB.POSITION_AM:
                    return amFragment;
            }
        }
    }

    @Override
    public int getCount() {
        return hasDAB ? 3 : 2 ;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
