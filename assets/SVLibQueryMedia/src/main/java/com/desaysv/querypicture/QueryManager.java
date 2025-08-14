package com.desaysv.querypicture;
/**
 * created by ZNB on 2022-01-11
 * 数据库查询、数据状态更新的管理器
 * 用于提供数据库查询状态、数据库内容变化更新
 *
 * 设备挂载状态、媒体库扫描状态，应该都由 UI 层去处理，这里只专职处理查询action
 *
 */

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.desaysv.querypicture.constant.USBConstants;
import com.desaysv.querypicture.query.AlbumQuery;
import com.desaysv.querypicture.query.MusicQuery;
import com.desaysv.querypicture.query.PictureQuery;
import com.desaysv.querypicture.query.VideoQuery;
import com.desaysv.querypicture.task.BaseQueryTask;
import com.desaysv.querypicture.task.QueryMusicTask;
import com.desaysv.querypicture.task.QueryPictureTask;
import com.desaysv.querypicture.task.QueryVideoTask;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibpicturebean.manager.MusicListManager;
import com.desaysv.svlibpicturebean.manager.PictureListManager;
import com.desaysv.svlibpicturebean.manager.VideoListManager;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB1MusicDataSubject;
import com.desaysv.usbbaselib.statussubject.USB1PictureDataSubject;
import com.desaysv.usbbaselib.statussubject.USB1VideoDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2MusicDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2PictureDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2VideoDataSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueryManager{
    private static final String TAG = "QueryManager";
//    private BaseQueryTask mQueryUSB1PictureTask;
//    private BaseQueryTask mQueryUSB2PictureTask;
//    private BaseQueryTask mQueryUSB1MusicTask;
//    private BaseQueryTask mQueryUSB2MusicTask;
//    private BaseQueryTask mQueryUSB1VideoTask;
//    private BaseQueryTask mQueryUSB2VideoTask;

    private ExecutorService singleUSB1PictureThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB2PictureThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB1MusicThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB2MusicThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB1VideoThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService singleUSB2VideoThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService albumThreadPool = Executors.newSingleThreadExecutor();

    private IQueryListener pictureListener;
    private IQueryListener videoListener;
    private IQueryListener musicListener;
    private static com.desaysv.querypicture.QueryManager mInstance;

    /**
     * 数据库状态变化监听
     */
    private List<com.desaysv.querypicture.QueryManager.IDataChangedListener> mDataChangedListListener = new ArrayList<>();
    private Context mContext;

    public static com.desaysv.querypicture.QueryManager getInstance(){
        synchronized (com.desaysv.querypicture.QueryManager.class){
            if (mInstance == null){
                mInstance = new com.desaysv.querypicture.QueryManager();
            }
        }
        return mInstance;
    }


    public interface IQueryListener {

        /**
         * 异步查询处理正式开始之前，
         * 用于更新UI
         * @param count
         */
        void onPreExecute(int count);

        /**
         * 异步查询处理中
         * 用于更新当前查询进度，可用于更新Loading的进度条
         * @param progress
         */
        void onProgressUpdate(int progress);

        /**
         * 异步查询结束，返回查询到的结果
         * @param pictureList
         * @param folderList
         */
        void onResult(List<FileMessage> pictureList, List<FolderMessage> folderList);

    }

    public interface IDataChangedListener {
        /**
         * 数据库扫描状态的更新
         * 包括 扫描中、扫描结束
         * @param type
         */
        void onChanged(int type);

    }

    /**
     * 注册图片查询监听器
     * @param context
     * @param listener
     */
    public void registerPictureListener(Context context, IQueryListener listener){
        mContext = context;
        pictureListener = listener;
    }

    /**
     * 注销图片查询监听器
     * @param context
     * @param listener
     */
    public void unregisterPictureListener(Context context, IQueryListener listener){
        pictureListener = null;
    }

    /**
     * 注册图视频查询监听器
     * @param context
     * @param listener
     */
    public void registerVideoListener(Context context, IQueryListener listener){
        mContext = context;
        videoListener = listener;
    }

    /**
     * 注销图视频查询监听器
     * @param context
     * @param listener
     */
    public void unregisterVideoListener(Context context, IQueryListener listener){
        videoListener = null;
    }

    /**
     * 注册音乐查询监听器
     * @param context
     * @param listener
     */
    public void registerMusicListener(Context context, IQueryListener listener){
        mContext = context;
        musicListener = listener;
    }

    /**
     * 注销音乐查询监听器
     * @param context
     * @param listener
     */
    public void unregisterMusicListener(Context context, IQueryListener listener){
        musicListener = null;
    }


    /**
     * 直接启动扫描，查询得到数据库全部图片内容
     */
    public void startQueryPicture(){
        Log.d(TAG,"startQueryPicture");
        singleUSB1PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
               new PictureQuery(mContext).doInBackGround();
            }
        });

        singleUSB2PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new PictureQuery(mContext).doInBackGround();
            }
        });

