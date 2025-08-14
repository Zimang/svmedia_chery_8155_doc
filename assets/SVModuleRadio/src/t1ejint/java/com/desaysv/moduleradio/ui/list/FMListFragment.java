package com.desaysv.moduleradio.ui.list;


import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioPlayListAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;

import java.lang.ref.WeakReference;

public class FMListFragment extends BaseFragment {

    //播放列表
    private RecyclerView rvPlayList;
    private RadioPlayListAdapter playListAdapter;
    private RelativeLayout rlRadioListEmpty;
    private MyHandler mHandler;

    @Override
    public int getLayoutResID() {
        return R.layout.radio_list_fragment;
    }

    @Override
    public void initView(View view) {
        rvPlayList = view.findViewById(R.id.rvPlayList);
        rvPlayList.setLayoutManager(new LinearLayoutManager(getContext()));//默认就是竖向
        playListAdapter = new RadioPlayListAdapter(getContext(), null);
        rvPlayList.setAdapter(playListAdapter);
        mHandler = new MyHandler(this);
        rlRadioListEmpty = view.findViewById(R.id.rlRadioListEmpty);
        ((TextView)view.findViewById(R.id.tvRadioEmpty)).setText(getResources().getString(R.string.radio_no_content));
    }

    @Override
    public void initData() {
        playListAdapter.updateList(RadioList.getInstance().getFMEffectRadioMessageList());
    }

    @Override
    public void initViewListener() {

    }

    @Override
    public void onStart() {
        super.onStart();
        //更新列表界面
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
    }

    @Override
    public void onStop() {
        super.onStop();
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        ModuleRadioTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    public void updateList(){
        if (RadioList.getInstance().getFMEffectRadioMessageList().size() < 1){
            rlRadioListEmpty.setVisibility(View.VISIBLE);
            rvPlayList.setVisibility(View.GONE);
        }else {
            rlRadioListEmpty.setVisibility(View.GONE);
            rvPlayList.setVisibility(View.VISIBLE);
            playListAdapter.updateList(RadioList.getInstance().getFMEffectRadioMessageList());
        }
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

        @Override
        public void onFMPlayListChange() {
            Log.d(TAG, "onFMPlayListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
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
        }

        @Override
        public void onSearchStatusChange(final boolean isSearching) {
            Log.d(TAG, "onSearchStatusChange: isSearching = " + isSearching);
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

    private static class MyHandler extends Handler {
        WeakReference<FMListFragment> weakReference;

        MyHandler(FMListFragment listFragment) {
            weakReference = new WeakReference<>(listFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final FMListFragment listFragment = weakReference.get();
            // 只有界面显示了，才进行接收数据
            Lifecycle lifecycle = listFragment.getLifecycle();
            Log.d(listFragment.TAG, "handleMessage: lifecycle = " + lifecycle);
            if (null != lifecycle) {
                Lifecycle.State currentState = lifecycle.getCurrentState();
                Log.d(listFragment.TAG, "handleMessage: currentState = " + currentState);
                if (Lifecycle.State.RESUMED != currentState) {
                    return;
                }
            }
            Log.d(listFragment.TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE:
                case RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS:
                case RadioConstants.MSG_UPDATE_RADIO_FM_LIST:
                case RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST:
                default:
                    listFragment.updateList();
                    break;
            }
        }
    }
}
