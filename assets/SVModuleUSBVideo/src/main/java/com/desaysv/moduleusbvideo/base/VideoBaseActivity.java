package com.desaysv.moduleusbvideo.base;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.desaysv.mediacommonlib.ui.BaseActivity;
import com.desaysv.moduleusbvideo.base.interfaces.ICommunication;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by extodc87 on 2022-11-1
 * Author: extodc87
 */
public abstract class VideoBaseActivity extends BaseActivity implements ICommunication {
    protected FragmentManager mFragmentManager;
    protected SparseArray<FragmentInfo> mFragmentInfo = new SparseArray<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mFragmentManager == null) {
            mFragmentManager = new FragmentManager();
        }
        Log.d(TAG, "onCreate: mFragmentManager = " + mFragmentManager);

        initFragments();
        /*
         * 向fragment管理类加载所有通过createFragmentInfo已经加入到集合的fragments
         */
        mFragmentManager.addViews(mFragmentInfo, getSupportFragmentManager());
    }

    public void initFragments() {
        Log.d(TAG, "initFragments: ");
    }

    /**
     * 创建FragmentInfo并存入Fragment管理容器
     *
     * @param contentID  装载控件ID，Fragment所在FrameLayout的ID
     * @param fragmentID 自定义FragmentID，Fragment的唯一标识
     * @param fragment   Fragment对象
     */
    protected FragmentInfo createAndAddFragmentInfo(int contentID, int fragmentID, Fragment fragment) {
        FragmentInfo fragmentInfo = mFragmentInfo.get(fragmentID);
        if (null == fragmentInfo) {
            fragmentInfo = new FragmentInfo.Builder()
                    .setContentID(contentID)
                    .setId(fragmentID)
                    .setFragment(fragment)
                    .build();
            mFragmentInfo.put(fragmentID, fragmentInfo);
            Log.d(TAG, "createAndAddFragmentInfo: fragmentInfo = " + fragmentInfo);
        }
        return fragmentInfo;
    }


    @Override
    public void showFragment(int id) {
        Log.d(TAG, "showFragment: mFragmentManager = " + mFragmentManager);
        mFragmentManager.showView(id);
    }

    @Override
    public void showFragments(List<Integer> ids) {
        mFragmentManager.showViews(ids);
    }

    @Override
    public void showFragmentsNow(List<Integer> ids) {
        mFragmentManager.showViewsNow(ids);
    }

    @Override
    public void backPage() {
        mFragmentManager.back();
    }

    @Override
    public List<Integer> getCurrentFragmentIDs() {
        List<Integer> list = new ArrayList<>();
        List<FragmentInfo> fragmentInfoList = mFragmentManager.getCurrentViewInfos();
        for (FragmentInfo fragmentInfo : fragmentInfoList) {
            list.add(fragmentInfo.getId());
        }
        return list;
    }

    /**
     * 获取当前前台显示的Fragment集合
     *
     * @return fragment集合
     */
    public List<Fragment> getCurrentFragments() {
        List<Fragment> list = new ArrayList<>();
        List<FragmentInfo> fragmentInfoList = mFragmentManager.getCurrentViewInfos();
        for (FragmentInfo fragmentInfo : fragmentInfoList) {
            list.add(fragmentInfo.getFragment());
        }
        return list;
    }

    /**
     * 初始化当前显示的fragment
     * TODO 切换主题后，需要重置当前显示的fragment
     *
     * @param currentInfos currentInfos
     */
    public void initCurrentVideInfos(List<FragmentInfo> currentInfos) {
        Log.d(TAG, "initCurrentVideInfos: " + currentInfos);
        mFragmentManager.initCurrentVideInfos(currentInfos);
    }
}