//        new QueryPictureTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (pictureListener != null) {
//                    pictureListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (pictureListener != null) {
//                    pictureListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (pictureListener != null) {
//                    pictureListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        }).execute();
    }

    /**
     * 直接启动扫描，查询得到数据库全部视频内容
     */
    public void startQueryVideo(){
        Log.d(TAG,"startQueryVideo");
        singleUSB1VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext).doInBackGround();
            }
        });

        singleUSB2VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext).doInBackGround();
            }
        });
//        new QueryVideoTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (videoListener != null) {
//                    videoListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (videoListener != null) {
//                    videoListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (videoListener != null) {
//                    videoListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        }).execute();
    }

    /**
     * 直接启动扫描，查询得到数据库全部音乐内容
     */
    public void startQueryMusic(){
        Log.d(TAG,"startQueryMusic");
        singleUSB1MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext).doInBackGround();
            }
        });

        singleUSB2MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext).doInBackGround();
            }
        });
//        new QueryMusicTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (musicListener != null) {
//                    musicListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (musicListener != null) {
//                    musicListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (musicListener != null) {
//                    musicListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        }).execute();
    }

    /**
     * 扫描当前目录下的图片内容
     * @param dir
     */
    public void startQueryUSB1PictureWithDir(String dir){
        Log.d(TAG,"startQueryUSB1PictureWithDir(String dir)");
        if (dir == null){
            dir = USBConstants.USBPath.USB0_PATH;
        }
        String finalDir = dir;
        singleUSB1PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new PictureQuery(mContext, finalDir,USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });

