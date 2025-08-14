package com.desaysv.libusbmedia.bean;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.ArrayMap;
import android.util.Log;

import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.libusbmedia.action.MediaControlAction;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.libusbmedia.interfaze.IMediaStatusTool;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.svliblyrics.lyrics.LyricsParser;
import com.desaysv.svliblyrics.lyrics.LyricsRow;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.libusbmedia.utils.MusicTool;
import com.desaysv.usbbaselib.bean.USBConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author lzm
 * @date 2019-6-13
 * 数据列表必须和控制，折行单元同一个级别，需要作为一个单例模式抽出来
 */
public class CurrentPlayInfo implements IStatusTool {

    private static final String TAG = "CurrentPlayInfo";
    //open的Reason进行保存
    private ChangeReason openReason = ChangeReasonData.NA;

    /************************************单例模式的获取*************************************/
    private static CurrentPlayInfo LocalMusicCurrentPlayInfo;
    private static CurrentPlayInfo USB1MusicCurrentPlayInfo;
    private static CurrentPlayInfo USB2MusicCurrentPlayInfo;
    private static CurrentPlayInfo USB1VideoCurrentPlayInfo;
    private static CurrentPlayInfo USB2VideoCurrentPlayInfo;
    private static CurrentPlayInfo RecentMusicCurrentPlayInfo;
    private Runnable loadMediaAlbumPicRunnable;
    private Runnable loadMediaPlayLyricsRunnable;
    private MediaType currentMediaType;

    /**
     * 单例模式，根据mediaType获取不同的对象
     *
     * @param mediaType USB1_MUSIC; USB2_MUSIC; USB1_VIDEO; USB2_VIDEO;
     * @return 获取数据状态单元
     */
    public static CurrentPlayInfo getInstance(MediaType mediaType) {
        Log.d(TAG, "getInstance: mediaType = " + mediaType);
        if (mediaType == MediaType.USB1_MUSIC) {
            if (USB1MusicCurrentPlayInfo == null) {
                synchronized (CurrentPlayInfo.class) {
                    if (USB1MusicCurrentPlayInfo == null) {
                        USB1MusicCurrentPlayInfo = new CurrentPlayInfo(mediaType);
                    }
                }
            }
            return USB1MusicCurrentPlayInfo;
        } else if (mediaType == MediaType.USB2_MUSIC) {
            if (USB2MusicCurrentPlayInfo == null) {
                synchronized (CurrentPlayInfo.class) {
                    if (USB2MusicCurrentPlayInfo == null) {
                        USB2MusicCurrentPlayInfo = new CurrentPlayInfo(mediaType);
                    }
                }
            }
            return USB2MusicCurrentPlayInfo;
        } else if (mediaType == MediaType.USB1_VIDEO) {
            if (USB1VideoCurrentPlayInfo == null) {
                synchronized (CurrentPlayInfo.class) {
                    if (USB1VideoCurrentPlayInfo == null) {
                        USB1VideoCurrentPlayInfo = new CurrentPlayInfo(mediaType);
                    }
                }
            }
            return USB1VideoCurrentPlayInfo;
        } else if (mediaType == MediaType.USB2_VIDEO) {
            if (USB2VideoCurrentPlayInfo == null) {
                synchronized (CurrentPlayInfo.class) {
                    if (USB2VideoCurrentPlayInfo == null) {
                        USB2VideoCurrentPlayInfo = new CurrentPlayInfo(mediaType);
                    }
                }
            }
            return USB2VideoCurrentPlayInfo;
        } else if (mediaType == MediaType.LOCAL_MUSIC) {
            if (LocalMusicCurrentPlayInfo == null) {
                synchronized (CurrentPlayInfo.class) {
                    if (LocalMusicCurrentPlayInfo == null) {
                        LocalMusicCurrentPlayInfo = new CurrentPlayInfo(mediaType);
                    }
                }
            }
            return LocalMusicCurrentPlayInfo;
        } else if (mediaType == MediaType.RECENT_MUSIC) {
            if (RecentMusicCurrentPlayInfo == null) {
                synchronized (CurrentPlayInfo.class) {
                    if (RecentMusicCurrentPlayInfo == null) {
                        RecentMusicCurrentPlayInfo = new CurrentPlayInfo(mediaType);
                    }
                }
            }
            return RecentMusicCurrentPlayInfo;
        }
        Log.e(TAG, "getInstance: with error parameter");
        return null;
    }

