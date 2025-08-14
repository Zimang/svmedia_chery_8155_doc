package com.desaysv.moduleusbpicture.trigger;

import android.util.Log;

import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleusbpicture.businesslogic.listsearch.USBPictureListSearchType;


/**
 * Created by lzm on 2019-6-3.
 * USB图片的app，初始化一些图片特别需要初始化的功能
 */

public class ModuleUSBPictureTrigger {

    private static final String TAG = "ModuleUSBPictureTrigger";

    private static ModuleUSBPictureTrigger instance;

    public static ModuleUSBPictureTrigger getInstance() {
        if (instance == null) {
            synchronized (ModuleUSBPictureTrigger.class) {
                if (instance == null) {
                    instance = new ModuleUSBPictureTrigger();
                }
            }
        }
        return instance;
    }

    private ModuleUSBPictureTrigger() {

    }


    public void initialize() {
        Log.d(TAG, "onCreate: ");
        USBPictureListSearchType.getInstance().initialize(AppBase.mContext);
    }


}
