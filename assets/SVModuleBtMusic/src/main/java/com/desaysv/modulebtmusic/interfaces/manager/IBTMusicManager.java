package com.desaysv.modulebtmusic.interfaces.manager;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;

import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.bean.SVDataListBean;
import com.desaysv.modulebtmusic.bean.SVDevice;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.interfaces.listener.IBTMusicListener;
import com.desaysv.modulebtmusic.interfaces.listener.IMusicInfoDownloadListener;

import java.util.List;

/**
 * 通用BTMusicManager接口类
 */
public interface IBTMusicManager {
    /**
     * 注册a2dp的回调监听
     *
     * @param listener {@link IBTMusicListener}
     * @param isRemote 是否是跨进程注册（客户端为true）
     */
    void registerListener(IBTMusicListener listener, boolean isRemote);

    /**
     * 反注册a2dp的回调监听
     *
     * @param a2dpListener {@link IBTMusicListener}
     */
    void unregisterListener(IBTMusicListener a2dpListener);

    /**
     * 注册监听开机时蓝牙音乐音源恢复的消息
     * 注意：接收端Service可根据 Constants.Actions.ACTION_BOOT_RESUME_AUDIO 来判断音源恢复的消息
     *
     * @param packageName 监听音源恢复的服务的包名
     * @param className   监听音源恢复的服务的类名
     */
    void registerAudioResumeService(String packageName, String className);

    /**
     * 注销监听开机时蓝牙音乐音源恢复的消息
     *
     * @param packageName 监听音源恢复的服务的包名
     * @param className   监听音源恢复的服务的类名
     */
    void unregisterAudioResumeService(String packageName, String className);

    /**
     * 获取A2DP协议是否已连接
     *
     * @return 是否已连接
     */
    boolean isA2DPConnected();

    /**
     * 获取AVRCP协议是否已连接
     *
     * @return 是否已连接
     */
    boolean isAVRCPConnected();

    /**
     * 获取A2DP协议的连接状态
     *
     * @param address 蓝牙地址
     * @return 连接状态 {@link BaseConstants.ProfileConnectionState}
     */
    int getConnectionState(String address);

    /**
     * 获取当前默认的A2DP连接设备地址
     *
     * @return 蓝牙设备地址
     */
    String getConnectedAddress();

    /**
     * 获取当前默认的A2DP连接设备
     *
     * @return 蓝牙设备
     */
    SVDevice getConnectedDevice();

    /**
     * 获取A2DP协议服务所连接的蓝牙设备
     *
     * @return 蓝牙设备列表
     */
    List<SVDevice> getConnectedDeviceList();

    /**
     * 通过VDB事件获取蓝牙音乐ID3信息
     *
     * @return 蓝牙音乐对象
     */
    SVMusicInfo getMusicPlayInfo();

    /**
     * 通过VDB事件获取蓝牙音乐ID3信息
     *
     * @return 蓝牙音乐对象
     */
    SVMusicInfo getRemoteMusicPlayInfo();

    /**
     * 获取蓝牙音乐播放列表
     *
     * @return
     */
    List<SVMusicInfo> getMusicInfoList();

    /**
     * 获取音乐列表对象（可用于判断数据列表是否成功从服务端获取）
     *
     * @return
     */
    SVDataListBean<SVMusicInfo> getMusicInfoListBean();

    /**
     * 连接A2DP协议
     *
     * @param address 需要连接的设备地址
     * @return 是否执行成功
     */
    boolean connect(String address);

    /**
     * 断开A2DP协议
     *
     * @param address 需要断开的设备地址
     * @return 是否执行成功
     */
    boolean disconnect(String address);

    /**
     * 在播放列表中通过MediaID去播放音乐
     *
     * @param mediaId The uri of the requested media.
     * @param extras  Optional extras that can include extra information
     *                about the media item to be played.
     */

    void playFromMediaId(String mediaId, Bundle extras);

    /**
     * 在播放列表中通过搜索关键字去播放音乐
     *
     * @param query  The search query.
     * @param extras Optional extras that can include extra information
     *               about the query.
     */

    void playFromSearch(String query, Bundle extras);

    /**
     * 在播放列表中通过uri去播放音乐
     *
     * @param uri    The URI of the requested media.
     * @param extras Optional extras that can include extra information about the media item
     *               to be played.
     */

