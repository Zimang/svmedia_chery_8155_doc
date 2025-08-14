package com.desaysv.svlibmediastore.query;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.media.MediaFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author uidq1846
 * @desc 音乐数据查询:默认获取全部列表、符合限制条件的数据
 * @time 2022-11-15 9:49
 */
public class MusicQuery extends BaseQuery {
    private static IQuery query;
    //音乐状态默认需要显示的列
    @SuppressLint("InlinedApi")
    public static final String[] AUDIO_PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.VOLUME_NAME,
            MediaStore.Audio.Media.MIME_TYPE
    };
    //默认需要判断的选项
    public static final String MIME_TYPE_SELECTION = "( " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " or " + MediaStore.Audio.Media.MIME_TYPE + " = ?"
            + " ) ";

    public static final String DATA_SELECTION = "( "
            + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " ) ";

    private MusicQuery() {
    }

    public static IQuery getInstance() {
        if (query == null) {
            synchronized (MusicQuery.class) {
                if (query == null) {
                    query = new MusicQuery();
                }
            }
        }
        return query;
    }

    @SuppressLint("InlinedApi")
    @Override
    protected String[] getProjection() {
        return AUDIO_PROJECTION;
    }

    @Override
    protected String getSelection() {
        //⽀持⾳频⽂件格式 MP3,WMA,OGG,APE,FLAC,M4A,WAV,AMR,MP2,HE-AAC,ACC
        //select * from audio where (mime_type = "audio/flac" or mime_type = "audio/mpeg") and _data like "/storage/usb0%"
        String selection =   "and " + MediaStore.Audio.Media.DATA + " like ?";
        if (isDataType) {
            selection = DATA_SELECTION + selection;
        } else {
            selection = MIME_TYPE_SELECTION + selection;
        }
        Log.d(TAG, "getSelection: selection = " + selection);
        return selection;
    }

    @Override
    protected String[] getSelectionArgs(String storagePath) {
        //⽀持⾳频⽂件格式 MP3,WMA,OGG,APE,FLAC,M4A,WAV,AMR,MP2,HE-AAC,ACC
        Log.d(TAG, "getSelectionArgs: storagePath = " + storagePath+" isDataType = "+isDataType);
        if (isDataType) {
            return new String[]{
                    "%.mp3",
                    "%.wma",
                    "%.aac",
                    "%.ogg",
                    "%.ape",
                    "%.flac",
                    "%.m4a",
                    "%.wav",
                    "%.amr",
                    "%.mp2",
                    storagePath + "%"
            };

        }else {
            return new String[]{
                    //MP3
                    MediaFormat.MIMETYPE_AUDIO_MPEG,
                    //WMA
                    "audio/x-ms-wma",
                    //OGG
                    "audio/ogg",
                    "audio/vorbis",
                    //APE
                    "audio/x-ape",
                    //FLAC
                    MediaFormat.MIMETYPE_AUDIO_FLAC,
                    //M4A 这个存疑？这个是视频封装格式，写在音频当中
                    //WAV 这个存疑？这个是封装格式
                    "audio/x-wav",
                    //AMR
                    MediaFormat.MIMETYPE_AUDIO_AMR_NB,
                    MediaFormat.MIMETYPE_AUDIO_AMR_WB,
                    "audio/amr",
                    "audio/ffmpeg",
                    //MP2
                    "audio/mpeg-L2",
                    //HE-AAC
                    //ACC 统一都是
                    MediaFormat.MIMETYPE_AUDIO_AAC,
                    storagePath + "%"
            };
        }
    }

    @Override
    protected String getOrder() {
        return MediaStore.Audio.Media.TITLE + " ASC";
    }

    @SuppressLint("InlinedApi")
    @Override
    protected List<FileMessage> query(Cursor cursor) {
        //Cache column indices
        //查看音乐需要哪些属性项
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
        int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
        int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        int modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
        int relativeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH);
        int volumeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.VOLUME_NAME);
        int mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);
        List<FileMessage> fileMessages = new ArrayList<>();
        while (cursor.moveToNext()) {
            // Get values of columns for a given audio
            FileMessage fileMessage = new FileMessage();
            fileMessage.setId(cursor.getInt(idColumn));
            fileMessage.setAlbumId(cursor.getLong(albumIdColumn));
            fileMessage.setAlbum(cursor.getString(albumColumn));
            fileMessage.setAuthor(cursor.getString(artistColumn));
            fileMessage.setLastModified(cursor.getLong(modifiedColumn));
            fileMessage.setPath(cursor.getString(dataColumn));
            fileMessage.setDuration(cursor.getInt(durationColumn));
            fileMessage.setName(cursor.getString(titleColumn));
            //这里添加下文件名逻辑,判null,增加健壮性
            if (fileMessage.getPath() != null) {
                int startIndex = fileMessage.getPath().lastIndexOf("/");
                int endIndex = fileMessage.getPath().lastIndexOf(".");
                fileMessage.setFileName(fileMessage.getPath().substring((startIndex + 1), endIndex));
            }
            fileMessage.setSize(cursor.getLong(sizeColumn));
            fileMessage.setRootPath(cursor.getString(relativeColumn));
            fileMessage.setDeviceUUID(cursor.getString(volumeColumn));
            fileMessage.setMimeType(cursor.getString(mimeColumn));
            fileMessages.add(fileMessage);
        }
        return fileMessages;
    }

    @Override
    protected Uri getMediaUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }
}
