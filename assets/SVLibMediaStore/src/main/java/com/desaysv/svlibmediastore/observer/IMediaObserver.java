package com.desaysv.svlibmediastore.observer;

import android.content.ContentProvider;
import android.database.ContentObserver;

/**
 * @author uidq1846
 * @desc 媒体扫描数据回调接口
 * @time 2022-11-14 15:26
 */
public interface IMediaObserver {

    /**
     * Register an observer class that gets callbacks when data identified by a
     * given content URI changes.
     * <p>
     * Starting in {@link android.os.Build.VERSION_CODES#O}, all content
     * notifications must be backed by a valid {@link ContentProvider}.
     *
     * @param observer The object that receives callbacks when changes occur.
     */
    void register(ContentObserver observer);

    /**
     * Register an observer class that gets callbacks when data identified by a
     * given content URI changes.
     * <p>
     * Starting in {@link android.os.Build.VERSION_CODES#O}, all content
     * notifications must be backed by a valid {@link ContentProvider}.
     *
     * @param notifyForDescendants When false, the observer will be notified
     *                             whenever a change occurs to the exact URI specified by
     *                             <code>uri</code> or to one of the URI's ancestors in the path
     *                             hierarchy. When true, the observer will also be notified
     *                             whenever a change occurs to the URI's descendants in the path
     *                             hierarchy.
     * @param observer             The object that receives callbacks when changes occur.
     */
    void register(ContentObserver observer, boolean notifyForDescendants);

    /**
     * 取消注册
     */
    void unregister();
}
