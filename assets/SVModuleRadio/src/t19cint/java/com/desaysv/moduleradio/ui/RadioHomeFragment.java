package com.desaysv.moduleradio.ui;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.radio.RadioManager;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.fragment.DABFragment;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioMainFragmentAdapter;
import com.desaysv.moduleradio.adapter.SearchHistoryListAdapter;
import com.desaysv.moduleradio.adapter.SearchResultListAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.utils.CurrentShowFragmentUtil;
import com.desaysv.moduleradio.utils.RadioCovertUtils;
import com.desaysv.moduleradio.view.AutoLineFeedLayoutManager;
import com.desaysv.moduleradio.view.DeleteHistoryDialog;
import com.desaysv.moduleradio.vr.SVRadioVRStateUtil;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * created by ZNB on 2022-10-26
 * Radio模块主界面，通过 Tab + ViewPager + Fragment 的形式，
 * 嵌入DAB、FM、AM、收藏等几个 Fragment
 */
public class RadioHomeFragment extends BaseFragment implements RadioBaseListFragment.IOnItemClickListener, DABFragment.IOnDABItemClickListener {

    private ViewPager vpRadioMain;

    //定义Tab对应的Fragment
    private FMListFragment fmListFragment;
    private AMListFragment amListFragment;
    private DABFragment dabFragment;
    private CollectListFragment collectListFragment;

    private MultiListFragment multiListFragment;

    private RelativeLayout rlRadioMain;
    private ImageView ivRadioSearch;

    private RelativeLayout rlSearch;
    private SearchView svSearch;
    private ImageView ivRadioSearchBack;
    private RelativeLayout rlSearchHistory;
    private ImageView ivHistoryDelete;

    private ImageView ivRadioScSearch;
    private TextView tvRadioScSearch;
    private EditText etRadioSc;
    private RecyclerView rvHistoryList;
    private RecyclerView rvSearchResultList;
    private RelativeLayout rlSearchResult;
    private RelativeLayout rlSearchResultNone;
    private SearchHistoryListAdapter searchHistoryListAdapter;
    private SearchResultListAdapter searchResultListAdapter;
    private DeleteHistoryDialog deleteHistoryDialog;
    private AutoLineFeedLayoutManager autoLineFeedLayoutManager;
    private boolean hasDAB = false;//用本地变量获取一次就好了，避免多次获取

    private boolean hasAM = false;//用本地变量获取一次就好了，避免多次获取

    private boolean hasMulti = false;//用本地变量获取一次就好了，避免多次获取

    private boolean fromUser = false;

    private MyHandler mHandler;

    @Override
    public int getLayoutResID() {
        return R.layout.radio_fragment_main;
    }

