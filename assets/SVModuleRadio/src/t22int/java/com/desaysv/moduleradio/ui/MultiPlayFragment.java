package com.desaysv.moduleradio.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.radio.RadioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.bumptech.glide.request.target.Target;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.MultiPlayListAdapter;
import com.desaysv.moduledab.common.DABMsg;
import com.desaysv.moduledab.dialog.RTDialog;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.CtgUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.adapter.SearchHistoryListAdapter;
import com.desaysv.moduleradio.adapter.SearchResultListAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.utils.RadioCovertUtils;
import com.desaysv.moduleradio.view.AutoLineFeedLayoutManager;
import com.desaysv.moduleradio.view.DeleteHistoryDialog;
import com.desaysv.moduleradio.view.FmSetupDialog;
import com.desaysv.moduleradio.view.ParameterDialog;
import com.desaysv.moduleradio.view.cursor.OnFrequencyChangedListener;
import com.desaysv.moduleradio.view.cursor.RadioCursor;
import com.desaysv.moduleradio.view.cursor.RadioCursor2;
import com.desaysv.svlibtoast.ToastUtil;
import com.sy.swbt.SettingSwitchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.ref.WeakReference;
import java.util.List;
import com.desaysv.moduledab.utils.ClickUtils;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;

public class MultiPlayFragment extends BaseFragment implements View.OnClickListener, IDABOperationListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MultiPlayFragment";

    private ImageView ivBack;
    private ImageView ivDABPause;
    private ImageView ivLogo;
    private TextView tvProgramStationName;
    private TextView tvRadioText;
    private TextView tvProgramType;
    private TextView tvDABListCount;

    private ImageView ivDABPre;
    private ImageView ivDABNext;
    private ImageView ivDABSearch;//进入DAB的细分列表
    private ImageView ivDABLike;//执行收藏/取消收藏 操作
    private ImageView ivDABSettings;//显示更多信息
    private RelativeLayout rlEmptyView;
    private RelativeLayout rvDABPlaySearching;
    private ImageView ivDABLoading;

    private RelativeLayout rlDABPlay;
    private RelativeLayout rlDABSettings;
    private ScrollView slDABSettings;
    private ImageView ivDABSettingsBack;
    private ImageView ivSingArm;
    private ObjectAnimator singArmAni;

    private RecyclerView rlDABPlayList;
    private MultiPlayListAdapter playListAdapter;

    private DABPlayHandler mHandler;

    private RTDialog rtDialog;

    private FmSetupDialog fmSetupDialog;

    private ParameterDialog parameterDialog;
    private RelativeLayout rlDABNoSignal;

    private TextView tvMultiType;

    private TextView tvTA;
    private TextView tvTF;

    /*以下是DAB公告设置部分*/
    //sf
    private RelativeLayout rlDABSF;//这个是用来响应点击整个项的情况
    private SettingSwitchView shDABSF;//这个是用来响应点击对应开关的情况

    //soft link
    private RelativeLayout rlDABSFSL;
    private SettingSwitchView shDABSFSL;

    //alarm
    private RelativeLayout rlDABAlarm;
    private SettingSwitchView shDABAlarm;

    //ta
    private RelativeLayout rlDABRTF;
    private SettingSwitchView shDABRTF;//TA

    //tf
    private RelativeLayout rlDABTF;
    private SettingSwitchView shDABTF;//Transport

    //warning
    private RelativeLayout rlDABWarning;
    private SettingSwitchView shDABWarning;

    //news
    private RelativeLayout rlDABNews;
    private SettingSwitchView shDABNews;

    //weather
    private RelativeLayout rlDABWeather;
    private SettingSwitchView shDABWeather;

    //event
    private RelativeLayout rlDABEvent;
    private SettingSwitchView shDABEvent;

    //special
    private RelativeLayout rlDABSpecial;
    private SettingSwitchView shDABSpecial;

    //program
    private RelativeLayout rlDABProgram;
    private SettingSwitchView shDABProgram;

    //sport
    private RelativeLayout rlDABSport;
    private SettingSwitchView shDABSport;

    //finance
    private RelativeLayout rlDABFinance;
    private SettingSwitchView shDABFinance;

    //DAB公告设置的数据结构，设置的内容都集中封装到里面传递给底层，底层也是返回同样的数据结构
    private DABAnnSwitch dabAnnSwitch;

    /*以上是DAB公告设置部分*/

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

    private RadioMessage preRadioMessage;

    private RadioCursor rcCursor;
    private RadioCursor2 rcCursor2;

    private boolean isRightRudder = ProductUtils.isRightRudder();
    private byte[] currentLogo;


    private ArrayList<String> testLogo = new ArrayList<>();

    private TypedValue value = new TypedValue();
    private TypedValue value1 = new TypedValue();

    private int currentScrollState = RecyclerView.SCROLL_STATE_IDLE;

    private boolean isOverlay2 = false;

    //点击搜索未搜到电台的情况下才Toast提示
    private boolean isClickScanBtn = false;

    //刻度尺
    protected RadioCursor radioCursor;

    protected RadioCursor2 radioCursor2;//主题二的刻度尺
    private ImageView  ivFreqPoint;
    @SuppressLint("Recycle")
    private ObjectAnimator albumRotation;
    @Override
    public int getLayoutResID() {
        return R.layout.fragment_multiplay_layout;
    }

    @Override
    public void initView(View view) {
        isOverlay2 = "overlay2".equals(ProductUtils.getTheme(getContext()));
        ivLogo = view.findViewById(R.id.ivLogo);
        tvProgramStationName = view.findViewById(R.id.tvProgramStationName);
        tvRadioText = view.findViewById(R.id.tvRadioText);
        //.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvProgramType = view.findViewById(R.id.tvProgramType);
        ivBack = view.findViewById(R.id.ivBack);
        ivDABPause = view.findViewById(R.id.ivDABPause);

        ivDABPre = view.findViewById(R.id.ivDABPre);
        ivDABNext = view.findViewById(R.id.ivDABNext);
        ivDABLike = view.findViewById(R.id.ivDABLike);
        ivDABSettings = view.findViewById(R.id.ivDABSettings);
        ivDABSearch = view.findViewById(R.id.ivDABSearch);
        rlEmptyView = view.findViewById(R.id.rlEmptyView);
        rvDABPlaySearching = view.findViewById(R.id.rvDABPlaySearching);
        ivDABLoading = view.findViewById(R.id.ivDABLoading);
        rlDABPlay = view.findViewById(R.id.rlDABPlay);
        rlDABSettings = view.findViewById(R.id.rlDABSettings);
        ivDABSettingsBack = view.findViewById(R.id.ivDABSettingsBack);
        slDABSettings = view.findViewById(R.id.slDABSettings);
        tvDABListCount = view.findViewById(R.id.tvDABListCount);
        ivSingArm = view.findViewById(R.id.ivSingArm);
        rlDABNoSignal = view.findViewById(R.id.rlDABNoSignal);

        tvMultiType = view.findViewById(R.id.tvMultiType);
        tvTA = view.findViewById(R.id.tvTA);
        tvTF = view.findViewById(R.id.tvTF);

        radioCursor = view.findViewById(R.id.rcCursor);
        radioCursor2 = view.findViewById(R.id.rcCursor2);
        ivFreqPoint = view.findViewById(R.id.ivFreqPoint);
        if (isOverlay2){
            radioCursor.setVisibility(View.GONE);
            radioCursor2.setVisibility(View.VISIBLE);
        }else {
            radioCursor2.setVisibility(View.GONE);
            radioCursor.setVisibility(View.VISIBLE);
        }

        rtDialog = new RTDialog(getContext(),R.style.dialogstyle);

        fmSetupDialog = new FmSetupDialog(getContext(), R.style.radio_dialogstyle);

        parameterDialog = new ParameterDialog(getContext(), R.style.radio_dialogstyle);

        rlDABPlayList = view.findViewById(R.id.rlDABPlayList);
        rlDABPlayList.setLayoutManager(new LinearLayoutManager(getContext()));
        playListAdapter = new MultiPlayListAdapter(getContext(),this);
        playListAdapter.setCanClickCollect(false);
        rlDABPlayList.setAdapter(playListAdapter);
        mHandler = new DABPlayHandler(this);

        //初始化设置项
        //sf
        rlDABSF = view.findViewById(R.id.rlDABSF);
        shDABSF = view.findViewById(R.id.shDABSF);
        //soft link
        rlDABSFSL = view.findViewById(R.id.rlDABSFSL);
        shDABSFSL = view.findViewById(R.id.shDABSFSL);
        //alarm
        rlDABAlarm = view.findViewById(R.id.rlDABAlarm);
        shDABAlarm = view.findViewById(R.id.shDABAlarm);
        //ta
        rlDABRTF = view.findViewById(R.id.rlDABRTF);
        shDABRTF = view.findViewById(R.id.shDABRTF);
        //transport
        rlDABTF = view.findViewById(R.id.rlDABTF);
        shDABTF = view.findViewById(R.id.shDABTF);
        //warning
        rlDABWarning = view.findViewById(R.id.rlDABWarning);
        shDABWarning = view.findViewById(R.id.shDABWarning);
        //news
        rlDABNews = view.findViewById(R.id.rlDABNews);
        shDABNews = view.findViewById(R.id.shDABNews);
        //weather
        rlDABWeather = view.findViewById(R.id.rlDABWeather);
        shDABWeather = view.findViewById(R.id.shDABWeather);
        //event
        rlDABEvent = view.findViewById(R.id.rlDABEvent);
        shDABEvent = view.findViewById(R.id.shDABEvent);
        //special
        rlDABSpecial = view.findViewById(R.id.rlDABSpecial);
        shDABSpecial = view.findViewById(R.id.shDABSpecial);
        //program
        rlDABProgram = view.findViewById(R.id.rlDABProgram);
        shDABProgram = view.findViewById(R.id.shDABProgram);
        //sport
        rlDABSport = view.findViewById(R.id.rlDABSport);
        shDABSport = view.findViewById(R.id.shDABSport);
        //finance
        rlDABFinance = view.findViewById(R.id.rlDABFinance);
        shDABFinance = view.findViewById(R.id.shDABFinance);

        ivRadioSearch = view.findViewById(R.id.ivRadioSearch);
        rlSearch = view.findViewById(R.id.rlSearch);
        svSearch = view.findViewById(R.id.svSearch);
        ivRadioScSearch  = view.findViewById(R.id.ivRadioScSearch);
        tvRadioScSearch = view.findViewById(R.id.tvRadioScSearch);
        etRadioSc = ((EditText)svSearch.findViewById(androidx.appcompat.R.id.search_src_text));
        etRadioSc.setHintTextColor(getResources().getColor(R.color.radio_text_sub_color));
        etRadioSc.setTextColor(getResources().getColor(R.color.radio_text_main_color));
        etRadioSc.setTextAlignment(EditText.TEXT_ALIGNMENT_VIEW_START);
        etRadioSc.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        etRadioSc.setLayoutParams(etRadioSc.getLayoutParams());
        etRadioSc.setTextDirection(View.TEXT_DIRECTION_LTR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            etRadioSc.setTextCursorDrawable(R.drawable.radio_searchview_cursor);
        }
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
    }

    @Override
    public void initData() {
        currentList  = RadioList.getInstance().getMultiRadioMessageList();
        currentList = RadioCovertUtils.sortWithName(getContext(),currentList);
    }

    @Override
    public void initViewListener() {
        ivBack.setOnClickListener(this);
        ivBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //parameterDialog.show();
                return false;
            }
        });

        radioCursor.setOnFrequencyChangeListener(onFrequencyListChangeListener);
        radioCursor2.setOnFrequencyChangeListener(onFrequencyListChangeListener);


        ivDABPre.setOnClickListener(this);
        ivDABPause.setOnClickListener(this);
        ivDABNext.setOnClickListener(this);
        ivDABSearch.setOnClickListener(this);
        ivDABLike.setOnClickListener(this);
        ivDABSettings.setOnClickListener(this);
        ivDABSettingsBack.setOnClickListener(this);

        rlDABSF.setOnClickListener(this);
        shDABSF.setOnCheckedChangeListener(this);
        //soft link
        rlDABSFSL.setOnClickListener(this);
        shDABSFSL.setOnCheckedChangeListener(this);
        //alarm
        rlDABAlarm.setOnClickListener(this);
        shDABAlarm.setOnCheckedChangeListener(this);
        //ta
        rlDABRTF.setOnClickListener(this);
        shDABRTF.setOnCheckedChangeListener(this);
        //transport
        rlDABTF.setOnClickListener(this);
        shDABTF.setOnCheckedChangeListener(this);
        //warning
        rlDABWarning.setOnClickListener(this);
        shDABWarning.setOnCheckedChangeListener(this);
        //news
        rlDABNews.setOnClickListener(this);
        shDABNews.setOnCheckedChangeListener(this);
        //weather
        rlDABWeather.setOnClickListener(this);
        shDABWeather.setOnCheckedChangeListener(this);
        //event
        rlDABEvent.setOnClickListener(this);
        shDABEvent.setOnCheckedChangeListener(this);
        //special
        rlDABSpecial.setOnClickListener(this);
        shDABSpecial.setOnCheckedChangeListener(this);
        //program
        rlDABProgram.setOnClickListener(this);
        shDABProgram.setOnCheckedChangeListener(this);
        //sport
        rlDABSport.setOnClickListener(this);
        shDABSport.setOnCheckedChangeListener(this);
        //finance
        rlDABFinance.setOnClickListener(this);
        shDABFinance.setOnCheckedChangeListener(this);

        ivDABPre.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        ivDABNext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        svSearch.setOnQueryTextListener(onQueryTextListener);
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
                rlDABPlay.setVisibility(View.GONE);
                ivBack.setVisibility(View.GONE);
                tvMultiType.setVisibility(View.GONE);
                svSearch.setQuery("",false);
                showHistoryList(true);
            }
        });
        ivRadioSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlSearch.setVisibility(View.GONE);
                rlDABPlay.setVisibility(View.VISIBLE);
                ivBack.setVisibility(View.VISIBLE);
                tvMultiType.setVisibility(View.VISIBLE);
                showHistoryList(false);
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

        rlDABPlayList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG,"onScrollStateChanged: " + newState);
                mHandler.removeMessages(DABMsg.MSG_NO_SCROLL);
                if (newState == RecyclerView.SCROLL_STATE_IDLE){//延迟5s再设置状态
                    mHandler.sendEmptyMessageDelayed(DABMsg.MSG_NO_SCROLL,DABMsg.NO_SCROLL_TIMEOUT);
                }else {
                    currentScrollState = newState;
                }
            }
        });


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
            rlSearchResult.setVisibility(View.VISIBLE);
            tvRadioScSearch.setEnabled(true);
            List<RadioMessage> resultList = RadioCovertUtils.getSearchResultList(newText, SPUtlis.getInstance().getIsShowCollectListMode());
            if (resultList.size() > 0) {
                rlSearchResultNone.setVisibility(View.GONE);
                rvSearchResultList.setVisibility(View.VISIBLE);
                searchResultListAdapter.updateList(resultList,newText);
            } else {
                rlSearchResultNone.setVisibility(View.VISIBLE);
                rvSearchResultList.setVisibility(View.GONE);
            }
        }else {
            rlSearchResult.setVisibility(View.GONE);
            tvRadioScSearch.setEnabled(false);
        }
    }
    private String currentQuery;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            Log.d(TAG,"onQueryTextSubmit:"+query);
            RadioList.getInstance().updateSearchHistoryList(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            //输入法第二次点击字母后，会先让用户预选，此时输入法会在输入框输入一个空格，
            //所以此处过滤输入的字母首尾空格
            android.view.textclassifier.Log.d(TAG,"onQueryTextChange:"+newText);
            showHistoryList(newText == null || newText.trim().isEmpty());
            showResultList(!(newText == null || newText.trim().isEmpty()),newText == null? null : newText.trim());
            currentQuery = newText == null? null : newText.trim();
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
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, resultMessage);
            rlSearch.setVisibility(View.GONE);
            rlDABPlay.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.VISIBLE);
            tvMultiType.setVisibility(View.VISIBLE);
            showHistoryList(false);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentScrollState = RecyclerView.SCROLL_STATE_IDLE;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        //监听电台内容的变化
        DABTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        //先更新一次列表数据，避免进来处于搜索时数据不更新
        List<RadioMessage> currentList = RadioList.getInstance().getMultiRadioMessageList();//ListUtils.getCurrentPlayList(getContext());
        tvDABListCount.setText(String.format(getResources().getString(R.string.radio_type_list)));
        updateAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentOnResume = true;
        Log.d(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentOnResume = false;
        isClickScanBtn = false;
        Log.d(TAG,"onPause");
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
        if (deleteHistoryDialog.isShowing()) {
            deleteHistoryDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        //监听电台内容的变化
        DABTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        if (null != loadingAnimator) {
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
        prePlayStatus = -1;
        if (null != fmSetupDialog && fmSetupDialog.isShowing()) {
            fmSetupDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        currentLogo = null;
        if (null != singArmAni) {
            singArmAni.cancel();
            singArmAni = null;
        }
        if (albumRotation != null) {
            albumRotation.cancel();
            albumRotation.end();
            albumRotation = null;
        }
    }


    @Override
    public void onClickDAB(RadioMessage radioMessage) {
        Log.d(TAG, "onClickDAB");
        Message message = new Message();
        message.what = DABMsg.MSG_CLICK_DAB;
        message.obj = radioMessage;
        mHandler.sendMessage(message);
    }

    @Override
    public void onCollectDAB(RadioMessage radioMessage) {
        Log.d(TAG, "onCollectDAB,radioMessage：" + radioMessage);
        Message message = new Message();
        message.what = DABMsg.MSG_COLLECT_DAB;
        message.obj = radioMessage;
        mHandler.sendMessage(message);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        DABMessage dabMessage = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage();
        if (R.id.ivDABPre == id){
            if (!ClickUtils.isAllowClick()) {
                return;
            }
            //pre
            updateCurrentScrollState();
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_SEEK_BACKWARD, ChangeReasonData.CLICK);
        }else if (R.id.ivDABNext == id){
            if (!ClickUtils.isAllowClick()) {
                return;
            }
            //next
            updateCurrentScrollState();
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_SEEK_FORWARD,ChangeReasonData.CLICK);
        }else if (R.id.ivDABPause == id){
            //play/stop
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.PLAY_OR_PAUSE,ChangeReasonData.CLICK);

        }else if (R.id.ivBack == id){
            ivBack.setVisibility(View.GONE);
            startLoadingAni(false);
            backClickListener.onDABBackClick();
        }else if (R.id.ivDABLike == id){
            if (ClickUtils.isAllowClick()) {//增加消抖处理
                if (DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().isCollect()) {
                    DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage());
                    ToastUtil.showToast(getContext(), getString(R.string.radio_cancel_collected));
                } else {
                    if (RadioList.getInstance().getMultiCollectRadioMessageList().size() > 29) {
                        ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
                    } else {
                        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage());
                    }
                }
            }
        }else if (R.id.ivDABSettings == id){
            if (DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getRadioType() == RadioMessage.FM_AM_TYPE){
                fmSetupDialog.show();
            }else {
                updateDABSettings();
                updateViewEnterSettings(true);
            }
        }else if (R.id.ivDABSettingsBack == id){
            updateViewEnterSettings(false);
        }else if (R.id.tvEPG == id){
            backClickListener.onDABPlayEnterEPGClick();
        }else if (R.id.ivDABSearch == id){
            isClickScanBtn = true;
            updateCurrentScrollState();
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_AST, ChangeReasonData.CLICK);
        }
    }

    /**
     * 进入或者退出DAB设置界面时，更新UI
     * @param enter
     */
    private void updateViewEnterSettings(boolean enter){
        Log.d(TAG,"updateViewEnterSettings:"+enter);
        if (enter){
            slDABSettings.scrollTo(0,0);
            rlDABSettings.setVisibility(View.VISIBLE);
            rlDABPlay.setVisibility(View.GONE);
            ivBack.setVisibility(View.GONE);
            tvMultiType.setVisibility(View.GONE);
        }else {
            rlDABSettings.setVisibility(View.GONE);
            rlDABPlay.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.VISIBLE);
            tvMultiType.setVisibility(View.VISIBLE);
        }
    }


    private void updateAll(){
        updateCurrentRadio();
        updateEffectList();
        updateCollectList();
//        updateDABSettings();
        updatePlayStatues();
        updateSearchStatues(false);//重新起来的时候，肯定处于非搜索状态
    }

    /**
     * 更新DAB设置项的显示
     */
    private static final int DAB_SETTING_ON = 1;
    private static final int DAB_SETTING_OFF = 0;
    public void updateDABSettings(){
        dabAnnSwitch = DABTrigger.getInstance().mRadioStatusTool.getDABAnnSwitchStatus();
        if (dabAnnSwitch != null) {//默认使用系统返回的值
            shDABSF.setChecked(dabAnnSwitch.getServiceFollow() == DAB_SETTING_ON);
            shDABSFSL.setChecked(dabAnnSwitch.getServiceFollow() == DAB_SETTING_ON);
            shDABAlarm.setChecked(dabAnnSwitch.getAlarm() == DAB_SETTING_ON);
            shDABRTF.setChecked(dabAnnSwitch.getRoadTrafficFlash() == DAB_SETTING_ON);
            shDABTF.setChecked(dabAnnSwitch.getTransportFlash() == DAB_SETTING_ON);
            shDABWarning.setChecked(dabAnnSwitch.getWarning() == DAB_SETTING_ON);
            shDABNews.setChecked(dabAnnSwitch.getNewsFlash() == DAB_SETTING_ON);
            shDABWeather.setChecked(dabAnnSwitch.getAreaWeatherFlash() == DAB_SETTING_ON);
            shDABEvent.setChecked(dabAnnSwitch.getEventAnnouncement() == DAB_SETTING_ON);
            shDABSpecial.setChecked(dabAnnSwitch.getSpecialEvent() == DAB_SETTING_ON);
            shDABProgram.setChecked(dabAnnSwitch.getProgramInformation() == DAB_SETTING_ON);
            shDABSport.setChecked(dabAnnSwitch.getSportReport() == DAB_SETTING_ON);
            shDABFinance.setChecked(dabAnnSwitch.getFinancialReport() == DAB_SETTING_ON);
        }
    }

    public void hideNoSignalWithTimeout(){
        Log.d(TAG,"hideNoSignalWithTimeout");
        rlDABNoSignal.setVisibility(View.GONE);
    }

    /**
     * 更新当前播放内容
     */
    private void updateCurrentRadio(){
        RadioMessage radioMessage = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG,"updateCurrentRadio，radioMessage："+radioMessage);
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE){
            tvMultiType.setText("FM");
            if (isOverlay2){
                radioCursor.setVisibility(View.GONE);
                radioCursor2.setVisibility(View.VISIBLE);
            }else {
                radioCursor2.setVisibility(View.GONE);
                radioCursor.setVisibility(View.VISIBLE);
            }
            ivFreqPoint.setVisibility(View.VISIBLE);
            String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage);
            if (radioName != null && radioName.trim().length() > 1){
                tvProgramStationName.setText(radioName);
            }else {
                tvProgramStationName.setText(radioMessage.getCalculateFrequency());
            }
            updateRDSFLag(DABTrigger.getInstance().mRadioStatusTool.getCurrentRDSFlagInfo());
            if (radioMessage.getRdsRadioText() != null){
                tvProgramType.setText(RadioCovertUtils.changeTypeToString(getContext(),radioMessage.getRdsRadioText().getProgramType()));
//                if (radioMessage.getRdsRadioText().getProgramStationName() != null && radioMessage.getRdsRadioText().getProgramStationName().trim().length() > 0){
//                    tvProgramStationName.setText(radioMessage.getRdsRadioText().getProgramStationName());
//                }
                if (radioMessage.getRdsRadioText().getRadioText() != null){
                    tvRadioText.setText(radioMessage.getRdsRadioText().getRadioText());
                }else {
                    tvRadioText.setText("");
                }
            }else {
                tvProgramType.setText("");
                tvRadioText.setText("");
            }
            ivLogo.setImageResource(R.mipmap.img_play_dab);

            if (isOverlay2) {
                radioCursor2.setBand(radioMessage.getRadioBand());
                radioCursor2.setFrequency(radioMessage.getRadioFrequency());
                radioCursor2.updateRadioMessage(radioMessage);
            }else {
                radioCursor.setBand(radioMessage.getRadioBand());
                radioCursor.setFrequency(radioMessage.getRadioFrequency());
                radioCursor.updateRadioMessage(radioMessage);
            }
        }else {
            tvMultiType.setText("DAB");
            radioCursor.setVisibility(View.GONE);
            radioCursor2.setVisibility(View.GONE);
            ivFreqPoint.setVisibility(View.GONE);
            tvTA.setVisibility(View.GONE);
            tvTF.setVisibility(View.GONE);
            rlDABNoSignal.setVisibility(View.GONE);
            //优先使用Sls
            byte[] logoList = radioMessage.getDabMessage().getSlsDataList();
            //次级使用存储的Logo
            if (logoList == null || logoList.length < 1){
                logoList = ListUtils.getOppositeDABLogo(radioMessage);
            }
            //最后使用当前获取到的Logo
            if (logoList == null){
                logoList = radioMessage.getDabMessage().getLogoDataList();
            }
            if (currentLogo != null && Arrays.equals(currentLogo, logoList)){
                Log.d(TAG,"updateCurrentRadio，currentLogo is same");
            }else {
                Log.d(TAG,"updateCurrentRadio，update currentLogo");
                if ((currentLogo == null || currentLogo.length == 0) && (logoList == null || logoList.length == 0)){

                }else {
//                    //分段打印一下当前的logo数据，拿来本地调试显示效果
//                   for (int i = 0;i<logoList.length;i++){
//                       testLogo.add(String.valueOf(logoList[i]));
//                       if ( i % 100 == 0){
//                           Log.d(TAG,"logo[" + i + "]" + testLogo);
//                       }
//                   }
                    RequestOptions option = RequestOptions
                            .bitmapTransform(new RoundedCorners(8))
                            .error(R.mipmap.img_play_dab);
                    Glide.with(this).load(logoList)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .apply(option)
                            .into(ivLogo);
                }
            }
            currentLogo = logoList;
            tvProgramStationName.setText(radioMessage.getDabMessage().getShortProgramStationName());
            tvProgramType.setText(CtgUtils.changeTypeToString(getContext(), radioMessage.getDabMessage().getProgramType()));
            if (radioMessage.getDabMessage().getDynamicLabel() != null && radioMessage.getDabMessage().getDynamicLabel().length() > 1) {// RT 内容不为空
                tvRadioText.setText(radioMessage.getDabMessage().getDynamicLabel());
            } else {
                tvRadioText.setText("");
            }
        }
        ivDABLike.setSelected(radioMessage.isCollect());
        rlDABPlayList.post(new Runnable() {
            @Override
            public void run() {
                if (needScrollPosition(radioMessage)){
                    Log.d(TAG,"needScrollPosition");
                    if (currentList.size() != 0) {
                        for (int i = 0; i < currentList.size(); i++) {//找到当前播放项在列表的位置
                            if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE){
                                if (radioMessage.getRadioFrequency() == currentList.get(i).getRadioFrequency()){
                                    rlDABPlayList.scrollToPosition(i);
                                    Log.d(TAG,"updateCurrentRadio start scroll index = " + i);
                                }
                            }else {
                                if (CompareUtils.isSameDAB(radioMessage,currentList.get(i))){
                                    rlDABPlayList.scrollToPosition(i);
                                    Log.d(TAG,"updateCurrentRadio start scroll index = " + i);
                                }
                            }
                        }
                    }
                }
                preRadioMessage = radioMessage.Clone();
                // 刷新列表 ，更新当前播放高亮位置
                playListAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean needScrollPosition(RadioMessage radioMessage){
        /*if (preRadioMessage == null){
            return true;
        }
        if (preRadioMessage.getRadioBand() == radioMessage.getRadioBand()){

            if (preRadioMessage.getRadioType() == radioMessage.getRadioType()){
                if (preRadioMessage.getRadioType() == RadioMessage.DAB_TYPE){
                    return !CompareUtils.isSameDAB(preRadioMessage,radioMessage);
                }else {
                    return preRadioMessage.getRadioFrequency() != radioMessage.getRadioFrequency();
                }
            }else {
                return true;
            }

        }else {
            return true;
        }*/
        return currentScrollState == RecyclerView.SCROLL_STATE_IDLE;
    }

    public void updateCurrentScrollState(){
        Log.d(TAG,"updateCurrentScrollState");
        currentScrollState = RecyclerView.SCROLL_STATE_IDLE;
    }

    /**
     * 根据RDSFlag状态更新界面
     */
    public void updateRDSFLag(RDSFlagInfo info) {
        Log.d(TAG, "updateRDSFLag,info:" + info);
        if (info != null && DABTrigger.getInstance().mRadioStatusTool.getRDSSettingsSwitchStatus() != null) {
            tvTA.setVisibility(DABTrigger.getInstance().mRadioStatusTool.getRDSSettingsSwitchStatus() != null && DABTrigger.getInstance().mRadioStatusTool.getRDSSettingsSwitchStatus().getTa() == 1 && info.getTp() == 1?View.VISIBLE : View.GONE);
            tvTF.setVisibility(info.getAf() == 1 ? DABTrigger.getInstance().mRadioStatusTool.getRDSSettingsSwitchStatus() != null && DABTrigger.getInstance().mRadioStatusTool.getRDSSettingsSwitchStatus().getAf() == 1 ?View.VISIBLE : View.GONE : View.GONE);
//            //DAB的情况下要隐藏
            if (DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getRadioType() != RadioMessage.FM_AM_TYPE){
                tvTA.setVisibility(View.GONE);
                tvTF.setVisibility(View.GONE);
            }
        } else {
            tvTA.setVisibility(View.GONE);
            tvTF.setVisibility(View.GONE);
        }
    }

    public void updateViewWithRDSSettingsChanged(RDSSettingsSwitch rdsSettingsSwitch) {
        Log.d(TAG, "updateViewWithRDSSettingsChanged,:" + rdsSettingsSwitch);
        if (rdsSettingsSwitch == null) {
            return;
        }
        if (rdsSettingsSwitch.getRds() == 1 && DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_FM) {//RDS开关打开
            //根据各自状态设置
            tvTA.setVisibility(rdsSettingsSwitch.getTa() == 1 ? DABTrigger.getInstance().mRadioStatusTool.getCurrentRDSFlagInfo() != null && DABTrigger.getInstance().mRadioStatusTool.getCurrentRDSFlagInfo().getTp() == 1 ?View.VISIBLE:View.GONE : View.GONE);
            tvTF.setVisibility(rdsSettingsSwitch.getAf() == 1 ? DABTrigger.getInstance().mRadioStatusTool.getCurrentRDSFlagInfo() != null && DABTrigger.getInstance().mRadioStatusTool.getCurrentRDSFlagInfo().getAf() == 1 ?View.VISIBLE:View.GONE : View.GONE);
        } else {//RDS开关关闭，那么全部都要隐藏
            tvTA.setVisibility(View.GONE);
            tvTF.setVisibility(View.GONE);
        }
    }

    /**
     * 更新播放状态
     */
    private int prePlayStatus = -1;//-1表示初始值，避免首次的问题。0是暂停，1是播放
    private void updatePlayStatues(){
        boolean isPlaying = DABTrigger.getInstance().mRadioStatusTool.isPlaying();
        ivDABPause.setSelected(!isPlaying);
        playListAdapter.notifyDataSetChanged();
        updatePlayAnim(isPlaying);
        if (isPlaying){
            // 初始化旋转动画，旋转中心默认为控件中点
            getResources().getValue(R.dimen.dab_ivsingarm_roate_0,value,true);
            getResources().getValue(R.dimen.dab_ivsingarm_roate_1,value1,true);

            singArmAni = ObjectAnimator.ofFloat(ivSingArm, "rotation",value.getFloat(),value1.getFloat());
        }else {
            getResources().getValue(R.dimen.dab_ivsingarm_roate_1,value,true);
            getResources().getValue(R.dimen.dab_ivsingarm_roate_0,value1,true);

            singArmAni = ObjectAnimator.ofFloat(ivSingArm, "rotation",value.getFloat(),value1.getFloat());
        }
        if (prePlayStatus != -1 && ((prePlayStatus == 1 && isPlaying) || (prePlayStatus == 0 && !isPlaying) )){
            Log.d(TAG,"play status is same, return singArmAni");
            return;
        }
        prePlayStatus = isPlaying ? 1 : 0;
        singArmAni.setDuration(1000);
        singArmAni.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
        singArmAni.start();
    }



    /**
     * 根据搜索状态更新界面
     *
     * @param isSearching isSearching
     */
    public void updateSearchStatues(boolean isSearching) {
        if (isSearching) {
            rlDABPlayList.setVisibility(View.GONE);
            rlEmptyView.setVisibility(View.GONE);
            startLoadingAni(true);
        } else {
            startLoadingAni(false);
            rlDABPlayList.setVisibility(View.VISIBLE);
            updateEffectList();
        }
    }

    private ObjectAnimator loadingAnimator;

    protected void startLoadingAni(boolean start) {
        Log.d(TAG, "startLoadingAni: start = " + start);
        if (loadingAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingAnimator = ObjectAnimator.ofFloat(ivDABLoading, "rotation", 0f, 360f);
            loadingAnimator.setDuration(1000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        Log.d(TAG, "startLoadingAni: isStarted = " + loadingAnimator.isStarted() + " isRunning = " + loadingAnimator.isRunning());
        if (start) {
            rvDABPlaySearching.setVisibility(View.VISIBLE);
            Log.d(TAG, "startLoadingAni: start");
            loadingAnimator.start();
        } else {
            loadingAnimator.cancel();
            rvDABPlaySearching.setVisibility(View.GONE);
            Log.d(TAG, "startLoadingAni: end");
        }
    }



    /**
     * 播放动效
     */
    private void updatePlayAnim(boolean isPlaying) {
        //专辑图旋转
        if (albumRotation == null) {
            albumRotation = ObjectAnimator.ofFloat(ivLogo, "rotation", 0f, 360.0f);
            albumRotation.setDuration(10000);
            albumRotation.setInterpolator(new LinearInterpolator());
            albumRotation.setRepeatCount(ObjectAnimator.INFINITE);
        }
        if (isPlaying){
            if (albumRotation.isStarted()) {
                albumRotation.resume();
            } else {
                albumRotation.start();
            }
        }else {
            albumRotation.pause();
        }
    }

    List<RadioMessage> currentList;
    /**
     * 有效列表更新
     */
    private void updateEffectList(){
        Log.d(TAG,"updateEffectList");
        if (DABTrigger.getInstance().mRadioStatusTool.isSearching()){
            return;
        }
        if (SPUtlis.getInstance().getIsShowCollectListMode()) {
            //显示收藏列表
            tvDABListCount.setText(getResources().getString(R.string.radio_collect_list));
            currentList = RadioList.getInstance().getCurrentCollectRadioMessageList();
        } else {
            tvDABListCount.setText(getResources().getString(R.string.radio_type_list));
            currentList  = RadioList.getInstance().getMultiRadioMessageList();//ListUtils.getCurrentPlayList(getContext());
            currentList = RadioCovertUtils.sortWithName(getContext(),currentList);
        }
        playListAdapter.updateDabList(currentList);
        playListAdapter.notifyDataSetChanged();
        if (currentList.size() < 1){
            rlEmptyView.setVisibility(View.VISIBLE);
            rlDABPlayList.setVisibility(View.GONE);
            ivRadioSearch.setEnabled(false);
            updateViewWithSignal(false);
        }else {
            rlEmptyView.setVisibility(View.GONE);
            rlDABPlayList.setVisibility(View.VISIBLE);
            ivRadioSearch.setEnabled(true);
            updateViewWithSignal(true);
            RadioMessage radioMessage = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
            Log.d(TAG,"updateEffectList scroll to position begin，radioMessage："+radioMessage);
            rlDABPlayList.post(new Runnable() {
                @Override
                public void run() {
                    if (needScrollPosition(radioMessage)){
                        if (currentList.size() != 0) {
                            for (int i = 0; i < currentList.size(); i++) {//找到当前播放项在列表的位置
                                if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE){
                                    if (radioMessage.getRadioFrequency() == currentList.get(i).getRadioFrequency()){
                                        rlDABPlayList.scrollToPosition(i);
                                    }
                                }else {
                                    if (CompareUtils.isSameDAB(radioMessage,currentList.get(i))){
                                        rlDABPlayList.scrollToPosition(i);
                                    }
                                }
                            }
                        }
                    }
                    preRadioMessage = radioMessage.Clone();
                }
            });
        }
    }

    /**
     * 收藏列表更新
     */
    private void updateCollectList(){
        Log.d(TAG,"updateCollectList");
        if (SPUtlis.getInstance().getIsShowCollectListMode()) {
            updateEffectList();
        } else {
            playListAdapter.notifyDataSetChanged();
        }
        ivDABLike.setSelected(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().isCollect());
    }

    public void updateLogo(byte[] logoByte){
        if (currentLogo != null && Arrays.equals(currentLogo, logoByte)){

        }else {
            RequestOptions option = RequestOptions
                    .bitmapTransform(new RoundedCorners(8))
                    .error(com.desaysv.moduledab.R.mipmap.img_play_dab);
            Glide.with(getContext()).load(logoByte)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .apply(option)
                    .into(ivLogo);
            currentLogo = logoByte;
        }
    }

    /**
     * 根据是否有信号或者列表置灰/高亮对应按键
     * @param hasSignal
     */
    public void updateViewWithSignal(boolean hasSignal){
        Log.d(TAG,"updateViewWithSignal:"+hasSignal);
        ivDABLike.setEnabled(hasSignal);
    }

    /**
     * 处理点击事件
     * @param radioMessage
     */
    public void handleClickDAB(RadioMessage radioMessage){
        Log.d(TAG,"handleClickDAB,radioMessage:"+radioMessage);
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
    }

    /**
     * 处理收藏逻辑
     * @param radioMessage
     */
    private void handleCollectDAB(RadioMessage radioMessage){
        Log.d(TAG,"handleCollectDAB,radioMessage:"+radioMessage);
        if (radioMessage.isCollect()) {
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, radioMessage);
            ToastUtil.showToast(getContext(), getString(R.string.radio_cancel_collected));
        }else {
            if (RadioList.getInstance().getMultiCollectRadioMessageList().size() > 29){
                ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
            }else {
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, radioMessage);
            }
        }
    }


    private final IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG,"onCurrentRadioMessageChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_RADIO);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG,"onPlayStatusChange,isPlaying:"+isPlaying);
            Message msg = new Message();
            msg.what = DABMsg.MSG_UPDATE_PLAY_STATUES;
            msg.obj = isPlaying;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG,"onAstListChanged");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_EFFECT_LIST);
        }

        @Override
        public void onSearchStatusChange(boolean isSearching) {
            Log.d(TAG,"onSearchStatusChange");
            Message msg = new Message();
            msg.what = DABMsg.MSG_UPDATE_SEARCH;
            msg.obj = isSearching;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG,"onSeekStatusChange");
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {
            Log.d(TAG,"onAnnNotify");
        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG,"onRDSFlagInfoChange");
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_RDS_FLAG, info));
        }

        @Override
        public void onDABLogoChanged(byte[] logoByte) {
//            Message msg = new Message();
//            msg.what = DABMsg.MSG_UPDATE_LOGO;
//            msg.obj = logoByte;
//            mHandler.sendMessage(msg);
        }

        @Override
        public void onRDSSettingsStatus(RDSSettingsSwitch rdsSettingsSwitch) {
            Log.d(TAG, "onRDSSettingsStatus: rdsSettingsSwitch = " + rdsSettingsSwitch);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RDS_SETTINGS, rdsSettingsSwitch));
        }
    };


    private final IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG,"onFMCollectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_COLLECT_LIST);
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG,"onAMCollectListChange");
        }

        @Override
        public void onDABCollectListChange() {
            Log.d(TAG,"onDABCollectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_COLLECT_LIST);
        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG,"onFMEffectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_EFFECT_LIST);
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG,"onAMEffectListChange");
        }

        @Override
        public void onDABEffectListChange() {
            Log.d(TAG,"onDABEffectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_EFFECT_LIST);
        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG,"onFMAllListChange");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG,"onAMAllListChange");
        }

        @Override
        public void onDABAllListChange() {
            Log.d(TAG,"onDABAllListChange");
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG,"onCheckedChanged,isChecked:"+isChecked);
        int id = buttonView.getId();
        if (dabAnnSwitch != null) {
            if (id == R.id.shDABSF){//HardLink，需要HAL确认协议
                dabAnnSwitch.setServiceFollow(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：HardLink
                PointTrigger.getInstance().trackEvent(Point.KeyName.HserFollowSwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABSFSL) {//SoftLink，需要HAL确认协议
                dabAnnSwitch.setServiceFollow(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：SoftLink
                PointTrigger.getInstance().trackEvent(Point.KeyName.SserFollowSwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABAlarm) {
                dabAnnSwitch.setAlarm(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Alarm
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_ALARM,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (buttonView.getId() == R.id.shDABRTF) {
                dabAnnSwitch.setRoadTrafficFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：TA
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_TA,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABTF) {
                dabAnnSwitch.setTransportFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：TF
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_TF,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABWarning) {
                dabAnnSwitch.setWarning(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Waring
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Warning,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABNews) {
                dabAnnSwitch.setNewsFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：News
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_News,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABWeather) {
                dabAnnSwitch.setAreaWeatherFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Weather
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Weather,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABEvent) {
                dabAnnSwitch.setEventAnnouncement(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Event
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Event,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABSpecial) {
                dabAnnSwitch.setSpecialEvent(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Special
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Special,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABProgram) {
                dabAnnSwitch.setProgramInformation(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Program
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Program,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABSport) {
                dabAnnSwitch.setSportReport(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Sport
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Sport,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABFinance) {
                dabAnnSwitch.setFinancialReport(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Finance
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Finance,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            }
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.SET_DAB_ANN_SWITCH,ChangeReasonData.CLICK,dabAnnSwitch);
        }
    }


    private static class DABPlayHandler extends Handler {

        private WeakReference<MultiPlayFragment> weakReference;

        public DABPlayHandler(MultiPlayFragment multiPlayFragment){
            weakReference = new WeakReference<>(multiPlayFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final MultiPlayFragment multiPlayFragment = weakReference.get();
            Log.d(TAG,"handleMessage:"+msg.what);
            if (multiPlayFragment == null || multiPlayFragment.isDetached()){
                Log.d(TAG, "multiPlayFragment.isDetach");
                return;
            }
            switch (msg.what){
                case DABMsg.MSG_UPDATE_RADIO:
                    multiPlayFragment.updateCurrentRadio();
                    break;
                case DABMsg.MSG_UPDATE_DAB_EFFECT_LIST:
                    multiPlayFragment.updateEffectList();
                    break;
                case DABMsg.MSG_UPDATE_DAB_COLLECT_LIST:
                    multiPlayFragment.updateCollectList();
                    break;
                case DABMsg.MSG_CLICK_DAB:
                    multiPlayFragment.handleClickDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_COLLECT_DAB:
                    multiPlayFragment.handleCollectDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_UPDATE_PLAY_STATUES:
                    multiPlayFragment.updatePlayStatues();
                    break;
                case DABMsg.MSG_UPDATE_SEARCH:
                    multiPlayFragment.updateSearchStatues((Boolean) msg.obj);
                    if (!(Boolean) msg.obj) {
                        //搜索结束
                        removeMessages(RadioConstants.MSG_TIPS);
                        sendEmptyMessageDelayed(RadioConstants.MSG_TIPS, 200);
                    }
                    break;
                case DABMsg.MSG_UPDATE_LOGO:
                    multiPlayFragment.updateLogo((byte[]) msg.obj);
                    break;
                case DABMsg.MSG_NO_SIGNAL_TIMEOUT:
                    multiPlayFragment.hideNoSignalWithTimeout();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_RDS_FLAG:
                    multiPlayFragment.updateRDSFLag((RDSFlagInfo) msg.obj);
                    break;
                case DABMsg.MSG_NO_SCROLL:
                    multiPlayFragment.updateCurrentScrollState();
                    break;
                case RadioConstants.MSG_TIPS:
                    multiPlayFragment.updateSearchTips(true);
                    break;
                case RadioConstants.MSG_UPDATE_RDS_SETTINGS:
                    multiPlayFragment.updateViewWithRDSSettingsChanged((RDSSettingsSwitch) msg.obj);
                default:
                    break;
            }
        }
    }

    private boolean isFragmentOnResume = false;

    private OnFrequencyChangedListener onFrequencyListChangeListener = new OnFrequencyChangedListener() {

        @Override
        public void onChanged(int band, float frequency) {
            Log.d(TAG, "onChanged: band = " + band + " frequency = " + frequency);
            //列表滚动导致界面刷新要单独开来，避免两个刷新互相影响,这个只是滚动用的，值刷新显示，不刷新播放状态
            RadioMessage radioMessage = new RadioMessage(band, (int) frequency);
//            updateRadioFrequency(radioMessage);
        }

        @Override
        public void onChangedAndOpenIt(int band, float frequency) {
            RadioMessage radioMessage = new RadioMessage(band, (int) frequency);
            Log.d(TAG, "onChangedAndOpenIt: radioMessage = " + radioMessage + " isFragmentOnResume = " + isFragmentOnResume);
            //add by lzm 只有界面在前台的时候才能回调，不然会出现滑动过程中切换到USB音乐界面，USB音乐界面音源被抢
            if (isFragmentOnResume) {
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.SLIDING_LIST, radioMessage);
            }
        }
    };

    /**
     * 显示暂未搜索到有效的电台Toast
     * @param show
     */
    public void updateSearchTips(boolean show){
        Log.d(TAG,"updateSearchTips:" + show);
        if (show) {
            if (!isClickScanBtn) return;
            isClickScanBtn = false;
            if (currentList == null || currentList.isEmpty()){
                ToastUtil.showToast(getContext(), getString(R.string.radio_search_no_list));
            }
        }
    }

    /**
     * 左上角返回按钮的点击事件
     */
    private IOnBackClickListener backClickListener;

    public void setBackClickListener(IOnBackClickListener backClickListener){
        this.backClickListener = backClickListener;
    }

    public interface IOnBackClickListener{

        void onDABBackClick();

        /**
         * 在DAB播放界面进入到列表页
         */
        void onDABPlayEnterListClick();

        /**
         * 在DAB播放界面进入到EPG
         */
        void onDABPlayEnterEPGClick();
    }
}
