package com.desaysv.usbpicture.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.desaysv.usbpicture.fragment.USB1PictureListFragment;
import com.desaysv.usbpicture.utils.ProductUtils;

/**
 * created by ZNB for T26 Design on 2022-04-19
 * 主界面用的 ViewPager + Fragment
 * 这里是适配器
 */
public class MainFragmentAdapter extends FragmentPagerAdapter{
    private static final String TAG = "MainFragmentAdapter";

    private Fragment albumFragment;
    private Fragment usbFragment;
    public MainFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public void setFragment(Fragment albumFragment, Fragment usbFragment){
        this.albumFragment = albumFragment;
        this.usbFragment = usbFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0){
            return ProductUtils.hasCVBox() ? albumFragment : usbFragment;
        }else {
            return usbFragment;
        }
    }

    @Override
    public int getCount() {
        return ProductUtils.hasCVBox() ? 2 : 1;
    }
}
