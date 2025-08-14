package com.desaysv.localmediasdk.bean;

import java.lang.ref.PhantomReference;

/**
 * Created by LZM on 2020-6-17
 * Comment 用来配置绑定不同的包名和服务
 */
public class PackageConfig {

    public static final String MUSIC_APP_PACKAGE = "com.desaysv.svaudioapp";
    public static final String MUSIC_APP_PACKAGE_T1EJ = "com.desaysv.svmusicapp";

    public static final String MUSIC_APP_AIDL_SERVICE = "com.desaysv.moduleusbmusic.service.MusicAidlService";

    public static final String RADIO_APP_PACKAGE = "com.desaysv.svaudioapp";


    public static final String RADIO_APP_AIDL_SERVICE = "com.desaysv.moduleradio.service.RadioAidlService";

    public static final String CARPLAY_MUSIC_APP_PACKAGE = "com.desaysv.mediaapp";

    public static final String CARPLAY_MUSIC_APP_AIDL_SERVICE = "com.desaysv.modulecarplaymusic.service.CarPlayMusicAidlService";

}
