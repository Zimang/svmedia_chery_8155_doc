package com.desaysv.modulebtmusic.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * @Description: 歌曲信息 music info
 * Common公共bean
 */
public class SVMusicInfo implements Parcelable, Serializable {
    /**
     * 给UI层预留的专用字段
     */
    private Object uiCustomized;
    /**
     * 歌曲名
     */
    private String mediaTitle;
    /**
     * 专辑名
     */
    private String albumName;
    /**
     * 歌手名
     */
    private String artistName;
    /**
     * 歌曲时长
     */
    private long duration;
    /**
     * 歌曲当前时间
     */
    private long progress;
    /**
     * 播放状态
     */
    private int playState;
    /**
     * A unique persistent id for the content or null.
     */
    private String mMediaId;
    /**
     * A subtitle suitable for display or null.
     */
    private String mSubtitle;
    /**
     * A description suitable for display or null.
     */
    private String mDescription;
    /**
     * A bitmap icon suitable for display or null.
     */
    private Bitmap mIcon;
    /**
     * A Uri for an icon suitable for display or null.
     */
    private Uri mIconUri;
    /**
     * Extras for opaque use by apps/system.
     */
    private Bundle mExtras;
    /**
     * A Uri to identify this content.
     */
    private Uri mMediaUri;
    /**
     * The album covert Art number of this item.
     */
    private String albumCoverArtUri;
    /**
     * The genre of this item.
     */
    private String genre;
    /**
     * The total track number (in the albume, etc.).
     */
    private long totalTrackNumber;
    /**
     * The track number of this item.
     */
    private long trackNumber;
    /**
     * The artwork for the album of the media's original source as a {@link Bitmap}.
     */
    private Bitmap albumCoverArt;

    public SVMusicInfo() {
    }

    protected SVMusicInfo(Parcel in) {
        mediaTitle = in.readString();
        albumName = in.readString();
        artistName = in.readString();
        duration = in.readLong();
        progress = in.readLong();
        playState = in.readInt();
        mMediaId = in.readString();
        mSubtitle = in.readString();
        mDescription = in.readString();
        mIconUri = Uri.parse(in.readString());
        mExtras = in.readBundle();
        mMediaUri = Uri.parse(in.readString());
        byte[] bitmapBytes = in.createByteArray();
        if (bitmapBytes != null) {
            mIcon = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        }
        albumCoverArtUri = in.readString();
        genre = in.readString();
        totalTrackNumber = in.readLong();
        trackNumber = in.readLong();
        byte[] albumCoverArtBytes = in.createByteArray();
        if (albumCoverArtBytes != null) {
            albumCoverArt = BitmapFactory.decodeByteArray(albumCoverArtBytes, 0, albumCoverArtBytes.length);
        }
    }

    public static final Creator<SVMusicInfo> CREATOR = new Creator<SVMusicInfo>() {
        @Override
        public SVMusicInfo createFromParcel(Parcel in) {
            return new SVMusicInfo(in);
        }

        @Override
        public SVMusicInfo[] newArray(int size) {
            return new SVMusicInfo[size];
        }
    };

    public Object getUiCustomized() {
        return uiCustomized;
    }

    public void setUiCustomized(Object uiCustomized) {
        this.uiCustomized = uiCustomized;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public long getDuration() {
        return duration;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public void setAlbumName(String albumNmae) {
        this.albumName = albumNmae;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
    }

    public String getMediaId() {
        return mMediaId;
    }

    public void setMediaId(String mMediaId) {
        this.mMediaId = mMediaId;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String mSubtitle) {
        this.mSubtitle = mSubtitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public void setIcon(Bitmap mIcon) {
        this.mIcon = mIcon;
    }

    public Uri getIconUri() {
        return mIconUri;
    }

    public void setIconUri(Uri mIconUri) {
        this.mIconUri = mIconUri;
    }

    public Bundle getExtras() {
        return mExtras;
    }

    public void setExtras(Bundle mExtras) {
        this.mExtras = mExtras;
    }

    public Uri getMediaUri() {
        return mMediaUri;
    }

    public void setMediaUri(Uri mMediaUri) {
        this.mMediaUri = mMediaUri;
    }

    public String getAlbumCoverArtUri() {
        return albumCoverArtUri;
    }

    public void setAlbumCoverArtUri(String albumCoverArtUri) {
        this.albumCoverArtUri = albumCoverArtUri;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getTotalTrackNumber() {
        return totalTrackNumber;
    }

    public void setTotalTrackNumber(long totalTrackNumber) {
        this.totalTrackNumber = totalTrackNumber;
    }

    public long getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(long trackNumber) {
        this.trackNumber = trackNumber;
    }

    public Bitmap getAlbumCoverArt() {
        return albumCoverArt;
    }

    public void setAlbumCoverArt(Bitmap albumCoverArt) {
        this.albumCoverArt = albumCoverArt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaTitle);
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeLong(duration);
        dest.writeLong(progress);
        dest.writeInt(playState);
        dest.writeString(mMediaId);
        dest.writeString(mSubtitle);
        dest.writeString(mDescription);
        dest.writeString(String.valueOf(mIconUri));
        dest.writeBundle(mExtras);
        dest.writeString(String.valueOf(mMediaUri));
        if (mIcon == null) {
            dest.writeByteArray(null);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mIcon.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes = baos.toByteArray();
            dest.writeByteArray(bytes);
        }
        dest.writeString(albumCoverArtUri);
        dest.writeString(genre);
        dest.writeLong(totalTrackNumber);
        dest.writeLong(trackNumber);
        if (albumCoverArt == null) {
            dest.writeByteArray(null);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            albumCoverArt.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes = baos.toByteArray();
            dest.writeByteArray(bytes);
        }
    }

    @Override
    public String toString() {
        return "mediaTitle = " + mediaTitle + ", albumName = " + albumName
                + ", artistName = " + artistName + ", duration = " + duration
                + ", progress = " + progress + ", playState = " + playState;
    }
}
