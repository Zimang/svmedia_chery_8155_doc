package com.desaysv.moduleusbvideo.businesslogic.listsearch;

import android.text.TextUtils;
import android.util.Log;

import com.desaysv.moduleusbvideo.bean.FolderBean;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Time: 2020-12-7
 * Author: 从广汽那里抄过来的
 * Description: 用来全局存储文件夹列表数据的单例
 *
 * @author uidp5370
 */
public class USBVideoFolderListData {
    private static final String TAG = "USBVideoFolderListData";

    private final List<IFolderMapDataChange> VideoFolderMapChangeList = new ArrayList<>();

    private static final class InstanceHolder {
        static final USBVideoFolderListData instance = new USBVideoFolderListData();
    }

    public static USBVideoFolderListData getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * USB1文件夹的全部音乐列表
     * String: 文件夹的路径作为key
     * List<FolderBean>: 文件夹列表作为value
     */
    private final Map<String, List<FolderBean>> mUSB1VideoFolderMap = new HashMap<>();

    /**
     * 清空USB1的Map列表
     */
    public void clearUSB1VideoFolderMap() {
        Log.d(TAG, "clearUSB1VideoFolderMap: ");
        mUSB1VideoFolderMap.clear();
        for (IFolderMapDataChange iMapDataChange : VideoFolderMapChangeList) {
            iMapDataChange.onUSB1VideoFolderMapChange();
        }
    }

    /**
     * remove Map数据中的某一个k-v
     */
    public void clearUSB1VideoFolderListInMap(String path) {
        Log.d(TAG, "clearUSB1VideoFolderListInMap: path = " + path);
        mUSB1VideoFolderMap.remove(path);
        for (IFolderMapDataChange iMapDataChange : VideoFolderMapChangeList) {
            iMapDataChange.onUSB1VideoFolderMapChange();
        }
    }

    /**
     * 往Map数据中添加数据
     *
     * @param path        作为Map的key
     * @param folderBeans 作为Map的value
     */
    public void addUSB1VideoFolderList(String path, List<FolderBean> folderBeans) {
        if (TextUtils.isEmpty(path) || folderBeans == null) {
            return;
        }
        Log.d(TAG, "addUSB1VideoFolderList: path = " + path + ", folderBeans.size = " + folderBeans.size());
        mUSB1VideoFolderMap.put(path, folderBeans);
    }

    /**
     * 返回文件夹列表
     *
     * @param path 取值的key
     * @return 文件夹列表，如果key不存在，那么创建一个新的list返回
     */
    public List<FolderBean> getUSB1VideoFolderList(String path) {
        Log.d(TAG, "getUSB1VideoFolderList: path = " + path);
        if (!mUSB1VideoFolderMap.containsKey(path)) {
            Log.d(TAG, "getUSB1VideoFolderList: build new list");
            mUSB1VideoFolderMap.put(path, new ArrayList<FolderBean>());
        }

        return mUSB1VideoFolderMap.get(path);
    }

    /**
     * 返回文件夹全部数据
     *
     * @return map
     */
    public Map<String, List<FolderBean>> getUSB1VideoFolderMap() {
        Log.d(TAG, "getUSB1VideoFolderMap: map.size = " + mUSB1VideoFolderMap.size());
        return mUSB1VideoFolderMap;
    }

    /**
     * 判断不同音乐所在文件夹是否重复，value值记录着这个path下的歌曲数量
     */
    private final HashMap<String, Integer> mUSB1FolderMap = new HashMap<>();

    /**
     * 获取路径下所有的音乐数量（包括子文件夹）
     *
     * @param path path
     * @return size
     */
    public int getUSB1AllVideoNumInPath(String path) {
        Log.d(TAG, "getUSB1AllVideoNumInPath: path = " + path);
        return mUSB1FolderMap.get(path);
    }

