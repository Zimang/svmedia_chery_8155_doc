package com.desaysv.moduleradio.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.desaysv.moduledab.fragment.DABFragment;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.ui.AMListFragment;
import com.desaysv.moduleradio.ui.CollectListFragment;
import com.desaysv.moduleradio.ui.FMListFragment;
import com.desaysv.moduleradio.ui.MultiListFragment;

public class RadioMainFragmentAdapter extends FragmentPagerAdapter {
    private static final String TAG = "RadioMainFragmentAdapter";
    private FMListFragment fmListFragment;
    private AMListFragment amListFragment;
    private CollectListFragment collectListFragment;
    private MultiListFragment multiListFragment;
    private DABFragment dabFragment;

    private String[] tabTitles = new String[]{};

    private boolean hasDAB = false;//用本地变量获取一次就好了，避免多次获取
    private boolean hasAM = false;//用本地变量获取一次就好了，避免多次获取

    private boolean hasMulti = false;//用本地变量获取一次就好了，避免多次获取

    public RadioMainFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);

        hasDAB = ProductUtils.hasDAB();
        hasAM = ProductUtils.hasAM();
        hasMulti = ProductUtils.hasMulti();
    }

    //Radio的和DAB分开，以便下线配置差异化
    public void setFragments(FMListFragment fmListFragment, AMListFragment amListFragment, CollectListFragment collectListFragment){
        this.fmListFragment = fmListFragment;
        this.amListFragment = amListFragment;
        this.collectListFragment = collectListFragment;
    }

    public void setFragments(MultiListFragment multiListFragment, AMListFragment amListFragment, CollectListFragment collectListFragment){
        this.multiListFragment = multiListFragment;
        this.amListFragment = amListFragment;
        this.collectListFragment = collectListFragment;
    }

    public void setFragments(MultiListFragment multiListFragment, CollectListFragment collectListFragment){
        this.multiListFragment = multiListFragment;
        this.collectListFragment = collectListFragment;
    }

    //Radio的和DAB分开，以便下线配置差异化
    public void setDABFragment(DABFragment dabFragment){
        this.dabFragment = dabFragment;
    }


    public void setTabTitles(String[] tabTitles){
        this.tabTitles = tabTitles;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (hasMulti){
            if (hasAM){
                switch (position){
                    case RadioConstants.TABWithMultiAM.POSITION_MULTI:
                    default:
                        return multiListFragment;
                    case RadioConstants.TABWithMultiAM.POSITION_AM:
                        return amListFragment;
                    case RadioConstants.TABWithMultiAM.POSITION_COLLECT:
                        return collectListFragment;
                }
            }else {
                switch (position){
                    case RadioConstants.TABWithoutAM.POSITION_MULTI:
                    default:
                        return multiListFragment;
                    case RadioConstants.TABWithoutAM.POSITION_COLLECT:
                        return collectListFragment;
                }
            }
        }else {
            if (hasDAB) {
                switch (position) {
                    case RadioConstants.TABWithDAB.POSITION_DAB:
                        return dabFragment;
                    case RadioConstants.TABWithDAB.POSITION_FM:
                    default:
                        return fmListFragment;
                    case RadioConstants.TABWithDAB.POSITION_AM:
                        return amListFragment;
                    case RadioConstants.TABWithDAB.POSITION_COLLECT:
                        return collectListFragment;
                }
            } else {
                switch (position) {
                    case RadioConstants.TABWithoutDAB.POSITION_FM:
                    default:
                        return fmListFragment;
                    case RadioConstants.TABWithoutDAB.POSITION_AM:
                        return amListFragment;
                    case RadioConstants.TABWithoutDAB.POSITION_COLLECT:
                        return collectListFragment;
                }
            }
        }
    }

    @Override
    public int getCount() {
        return hasMulti ? hasAM ? 3 : 2 : hasDAB ? 4 : 3 ;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
