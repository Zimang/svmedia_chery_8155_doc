package com.desaysv.querypicture.task;
/**
 * created by ZNB on 2022-01-13
 * 异步处理查询数据库，最终结果返回的是符合条件的 图片(音乐、视频)列表和文件夹列表
 * 数据库查询不算耗时，但是数据转换为 图片(音乐、视频)或文件夹、文件夹包含的内容数量，这些都是耗时操作
 * 因此需要使用异步处理实现
 */

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.querypicture.constant.USBConstants;
import com.desaysv.querypicture.utils.PinyinComparator;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseQueryTask extends AsyncTask<Object, Integer, List<FileMessage>> {
    protected final String TAG = this.getClass().getSimpleName();
    protected List<FileMessage> mAllFileMessageList = new ArrayList<>();
    protected List<FolderMessage> folderMessageList = new ArrayList<>();
    protected List<FileMessage> mCurrentFileMessageList = new ArrayList<>();
    protected IQueryResultListener mListener;
    private Context mContext;
    protected Cursor mCursor;
    protected int count;
    protected String directory = "";
    protected int usbType = 0;
    protected boolean forceUpdate = false;

    protected static Uri queryAndroidUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;//Android 原生的数据库，这个是为了后续切到原生数据库
    protected static Uri querySVUri = Uri.parse("content://com.desaysv.mediaprovider.picture/picture");

    public BaseQueryTask(Context context, IQueryResultListener listener) {
        mContext = context;
        this.mListener = listener;
    }

    public BaseQueryTask(Context context, IQueryResultListener listener, String dir) {
        mContext = context;
        this.mListener = listener;
        this.directory = dir;
    }

    public BaseQueryTask(Context context, IQueryResultListener listener, String dir, boolean forceUpdate) {
        mContext = context;
        this.mListener = listener;
        this.directory = dir;
        this.forceUpdate = forceUpdate;

        if (forceUpdate){
            if (dir.contains(USBConstants.USBPath.USB0_PATH)){
                clearUSB1Cache();
            }else if (dir.contains(USBConstants.USBPath.USB1_PATH)){
                clearUSB2Cache();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<FileMessage> messageList) {
        super.onPostExecute(messageList);
        Log.d(TAG,"onPostExecute : " + directory);
        if (directory.contains(USBConstants.USBPath.USB0_PATH)){
            Log.d(TAG,"addCurrentUSB1List");
            addCurrentUSB1List();
        }else if (directory.contains(USBConstants.USBPath.USB1_PATH)){
            Log.d(TAG,"addCurrentUSB2List");
            addCurrentUSB2List();
        }
        if (mListener != null)
            mListener.onResult(mAllFileMessageList,mCurrentFileMessageList,folderMessageList);//实际返回的是当前目录的图片(音乐、视频)列表和文件夹列表
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mListener != null)
            mListener.onProgressUpdate(values[0]);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("Range")
    @Override
    protected List<FileMessage> doInBackground(Object... objects) {

        if (directory.contains(USBConstants.USBPath.USB0_PATH)) {
            if (HasUSB1Cache()){
                return null;
            }
        }else if (directory.contains(USBConstants.USBPath.USB1_PATH)){
            if (HasUSB2Cache()){
                return null;
            }
        }

        if (MediaKey.SUPPORT_ANDROID_MEDIA_PROVIDER){
            queryWithAndroidMediaProvider();
        }else {
            queryWithDeSayMediaProvider();
        }

        getFolderAndCurrentFileMessageList();

        Sort();

        if (directory.equals(USBConstants.USBPath.USB0_PATH)){
            addUSB1List();
        }else if (directory.equals(USBConstants.USBPath.USB1_PATH)){
            addUSB2List();
        }

        if (directory.contains(USBConstants.USBPath.USB0_PATH)){
            addUSB1CacheList();
        }else if (directory.contains(USBConstants.USBPath.USB1_PATH)){
            addUSB2CacheList();
        }

        return mAllFileMessageList;
    }

    @SuppressLint("Range")
    private void queryWithDeSayMediaProvider(){
        ContentResolver resolver = mContext.getContentResolver();
        if (directory != null && directory.length() > 0) {// 根据目录查询
            String selection = MediaKey.ROOT_PATH + "=?";
            String[] args = new String[]{directory};
            if (directory.equals(USBConstants.USBPath.USB0_PATH) || directory.equals(USBConstants.USBPath.USB1_PATH)) {// 传入的是根目录
                // keep current
            } else {//传入的是其它目录
                selection = MediaKey.PATH + " like ?";
                args = new String[]{directory + "%"};
            }
            Log.d(TAG, "doInBackground: directory:" + directory);
            mCursor = resolver.query(getQuerySVUri(), null, selection, args, null);
        } else {//当前这个没有使用
            mCursor = resolver.query(getQuerySVUri(), null, null, null, null);
        }

        count = 0;
        while (mCursor.moveToNext()) {// 查到数据库所有的图片(音乐、视频)
            mAllFileMessageList.add(getSVMessage());
            count++;
            publishProgress(count);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("Range")
    private void queryWithAndroidMediaProvider(){
        ContentResolver resolver = mContext.getContentResolver();
        if (directory != null && directory.length() > 0) {// 根据目录查询
            String selection = getQuerySelection();
            String[] args = new String[]{"%" + directory + "%"};
            Log.d(TAG, "doInBackground: directory:" + directory);
            Log.d(TAG, "doInBackground: forceupdate:" + forceUpdate);
            mCursor = resolver.query(getQueryAndroidUri(), null, selection, args, null);
        } else {//当前这个没有使用
            mCursor = resolver.query(getQueryAndroidUri(), null, null, null, null);
        }

        count = 0;
        while (mCursor.moveToNext()) {// 查到数据库所有的图片(音乐、视频)
            if (filter()){
                continue;
            }
            mAllFileMessageList.add(getAndroidMessage());
            count++;
            publishProgress(count);
        }
    }

    /**
     * 过滤器，用于过滤指定类型
      * @return
     */
    protected abstract boolean filter();

    /**
     * 通过查询到的所有图片(音乐、视频)，获取图片(音乐、视频)的路径，
     * 根据路径和传入的要查询的directory，获得 directory内的 文件夹和图片(音乐、视频)
     */
    private void getFolderAndCurrentFileMessageList() {
        mCurrentFileMessageList.clear();
        folderMessageList.clear();
        FolderMessage tempFolder = null;
        for (int i = 0; i < mAllFileMessageList.size(); i++) {
            String str = mAllFileMessageList.get(i).getPath();
            String subStr = str.substring(directory.length() + 1);//subStr 最开头是 ‘/’, 所以 +1，获得要查询目录下的子文件(夹)路径
            if (subStr != null && subStr.contains("/")){//表示是一个文件夹
                FolderMessage folderMessage = new FolderMessage();
                String folderName = subStr.split("/")[0];
                String path = directory + "/" +folderName;
                folderMessage.setName(folderName);
                folderMessage.setPath(path);
                folderMessage.setParentPath(directory);

                if (path.contains(USBConstants.USBPath.USB0_PATH)){
                    if (!containUSB1Folder(path)){
                        folderMessageList.add(folderMessage);
                        addCacheUSB1Folder(path,folderMessage);
                    }
                }else if (path.contains(USBConstants.USBPath.USB1_PATH)){
                    if (!containUSB2Folder(path)){
                        folderMessageList.add(folderMessage);
                        addCacheUSB2Folder(path,folderMessage);
                    }
                }

            }else {//表示是个图片(音乐、视频)文件
                mCurrentFileMessageList.add(mAllFileMessageList.get(i));
            }

        }

        // 再起一个循环，获取每个 folder 下有多少个图片(音乐、视频)
        if (folderMessageList != null && folderMessageList.size() > 0) {
            for (FolderMessage folderMessage : folderMessageList) {
                int picCount = 0;
                for (FileMessage fileMessage : mAllFileMessageList) {
                    if (fileMessage.getPath().startsWith(folderMessage.getPath())) {
                        picCount++;
                    }
                }
               folderMessage.setCount(picCount);
            }
        }
    }
    
    
    public interface IQueryResultListener {
        void onPreExecute(int count);
        void onProgressUpdate(int progress);
        void onResult(List<FileMessage> allList, List<FileMessage> currentDirectoryList, List<FolderMessage> currentDirectoryFolderList);
    }

    /**
     * 排序，默认使用名称字母排序
     * 如果需要自定义，可以复写
     */
    protected void Sort(){
        // 只在根目录时需要排一次总的列表
        if (directory.equals(USBConstants.USBPath.USB0_PATH) || directory.equals(USBConstants.USBPath.USB1_PATH)){
            Collections.sort(mAllFileMessageList, new Comparator<FileMessage>() {
                Comparator<String> comparator = new PinyinComparator();

                @Override
                public int compare(FileMessage o1, FileMessage o2) {
                    return comparator.compare(o1.getFileName(), o2.getFileName());
                }
            });
        }

        // 当前目录的图片文件排序
        Collections.sort(mCurrentFileMessageList, new Comparator<FileMessage>() {
            Comparator<String> comparator = new PinyinComparator();

            @Override
            public int compare(FileMessage o1, FileMessage o2) {
                return comparator.compare(o1.getFileName(), o2.getFileName());
            }
        });

        // 当前目录文件夹的排序
        Collections.sort(folderMessageList, new Comparator<FolderMessage>() {
            Comparator<String> comparator = new PinyinComparator();

            @Override
            public int compare(FolderMessage o1, FolderMessage o2) {
                return comparator.compare(o1.getName(), o2.getName());
            }
        });
    }

    /**
     * 为了支持查询不同的媒体类型，
     * 下面这些代码都是需要被复写的，
     */
    protected abstract Uri getQueryAndroidUri();

    protected abstract Uri getQuerySVUri();

    protected abstract String getQuerySelection();

    protected abstract boolean HasUSB1Cache();

    protected abstract boolean HasUSB2Cache();

    protected abstract void addUSB1List();

    protected abstract void addUSB2List();

    @SuppressLint("Range")
    protected abstract FileMessage getSVMessage();

    @SuppressLint("Range")
    protected abstract FileMessage getAndroidMessage();

    /**
     * 设置对应目录的列表缓存
     */
    protected abstract void addUSB1CacheList();


    protected abstract void addUSB2CacheList();

    protected abstract void addCurrentUSB1List();

    protected abstract void addCurrentUSB2List();

    protected abstract void addCacheUSB1Folder(String path , FolderMessage folderMessage);

    protected abstract void addCacheUSB2Folder(String path , FolderMessage folderMessage);

    protected abstract boolean containUSB1Folder(String path);

    protected abstract boolean containUSB2Folder(String path);

    protected abstract void clearUSB1Cache();

    protected abstract void clearUSB2Cache();
}
