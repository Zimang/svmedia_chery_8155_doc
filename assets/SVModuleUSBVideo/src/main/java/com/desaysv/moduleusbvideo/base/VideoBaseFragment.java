package com.desaysv.moduleusbvideo.base;

import android.util.Log;

import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduleusbvideo.base.interfaces.ICommunication;

/**
 * Create by extodc87 on 2022-11-1
 * Author: extodc87
 */
public abstract class VideoBaseFragment extends BaseFragment {
    protected ICommunication mCommunication;

    public void setCommunication(ICommunication mCommunication) {
        this.mCommunication = mCommunication;
    }

    /**
     * 用于Activity传初始数据给fragment
     *
     * @param data data
     */
    public void setInitData(Object data) {
        Log.d(TAG, "setInitData: data = " + data);
    }

}
