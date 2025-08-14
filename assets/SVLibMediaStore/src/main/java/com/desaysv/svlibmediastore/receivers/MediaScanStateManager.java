package com.desaysv.svlibmediastore.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.svlibmediastore.query.ImageQuery;
import com.desaysv.svlibmediastore.query.MusicQuery;
import com.desaysv.svlibmediastore.query.VideoQuery;
import com.desaysv.svlibmediastore.observer.MediaObserver;
import com.desaysv.usbbaselib.bean.USBConstants;

import java.util.Arrays;

/**
 * @author uidq1846
 * @desc 媒体扫描状态管理者
 * @time 2022-11-14 16:10
 */
public class MediaScanStateManager implements IScan {
    private final String TAG = MediaScanStateManager.class.getSimpleName();
    private MediaScannerConnection connection;
    private Context context;
    private Handler observerHandler;
    private static IScan iMediaScanStateManager;
    private StorageManager storageManager;

    private MediaScanStateManager() {
    }

    /**
     * 获取单例
     *
     * @return IScan
     */
    public static IScan getInstance() {
        if (iMediaScanStateManager == null) {
            synchronized (MediaScanStateManager.class) {
                if (iMediaScanStateManager == null) {
                    iMediaScanStateManager = new MediaScanStateManager();
                }
            }
        }
        return iMediaScanStateManager;
    }

    @Override
    public void scan(String path) {
        Log.d(TAG, "scan: path = " + path);
        if (connection.isConnected()) {
            connection.scanFile(path, null);
        }
    }

    @Override
    public void scan(String[] paths) {
        Log.d(TAG, "scan: paths = " + Arrays.toString(paths));
        if (connection.isConnected()) {
            MediaScannerConnection.scanFile(context, paths, null, null);
        }
    }

    @Override
    public void init(Context context) {
        this.context = context;
        connection = new MediaScannerConnection(context, connectionClient);
        connection.connect();
        storageManager = context.getSystemService(StorageManager.class);
        initQuery();
        initReceiver();
        initObserver();
    }

    /**
     * 初始化数据查询
     */
    private void initQuery() {
        MusicQuery.getInstance().init(context);
        VideoQuery.getInstance().init(context);
        ImageQuery.getInstance().init(context);
    }

    /**
     * 初始化数据监听器
     */
    private void initObserver() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        observerHandler = new Handler(handlerThread.getLooper());
        MediaObserver audioObserver = new MediaObserver(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        audioObserver.register(audioDataObserver, true);
        MediaObserver videoObserver = new MediaObserver(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        videoObserver.register(videoDataObserver);
        MediaObserver picObserver = new MediaObserver(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        picObserver.register(picDataObserver);
    }

    /**
     * 初始化状态监听
     */
    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        context.registerReceiver(USBMusicDataUpdateReceiverAndroid, intentFilter);
    }

    /**
     * 文件更新广播
     */
    private final MediaScannerConnection.MediaScannerConnectionClient connectionClient = new MediaScannerConnection.MediaScannerConnectionClient() {
        @Override
        public void onMediaScannerConnected() {
            Log.d(TAG, "onMediaScannerConnected: ");
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Log.d(TAG, "onScanCompleted: path = " + path + " uri = " + uri);
            //删除时，文件不存在，所以报Unknown volume for content://media/external/audio/media/6158错误，所以这里直接通知
            //当前需求是只有本地有删除，所以写死，实际需根据path决定
            if (path == null || TextUtils.isEmpty(path)) {
                Log.w(TAG, "onScanCompleted: path is unable!");
                return;
            }
            if (path.startsWith(USBConstants.USBPath.LOCAL_PATH)) {
                MediaScanBroadcastUtils.sendMusicID3UpdateMsg(context, USBConstants.USBPath.LOCAL_PATH, USBConstants.ProviderScanStatus.SCANNING);
            } else if (path.startsWith(USBConstants.USBPath.USB0_PATH)) {
                MediaScanBroadcastUtils.sendMusicID3UpdateMsg(context, USBConstants.USBPath.USB0_PATH, USBConstants.ProviderScanStatus.SCANNING);
            } else if (path.startsWith(USBConstants.USBPath.USB1_PATH)) {
                MediaScanBroadcastUtils.sendMusicID3UpdateMsg(context, USBConstants.USBPath.USB1_PATH, USBConstants.ProviderScanStatus.SCANNING);
            }
        }
    };

    /**
     * 媒体数据变化、当收到变化状态时，用原来的广播通知，原来的状态则不会发生变化
     */
    private final BroadcastReceiver USBMusicDataUpdateReceiverAndroid = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果扫描开关没打开的话、则不做处理
            if (!USBConstants.SupportProvider.IS_SUPPORT_ANDROID) {
                Log.d(TAG, "onReceive: using desay sv scanner,so return");
                return;
            }
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);
            String path = intent.getData().getPath();
            Log.d(TAG, "onReceive: path = " + path);
            if (path != null && !path.startsWith("/storage/usb") && !path.startsWith(USBConstants.USBPath.LOCAL_PATH)) {
                Log.d(TAG, "Not usb path, return");
                return;
            }
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                MediaScanBroadcastUtils.sendMusicID3UpdateMsg(context, path, USBConstants.ProviderScanStatus.SCANNING);
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MediaScanBroadcastUtils.sendMusicID3UpdateMsg(context, path, USBConstants.ProviderScanStatus.SCAN_FINISHED);
            }
        }
    };

    /**
     * 数据变化回调监听
     */
    private final ContentObserver audioDataObserver = new ContentObserver(observerHandler) {
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "audioDataObserver: onChange: selfChange = " + selfChange + " uri = " + uri);
            //content://media/external/audio/media
            if (uri != null && !uri.toString().contains(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                return;
            }
            //如果扫描开关没打开的话、则不做处理
            if (!USBConstants.SupportProvider.IS_SUPPORT_ANDROID) {
                Log.d(TAG, "onChange: using desay sv scanner,so return");
                return;
            }
            //这里存在一个问题，就是如何区分是USB1的还是2的内容、Android R才行，其它版本另外想招
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    StorageVolume storageVolume = storageManager.getStorageVolume(uri);
                    String path = storageVolume.getDirectory().getPath();
                    //挂载路径
                    Log.d(TAG, "onChange: storageVolume path = " + path);
                    if (USBConstants.USBPath.LOCAL_PATH.equals(path)) {
                        Log.w(TAG, "onChange: LOCAL_PATH is no need to scanning!!!");
                        return;
                    }
                    MediaScanBroadcastUtils.sendMusicID3UpdateMsg(context, path, USBConstants.ProviderScanStatus.SCANNING);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onChange: IllegalStateException");
                }
            }
        }
    };

    /**
     * 数据变化回调监听
     */
    private final ContentObserver videoDataObserver = new ContentObserver(observerHandler) {
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "videoDataObserver: onChange: selfChange = " + selfChange + " uri = " + uri);
            // TODO: 2022-11-14 暂时只是完成音乐的、后续看需要增加
        }
    };

    /**
     * 数据变化回调监听
     */
    private final ContentObserver picDataObserver = new ContentObserver(observerHandler) {
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "picDataObserver: onChange: selfChange = " + selfChange + " uri = " + uri);
            // TODO: 2022-11-14 暂时只是完成音乐的、后续看需要增加
        }
    };
}
