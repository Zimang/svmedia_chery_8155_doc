package com.desaysv.svlibmediastore.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;

/**
 * @author uidq1846
 * @desc 媒体数据库状态回调类
 * @time 2022-11-14 15:30
 */
public class MediaObserver implements IMediaObserver {
    private final String TAG = MediaObserver.class.getSimpleName();
    private final ContentResolver contentResolver;
    private final Uri uri;
    private ContentObserver observer;

    /**
     * 构造方法
     *
     * @param context 上下文对象
     * @param uri     The URI to watch for changes. This can be a specific row URI,
     *                or a base URI for a whole class of content.
     */
    public MediaObserver(Context context, Uri uri) {
        this.uri = uri;
        contentResolver = context.getContentResolver();
    }

    @Override
    public void register(ContentObserver observer) {
        register(observer, false);
    }

    @Override
    public void register(ContentObserver observer, boolean notifyForDescendants) {
        if (this.observer != null && this.observer.equals(observer)) {
            unregister();
        }
        this.observer = observer;
        contentResolver.registerContentObserver(uri, notifyForDescendants, observer);
        Log.d(TAG, "register: " + observer);
    }

    @Override
    public void unregister() {
        Log.d(TAG, "unregister: " + observer);
        if (observer != null) {
            contentResolver.unregisterContentObserver(observer);
        }
    }
}