//        mQueryUSB1PictureTask = new QueryPictureTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (pictureListener != null) {
//                    pictureListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (pictureListener != null) {
//                    pictureListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (pictureListener != null) {
//                    pictureListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },dir, USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB1PictureTask.execute();
    }


    /**
     * 扫描当前目录下的图片内容
     * @param dir
     */
    public void startQueryUSB2PictureWithDir(String dir){
        Log.d(TAG,"startQueryUSB2PictureWithDir(String dir)");
        if (dir == null){
            dir = USBConstants.USBPath.USB1_PATH;
        }
        String finalDir = dir;
        singleUSB2PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new PictureQuery(mContext, finalDir,USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB2PictureTask = new QueryPictureTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (pictureListener != null) {
//                    pictureListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (pictureListener != null) {
//                    pictureListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (pictureListener != null) {
//                    pictureListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },dir, USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB2PictureTask.execute();
    }

    /**
     * 扫描当前目录下的视频内容
     * @param dir
     */
    public void startQueryUSB1VideoWithDir(String dir){
        Log.d(TAG,"startQueryUSB1VideoWithDir(String dir)");

        if (dir == null){
            dir = USBConstants.USBPath.USB0_PATH;
        }
        String finalDir = dir;
        singleUSB1VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext, finalDir,USB1VideoDataSubject.getInstance().getUSB1VideoSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB1VideoTask= new QueryVideoTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (videoListener != null) {
//                    videoListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (videoListener != null) {
//                    videoListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (videoListener != null) {
//                    videoListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },dir, USB1VideoDataSubject.getInstance().getUSB1VideoSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB1VideoTask.execute();
    }


    /**
     * 扫描当前目录下的视频内容
     * @param dir
     */
    public void startQueryUSB2VideoWithDir(String dir){
        Log.d(TAG,"startQueryUSB2VideoWithDir(String dir)");

        if (dir == null){
            dir = USBConstants.USBPath.USB1_PATH;
        }
        String finalDir = dir;
        singleUSB2VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext, finalDir,USB2VideoDataSubject.getInstance().getUSB2VideoSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB2VideoTask= new QueryVideoTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (videoListener != null) {
//                    videoListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (videoListener != null) {
//                    videoListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (videoListener != null) {
//                    videoListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },dir, USB2VideoDataSubject.getInstance().getUSB2VideoSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB2VideoTask.execute();
    }


    /**
     * 扫描当前目录下的音乐内容
     * @param dir
     */
    public void startQueryUSB1MusicWithDir(String dir){
        Log.d(TAG,"startQueryUSB1MusicWithDir(String dir)");

        if (dir == null){
            dir = USBConstants.USBPath.USB0_PATH;
        }
        String finalDir = dir;
        singleUSB1MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext, finalDir,USB1MusicDataSubject.getInstance().getUSB1MusicSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB1MusicTask = new QueryMusicTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (musicListener != null) {
//                    musicListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (musicListener != null) {
//                    musicListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (musicListener != null) {
//                    musicListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },dir, USB1MusicDataSubject.getInstance().getUSB1MusicSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB1MusicTask.execute();
    }


    /**
     * 扫描当前目录下的音乐内容
     * @param dir
     */
    public void startQueryUSB2MusicWithDir(String dir){
        Log.d(TAG,"startQueryUSB2MusicWithDir(String dir)");

        if (dir == null){
            dir = USBConstants.USBPath.USB1_PATH;
        }
        String finalDir = dir;
        singleUSB2MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext, finalDir,USB2MusicDataSubject.getInstance().getUSB2MusicSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB2MusicTask = new QueryMusicTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (musicListener != null) {
//                    musicListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (musicListener != null) {
//                    musicListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (musicListener != null) {
//                    musicListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },dir, USB2MusicDataSubject.getInstance().getUSB2MusicSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB2MusicTask.execute();
    }


    /**
     * 扫描 USB0_PATH 目录下的图片内容
     */
    public void startQueryPictureWithUSB1(){
        Log.d(TAG,"startQueryPictureWithUSB1()");
        singleUSB1PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new PictureQuery(mContext, USBConstants.USBPath.USB0_PATH,USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB1PictureTask = new QueryPictureTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (pictureListener != null) {
//                    pictureListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (pictureListener != null) {
//                    pictureListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (pictureListener != null) {
//                    pictureListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        }, USBConstants.USBPath.USB0_PATH,USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB1PictureTask.execute();
    }

    /**
     * 扫描 USB0_PATH 目录下的视频内容
     */
    public void startQueryVideoWithUSB1(){
        Log.d(TAG,"startQueryVideoWithUSB1()");
        singleUSB1VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext, USBConstants.USBPath.USB0_PATH,USB1VideoDataSubject.getInstance().getUSB1VideoSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB1VideoTask= new QueryVideoTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (videoListener != null) {
//                    videoListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (videoListener != null) {
//                    videoListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (videoListener != null) {
//                    videoListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        }, USBConstants.USBPath.USB0_PATH, USB1VideoDataSubject.getInstance().getUSB1VideoSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB1VideoTask.execute();
    }

    /**
     * 扫描 USB0_PATH 目录下的音乐内容
     */
    public void startQueryMusicWithUSB1(){
        Log.d(TAG,"startQueryMusicWithUSB1()");

        singleUSB1MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext, USBConstants.USBPath.USB0_PATH,USB1MusicDataSubject.getInstance().getUSB1MusicSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB1MusicTask = new QueryMusicTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (musicListener != null) {
//                    musicListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (musicListener != null) {
//                    musicListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (musicListener != null) {
//                    musicListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        }, USBConstants.USBPath.USB0_PATH,USB1MusicDataSubject.getInstance().getUSB1MusicSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB1MusicTask.execute();
    }

    /**
     * 扫描 USB1_PATH 目录下的图片内容
     */
    public void startQueryPictureWithUSB2(){
        Log.d(TAG,"startQueryPictureWithUSB2()");
        singleUSB2PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new PictureQuery(mContext, USBConstants.USBPath.USB1_PATH,USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB2PictureTask = new QueryPictureTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (pictureListener != null) {
//                    pictureListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (pictureListener != null) {
//                    pictureListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (pictureListener != null) {
//                    pictureListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },USBConstants.USBPath.USB1_PATH,USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB2PictureTask.execute();
    }

    /**
     * 扫描 USB1_PATH 目录下的视频内容
     */
    public void startQueryVideoWithUSB2(){
        Log.d(TAG,"startQueryVideoWithUSB2()");

        singleUSB2VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext, USBConstants.USBPath.USB1_PATH,USB2VideoDataSubject.getInstance().getUSB2VideoSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB2VideoTask = new QueryVideoTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (videoListener != null) {
//                    videoListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (videoListener != null) {
//                    videoListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (videoListener != null) {
//                    videoListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },USBConstants.USBPath.USB1_PATH,USB2VideoDataSubject.getInstance().getUSB2VideoSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB2VideoTask.execute();
    }

    /**
     * 扫描 USB1_PATH 目录下的音乐内容
     */
    public void startQueryMusicWithUSB2(){
        Log.d(TAG,"startQueryMusicWithUSB2()");

        singleUSB2MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext, USBConstants.USBPath.USB1_PATH,USB2MusicDataSubject.getInstance().getUSB2MusicSearchType() != SearchType.SEARCHED_HAVE_DATA).doInBackGround();
            }
        });
//        mQueryUSB2MusicTask= new QueryMusicTask(mContext, new BaseQueryTask.IQueryResultListener() {
//            @Override
//            public void onPreExecute(int count) {
//                if (musicListener != null) {
//                    musicListener.onPreExecute(count);
//                }
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if(musicListener != null) {
//                    musicListener.onProgressUpdate(progress);
//                }
//            }
//
//            @Override
//            public void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList) {
//                if (musicListener != null) {
//                    musicListener.onResult(currentDirectoryList, currentDirectoryFolderList);
//                }
//            }
//        },USBConstants.USBPath.USB1_PATH,USB2MusicDataSubject.getInstance().getUSB2MusicSearchType() != SearchType.SEARCHED_HAVE_DATA);
//        mQueryUSB2MusicTask.execute();
    }




    public void init(Context context){
        mContext = context;
    }

    /**
     * USB1扫描中的数据查询
     */
    public void startQueryPictureWithUSB1Scan(int scanStatus){
        Log.d(TAG,"startQueryPictureWithUSB1Scan(int scanStatus)");

        PictureListManager.getInstance().setUsb1ScanStatus(scanStatus);
        singleUSB1PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new PictureQuery(mContext,USBConstants.USBPath.USB0_PATH, true).doInBackGround();
            }
        });
//        mQueryUSB1PictureTask= new QueryPictureTask(mContext, null, USBConstants.USBPath.USB0_PATH, true);
//        mQueryUSB1PictureTask.execute();
    }

    /**
     * USB1扫描中的数据查询
     */
    public void startQueryPictureWithUSB1Scan(int scanStatus,String dir){
        Log.d(TAG,"startQueryPictureWithUSB1Scan(int scanStatus,String dir)");

        PictureListManager.getInstance().setUsb1ScanStatus(scanStatus);
        startQueryUSB1PictureWithDir(dir);
    }

    /**
     * USB1扫描中的数据查询
     */
    public void startQueryVideoWithUSB1Scan(int scanStatus){
        Log.d(TAG,"startQueryVideoWithUSB1Scan(int scanStatus)");

        VideoListManager.getInstance().setUsb1ScanStatus(scanStatus);
        singleUSB1VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext,USBConstants.USBPath.USB0_PATH, true).doInBackGround();
            }
        });
//        mQueryUSB1VideoTask = new QueryVideoTask(mContext, null, USBConstants.USBPath.USB0_PATH, true);
//        mQueryUSB1VideoTask.execute();
    }

    /**
     * USB1扫描中的数据查询
     */
    public void startQueryVideoWithUSB1Scan(int scanStatus,String dir){
        Log.d(TAG,"startQueryVideoWithUSB1Scan(int scanStatus,String dir)");

        VideoListManager.getInstance().setUsb1ScanStatus(scanStatus);
        startQueryUSB1VideoWithDir(dir);
    }

    /**
     * USB1扫描中的数据查询
     */
    public void startQueryMusicWithUSB1Scan(int scanStatus){
        Log.d(TAG,"startQueryMusicWithUSB1Scan(int scanStatus)");

        MusicListManager.getInstance().setUsb1ScanStatus(scanStatus);
        singleUSB1MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext,USBConstants.USBPath.USB0_PATH, true).doInBackGround();
            }
        });