    /**
     * 将音乐列表添加到 Map 中
     *
     * <p>
     * 添加歌曲：
     * 拿到一首歌就要算出歌的上一级目录，然后拿到对应list添加进入就行了
     *
     * <p>
     * 计算文件夹歌曲数量(包括子文件夹):
     * 例如/storage/usb1/MP3/青花瓷.mp3，可以拆分成3个路径，/storage，/storage/usb1，/storage/usb1/MP3，
     * 歌曲在这些目录下，那么mUSB1FolderMap对应的value需要 +1，value代表的是key目录下歌曲的数量
     *
     * <p>
     * 文件夹添加：
     * 同上面3个路径，循环这三个路径，在mUSB1VideoFolderMap比较是否存在，如果不存在则创建一个新的list，
     * 将新路径作为key，新list作为value添加进去，如果以及有了，那么不添加
     */
    public void addVideoListInUSB1Map(List<FileMessage> videoList) {
        Log.d(TAG, "addVideoListInUSB1Map: size = " + videoList.size());
        mUSB1VideoFolderMap.clear();
        mUSB1FolderMap.clear();
        // 循环遍历整个文件列表
        for (FileMessage video : videoList) {
            // videoIndex为音乐名在整个path中的index
            int videoIndex = video.getPath().lastIndexOf("/");
            // 音乐路径 = 全部path - 音乐名
            String videoPath = video.getPath().substring(0, videoIndex);
            Log.d(TAG, "addVideoListInUSB1Map: videoPath = " + videoPath + ", videoName = " + video.getName());
            // stringBuilder用于路径叠加计算
            StringBuilder stringBuilder = new StringBuilder();
            // 这个for主要是添加文件夹 和 计算当前文件夹包含的歌曲数量(包括子文件夹)
            for (String folderName : videoPath.substring(1).split("/")) {
                stringBuilder.append("/");
                stringBuilder.append(folderName);
                // 如果在map中没有发现这个文件夹
                if (!mUSB1FolderMap.containsKey(stringBuilder.toString())) {
                    // 新建一个map，value是1，代表这个文件夹下目前只有1首歌
                    mUSB1FolderMap.put(stringBuilder.toString(), 1);
                    int folderIndex = stringBuilder.toString().lastIndexOf("/");
                    Log.d(TAG, "addVideoListInUSB1Map: folderName = " + folderName + ", folderPath = " +
                            stringBuilder.substring(0, folderIndex));
                    Log.d(TAG, "addVideoListInUSB1Map: add " + stringBuilder.substring(0, folderIndex));
                    // 新建一个文件夹，添加进去
                    getUSB1VideoFolderList(stringBuilder.substring(0, folderIndex)).
                            add(0, new FolderBean(true, folderName, stringBuilder.toString()));
                } else {
                    // 文件夹存在了，那么需要将歌曲数量+1
                    mUSB1FolderMap.put(stringBuilder.toString(), mUSB1FolderMap.get(stringBuilder.toString()) + 1);
                }
            }
            // 这个最好理解的，拿到一首歌就要算出歌的上一级目录，然后拿到对应list添加进入就行了
            getUSB1VideoFolderList(videoPath).add(new FolderBean(video));
        }

        for (IFolderMapDataChange iMapDataChange : VideoFolderMapChangeList) {
            iMapDataChange.onUSB1VideoFolderMapChange();
        }
    }


    /**
     * USB2文件夹的全部音乐列表
     * String: 文件夹的路径作为key
     * List<FolderBean>: 文件夹列表作为value
     */
    private final Map<String, List<FolderBean>> mUSB2VideoFolderMap = new HashMap<>();

    /**
     * 清空USB2的Map列表
     */
    public void clearUSB2VideoFolderMap() {
        Log.d(TAG, "clearUSB2VideoFolderMap: ");
        mUSB2VideoFolderMap.clear();
        for (IFolderMapDataChange iMapDataChange : VideoFolderMapChangeList) {
            iMapDataChange.onUSB2VideoFolderMapChange();
        }
    }

    /**
     * remove Map数据中的某一个k-v
     */
    public void clearUSB2VideoFolderListInMap(String path) {
        Log.d(TAG, "clearUSB2VideoFolderListInMap: path = " + path);
        mUSB2VideoFolderMap.remove(path);
        for (IFolderMapDataChange iMapDataChange : VideoFolderMapChangeList) {
            iMapDataChange.onUSB2VideoFolderMapChange();
        }
    }

