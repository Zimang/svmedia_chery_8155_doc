package com.desaysv.moduledab.fragment;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.DABListPagerAdapter;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduledab.view.NoSmoothViewPager;
import com.google.android.material.tabs.TabLayout;

/**
 * created by ZNB on 2022-12-21
 * DAB播放页进入的电台列表主页，里面有好几种分类列表
 */
public class DABListMainFragment extends BaseFragment implements View.OnClickListener, IDABOperationListener, DABListBaseFragment.IOnEnterNextListener {

    private static final String TAG = "DABListMainFragment";

    private ImageView ivDABPlayListBack;
    private TabLayout tlDABPlayList;
    private NoSmoothViewPager vpDABPlayListMain;

    private DABListAllFragment dabListAllFragment;
    private DABListEnsembleFragment dabListEnsembleFragment;
    private DABListTypeFragment dabListTypeFragment;
    private DABListCollectFragment dabListCollectFragment;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_dabplaylist_main_layout;
    }

    @Override
    public void initView(View view) {
        ivDABPlayListBack = view.findViewById(R.id.ivDABPlayListBack);
        tlDABPlayList = view.findViewById(R.id.tlDABPlayList);
        vpDABPlayListMain = view.findViewById(R.id.vpDABPlayListMain);
        if (ProductUtils.isRightRudder()){
            ivDABPlayListBack.setImageResource(R.mipmap.icon_dab_settings_back_right);
        }

        //fragment里面嵌套fragment，所以是child
        FragmentManager fragmentManager = getChildFragmentManager();

        DABListPagerAdapter dabListPagerAdapter = new DABListPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,getContext().getResources().getStringArray(R.array.dab_list_tab));

        dabListAllFragment =  new DABListAllFragment();
        dabListEnsembleFragment = new DABListEnsembleFragment();
        dabListTypeFragment = new DABListTypeFragment();
        dabListCollectFragment = new DABListCollectFragment();
        dabListPagerAdapter.setDabListAllFragment(dabListAllFragment);
        dabListPagerAdapter.setDabListEnsembleFragment(dabListEnsembleFragment);
        dabListPagerAdapter.setDabListTypeFragment(dabListTypeFragment);
        dabListPagerAdapter.setDabListCollectFragment(dabListCollectFragment);

        tlDABPlayList.setupWithViewPager(vpDABPlayListMain, true);
        vpDABPlayListMain.setAdapter(dabListPagerAdapter);
        if (ProductUtils.isRightRudder()) {
            vpDABPlayListMain.setRotationY(180);
        }


        for (int i = 0; i < tlDABPlayList.getTabCount(); i++) {
            TabLayout.Tab tab = tlDABPlayList.getTabAt(i);
            if (tab != null) {
                tab.view.setLongClickable(false);
                tab.view.setTooltipText(null);
                //为了适配阿语的Noto字库，需要设置TextView的高度，不能使用原生的wrap
                TextView textView = (TextView)tab.view.getChildAt(1);
                ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
                layoutParams.height = 46;
                textView.setLayoutParams(layoutParams);
            }
        }


        //埋点：进入全部电台
        PointTrigger.getInstance().trackEvent(Point.KeyName.DABListClickAll);


        vpDABPlayListMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    //埋点：进入全部电台
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABListClickAll);
                }else if (position == 1){
                    //埋点：进入电台列表
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABListClickList);
                }else if (position == 2){
                    //埋点：进入分类列表
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABListClickType);
                }else if (position == 3){
                    //埋点：进入收藏列表
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABListClickCollect);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void initData() {

    }

    @Override
    public void initViewListener() {
        Log.d(TAG,"initViewListener");
        ivDABPlayListBack.setOnClickListener(this);
        dabListAllFragment.setOnEnterNextListener(this);
        dabListEnsembleFragment.setOnEnterNextListener(this);
        dabListTypeFragment.setOnEnterNextListener(this);
        dabListCollectFragment.setOnEnterNextListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivDABPlayListBack){
            backClickListener.onDABListBackClick();
        }
    }

    /**
     * 左上角返回按钮的点击事件
     */
    private IOnBackClickListener backClickListener;

    public void setBackClickListener(IOnBackClickListener backClickListener){
        this.backClickListener = backClickListener;
    }

    @Override
    public void onEnterNextList(boolean enterNextList) {
        Log.d(TAG,"onEnterNextList:"+enterNextList);
        if (enterNextList) {
            tlDABPlayList.setVisibility(View.GONE);
            ivDABPlayListBack.setVisibility(View.GONE);
            vpDABPlayListMain.setCanSwipe(false);
        }else {
            tlDABPlayList.setVisibility(View.VISIBLE);
            ivDABPlayListBack.setVisibility(View.VISIBLE);
            vpDABPlayListMain.setCanSwipe(true);
        }
    }


    @Override
    public void onOpenDABRadio() {
        backClickListener.onDABListBackClick();
    }

    public interface IOnBackClickListener{

        void onDABListBackClick();
    }
}
