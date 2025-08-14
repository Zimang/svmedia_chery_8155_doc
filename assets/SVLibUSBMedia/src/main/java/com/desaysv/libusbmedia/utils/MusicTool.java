package com.desaysv.libusbmedia.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.desaysv.mediacommonlib.base.AppBase;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by LZM on 2019-9-19
 * Comment 音乐的一些特有的工具类
 */
public class MusicTool {

    private static final String TAG = "MusicTool";

    /**
     * 解析媒体的专辑图片
     *
     * @param path 音乐的路径
     * @return byte数组，专辑数据
     */
    public static byte[] getID3Path_ForFragment(String path) {
        Log.d(TAG, "getID3Path: path = " + path);
        byte[] artwork = new byte[0];
        File file = new File(path);
        if (!file.exists()) {
            return artwork;
        }
        Uri selectedAudio = Uri.fromFile(file);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            Log.d(TAG, "getID3Path: setDataSource path = " + path);
            mmr.setDataSource(AppBase.mContext, selectedAudio); // the URI of audio file
            artwork = mmr.getEmbeddedPicture();
        } catch (RuntimeException ignored) {
            Log.e(TAG, "getID3Path_ForFragment: ignored = " + ignored);
        } finally {
            try {
                mmr.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        return artwork;
    }


    /**
     * 压缩媒体的封面数据
     *
     * @param picData
     */
    public static byte[] getCompressionPicByte(byte[] picData) {
        if (picData == null || picData.length == 0) {
            Log.d(TAG, "getCompressionPicByte: picData is no effect");
            return null;
        }
        Log.d(TAG, "getCompressionPicByte: picData = " + picData);
        //构造一个bitmap，并进行数据压缩
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(picData, 0, picData.length, options);
        if (bitmap==null){
            return new byte[0];
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) 256 / width);
        float scaleHeight = ((float) 256 / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] newPicData = baos.toByteArray();
        Log.d(TAG, "getCompressionPicByte: newPicData = " + newPicData.length);
        return newPicData;
    }

}