    /**
     * 往Map数据中添加数据
     *
     * @param path        作为Map的key
     * @param folderBeans 作为Map的value
     */
    public void addUSB2VideoFolderList(String path, List<FolderBean> folderBeans) {
        if (TextUtils.isEmpty(path) || folderBeans == null) {
            return;
        }
        Log.d(TAG, "addUSB2VideoFolderList: path = " + path + ", folderBeans.size = " + folderBeans.size());
        mUSB2VideoFolderMap.put(path, folderBeans);
    }

    /**
     * 返回文件夹列表
     *
     * @param path 取值的key
     * @return 文件夹列表，如果key不存在，那么创建一个新的list返回
     */
    public List<FolderBean> getUSB2VideoFolderList(String path) {
        Log.d(TAG, "getUSB2VideoFolderList: path = " + path);
        if (!mUSB2VideoFolderMap.containsKey(path)) {
            Log.d(TAG, "getUSB2VideoFolderList: build new list");
            mUSB2VideoFolderMap.put(path, new ArrayList<FolderBean>());
        }

        return mUSB2VideoFolderMap.get(path);
    }

    /**
     * 返回文件夹全部数据
     *
     * @return map
     */
    public Map<String, List<FolderBean>> getUSB2VideoFolderMap() {
        Log.d(TAG, "getUSB2VideoFolderMap: map.size = " + mUSB2VideoFolderMap.size());
        return mUSB2VideoFolderMap;
    }

    /**
     * 判断不同音乐所在文件夹是否重复，value值记录着这个path下的歌曲数量
     */
    private final HashMap<String, Integer> mUSB2FolderMap = new HashMap<>();

    /**
     * 获取路径下所有的音乐数量（包括子文件夹）
     *
     * @param path path
     * @return size
     */
    public int getUSB2AllVideoNumInPath(String path) {
        Log.d(TAG, "getUSB2AllVideoNumInPath: path = " + path);
        return mUSB2FolderMap.get(path);
    }

    /**
     * 将音乐列表添加到 Map 中（算法思想同 USB1 操作，在上面有提到）
     */
    public void addVideoListInUSB2Map(List<FileMessage> videoList) {
        Log.d(TAG, "addVideoListInUSB2Map: size = " + videoList.size());
        mUSB2VideoFolderMap.clear();
        mUSB2FolderMap.clear();
        for (FileMessage video : videoList) {
            int videoIndex = video.getPath().lastIndexOf("/");
            String videoPath = video.getPath().substring(0, videoIndex);
            Log.d(TAG, "addVideoListInUSB2Map: videoPath = " + videoPath + ", videoName = " + video.getFileName());
            StringBuilder stringBuilder = new StringBuilder();
            for (String folderName : videoPath.substring(1).split("/")) {
                stringBuilder.append("/");
                stringBuilder.append(folderName);
                if (!mUSB2FolderMap.containsKey(stringBuilder.toString())) {
                    mUSB2FolderMap.put(stringBuilder.toString(), 1);
                    int folderIndex = stringBuilder.toString().lastIndexOf("/");
                    Log.d(TAG, "addVideoListInUSB2Map: folderName = " + folderName + ", folderPath = " +
                            stringBuilder.substring(0, folderIndex));
                    Log.d(TAG, "addVideoListInUSB2Map: add " + stringBuilder.substring(0, folderIndex));
                    getUSB2VideoFolderList(stringBuilder.substring(0, folderIndex)).
                            add(0, new FolderBean(true, folderName, stringBuilder.toString()));
                } else {
                    // 歌曲数量+1
                    mUSB2FolderMap.put(stringBuilder.toString(), mUSB2FolderMap.get(stringBuilder.toString()) + 1);
                }
            }

            getUSB2VideoFolderList(videoPath).add(new FolderBean(video));
        }

        for (IFolderMapDataChange iMapDataChange : VideoFolderMapChangeList) {
            iMapDataChange.onUSB2VideoFolderMapChange();
        }
    }

