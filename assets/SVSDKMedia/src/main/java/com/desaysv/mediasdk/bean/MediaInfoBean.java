package com.desaysv.mediasdk.bean;

import com.desaysv.localmediasdk.sdk.bean.Constant;

/**
 * Created by LZM on 2020-3-23
 * Comment 媒体json数据的构造类,提供了builder的构造方法
 */
public class MediaInfoBean {
    private static final String TAG = "MediaInfoBean";

    /**
     * ============================================================================================================
     *                                                    媒体音乐部分
     * ============================================================================================================
     */

    /**
     * 私有构造，不让直接创建对象
     */
    private MediaInfoBean() {
    }

    /**
     * 对应的音源
     */
    private String source;

    /**
     * 歌曲名称
     */
    private String mTitle;

    /**
     * 专辑名称
     */
    private String mAlbum;

    /**
     * 专辑图ID，用于获取专辑图
     */
    private long albumId;

    /**
     * 歌手
     */
    private String mArtist;

    /**
     * 文件的绝对路径
     */
    private String mPath;

    /**
     * 曲目播放状态
     */
    private Boolean mPlayStatus;

    /**
     * 曲目的总时长
     */
    private Integer mDuration;

    /**
     * 曲目当前播放时长
     */
    private Integer mCurrentPlayTime;

    /**
     * 曲目收藏状态
     */
    private Boolean mCollectStatus;

    /**
     * 曲目循环状态
     */
    private String mLoopType;

    /**
     * USB设备连接状态
     * {@link Constant.DeviceState}
     */
    private String usb1ConnectState;

    /**
     * 设置进度
     */
    private int seekToTime;

    /**
     * ============================================================================================================
     *                                                    电台部分
     * ============================================================================================================
     */
    /**
     * 用来给 FM/AM/DAB 设置对应值
     * 频点值
     */
    private String mFrequency;

    /**
     * dab的服务id
     */
    private String mServiceId;

    /**
     * dab的组件id
     */
    private String mComponentId;

    /**
     * Radio的band信息
     */
    private Integer mBand;

    /**
     * 搜索状态
     */
    private Boolean mSearchStatus;

    /**
     * ============================================================================================================
     *                                                    启动页面部分
     * ============================================================================================================
     */

    /**
     * 是否启动前台
     */
    private boolean isForeground;

    /**
     * 启动携带的标签
     * {@link Constant.OpenSourceViewType}
     */
    private int flag;

