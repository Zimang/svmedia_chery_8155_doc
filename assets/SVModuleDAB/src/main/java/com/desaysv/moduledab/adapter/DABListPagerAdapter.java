package com.desaysv.moduledab.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.desaysv.moduledab.fragment.DABListAllFragment;
import com.desaysv.moduledab.fragment.DABListEnsembleFragment;
import com.desaysv.moduledab.fragment.DABListTypeFragment;
import com.desaysv.moduledab.fragment.DABListCollectFragment;


/**
 * Created by ZNB on 2022-12-16
 * Comment viewPager的适配器
 */
public class DABListPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "DABListPagerAdapter";
    private String[] tabTitles = new String[]{};

    private DABListAllFragment dabListAllFragment;
    private DABListEnsembleFragment dabListEnsembleFragment;
    private DABListTypeFragment dabListTypeFragment;
    private DABListCollectFragment dabListCollectFragment;

    public DABListPagerAdapter(@NonNull FragmentManager fm, int behavior,String[] tabTitles) {
        super(fm, behavior);
        this.tabTitles = tabTitles;
    }

    public void setDabListAllFragment(DABListAllFragment dabListAllFragment) {
        this.dabListAllFragment = dabListAllFragment;
    }

    public void setDabListEnsembleFragment(DABListEnsembleFragment dabListEnsembleFragment) {
        this.dabListEnsembleFragment = dabListEnsembleFragment;
    }

    public void setDabListTypeFragment(DABListTypeFragment dabListTypeFragment) {
        this.dabListTypeFragment = dabListTypeFragment;
    }

    public void setDabListCollectFragment(DABListCollectFragment dabListCollectFragment) {
        this.dabListCollectFragment = dabListCollectFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
            default:
                return dabListAllFragment;
            case 1:
                return dabListEnsembleFragment;
            case 2:
                return dabListTypeFragment;
            case 3:
                return dabListCollectFragment;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