    /**
     * 设置音乐列表数据的变化回调
     *
     * @param iVideoMapChange 回调
     */
    public void setVideoFolderMapChangeListener(IFolderMapDataChange iVideoMapChange) {
        Log.d(TAG, "setVideoFolderMapChangeListener: " + iVideoMapChange.toString());
        VideoFolderMapChangeList.remove(iVideoMapChange);
        VideoFolderMapChangeList.add(iVideoMapChange);
    }

    /**
     * 清除音乐数据的状态回调
     *
     * @param iVideoMapChange 音乐数据变化的回调
     */
    public void removeVideoFolderMapChangeListener(IFolderMapDataChange iVideoMapChange) {
        Log.d(TAG, "removeVideoFolderMapChangeListener: " + iVideoMapChange.toString());
        VideoFolderMapChangeList.remove(iVideoMapChange);
    }


    /**
     * 用来监听数据变化的逻辑，然后回调给各个监听者
     */
    public interface IFolderMapDataChange {

        /**
         * USB1音乐文件夹数据变化触发的回调
         */
        void onUSB1VideoFolderMapChange();

        /**
         * USB2音乐文件夹数据变化触发的回调
         */
        void onUSB2VideoFolderMapChange();

    }


    /* ************************************************************************************/
    /**
     * USB1文件夹当前播放列表
     */
    private final List<FileMessage> mUSB1FolderCurrentPlayList = new ArrayList<>();

    /**
     * 获取这个路径下的音乐播放列表
     *
     * @param path 音乐文件的路径
     * @return mUSB1FolderCurrentPlayList
     */
    public List<FileMessage> getUSB1VideoFolderPlayList(String path) {
        Log.d(TAG, "getUSB1VideoFolderPlayList: ");
        List<FolderBean> folderBeans = getUSB1VideoFolderList(getParentPath(path));
        Log.d(TAG, "getUSB1VideoFolderPlayList: folderBeans = " + folderBeans.size());
        mUSB1FolderCurrentPlayList.clear();
        for (FolderBean folderBean : folderBeans) {
            if (!folderBean.isFolder()) {
                mUSB1FolderCurrentPlayList.add(folderBean.getVideo());
                Log.d(TAG, "getUSB1VideoFolderPlayList: " + folderBean.getVideo().toString());
            }
        }
        Log.d(TAG, "getUSB1VideoFolderPlayList: mUSB1FolderCurrentPlayList.size = " + mUSB1FolderCurrentPlayList.size());
        return mUSB1FolderCurrentPlayList;
    }

    /**
     * 获取文件夹的路径
     *
     * @param path 音乐文件的路径
     * @return 文件夹的路径
     */
    public String getParentPath(String path) {
        Log.d(TAG, "getParentPath: path = " + path);
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "getParentPath: path is empty");
            return "";
        }
        int index = path.lastIndexOf("/");
        Log.d(TAG, "getParentPath: index = " + index);
        String parentPath = path.substring(0, index);
        Log.d(TAG, "getParentPath: parentPath = " + parentPath);
        return parentPath;
    }

    /**
     * USB2文件夹当前播放列表
     */
    private final List<FileMessage> mUSB2FolderCurrentPlayList = new ArrayList<>();

    /**
     * 获取 USB2文件夹播放列表
     *
     * @param path 音乐文件的路径
     * @return mUSB2FolderCurrentPlayList
     */
    public List<FileMessage> getUSB2VideoFolderPlayList(String path) {
        Log.d(TAG, "getUSB2VideoFolderPlayList: ");
        List<FolderBean> folderBeans = getUSB2VideoFolderList(getParentPath(path));
        Log.d(TAG, "getUSB2VideoFolderPlayList: folderBeans = " + folderBeans.size());
        mUSB2FolderCurrentPlayList.clear();
        for (FolderBean folderBean : folderBeans) {
            if (!folderBean.isFolder()) {
                mUSB2FolderCurrentPlayList.add(folderBean.getVideo());
                Log.d(TAG, "getUSB2VideoFolderPlayList: " + folderBean.getVideo().toString());
            }
        }
        Log.d(TAG, "getUSB2VideoFolderPlayList: mUSB2FolderCurrentPlayList.size = " + mUSB2FolderCurrentPlayList.size());
        return mUSB2FolderCurrentPlayList;
    }
}
