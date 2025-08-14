package com.desaysv.localmediasdk.bean;

import android.app.Service;

/**
 * Created by LZM on 2020-3-24
 * Comment 启动媒体音源的数据常量
 */
public class StartSourceIntentBean {

    public static final String DESAYSV_ACTION_START_SOURCE = "com.desaysv.mediaapp.action.startSource";

    public static final String KEY_START_SOURCE = "key_start_source";

    public static final String KEY_IS_FOREGROUND = "key_is_foreground";

    public static final String KEY_IS_REQUEST_FOCUS = "key_is_request_focus";

    public static final String KEY_OPEN_REASON = "key_open_reason";

    public static final String OPEN_REASON_MODE = "mode";

    public static final String OPEN_REASON_BOOT_RESUME = "boot_resume";

    public static final String OPEN_REASON_OTHER_APP_START = "other_app_start";

}
