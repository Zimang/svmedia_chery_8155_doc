package com.desaysv.moduleradio.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import com.desaysv.mediacommonlib.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class RadioFragmentListAdapter extends FragmentPagerAdapter {
    private static final String TAG = "RadioFragmentListAdapter";

    private String[] tabTitles = new String[]{};

    private List<BaseFragment> fragmentList;

    private FragmentManager fragmentManager;

    public RadioFragmentListAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        fragmentManager= fm;
    }

    public void setFragments(List<BaseFragment> fragmentList){
        //动态替换fragment，传入fragmentList每次需要new
        if(this.fragmentList != null){
            FragmentTransaction ft = fragmentManager.beginTransaction();
            for(Fragment f:this.fragmentList){
                ft.remove(f);
            }
            ft.commit();
            ft = null;
            fragmentManager.executePendingTransactions();
        }

        this.fragmentList = fragmentList;
        notifyDataSetChanged();
    }

    public void setTabTitles(String[] tabTitles){
        this.tabTitles = tabTitles;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList == null ? 0: fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
