package com.desaysv.svmediaservicelogic.systemcontrol;

import android.util.Log;
import android.view.KeyEvent;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediasdk.manager.DsvSourceMediaManager;


/**
 * Created by LZM on 2020-3-16
 * Comment 硬按键的控制器，按键控制的地方
 * @author uidp5370
 */
public class HardKeyControl {

    private static final String TAG = "HardKeyControl";

    private static HardKeyControl instance;

    public static HardKeyControl getInstance() {
        if (instance == null) {
            synchronized (HardKeyControl.class) {
                if (instance == null) {
                    instance = new HardKeyControl();
                }
            }
        }
        return instance;
    }

    private HardKeyControl() {

    }

    /**
     * 处理系统给过来的硬按键，做统一的逻辑处理
     *
     * @param keyEvent 硬按键的按键值
     */
    public void setHardKeyEvent(KeyEvent keyEvent) {
        Log.d(TAG, "getKeyEvent: keyEvent = " + keyEvent);
        //TODO:实现硬按键的业务逻辑
        int action = keyEvent.getAction();
        if (action == KeyEvent.ACTION_UP) {
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext);
            Log.d(TAG, "setHardKeyEvent: currentSource = " + currentSource);
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    DsvSourceMediaManager.getInstance().next();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    DsvSourceMediaManager.getInstance().pre();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    DsvSourceMediaManager.getInstance().pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    DsvSourceMediaManager.getInstance().play();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    DsvSourceMediaManager.getInstance().playOrPause();
                    break;
            }
        }
    }

}