    public void setSource(String source) {
        this.source = source;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public void setCollectStatus(boolean mCollectStatus) {
        this.mCollectStatus = mCollectStatus;
    }

    public void setLoopType(String mLoopType) {
        this.mLoopType = mLoopType;
    }

    public void setPlayStatus(boolean mPlayStatus) {
        this.mPlayStatus = mPlayStatus;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void setCurrentPlayTime(int mCurrentPalyTime) {
        this.mCurrentPlayTime = mCurrentPalyTime;
    }

    public void setSearchStatus(Boolean mSearchStatus) {
        this.mSearchStatus = mSearchStatus;
    }

    public Boolean getSearchStatus() {
        return mSearchStatus;
    }

    public String getFreq() {
        return mFrequency;
    }

    public void setFreq(String freq) {
        this.mFrequency = freq;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public void setServiceId(String serviceId) {
        this.mServiceId = serviceId;
    }

    public String getComponentId() {
        return mComponentId;
    }

    public void setComponentId(String componentId) {
        this.mComponentId = componentId;
    }

    public void setBand(Integer mBand) {
        this.mBand = mBand;
    }

    public String getSource() {
        return source;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getPath() {
        return mPath;
    }

    public Boolean getPlayStatus() {
        return mPlayStatus;
    }

    public Integer getDuration() {
        return mDuration;
    }

    public Integer getCurrentPlayTime() {
        return mCurrentPlayTime;
    }

    public Boolean getCollectStatus() {
        return mCollectStatus;
    }

    public String getLoopType() {
        return mLoopType;
    }

    public Integer getBand() {
        return mBand;
    }

    public String getUsb1ConnectState() {
        return usb1ConnectState;
    }

    public void setUsb1ConnectState(String usb1ConnectState) {
        this.usb1ConnectState = usb1ConnectState;
    }

    public int getSeekToTime() {
        return seekToTime;
    }

    public void setSeekToTime(int seekToTime) {
        this.seekToTime = seekToTime;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean foreground) {
        this.isForeground = foreground;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "MediaInfoBean{" +
                "source='" + source + '\'' +
                ", mTitle = '" + mTitle + '\'' +
                ", mAlbum = '" + mAlbum + '\'' +
                ", albumId = '" + albumId + '\'' +
                ", mArtist = '" + mArtist + '\'' +
                ", mPath = '" + mPath + '\'' +
                ", mPlayStatus = " + mPlayStatus +
                ", mDuration = " + mDuration +
                ", mCurrentPlayTime = " + mCurrentPlayTime +
                ", mCollectStatus = " + mCollectStatus +
                ", mLoopType = " + mLoopType +
                ", mFrequency = " + mFrequency +
                ", mServiceId = " + mServiceId +
                ", mComponentId = " + mComponentId +
                ", mBand = " + mBand +
                ", mSearchStatus = " + mSearchStatus +
                ", flag = " + flag +
                ", isForeground = " + isForeground +
                ", seekToTime = " + seekToTime +
                ", usb1ConnectState = " + usb1ConnectState +
                '}';
    }

    public static class Builder {

        private String source;

        private String mTitle;

        private String mAlbum;

        private String mArtist;

        private long albumId;

        private String mPath;

        private Boolean mPlayStatus;

        private Integer mDuration;

        private Integer mCurrentPlayTime;

        private Boolean mCollectStatus;

        private String mLoopType;

        //用来给 FM/AM/DAB 设置对应值
        private String mFrequency;//频点值

        private String mServiceId;//dab的服务id

        private String mComponentId;//dab的组件id

        private Integer mBand;

        private Boolean mSearchStatus;

        /**
         * USB设备连接状态
         * {@link Constant.DeviceState}
         */
        private String usb1ConnectState;

        /**
         * 设置进度
         */
        private int seekToTime;

        /**
         * 是否启动前台
         */
        private boolean isForeground;

        /**
         * 启动携带的标签
         * {@link Constant.OpenSourceViewType}
         */
        private int flag;

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setAlbum(String album) {
            this.mAlbum = album;
            return this;
        }

        public Builder setAlbumId(long albumId) {
            this.albumId = albumId;
            return this;
        }

        public Builder setArtist(String artits) {
            this.mArtist = artits;
            return this;
        }

        public Builder setPath(String path) {
            this.mPath = path;
            return this;
        }

        public Builder setPlayStatus(boolean playStatus) {
            this.mPlayStatus = playStatus;
            return this;
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;
            return this;
        }

        public Builder setCurrentPlayTime(int currentPlayTime) {
            this.mCurrentPlayTime = currentPlayTime;
            return this;
        }


        public Builder setCollectStatus(Boolean mCollectStatus) {
            this.mCollectStatus = mCollectStatus;
            return this;
        }

        public Builder setLoopType(String mLoopType) {
            this.mLoopType = mLoopType;
            return this;
        }

        public Builder setFrequency(String mFrequency) {
            this.mFrequency = mFrequency;
            return this;
        }

        public Builder setServiceId(String mServiceId) {
            this.mServiceId = mServiceId;
            return this;
        }

        public Builder setComponentId(String mComponentId) {
            this.mComponentId = mComponentId;
            return this;
        }

        public Builder setBand(Integer mBand) {
            this.mBand = mBand;
            return this;
        }

        public Builder setSearchStatus(Boolean mSearchStatus) {
            this.mSearchStatus = mSearchStatus;
            return this;
        }

        public Builder setUsb1ConnectState(String usb1ConnectState) {
            this.usb1ConnectState = usb1ConnectState;
            return this;
        }

        public Builder setSeekToTime(int seekToTime) {
            this.seekToTime = seekToTime;
            return this;
        }

        public Builder setForeground(boolean foreground) {
            isForeground = foreground;
            return this;
        }

        public Builder setFlag(int flag) {
            this.flag = flag;
            return this;
        }

        /**
         * 构建 MediaInfoBean对象
         *
         * @return MediaInfoBean
         */
        public MediaInfoBean created() {
            MediaInfoBean mediaInfoBean = new MediaInfoBean();
            if (source != null) {
                mediaInfoBean.setSource(source);
            }
            if (mTitle != null) {
                mediaInfoBean.setTitle(mTitle);
            }
            if (mAlbum != null) {
                mediaInfoBean.setAlbum(mAlbum);
            }
            mediaInfoBean.setAlbumId(this.albumId);
            if (mArtist != null) {
                mediaInfoBean.setArtist(mArtist);
            }
            if (mPath != null) {
                mediaInfoBean.setPath(mPath);
            }
            if (mPlayStatus != null) {
                mediaInfoBean.setPlayStatus(mPlayStatus);
            }
            if (mDuration != null) {
                mediaInfoBean.setDuration(mDuration);
            }
            if (mCurrentPlayTime != null) {
                mediaInfoBean.setCurrentPlayTime(mCurrentPlayTime);
            }
            if (mCollectStatus != null) {
                mediaInfoBean.setCollectStatus(mCollectStatus);
            }
            if (mLoopType != null) {
                mediaInfoBean.setLoopType(mLoopType);
            }
            if (mFrequency != null) {
                mediaInfoBean.setFreq(mFrequency);
            }
            if (mServiceId != null) {
                mediaInfoBean.setServiceId(mServiceId);
            }
            if (mComponentId != null) {
                mediaInfoBean.setComponentId(mComponentId);
            }
            if (mBand != null) {
                mediaInfoBean.setBand(mBand);
            }
            if (mSearchStatus != null) {
                mediaInfoBean.setSearchStatus(mSearchStatus);
            }
            if (usb1ConnectState != null) {
                mediaInfoBean.setUsb1ConnectState(usb1ConnectState);
            }
            mediaInfoBean.setSeekToTime(seekToTime);
            mediaInfoBean.setForeground(isForeground);
            mediaInfoBean.setFlag(flag);
            return mediaInfoBean;
        }
    }
}