//        mQueryUSB1MusicTask = new QueryMusicTask(mContext, null, USBConstants.USBPath.USB0_PATH, true);
//        mQueryUSB1MusicTask.execute();
    }

    /**
     * USB1扫描中的数据查询
     */
    public void startQueryMusicWithUSB1Scan(int scanStatus, String dir){
        Log.d(TAG,"startQueryMusicWithUSB1Scan(int scanStatus,String dir)");

        MusicListManager.getInstance().setUsb1ScanStatus(scanStatus);
        startQueryUSB1MusicWithDir(dir);
    }

    /**
     * USB2扫描中的数据查询
     */
    public void startQueryPictureWithUSB2Scan(int scanStatus){
        Log.d(TAG,"startQueryPictureWithUSB2Scan(int scanStatus)");

        PictureListManager.getInstance().setUsb2ScanStatus(scanStatus);
        singleUSB2PictureThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new PictureQuery(mContext,USBConstants.USBPath.USB1_PATH, true).doInBackGround();
            }
        });
//        mQueryUSB2PictureTask = new QueryPictureTask(mContext, null, USBConstants.USBPath.USB1_PATH, true);
//        mQueryUSB2PictureTask.execute();
    }

    /**
     * USB2扫描中的数据查询
     */
    public void startQueryPictureWithUSB2Scan(int scanStatus,String dir){
        Log.d(TAG,"startQueryPictureWithUSB2Scan(int scanStatus,String dir)");

        PictureListManager.getInstance().setUsb2ScanStatus(scanStatus);
        startQueryUSB2PictureWithDir(dir);
    }

    /**
     * USB2扫描中的数据查询
     */
    public void startQueryVideoWithUSB2Scan(int scanStatus){
        Log.d(TAG,"startQueryVideoWithUSB2Scan(int scanStatus)");

        VideoListManager.getInstance().setUsb2ScanStatus(scanStatus);
        singleUSB2VideoThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new VideoQuery(mContext,USBConstants.USBPath.USB1_PATH, true).doInBackGround();
            }
        });
