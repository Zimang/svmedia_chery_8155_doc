package com.desaysv.moduleusbmusic.adapter;

/**
 * @author uidq1846
 * @desc Fragment切换使用的适配器
 * @time 2022-11-16 20:47
 */

import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragments;
    private String[] titles;

    public ScreenSlidePagerAdapter(FragmentManager fm, List<Fragment> fragments, String[] titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (titles.length == fragments.size()) {
            return titles[position];
        }
        return super.getPageTitle(position);
    }

    /**
     * fragments
     *
     * @param fragments fragments
     */
    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
        notifyDataSetChanged();
    }

    @Override
    public void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader) {
        super.restoreState(state, loader);
    }

    @Nullable
    @Override
    public Parcelable saveState() {
        //界面销毁是否需要保存状态
        return null;
    }
}