    void playFromUri(Uri uri, Bundle extras);

    /**
     * 播放音乐
     */

    void play();

    /**
     * 暂停音乐
     */

    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 下一曲
     */

    void skipToNext();

    /**
     * 上一曲
     */

    void skipToPrevious();

    /**
     * @param isFastForward true:快进开始；false:快进结束
     */

    void fastForward(boolean isFastForward);

    /**
     * @param isFastRewind true:快退开始；false:快退结束
     */

    void fastRewind(boolean isFastRewind);

    /**
     * 申请蓝牙音乐的音频焦点
     */

    void requestBTMusicAudioFocus();

    /**
     * 在播放列表中通过MediaID去播放音乐
     *
     * @param mediaId             The uri of the requested media.
     * @param extras              Optional extras that can include extra information
     *                            about the media item to be played.
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncPlayFromMediaId(String mediaId, Bundle extras, boolean isRequestAudioFocus);

    /**
     * 在播放列表中通过搜索关键字去播放音乐
     *
     * @param query               The search query.
     * @param extras              Optional extras that can include extra information
     *                            about the query.
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncPlayFromSearch(String query, Bundle extras, boolean isRequestAudioFocus);

    /**
     * 在播放列表中通过uri去播放音乐
     *
     * @param uri                 The URI of the requested media.
     * @param extras              Optional extras that can include extra information about the media item
     *                            to be played.
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncPlayFromUri(Uri uri, Bundle extras, boolean isRequestAudioFocus);

    /**
     * 播放音乐
     *
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncPlay(boolean isRequestAudioFocus);

    /**
     * 暂停音乐
     *
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncPause(boolean isRequestAudioFocus);

    /**
     * 下一曲
     *
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncSkipToNext(boolean isRequestAudioFocus);

    /**
     * 上一曲
     *
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncSkipToPrevious(boolean isRequestAudioFocus);

    /**
     * @param isFastForward       true:快进开始；false:快进结束
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncFastForward(boolean isFastForward, boolean isRequestAudioFocus);

    /**
     * @param isFastRewind        true:快退开始；false:快退结束
     * @param isRequestAudioFocus 是否申请蓝牙音乐音频焦点
     */
    boolean syncFastRewind(boolean isFastRewind, boolean isRequestAudioFocus);

    /**
     * 申请蓝牙音乐的音频焦点
     *
     * @return 焦点的请求结果 {@link AudioManager#AUDIOFOCUS_REQUEST_FAILED},{@link AudioManager#AUDIOFOCUS_REQUEST_GRANTED},{@link AudioManager#AUDIOFOCUS_REQUEST_DELAYED}
     */
    int syncRequestBTMusicAudioFocus();

    /**
     * 设置A2DP是否静音
     *
     * @param isMute true:静音; false:非静音
     */

    void setA2DPMuteState(boolean isMute);

    /**
     * 判断A2DP是否静音
     *
     * @return true:静音; false:非静音
     */
    boolean isA2DPMute();

    /**
     * 获取音乐播放列表
     *
     * @param listener 获取音乐播放列表过程回调监听
     */
    void loadMusicInfoList(IMusicInfoDownloadListener listener);

    /**
     * 获取支持的播放模式
     * Get the supported settings.
     *
     * @return 支持的播放模式 {@link Constants.PlayerSettingsType}
     */
    int getPlayerSettings();

    /**
     * 设置播放模式
     * Add a setting value. The setting must be part of possible settings in getSettings().
     *
     * @param type  播放模式的类型 {@link Constants.PlayerSettingsType}
     * @param state 播放模式对应的状态 {@link Constants.PlayerSettingsState}
     * @return
     */
    boolean setPlayerSettings(int type, int state);

    /**
     * 获取播放模式对应的状态
     * Get a setting value. The setting must be part of possible settings in getSettings().
     *
     * @param type 播放模式的类型 {@link Constants.PlayerSettingsType}
     * @return 播放模式对应的状态 {@link Constants.PlayerSettingsState}
     */
    default int getSettingValue(int type) {
        return Constants.PlayerSettingsState.STATE_INVALID;
    }
}