//        mQueryUSB2VideoTask = new QueryVideoTask(mContext, null, USBConstants.USBPath.USB1_PATH, true);
//        mQueryUSB2VideoTask.execute();
    }

    /**
     * USB2扫描中的数据查询
     */
    public void startQueryVideoWithUSB2Scan(int scanStatus,String dir){
        Log.d(TAG,"startQueryVideoWithUSB2Scan(int scanStatus,String dir)");

        VideoListManager.getInstance().setUsb2ScanStatus(scanStatus);
        startQueryUSB2VideoWithDir(dir);
    }

    /**
     * USB2扫描中的数据查询
     */
    public void startQueryMusicWithUSB2Scan(int scanStatus){
        Log.d(TAG,"startQueryMusicWithUSB2Scan(int scanStatus)");

        MusicListManager.getInstance().setUsb2ScanStatus(scanStatus);
        singleUSB2MusicThreadPool.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                new MusicQuery(mContext,USBConstants.USBPath.USB1_PATH, true).doInBackGround();
            }
        });
//        mQueryUSB2MusicTask = new QueryMusicTask(mContext, null, USBConstants.USBPath.USB1_PATH, true);
//        mQueryUSB2MusicTask.execute();
    }

    /**
     * USB2扫描中的数据查询
     */
    public void startQueryMusicWithUSB2Scan(int scanStatus,String dir){
        Log.d(TAG,"startQueryMusicWithUSB2Scan(int scanStatus,String dir)");

        MusicListManager.getInstance().setUsb2ScanStatus(scanStatus);
        startQueryUSB2MusicWithDir(dir);
    }

    /**
     * 查询CVBOX的相册数据
     */
    public void startQueryAlbum(){
        albumThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                new AlbumQuery(mContext).doInBackGround();
            }
        });
    }
}