    @Override
    public void initView(View view) {
        Log.d(TAG, "initView");
        mHandler = new MyHandler(this);
        hasDAB = ProductUtils.hasDAB();

        hasAM = ProductUtils.hasAM();

        hasMulti = ProductUtils.hasMulti();

        //fragment里面嵌套fragment，所以是child
        FragmentManager fragmentManager = getChildFragmentManager();
        rlRadioMain = view.findViewById(R.id.rlRadioMain);
        ivRadioSearch = view.findViewById(R.id.ivRadioSearch);
        rlSearch = view.findViewById(R.id.rlSearch);
        svSearch = view.findViewById(R.id.svSearch);
        ivRadioScSearch  = view.findViewById(R.id.ivRadioScSearch);
        tvRadioScSearch = view.findViewById(R.id.tvRadioScSearch);
        etRadioSc = ((EditText)svSearch.findViewById(androidx.appcompat.R.id.search_src_text));
        etRadioSc.setHintTextColor(getResources().getColor(R.color.radio_text_sub_color));
        etRadioSc.setTextColor(getResources().getColor(R.color.radio_text_input_color));
        etRadioSc.setTextAlignment(EditText.TEXT_ALIGNMENT_VIEW_START);
        etRadioSc.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        etRadioSc.setLayoutParams(etRadioSc.getLayoutParams());

        ivRadioSearchBack = view.findViewById(R.id.ivRadioSearchBack);
        rlSearchHistory = view.findViewById(R.id.rlSearchHistory);
        ivHistoryDelete = view.findViewById(R.id.ivHistoryDelete);
        rvHistoryList = view.findViewById(R.id.rvHistoryList);
        searchHistoryListAdapter = new SearchHistoryListAdapter(getContext(), onSearchHistoryItemClickListener);
        autoLineFeedLayoutManager = new AutoLineFeedLayoutManager(getContext());
        rvHistoryList.setLayoutManager(autoLineFeedLayoutManager);
        rvHistoryList.addItemDecoration(new AutoLineFeedLayoutManager.HistoryItemDecoration(getContext()));
        rvHistoryList.setAdapter(searchHistoryListAdapter);
        deleteHistoryDialog = new DeleteHistoryDialog(getContext(), R.style.radio_dialogstyle,onDeleteClickListener);

        rvSearchResultList = view.findViewById(R.id.rvSearchResultList);
        searchResultListAdapter = new SearchResultListAdapter(getContext(),onSearchResultItemClickListener);
        rvSearchResultList.setLayoutManager(new GridLayoutManager(getContext(),2));
        rvSearchResultList.setAdapter(searchResultListAdapter);

        rlSearchResult = view.findViewById(R.id.rlSearchResult);
        rlSearchResultNone = view.findViewById(R.id.rlSearchResultNone);
        svSearch.setOnQueryTextListener(onQueryTextListener);
        etRadioSc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.playSoundEffect(SoundEffectConstants.CLICK);
                }
            }
        });
        ivHistoryDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteHistoryDialog.show();
            }
        });
        ivRadioSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlSearch.setVisibility(View.VISIBLE);
                rlRadioMain.setVisibility(View.GONE);
                svSearch.setQuery("",false);
                showHistoryList(true);
                onItemClickListener.gotoSearchView(true);
            }
        });
        ivRadioSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlSearch.setVisibility(View.GONE);
                rlRadioMain.setVisibility(View.VISIBLE);
                showHistoryList(false);
                onItemClickListener.gotoSearchView(false);
            }
        });
        ivRadioScSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etRadioSc.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        tvRadioScSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etRadioSc.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        TabLayout tbRadioMain = view.findViewById(R.id.tbRadioMain);
        vpRadioMain = view.findViewById(R.id.vpRadioMain);

        RadioMainFragmentAdapter mainFragmentAdapter = new RadioMainFragmentAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        //这一部分很重要，用于重载之后，从系统保存的对象取出 fragment(例如白天黑夜切换)
        //否则因为系统默认恢复的是保存的fragment，走的是恢复对象的生命周期，
        //而这里又new了新的fragment对象，就会导致new的对象里面的参数实际并没有初始化，因为new这个fragment不会被add进去，不会执行相应的生命周期
        //就会导致各种空指针异常
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null || fragments.size() == 0) {
            if (hasMulti){
                multiListFragment = new MultiListFragment();
                multiListFragment.setOnItemClickListener(this);
                if (hasAM){
                    amListFragment = new AMListFragment();
                    amListFragment.setOnItemClickListener(this);
                }
            }else {
                if (hasDAB) {
                    dabFragment = new DABFragment();
                    dabFragment.setOnDABItemClickListener(this);
                }
                fmListFragment = new FMListFragment();
                amListFragment = new AMListFragment();
                fmListFragment.setOnItemClickListener(this);
                amListFragment.setOnItemClickListener(this);

            }
            collectListFragment = new CollectListFragment();
        } else {
            for (Fragment fragment : fragments) {
                Log.d(TAG,"fragments test");
                if (fragment instanceof DABFragment) {
                    dabFragment = (DABFragment) fragment;
                } else if (fragment instanceof FMListFragment) {
                    fmListFragment = (FMListFragment) fragment;
                } else if (fragment instanceof AMListFragment) {
                    amListFragment = (AMListFragment) fragment;
                } else if (fragment instanceof CollectListFragment) {
                    collectListFragment = (CollectListFragment) fragment;
                } else if (fragment instanceof MultiListFragment) {
                    multiListFragment = (MultiListFragment) fragment;
                }
            }
            //未执行到的fragment不会被保存，所以需要进行判空
            if (hasMulti){
                if (multiListFragment == null){
                    multiListFragment = new MultiListFragment();
                }
                multiListFragment.setOnItemClickListener(this);
                if (hasAM){
                    amListFragment = new AMListFragment();
                    amListFragment.setOnItemClickListener(this);
                }
            }else {

                if (dabFragment == null){
                    if (hasDAB) {
                        dabFragment = new DABFragment();
                        dabFragment.setOnDABItemClickListener(this);
                    }
                }
                if (fmListFragment == null){
                    fmListFragment = new FMListFragment();
                }
                if (amListFragment == null){
                    amListFragment = new AMListFragment();
                }
                fmListFragment.setOnItemClickListener(this);
                amListFragment.setOnItemClickListener(this);
            }

            if (collectListFragment == null){
                collectListFragment = new CollectListFragment();
            }
        }
        collectListFragment.setOnItemClickListener(this);

        if (hasMulti) {
            if (hasAM){
                mainFragmentAdapter.setFragments(multiListFragment, amListFragment,collectListFragment);
                mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio_multi_am));
            }else {
                mainFragmentAdapter.setFragments(multiListFragment, collectListFragment);
                mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio_no_am));
            }
        }else {
            mainFragmentAdapter.setFragments(fmListFragment, amListFragment, collectListFragment);
            if (hasDAB) {
                mainFragmentAdapter.setDABFragment(dabFragment);
                mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio_dab));
            } else {
                mainFragmentAdapter.setTabTitles(getResources().getStringArray(R.array.radio));
            }
        }
        tbRadioMain.setupWithViewPager(vpRadioMain, true);
        vpRadioMain.setAdapter(mainFragmentAdapter);
        if (ProductUtils.isRightRudder()) {
            vpRadioMain.setRotationY(180);
        }

        for (int i = 0; i < tbRadioMain.getTabCount(); i++) {
            TabLayout.Tab tab = tbRadioMain.getTabAt(i);
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
    }

    //用于进入应用时，自动跳转对应界面
    public void setCurrentTab(int currentTab) {
        Log.d(TAG, "setCurrentTab:" + currentTab);
        SVRadioVRStateUtil.getInstance().setCurrentTab(currentTab);
        if (hasMulti){
            if (ivRadioSearch != null){
                ivRadioSearch.setVisibility(View.GONE);
                if (currentTab == RadioConstants.TABWithMultiAM.POSITION_MULTI || currentTab ==  RadioConstants.TABWithoutAM.POSITION_MULTI){
                    ivRadioSearch.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void initData() {
        fromUser = false;
        Log.d(TAG, "initData,currentTab:"+SVRadioVRStateUtil.getInstance().getCurrentTab());
        RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "initData: currentRadioMessage = " + currentRadioMessage);

        if (SVRadioVRStateUtil.getInstance().getCurrentTab() == -1) {//不等于0 表示这个值已经改变过了，表示有其他地方设置了，需要优先考虑
            if (currentRadioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
                if (hasMulti){
                    setCurrentTab(RadioConstants.TABWithMultiAM.POSITION_MULTI);
                }else {
                    setCurrentTab(RadioConstants.TABWithDAB.POSITION_DAB);
                }
            } else {
                int radioBand = currentRadioMessage.getRadioBand();
                if (radioBand == RadioManager.BAND_FM || radioBand == RadioManager.BAND_FM_HD) {
                    if (hasMulti){
                        setCurrentTab(RadioConstants.TABWithMultiAM.POSITION_MULTI);
                    }else {
                        if (hasDAB) {
                            setCurrentTab(RadioConstants.TABWithDAB.POSITION_FM);
                        } else {
                            setCurrentTab(RadioConstants.TABWithoutDAB.POSITION_FM);
                        }
                    }
                } else {
                    if (hasMulti){
                        setCurrentTab(RadioConstants.TABWithMultiAM.POSITION_AM);
                    }else {
                        if (hasDAB) {
                            setCurrentTab(RadioConstants.TABWithDAB.POSITION_AM);
                        } else {
                            setCurrentTab(RadioConstants.TABWithoutDAB.POSITION_AM);
                        }
                    }
                }
            }
        }
        vpRadioMain.setCurrentItem(SVRadioVRStateUtil.getInstance().getCurrentTab(), false);
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        Log.d(TAG, "initData: end");
    }

    @Override
    public void initViewListener() {
        Log.d(TAG, "initViewListener");
        vpRadioMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: position = " + position);
            }

            @Override
            public void onPageSelected(int position) {
                if (!fromUser){
                    return;
                }
                Log.d(TAG, "onPageSelected,position: " + position);
                setCurrentTab(position);
                if (hasMulti){

                }else {
                    if (hasDAB) {
                        if (position == RadioConstants.TABWithDAB.POSITION_FM){
                            //埋点：打开FM
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.OPEN
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：关闭其它
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickAM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：列表事件
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseCollectList,Point.Field.OperType,Point.FieldValue.CLOSE);

                        }else if (position == RadioConstants.TABWithDAB.POSITION_AM){
                            //埋点：打开AM
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickAM,Point.Field.OperType,Point.FieldValue.OPEN
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：关闭其它
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：列表事件
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseCollectList,Point.Field.OperType,Point.FieldValue.CLOSE);

                        }else if (position == RadioConstants.TABWithDAB.POSITION_DAB){
                            //埋点：打开DAB
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickDAB,Point.Field.OperType,Point.FieldValue.OPEN
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：关闭其它
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickAM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：列表事件
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseCollectList,Point.Field.OperType,Point.FieldValue.CLOSE);
                        }else if (position == RadioConstants.TABWithDAB.POSITION_COLLECT){
                            //埋点：关闭其它
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickAM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickDAB,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：列表事件
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseCollectList,Point.Field.OperType,Point.FieldValue.OPEN);
                        }
                    } else {
                        if (position == RadioConstants.TABWithoutDAB.POSITION_FM){
                            //埋点：打开FM
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.OPEN
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：关闭其它
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickAM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：列表事件
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseCollectList,Point.Field.OperType,Point.FieldValue.CLOSE);

                        }else if (position == RadioConstants.TABWithoutDAB.POSITION_AM){
                            //埋点：打开AM
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickAM,Point.Field.OperType,Point.FieldValue.OPEN
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：关闭其它
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：列表事件
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseCollectList,Point.Field.OperType,Point.FieldValue.CLOSE);

                        }else if (position == RadioConstants.TABWithoutDAB.POSITION_COLLECT){
                            //埋点：关闭其它
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickAM,Point.Field.OperType,Point.FieldValue.CLOSE
                                    ,Point.Field.OperStyle,Point.FieldValue.OpeCLick);
                            //埋点：列表事件
                            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseCollectList,Point.Field.OperType,Point.FieldValue.OPEN);
                        }
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged: state = " + state);
            }
        });

        svSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 不需要做任何事情，因为我们不想在SearchView内部点击时做任何事
            }
        });

        rlSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Rect outRect = new Rect();
                    svSearch.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        return true;
                    }
                }
                return false;
            }
        });




        Log.d(TAG, "initViewListener: end");
    }

    @Override
    public void onItemCLick() {
        Log.d(TAG, "onItemCLick: to play page ");
        if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_AM){
            onItemClickListener.onItemCLick(Constants.NavigationFlag.FLAG_PLAY);
        }else {
            onItemClickListener.onItemCLick(hasMulti ? Constants.NavigationFlag.FLAG_MULTI_PLAY : Constants.NavigationFlag.FLAG_PLAY);
        }
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
    }

    @Override
    public void onItemCLickDAB() {
        Log.d(TAG, "onItemCLickDAB: to play page ");
        onItemClickListener.onItemCLick(hasMulti ? Constants.NavigationFlag.FLAG_MULTI_PLAY : Constants.NavigationFlag.FLAG_DAB_PLAY);
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
    }

    @Override
    public void onDABItemCLick() {
        Log.d(TAG, "onItemCLick: to play page  or  dab ");
        onItemClickListener.onItemCLick(hasMulti ? Constants.NavigationFlag.FLAG_MULTI_PLAY : Constants.NavigationFlag.FLAG_DAB_PLAY);
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
    }

    @Override
    public void onFragmentNewIntent() {
        super.onFragmentNewIntent();
        if (isAdded() && vpRadioMain != null) {
            vpRadioMain.setCurrentItem(SVRadioVRStateUtil.getInstance().getCurrentTab(), false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        CurrentShowFragmentUtil.isRadioHomeListPageOnStart = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        CurrentShowFragmentUtil.isRadioHomeListPageOnStart = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        fromUser = true;
        if (vpRadioMain != null) {
            vpRadioMain.setCurrentItem(SVRadioVRStateUtil.getInstance().getCurrentTab(), false);
        }
        updateSearchIconEnable();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
        ModuleRadioTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
    }

    /**
     * 列表项点击事件，透传给主Fragment进行界面替换
     */
    protected IOnItemClickListener onItemClickListener;

    public void setOnItemClickListener(IOnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface IOnItemClickListener {
        void onItemCLick(int flag);
        void gotoSearchView(boolean isSearchView);
    }


    public void showHistoryList(boolean show){
        if (show) {
            if (RadioList.getInstance().getSearchHistoryList().size() > 0) {
                rlSearchHistory.setVisibility(View.VISIBLE);
                searchHistoryListAdapter.updateList(RadioList.getInstance().getSearchHistoryList());
            } else {
                rlSearchHistory.setVisibility(View.GONE);
            }
            tvRadioScSearch.setEnabled(false);
            showResultList(false,"");
        }else {
            autoLineFeedLayoutManager.resetVerticalScrollOffset();
            rlSearchHistory.setVisibility(View.GONE);
        }
    }

    public void showResultList(boolean show,String newText){
        if (show) {
            tvRadioScSearch.setEnabled(true);
            rlSearchResult.setVisibility(View.VISIBLE);
            List<RadioMessage> resultList = RadioCovertUtils.getSearchResultList(newText, false);
            if (resultList.size() > 0) {
                rlSearchResultNone.setVisibility(View.GONE);
                rvSearchResultList.setVisibility(View.VISIBLE);
                searchResultListAdapter.updateList(resultList,newText);
            } else {
                rlSearchResultNone.setVisibility(View.VISIBLE);
                rvSearchResultList.setVisibility(View.GONE);
            }
        }else {
            tvRadioScSearch.setEnabled(false);
            rlSearchResult.setVisibility(View.GONE);
        }
    }

    private String currentQuery;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            Log.d(TAG,"onQueryTextSubmit:"+query);
            String trimText;
            if (query == null) {
                trimText = "";
            } else {
                trimText = query.trim();
            }
            RadioList.getInstance().updateSearchHistoryList(trimText);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Log.d(TAG,"onQueryTextChange:"+newText);
            String trimText;
            if (newText == null) {
                trimText = "";
            } else {
                trimText = newText.trim();
            }
            showHistoryList(trimText.isEmpty());
            showResultList(!trimText.isEmpty(),trimText);
            currentQuery = trimText;
            return false;
        }
    };

    private SearchHistoryListAdapter.OnItemClickListener onSearchHistoryItemClickListener = new SearchHistoryListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, String history) {
            Log.d(TAG,"onSearchHistoryItemClick,position:"+position+", history:"+history);
            svSearch.setQuery(history,true);
        }
    };

    private SearchResultListAdapter.OnItemClickListener onSearchResultItemClickListener = new SearchResultListAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, RadioMessage resultMessage) {
            Log.d(TAG,"onSearchResultItemClick,position:"+position+", resultMessage:"+resultMessage);
            SPUtlis.getInstance().saveShowCollectListMode(false);
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, resultMessage);
            onItemClickListener.onItemCLick(Constants.NavigationFlag.FLAG_MULTI_PLAY );
            SVRadioVRStateUtil.getInstance().setCurrentTab(-1);
            if (currentQuery != null && currentQuery.length()>0) {
                svSearch.setQuery(currentQuery, true);
                currentQuery = null;//阅后即焚
            }
        }
    };

    private DeleteHistoryDialog.OnDeleteClickListener onDeleteClickListener = new DeleteHistoryDialog.OnDeleteClickListener() {
        @Override
        public void onDeleteClick() {
            Log.d(TAG,"onDeleteClick");
            showHistoryList(false);
            RadioList.getInstance().deleteSearchHistoryList();
        }
    };

    IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS);
        }

        @Override
        public void onAstListChanged(int band) {

        }

        @Override
        public void onSearchStatusChange(final boolean isSearching) {
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {

        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {

        }
    };

    IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
        }

        @Override
        public void onAMCollectListChange() {
        }

        @Override
        public void onDABCollectListChange() {

        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG, "onFMEffectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
        }

        @Override
        public void onAMEffectListChange() {

        }

        @Override
        public void onDABEffectListChange() {
            Log.d(TAG, "onDABEffectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
        }

        @Override
        public void onFMAllListChange() {

        }

        @Override
        public void onAMAllListChange() {
        }

        @Override
        public void onDABAllListChange() {

        }
    };

    public void updateSearchResultList(){
        searchResultListAdapter.notifyDataSetChanged();
    }

    public void updateSearchIconEnable(){
        ivRadioSearch.setEnabled(RadioList.getInstance().getMultiRadioMessageList().size()>0);
    }

    private static class MyHandler extends Handler {
        WeakReference<RadioHomeFragment> weakReference;

        MyHandler(RadioHomeFragment radiohomeFragment) {
            weakReference = new WeakReference<>(radiohomeFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final RadioHomeFragment radiohomeFragment = weakReference.get();
            Log.d(radiohomeFragment.TAG, "handleMessage:" + msg.what);
            if (radiohomeFragment.isDetached()){
                Log.d(radiohomeFragment.TAG, "radiohomeFragment.isDetach");
                return;
            }
            switch (msg.what) {
                case RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE:
                case RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS:
                    radiohomeFragment.updateSearchResultList();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_FM_LIST:
                    radiohomeFragment.updateSearchIconEnable();
                    break;
            }
        }
    }
}
