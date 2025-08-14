package com.desaysv.moduleusbvideo.util;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Create by extodc87 on 2022-11-2
 * Author: extodc87
 */
public class ExecutorSingleUtils {
    private static final String TAG = "ExecutorServiceUtils";
    // 创建一个可缓存线程池
    private final ExecutorService singleThreadExecutor;

    private static final class InstanceHolder {
        static final ExecutorSingleUtils instance = new ExecutorSingleUtils();
    }

    public static ExecutorSingleUtils getInstance() {
        return ExecutorSingleUtils.InstanceHolder.instance;
    }

    private ExecutorSingleUtils() {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    public Future<?> submit(Runnable task) {
        return singleThreadExecutor.submit(task);
    }


    public void shutdownAndAwaitTermination() {
        shutdownAndAwaitTermination(singleThreadExecutor);
    }

    /**
     * 销毁线程池
     *
     * @param pool pool
     */
    public void shutdownAndAwaitTermination(ExecutorService pool) {
        Log.d(TAG, "shutdownAndAwaitTermination: pool = " + pool);
        if (null == pool) {
            return;
        }
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    Log.e(TAG, "shutdownAndAwaitTermination: Pool did not terminate");
                }
            }
        } catch (Exception ie) {
            Log.e(TAG, "shutdownAndAwaitTermination: ", ie);
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
