package com.desaysv.moduledab.fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduledab.common.DABMsg;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.svlibtoast.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * created by ZNB on 2022-12-21
 * DAB播放页进入的电台列表的基类
 */
public class DABListBaseFragment extends BaseFragment implements IDABOperationListener {

    private static final String TAG = "DABListBaseFragment";

    public DABListFragmentHandler mHandler;

    public int currentListType = Constant.LIST_TYPE_ALL;

    public int currentId = -1;//ensembleID 或 programType

    //这个是子类要单独复写，这里默认给ALL
    @Override
    public int getLayoutResID() {
        return R.layout.fragment_dab_listall_layout;
    }

    @Override
    public void initView(View view) {
        Log.d(TAG,"initView,"+this);
        mHandler = new DABListFragmentHandler(this);
        if(ProductUtils.isRightRudder()){
            view.setRotationY(180);
        }
    }

    @Override
    public void initData() {
        Log.d(TAG,"initData,"+this);
    }

    @Override
    public void initViewListener() {
        Log.d(TAG,"initViewListener");
    }

    //这个是用来打开播放DAB的
    @Override
    public void onClickDAB(RadioMessage radioMessage) {
        Message message = new Message();
        message.what = DABMsg.MSG_CLICK_DAB;
        message.obj = radioMessage;
        mHandler.sendMessage(message);
    }

    @Override
    public void onCollectDAB(RadioMessage radioMessage) {
        mHandler.removeMessages(DABMsg.MSG_COLLECT_DAB);
        Message message = new Message();
        message.what = DABMsg.MSG_COLLECT_DAB;
        message.obj = radioMessage;
        mHandler.sendMessageDelayed(message,50);
    }

    //这个是用来打开二级界面的
    @Override
    public void onClickDAB(RadioMessage radioMessage, boolean enterNextList) {
        handlerClickItem(radioMessage, enterNextList);
    }

    /**
     * 处理 Item的点击事件
     * 有些界面是有二级列表的
     * @param enterNextList
     */
    public void handlerClickItem(RadioMessage radioMessage, boolean enterNextList){
        if (enterNextList){
            if (enterNextListener != null) {
                enterNextListener.onEnterNextList(true);
            }
            updateCurrentListType(radioMessage,true);
            updateList();
        }else {
            Message message = new Message();
            message.what = DABMsg.MSG_CLICK_DAB;
            message.obj = radioMessage;
            mHandler.sendMessage(message);
        }
    }


    public void updateList(){

    }


    public void updateCurrentInfo(){

    }


    public void updatePlayStatus(){

    }

    public void updateSearch(boolean isSearch){

    }

    /**
     * 设置当前的列表类型，由子类控制
     * 这个实际只需要 集合分类、类别分类时处理
     */
    public void updateCurrentListType(RadioMessage radioMessage, boolean enterNextList){

    }


    public int getCurrentListType(){
        Log.d(TAG,"getCurrentListType:"+currentListType);
        return currentListType;
    }


    /**
     * 按照UI，应该要分出 6 种 列表类型
     * 由子类去控制
     * @return
     */
    public List<RadioMessage> getCurrentShowList(){
        return RadioList.getInstance().getDABEffectRadioMessageList();
    }

    /**
     * 处理点击逻辑，真正打开DAB进行播放的操作
     *
     * @param radioMessage
     */
    public void handleClickDAB(RadioMessage radioMessage) {
        //更新播放列表
        if (getCurrentListType() == Constant.LIST_TYPE_CATEGORY_SUB){//类别分类列表
            ListUtils.savePlayListTag(getContext(),getCurrentListType(),radioMessage.getDabMessage().getProgramType());
        }else if (getCurrentListType() == Constant.LIST_TYPE_ENSEMBLE_SUB){//集合分类列表
            ListUtils.savePlayListTag(getContext(),getCurrentListType(),radioMessage.getDabMessage().getEnsembleId());
        }else {
            ListUtils.savePlayListTag(getContext(),getCurrentListType(),-1);
        }

        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
        if (enterNextListener != null) {
            enterNextListener.onOpenDABRadio();
        }
    }

    /**
     * 处理收藏逻辑
     *
     * @param radioMessage
     */
    public void handleCollectDAB(RadioMessage radioMessage) {
        if (radioMessage.isCollect()) {
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, radioMessage);
        } else {
            if (RadioList.getInstance().getDABCollectRadioMessageList().size() > 19){
                ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
            }else {
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, radioMessage);
            }
        }
    }


    /**
     * 进入或退出二级列表、播放列表某项DAB电台的回调
     */
    public IOnEnterNextListener enterNextListener;

    public void setOnEnterNextListener(IOnEnterNextListener enterNextListener){
        this.enterNextListener = enterNextListener;
    }

    public interface IOnEnterNextListener{

        //进入或退出二级列表
        void onEnterNextList(boolean enterNextList);
        //播放列表某项DAB电台
        void onOpenDABRadio();
    }



    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        //监听电台内容的变化
        DABTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        updateList();
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
        DABTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
    }

    private final IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG, "onCurrentRadioMessageChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_RADIO);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChange,isPlaying:" + isPlaying);
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_PLAY_STATUES);
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG, "onAstListChanged");
        }

        @Override
        public void onSearchStatusChange(boolean isSearching) {
            Log.d(TAG, "onSearchStatusChange");
            Message msg = new Message();
            msg.what = DABMsg.MSG_UPDATE_SEARCH;
            msg.obj = isSearching;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG, "onSeekStatusChange");
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {
            Log.d(TAG, "onAnnNotify");
        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG, "onRDSFlagInfoChange");
        }
    };


    private final IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG, "onFMCollectListChange");
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG, "onAMCollectListChange");
        }

        @Override
        public void onDABCollectListChange() {
            Log.d(TAG, "onDABCollectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_COLLECT_LIST);
        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG, "onFMEffectListChange");
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG, "onAMEffectListChange");
        }

        @Override
        public void onDABEffectListChange() {
            Log.d(TAG, "onDABEffectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_EFFECT_LIST);
        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG, "onFMAllListChange");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG, "onAMAllListChange");
        }

        @Override
        public void onDABAllListChange() {
            Log.d(TAG, "onDABAllListChange");
        }
    };


    private static class DABListFragmentHandler extends Handler {

        private WeakReference<DABListBaseFragment> weakReference;

        public DABListFragmentHandler(DABListBaseFragment dabFragment) {
            weakReference = new WeakReference<>(dabFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final DABListBaseFragment dabListBaseFragment = weakReference.get();
            Log.d(TAG, "handleMessage:" + msg.what);
            switch (msg.what) {
                case DABMsg.MSG_UPDATE_RADIO:
                case DABMsg.MSG_UPDATE_DAB_COLLECT_LIST:
                case DABMsg.MSG_UPDATE_DAB_EFFECT_LIST:
                    dabListBaseFragment.updateList();
                    break;
                case DABMsg.MSG_CLICK_DAB:
                    dabListBaseFragment.handleClickDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_COLLECT_DAB:
                    dabListBaseFragment.handleCollectDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_UPDATE_PLAY_STATUES:
                    dabListBaseFragment.updatePlayStatus();
                    break;
                case DABMsg.MSG_UPDATE_SEARCH:
                    dabListBaseFragment.updateSearch((Boolean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

}
