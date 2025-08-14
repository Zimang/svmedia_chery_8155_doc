package com.desaysv.moduleradio.ui;

import android.hardware.radio.RadioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioMessageListAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;

import java.lang.ref.WeakReference;


public abstract class RadioBaseListFragment extends BaseFragment implements RadioMessageListAdapter.OnItemClickListener {

    protected final String TAG = this.getClass().getSimpleName();

    protected IStatusTool mGetRadioStatusTool;
    protected IControlTool mRadioControlTool;

    protected boolean isItemClick = false;//判断是否item点击

    public RadioBaseListFragment() {
        //初始化需要创建，因为在RadioMainFragment的initData当中调用，而如果此时ListFragment仅仅是创建了实例，调用则出现空指针报错
        mGetRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        mRadioControlTool = ModuleRadioTrigger.getInstance().mRadioControl;
    }

    public MyHandler mHandler;

    private static class MyHandler extends Handler {
        WeakReference<RadioBaseListFragment> weakReference;

        MyHandler(RadioBaseListFragment radioBaseListFragment) {
            weakReference = new WeakReference<>(radioBaseListFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final RadioBaseListFragment radioBaseListFragment = weakReference.get();
//            Log.d(radioBaseListFragment.TAG, "handleMessage:" + msg.what + "  isVisible = " + radioBaseListFragment.isVisible());
            if (radioBaseListFragment == null){
                return;
            }
            // 只有界面显示了，才进行接收数据
            Lifecycle lifecycle = radioBaseListFragment.getLifecycle();
            Log.d(radioBaseListFragment.TAG, "handleMessage: lifecycle = " + lifecycle);
            if (null != lifecycle) {
                Lifecycle.State currentState = lifecycle.getCurrentState();
                Log.d(radioBaseListFragment.TAG, "handleMessage: currentState = " + currentState);
                if (Lifecycle.State.RESUMED != currentState) {
                    return;
                }
            }
            Log.d(radioBaseListFragment.TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE:
                    radioBaseListFragment.updateRadioMessageView();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS:
                    radioBaseListFragment.updatePlayAnim();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_FM_LIST:
                    removeMessages(RadioConstants.MSG_SCAN_TIMEOUT);
                    radioBaseListFragment.updateFMListFragment();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST:
                    radioBaseListFragment.updateCollectListFragment();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_AM_LIST:
                    removeMessages(RadioConstants.MSG_SCAN_TIMEOUT);
                    radioBaseListFragment.updateAMListFragment();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS:
                    radioBaseListFragment.updateSearchingView((Boolean) msg.obj);
                    break;
                //搜索倒计时
                case RadioConstants.MSG_SCAN_TIMEOUT:
                    radioBaseListFragment.updateViewWithScanTimeOut();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void initData() {
        mHandler = new MyHandler(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //更新列表界面
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(RadioConstants.MSG_SCAN_TIMEOUT);
        // 界面不可见是，设置界面搜索状态为false
        updateSearchingView(false);
        mRadioControlTool.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
    }

    @Override
    public void onStop() {
        super.onStop();
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        ModuleRadioTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG, "onFMCollectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST);
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG, "onAMCollectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST);
        }

        @Override
        public void onDABCollectListChange() {
            Log.d(TAG, "onDABCollectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST);
        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG, "onFMEffectListChange: ");
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG, "onAMEffectListChange: ");
        }

        @Override
        public void onDABEffectListChange() {
        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG, "onFMAllListChange: ");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG, "onAMAllListChange: ");
        }

        @Override
        public void onDABAllListChange() {
        }
    };

    IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG, "onCurrentRadioMessageChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS);
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG, "onAstListChanged: ");
            if (band == RadioManager.BAND_FM) {
                mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
            } else if (band == RadioManager.BAND_AM) {
                mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_AM_LIST);
            }else {
                mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
            }
        }

        @Override
        public void onSearchStatusChange(final boolean isSearching) {
            Log.d(TAG, "onSearchStatusChange: isSearching = " + isSearching);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS, isSearching));
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG, "onSeekStatusChange: isSeeking = " + isSeeking);
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {

        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {

        }
    };

    /**
     * 更新搜索界面
     *
     * @param isSearching 是否正在搜素
     */
    public abstract void updateSearchingView(boolean isSearching);

    /**
     * 更新当前播放电台图标
     */
    public abstract void updateRadioMessageView();

    /**
     * 收藏电台数量发生改变时，刷新列表
     */
    void updateCollectListFragment() {
    }

    /**
     * 有效电台发生改变时，刷新列表
     */
    void updateFMListFragment() {

    }

    /**
     * 有效电台发生改变时，刷新列表
     */
    void updateAMListFragment() {
    }

    /**
     * 播放电台的播放状态发生改变
     */
    public abstract void updatePlayAnim();


    /**
     * 搜索
     */
    public void startScan(boolean forceScan) {
        Log.d(TAG, "startScan: " + getLifecycle().getCurrentState());
        //一分钟后发送扫描超时消息
        mHandler.sendEmptyMessageDelayed(RadioConstants.MSG_SCAN_TIMEOUT, RadioConstants.SCAN_TIMEOUT);
    }

    /**
     * 搜索超时
     */
    public void updateViewWithScanTimeOut() {

    }

    @Override
    public void onItemClick(int position, RadioMessage radioMessage) {
        Log.d(TAG, "onItemClick:" + position + " radioMessage = " + radioMessage);
        //要么等打开电台的回调收到再跳转，要么把position传给PlayActivity，再根据position拿到当前播放内容
        //不然会导致跳转到PlayActivity后，拿到当前播放内容还是上一个的情况
        //如果是正在播放的电台，那就直接跳转
        SPUtlis.getInstance().saveShowCollectListMode(false);
        RadioMessage currentRadioMessage = mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "onItemClick: currentRadioMessage = " + currentRadioMessage);
        if (radioMessage.getRadioBand() == RadioMessage.DAB_BAND){
            if (CompareUtils.isSameDAB(radioMessage,currentRadioMessage)) {
                if (!mGetRadioStatusTool.isPlaying()) {
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PLAY, ChangeReasonData.CLICK_ITEM, radioMessage);
                }
                if (null != onItemClickListener) {
                    onItemClickListener.onItemCLick();
                }
            } else {
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
                isItemClick = true;
            }
        }else {
            if (currentRadioMessage.getRadioBand() == radioMessage.getRadioBand()
                    && currentRadioMessage.getRadioFrequency() == radioMessage.getRadioFrequency()) {
                if (!mGetRadioStatusTool.isPlaying()) {
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PLAY, ChangeReasonData.CLICK_ITEM, radioMessage);
                }
                if (null != onItemClickListener) {
                    onItemClickListener.onItemCLick();
                }
            } else {
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
                isItemClick = true;
            }
        }
    }

    /**
     * 列表项点击事件，透传给主Fragment进行界面替换
     */
    protected IOnItemClickListener onItemClickListener;

    public void setOnItemClickListener(IOnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface IOnItemClickListener {
        void onItemCLick();
        void onItemCLickDAB();//收藏列表界面点击的DAB
    }

}
