package com.desaysv.mediacommonlib.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author uidp5370
 * @date 2019-2-18
 * 这是common lib里面的基础fragment，考虑到需要前后台，所以这里做了一个记录
 */

public abstract class BaseFragment extends Fragment {

    public final String TAG = getClass().getSimpleName();


    protected View mRootView;

    /**
     * 获取Fragment的资源ID
     *
     * @return layout的资源ID
     */
    public abstract int getLayoutResID();

    /**
     * 初始化界面
     *
     * @param view rootView
     */
    public abstract void initView(View view);

    /**
     * 初始化基本的数据
     */
    public abstract void initData();

    /**
     * 初始化界面的回调
     */
    public abstract void initViewListener();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //在界面第一次创建的时候，就会调用恢复播放的逻辑，所以需要在这里加入设置界面为前台的逻辑
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: isFragmentOnResume = " + true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        mRootView = inflater.inflate(getLayoutResID(), null);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        initView(mRootView);
        initData();
        initViewListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewRecording.getInstance().setViewIsFg(this.getClass().getName(), true);
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: hidden = " + hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setIntent(null);
        Log.d(TAG, "onResume: setIntent null");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewRecording.getInstance().setViewIsFg(this.getClass().getName(), false);
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    /**
     * 增加类似activity的新意图方法，避免设置属性值后，因onState
     */
    public void onFragmentNewIntent() {
        Log.d(TAG, "onNewIntent: ");
    }
}