    //一个子线程的Handler，用来解析专辑封面信息
    private Handler mHandler;

    /**
     * 构造函数
     *
     * @param mediaType USB1_MUSIC; USB2_MUSIC; USB1_VIDEO; USB2_VIDEO;
     */
    private CurrentPlayInfo(MediaType mediaType) {
        Log.d(TAG, "CurrentPlayInfo: mediaType = " + mediaType);
        mMediaStatusTool = MediaControlAction.getInstance(mediaType);
        currentMediaType = mediaType;
        HandlerThread handlerThread = new HandlerThread("" + mediaType);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }


    /****************************************************媒体状态与状态获取接口*******************************************/

    //当前播放歌词解析成的列表
    private List<LyricsRow> mCurrentPlayLyrics = new ArrayList<>();

    /**
     * 获取当前播放的歌词列表
     *
     * @return List<LyricsRow>
     */
    @Override
    public List<LyricsRow> getCurrentPlayLyrics() {
        return mCurrentPlayLyrics;
    }

    /**
     * 设置当前的歌词列表
     *
     * @param mCurrentPlayLyrics mCurrentPlayLyrics
     */
    public void setCurrentPlayLyrics(List<LyricsRow> mCurrentPlayLyrics) {
        this.mCurrentPlayLyrics = mCurrentPlayLyrics;
        Log.d(TAG, "setCurrentPlayLyrics: mCurrentPlayLyrics = " + mCurrentPlayLyrics.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                Log.d(TAG, "setCurrentPlayLyrics: onLyricsChange: className = " + entry.getKey());
                //由于切换的时候，时间是不断的回调，有可能出现空指针异常，所以这里加入判空逻辑
                if (entry.getValue() != null) {
                    entry.getValue().onLyricsChange();
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    //这个是当前播放的媒体信息，为Null会引起空指针
    private FileMessage mCurrentPlayItem = new FileMessage();

    /**
     * 设置当前的播放的媒体信息
     *
     * @param currentPlayItem 当前播放的媒体信息
     */
    public void setCurrentPlayItem(FileMessage currentPlayItem) {
        Log.d(TAG, "setCurrentPlayItem: currentPlayItem = " + currentPlayItem + " mCurrentPlayItem = " + mCurrentPlayItem);
        //只有数据变化的时候，才进行刷新
        //add by LZ 这里这么做是不行的，当前的info实例存在多个（不同type），当音源切换时到其它再切换回来时，这个type下就会认为是相同的，不再进行更新
        //所以不单单这样进行判断（还要考虑焦点更新等等状态下要重置这个type的状态）条件多，还不如设置时都更新，由界面去做这个规避动作（这样至少回调不会出错）
        //暂时如此修改，要不各个模块当中
        if (mCurrentPlayItem != currentPlayItem) {
            mCurrentPlayItem = currentPlayItem;
            initMediaPlayLyrics(mCurrentPlayItem);
            getMediaAlbumPic(mCurrentPlayItem);
        }
        //数据切换的时候需要更新专辑数据
        OnMediaInfoChange();
    }

    /**
     * 根据当前路径解析歌词状态
     */
    private void initMediaPlayLyrics(FileMessage fileMessage) {
        if (loadMediaPlayLyricsRunnable != null) {
            mHandler.removeCallbacks(loadMediaPlayLyricsRunnable);
        }
        loadMediaPlayLyricsRunnable = () -> {
            Log.d(TAG, "initMediaPlayLyrics mCurrentPlayItem = " + mCurrentPlayItem);
            Log.d(TAG, "initMediaPlayLyrics fileMessage = " + fileMessage);
            if (fileMessage == null) {
                setCurrentPlayLyrics(new ArrayList<>());
            } else {
                String musicPath = fileMessage.getPath();
                int index = musicPath.lastIndexOf(".");
                Log.d(TAG, "initMediaPlayLyrics index = " + index);
                if (index != -1) {
                    String lrcFilePath = musicPath.substring(0, index) + ".lrc";
                    Log.d(TAG, "initMediaPlayLyrics lrcFilePath = " + lrcFilePath);
                    File file = new File(lrcFilePath);
                    if (file.exists()) {
                        setCurrentPlayLyrics(LyricsParser.getLyricsRows(file));
                    } else {
                        setCurrentPlayLyrics(new ArrayList<>());
                    }
                } else {
                    setCurrentPlayLyrics(new ArrayList<>());
                }
            }
        };
        mHandler.post(loadMediaPlayLyricsRunnable);
    }

    /**
     * 在音乐切换的时候获取专辑封面的数据
     */
    private void getMediaAlbumPic(final FileMessage fileMessage) {
        //开一个子线程获取专辑封面数据，由于可能出现频繁点击，考虑到同异步处理，所以使用handler来说实现消息队列
        if (loadMediaAlbumPicRunnable != null) {
            mHandler.removeCallbacks(loadMediaAlbumPicRunnable);
        }
        loadMediaAlbumPicRunnable = () -> {
            Log.d(TAG, "getMediaAlbumPic mCurrentPlayItem = " + mCurrentPlayItem);
            Log.d(TAG, "getMediaAlbumPic fileMessage = " + fileMessage);
            if (fileMessage == null) {
                setAlbumPicData(null, null);
            } else {
                if (fileMessage.getPath().equals(mCurrentPlayItem.getPath())) {
                    Log.d(TAG, "getMediaAlbumPic loadPic ");
                    //解析媒体的专辑封面数据，这个数据是没有经过压缩的，不能直接通过AIDL给到外部应用，binder会爆掉的
                    byte[] data = MusicTool.getID3Path_ForFragment(fileMessage.getPath());
                    //将媒体的专辑封面数据进行压缩，这里压缩之后的数据，只有十几k，所以可以使用binder传输给外部媒体
                    byte[] newData = MusicTool.getCompressionPicByte(data);
                    //设置图片数据变化
                    if (fileMessage.getPath().equals(mCurrentPlayItem.getPath())) {
                        setAlbumPicData(data, newData);
                    }
                }
            }
        };
        mHandler.post(loadMediaAlbumPicRunnable);
    }

    //图片数据
    private byte[] mAlbumPic;

    //压缩过后的图片数据
    private byte[] mCompressionAlbumPic;

    /**
     * 设置当前的播放媒体的图片数据
     *
     * @param picData            图片数据
     * @param compressionPicData 压缩过的图片数据
     */
    private void setAlbumPicData(byte[] picData, byte[] compressionPicData) {
        if (compressionPicData == null) {
            Log.d(TAG, "setAlbumPicData: compressionPicData is null");
        } else {
            Log.d(TAG, "setAlbumPicData: compressionPicData = " + compressionPicData.length);
        }
        mAlbumPic = picData;
        mCompressionAlbumPic = compressionPicData;
        onAlbumPicDataChange();
    }

    /**
     * 获取图片数据
     *
     * @return 压缩之后的图片数据
     */
    @Override
    public byte[] getAlbumPic() {
        Log.d(TAG, "getAlbumPic: mCurrentPlayItem = " + mCurrentPlayItem);
        if (mAlbumPic == null) {
            Log.d(TAG, "getAlbumPic: mAlbumPic is null");
        } else {
            Log.d(TAG, "getAlbumPic: mAlbumPic = " + mAlbumPic.length);
        }
        return mAlbumPic;
    }

    /**
     * 获取压缩过的专辑图片，可以通过AIDL对外提供
     *
     * @return 压缩过的专辑图片
     */
    @Override
    public byte[] getCompressionAlbumPic() {
        Log.d(TAG, "getCompressionAlbumPic: mCurrentPlayItem = " + mCurrentPlayItem);
        if (mCompressionAlbumPic == null) {
            Log.d(TAG, "getCompressionAlbumPic: mCompressionPicData is null");
        } else {
            Log.d(TAG, "getCompressionAlbumPic: mCompressionPicData = " + mCompressionAlbumPic.length);
        }
        return mCompressionAlbumPic;
    }


    //获取当前的循环模式（可能需要做掉电上电的记忆）
    private String mCurrentLoopMode = USBConstants.LoopType.CYCLE;

    /**
     * 设置当前的循环模式
     *
     * @param currentLoopMode 当前的循环模式在LoopType中定义了循环模式
     */
    public void setCurrentLoopMode(String currentLoopMode) {
        Log.d(TAG, "setCurrentLoopMode: currentLoopMode = " + currentLoopMode);
        mCurrentLoopMode = currentLoopMode;
        MediaPlayStatusSaveUtils.getInstance().saveLoopType(mCurrentLoopMode);
        OnLoopTypeChange();
    }

    //这一个是当前播放的列表
    private List<FileMessage> mCurrentPlayList;

    //当前播放列表的列表数量
    private int mCurrentListCount = 0;

    /**
     * 获取当前的播放列表
     *
     * @return mCurrentPlayList
     */
    public List<FileMessage> getCurrentPlayList() {
        return mCurrentPlayList;
    }

    /**
     * 设置当前的播放列表
     *
     * @param currentPlayList 当前的播放列表
     */
    public void setCurrentPlayList(List<FileMessage> currentPlayList) {
        Log.d(TAG, "setCurrentPlayList: mCurrentPlayList size = " + currentPlayList.size());
        mCurrentPlayList = currentPlayList;
        notifyPlayListChange();
        resetCurrentPlayPosition();
        mCurrentListCount = this.mCurrentPlayList.size();
    }

    //这个是当前播放的媒体的
    private int mCurrentPlayPosition;

    /**
     * 获取当前播放的item在列表中的位置 -- 这个接口没有对外给到界面使用，然后由于里面做了列表的递归，所以只能在子线程中使用
     *
     * @return 获取当前播放的媒体文件在列表中的那个位置
     */
    public int getCurrentPlayPosition() {
        Log.d(TAG, "getCurrentPlayPosition: mCurrentPlayPosition = " + mCurrentPlayPosition);
        //由于要实现分段加载，并且排序，所以如果列表发生改变之后，需要刷新当前播放item在列表中的位置
        mCurrentPlayPosition = resetCurrentPlayPosition();
        return mCurrentPlayPosition;
    }


    /**
     * 根据当前播放的媒体信息，更新列表的位置
     *
     * @return 当前播放信息在列表中的位置，如果获取不到，就保持原来的位置
     */
    private int resetCurrentPlayPosition() {
        int position = mCurrentPlayPosition;
        Log.d(TAG, "resetCurrentPlayPosition: mCurrentListCount = " + mCurrentListCount);
        if (mCurrentPlayList != null) {
            //如果设置下曲的列表个数，和当前列表的个数量不一致的时候，就需要进行遍历，获取文件的当前位置
            Log.d(TAG, "resetCurrentPlayPosition: mCurrentPlayList size = " + mCurrentPlayList.size());
            if (mCurrentListCount != mCurrentPlayList.size()) {
                if (mCurrentPlayItem != null) {
                    for (int i = 0; i < mCurrentPlayList.size(); i++) {
                        if (mCurrentPlayList.get(i).getPath().equals(mCurrentPlayItem.getPath())) {
                            position = i;
                            break;
                        }
                    }
                }
                //刷新完位置之后，需要根系当前位置和列表个数的对应关系
                mCurrentListCount = mCurrentPlayList.size();
            }
        }
        Log.d(TAG, "resetCurrentPlayPosition: mCurrentPlayPosition = " + mCurrentPlayPosition + " position = " + position);
        return position;
    }

    /**
     * 设置当前播放的item在当前播放列表中的位置
     *
     * @param mCurrentPlayPosition 当前播放的位置
     */
    public void setCurrentPlayPosition(int mCurrentPlayPosition) {
        Log.d(TAG, "setCurrentPlayPosition: mCurrentPlayPosition = " + mCurrentPlayPosition);
        this.mCurrentPlayPosition = mCurrentPlayPosition;
    }

    //这个是当前播放列表的type：全部，艺术家，文件夹等
    private CurrentPlayListType mCurrentPlayListType;

    /**
     * 设置当前播放列表的类型
     *
     * @param mCurrentPlayListType ALL，ARTIST，ALBUM，FLODER，COLLECT
     */
    public void setCurrentPlayListType(CurrentPlayListType mCurrentPlayListType) {
        this.mCurrentPlayListType = mCurrentPlayListType;
    }

    /**
     * 获取当前播放的item的总时间
     *
     * @return getDuration
     */
    public int getTotalPlayTime() {
        return mMediaStatusTool.getDuration();
    }

    /**
     * 获取当前媒体的播放时间
     *
     * @return getCurrentPlayTime
     */
    private int getMediaPlayTime() {
        return mMediaStatusTool.getCurrentPlayTime();
    }


    /**
     * 获取当前播放的列表，对外的接口
     *
     * @return mCurrentPlayList
     */
    @Override
    public List<FileMessage> getPlayList() {
        Log.d(TAG, "getPlayList: mCurrentPlayList");
        return mCurrentPlayList;
    }

    //设备的播放状态,实时的控制状态
    private boolean isPlaying = false;

    /**
     * 设置播放状态
     *
     * @param playing true：播放中；false：暂停
     */
    public void setPlaying(boolean playing) {
        isPlaying = playing;
        OnPlayStatusChange(isPlaying);
    }

    /**
     * 获取播放状态
     *
     * @return isPlaying
     */
    public boolean getPlaying() {
        return isPlaying;
    }

    /**
     * 设置媒体播放状态
     *
     * @param mediaPlayType NORMAL，OPENING，ERROR，NO_VIDEO
     */
    public void setMediaPlayType(MediaPlayType mediaPlayType) {
        Log.d(TAG, "setMediaPlayType: mediaPlayType = " + mediaPlayType);
        OnMediaTypeChange(mediaPlayType);
    }

    /**
     * 更新播放时间，媒体播放器跳转完成后要触发时间更新
     */
    public void updatePlayTime(int currentPlayTime, int duration) {
        OnPlayTimeChange(currentPlayTime, duration);
    }


    /****************************************************状态获取接口****************************************/

    /**
     * 获取当前播放的媒体信息，接口对外
     *
     * @return 当前的媒体信息
     */
    @Override
    public FileMessage getCurrentPlayItem() {
        return mCurrentPlayItem;
    }

    /**
     * 获取循环模式，接口对外
     *
     * @return 当前的循环模式
     */
    @Override
    public String getLoopType() {
        //由于循环模式的存储是异步的，所以在获取的时候，就不能获取底层存储的，要获取当前记忆的
        mCurrentLoopMode = MediaPlayStatusSaveUtils.getInstance().getLoopType();
        Log.d(TAG, "getLoopType: mCurrentLoopMode = " + mCurrentLoopMode);
        return mCurrentLoopMode;
    }

    /**
     * 获取当前是否是播放模式
     *
     * @return true：播放中 false：暂停中
     */
    @Override
    public boolean isPlaying() {
        return mMediaStatusTool.isPlaying();
    }


    /**
     * 获取收藏状态
     *
     * @return true：收藏，false：非收藏
     */
    @Override
    public boolean getCollect() {
        return false;
    }

    /**
     * 获取当前播放的的列表模式（收藏，艺术家，全部，专辑）
     *
     * @return mCurrentPlayListType
     */
    @Override
    public CurrentPlayListType getCurrentPlayListType() {
        return mCurrentPlayListType;
    }


    /**
     * 获取当前播放的总时间
     *
     * @return getTotalPlayTime
     */
    @Override
    public int getDuration() {
        return getTotalPlayTime();
    }

    /**
     * 获取当前播放的时间
     *
     * @return getMediaPlayTiem
     */
    @Override
    public int getCurrentPlayTime() {
        return getMediaPlayTime();
    }

    /**
     * 获取当前播放的媒体文件在列表中的位置，这个方法有循环逻辑，建议放在子线程调用
     *
     * @return mCurrentPlayPosition
     */
    @Override
    public int getCurrentItemPosition() {
        //如果列表的大小发生了改变，那当前的位置也需要刷新
        mCurrentPlayPosition = resetCurrentPlayPosition();
        return mCurrentPlayPosition;
    }

    private ChangeReason pauseReason = ChangeReasonData.NA;

    private IMediaStatusTool mMediaStatusTool;

    /**************************************************状态变化接口***********************************************/
    private ArrayMap<String, IMediaStatusChange> mMediaStatusChangeArrayMap = new ArrayMap<>();
    //进行加锁处理
    private ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private Lock writeLock = mReentrantLock.writeLock();

    /**
     * 注册媒体变化的接口回调
     *
     * @param mMediaStatusChangeListener 变化回调
     */
    public void registerMediaStatusChangeListener(String className, IMediaStatusChange mMediaStatusChangeListener) {
        writeLock.lock();
        try {
            mMediaStatusChangeArrayMap.put(className, mMediaStatusChangeListener);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 注销媒体变化的接口回调
     * 注意：这里有个线程安全问题，在回调发生时如果此时移除了回调，则会出现空指针
     * 但是，期间包含了上抛时间变化，所以，直接对mMediaStatusChangeArrayMap增加锁不是太好，
     * 因为这样可能造成时间更新会停一下再走
     *
     * @param className 变化回调
     */
    public void unregisterMediaStatusChangerListener(String className) {
        writeLock.lock();
        try {
            mMediaStatusChangeArrayMap.remove(className);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 媒体的播放状态发送了改变
     */
    private void OnPlayStatusChange(boolean isPlaying) {
        Log.d(TAG, "OnPlayStatusChange: mMediaStatusCallbackList size = " + mMediaStatusChangeArrayMap.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                Log.d(TAG, "OnPlayStatusChange: className = " + entry.getKey());
                entry.getValue().onPlayStatusChange(isPlaying);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 媒体的状态发送改变
     *
     * @param mediaPlayType NORMAL，OPENING，ERROR，NO_VIDEO
     */
    private void OnMediaTypeChange(MediaPlayType mediaPlayType) {
        Log.d(TAG, "OnMediaTypeChange: mMediaStatusCallbackList size = " + mMediaStatusChangeArrayMap.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                Log.d(TAG, "OnMediaTypeChange: className = " + entry.getKey());
                entry.getValue().onMediaTypeChange(mediaPlayType);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 媒体的ID3信息发生改变
     */
    private void OnMediaInfoChange() {
        Log.d(TAG, "OnMediaInfoChange: mMediaStatusCallbackList size = " + mMediaStatusChangeArrayMap.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                Log.d(TAG, "OnMediaInfoChange: className = " + entry.getKey());
                if (entry.getValue() != null) {
                    entry.getValue().onMediaInfoChange();
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 媒体的播放时间发生改变
     */
    private void OnPlayTimeChange(int currentPlayTime, int duration) {
        //Log.d(TAG, "OnPlayTimeChange: mMediaStatusCallbackList size = " + mMediaStatusChangeArrayMap.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                //Log.d(TAG, "OnPlayTimeChange: className = " + entry.getKey());
                //由于切换的时候，时间是不断的回调，有可能出现空指针异常，所以这里加入判空逻辑
                if (entry.getValue() != null) {
                    entry.getValue().onPlayTimeChange(currentPlayTime, duration);
                }
            }
        } finally {
            readLock.unlock();
        }
    }


    /**
     * 图片数据发生改变触发的回调
     */
    private void onAlbumPicDataChange() {
        Log.d(TAG, "onAlbumPicDataChange: mMediaStatusCallbackList size = " + mMediaStatusChangeArrayMap.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                Log.d(TAG, "onAlbumPicDataChange: className = " + entry.getKey());
                //由于切换的时候，时间是不断的回调，有可能出现空指针异常，所以这里加入判空逻辑
                if (entry.getValue() != null) {
                    entry.getValue().onAlbumPicDataChange();
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 媒体的循环模式发生改变
     */
    private void OnLoopTypeChange() {
        Log.d(TAG, "OnLoopTypeChange: mMediaStatusCallbackList size = " + mMediaStatusChangeArrayMap.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                Log.d(TAG, "OnLoopTypeChange: className = " + entry.getKey());
                entry.getValue().onLoopTypeChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 媒体的循环模式发生改变
     */
    private void notifyPlayListChange() {
        Log.d(TAG, "notifyPlayListChange: mMediaStatusCallbackList size = " + mMediaStatusChangeArrayMap.size());
        readLock.lock();
        try {
            for (Map.Entry<String, IMediaStatusChange> entry : mMediaStatusChangeArrayMap.entrySet()) {
                Log.d(TAG, "notifyPlayListChange: className = " + entry.getKey());
                entry.getValue().onPlayListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 获取媒体暂停的原因
     *
     * @return ChangeReason
     */
    public ChangeReason getPauseReason() {
        Log.d(TAG, "getPauseReason: getPauseReason = " + pauseReason);
        return pauseReason;
    }

    /**
     * 设置暂停的原因
     *
     * @param pauseReason 暂停的原因
     */
    public void setPauseReason(ChangeReason pauseReason) {
        Log.d(TAG, "setPauseReason: pauseReason = " + pauseReason);
        this.pauseReason = pauseReason;
    }

    /**
     * 获取设置OPEN媒体时的原因
     *
     * @return ChangeReason
     */
    public ChangeReason getOpenReason() {
        Log.d(TAG, "getOpenReason: openReason = " + openReason);
        return openReason;
    }

    /**
     * 设置OPEN的原因
     *
     * @param openReason 暂停的原因
     */
    public void setOpenReason(ChangeReason openReason) {
        Log.d(TAG, "setOpenReason: openReason = " + openReason);
        MusicSetting.getInstance().putInt(SourceTypeUtils.MEDIA_TYPE, currentMediaType.ordinal());
        this.openReason = openReason;
    }
}
