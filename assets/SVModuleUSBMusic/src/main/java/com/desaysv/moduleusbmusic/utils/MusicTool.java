package com.desaysv.moduleusbmusic.utils;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.moduleusbmusic.bean.FolderItem;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        } catch (RuntimeException e) {
            Log.e(TAG, "getID3Path_ForFragment: e = " + e);
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
     * 获取文件夹map
     *
     * @param musicList Map<String, List<FolderItem>>
     * @return Map<String, List < FolderItem>>
     */
    public static Map<String, List<FolderItem>> getMusicAllMapFromFileMessages(List<FileMessage> musicList) {
        Log.i(TAG, "getMusicAllMapFromFileMessages: start musicList: " + musicList.size());
        Map<String, List<FolderItem>> map = new HashMap<>();
        for (FileMessage fileMessage : musicList) {
            //文件夹路径
            String folderPath = fileMessage.getRootPath();
            //如果还没有，则先添加列表
            if (!map.containsKey(folderPath)) {
                //采用链表实现，提高文件夹插入开头的性能
                map.put(folderPath, new LinkedList<FolderItem>());
            }
            //把信息添加到文件当中
            List<FolderItem> folderItems = map.get(folderPath);
            if (folderItems == null) {
                Log.w(TAG, "getMusicAllMapFromFileMessages: map did not containsKey 1 " + folderPath);
                continue;
            }
            //这里都是具体条目，无需填充其它
            FolderItem folderItem = new FolderItem();
            folderItem.setFileMessage(fileMessage);
            folderItem.setNotePath(folderPath);
            //原生数据库的形式为"/","音乐/"，"音乐/音乐/"
            int lastIndex = folderPath.lastIndexOf("/");
            if (lastIndex > 0) {
                String secondLast = folderPath.substring(0, lastIndex);
                int index = secondLast.lastIndexOf("/");
                if (index > 0) {
                    folderItem.setParentNotePath(secondLast.substring(0, index + 1));
                } else {
                    folderItem.setParentNotePath("/");
                }
            } else {
                if (lastIndex < 0) {
                    Log.w(TAG, "getMusicAllMapFromFileMessages: lastIndex out of range");
                    continue;
                }
                folderItem.setParentNotePath(folderPath);
            }
            folderItems.add(folderItem);
            //这里划分下文件夹
            String[] split = folderPath.split("/");
            StringBuilder builderPath = new StringBuilder();
            String parentPath = "/";
            for (String folderName : split) {
                if (!parentPath.equals("/")) {
                    parentPath = builderPath.toString();
                }
                //先把当前的装太缓存
                builderPath.append(folderName).append("/");
                if (!map.containsKey(parentPath)) {
                    map.put(parentPath, new LinkedList<>());
                }
                LinkedList<FolderItem> folders = (LinkedList<FolderItem>) map.get(parentPath);
                FolderItem folder = new FolderItem();
                folder.setParentNotePath(parentPath);
                folder.setNoteName(folderName);
                if (parentPath.equals("/")) {
                    parentPath = "";
                    folder.setNotePath(folderName + "/");
                } else {
                    folder.setNotePath(parentPath + folderName + "/");
                }
                //文件夹添加到
                // TODO: 2022-12-13 这里记得修改避免重复的文件夹
                if (folders != null) {
                    boolean hasSameItem = false;
                    //这里已经三层for，时间复杂度高
                    for (FolderItem item : folders) {
                        if (item.getNotePath().equals(folder.getNotePath())) {
                            hasSameItem = true;
                            break;
                        }
                    }
                    if (!hasSameItem) {
                        folders.addFirst(folder);
                    }
                } else {
                    Log.w(TAG, "getMusicAllMapFromFileMessages: map did not containsKey 2 " + parentPath);
                }
            }
        }
        Log.i(TAG, "getMusicAllMapFromFileMessages: end");
        return map;
    }

    /**
     * 文件拷贝功能
     * Java NIO包括transferFrom方法,根据文档应该比文件流复制的速度更快
     *
     * @param oldPath oldPath
     * @param newPath newPath
     * @return 拷贝成功与否
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean copyFile(String oldPath, String newPath, FileUtils.ProgressListener listener) {
        Log.d(TAG, "copyFile: oldPath = " + oldPath + " newPath = " + newPath);
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            File oldFile = new File(oldPath);
            if (!oldFile.exists()) {
                Log.e(TAG, "copyFile: fail oldFile no exists , oldPath: " + oldPath);
                return false;
            }
            //文件后缀
            String tempPath = PathRenameUtils.getRenameTempPath(newPath);
            if (TextUtils.isEmpty(tempPath)) {
                Log.e(TAG, "copyFile: newPath has not real path. newPath: " + newPath);
                return false;
            }
            File tempFile = new File(tempPath);
            fileOutputStream = new FileOutputStream(tempFile);
            fileInputStream = new FileInputStream(oldFile);
            FileUtils.copy(fileInputStream, fileOutputStream, new CancellationSignal(), MediaThreadPoolExecutorUtils.getInstance().getThreadPoolExecutor(), listener);
            Log.d(TAG, "copyFile: sync");
            fileInputStream.getFD().sync();
            fileOutputStream.getFD().sync();
            //拷贝完成需重新命名
            boolean renamePathState = PathRenameUtils.renamePath(tempPath, newPath);
            if (renamePathState) {
                //命名成功，通知刷新媒体库
                //MediaScanStateManager.getInstance().scan(newPath.substring(0, newPath.lastIndexOf("/")));
                Log.d(TAG, "copyFile: end");
            } else {
                Log.e(TAG, "copyFile: rename path fail.");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "copyFile: copy IOException " + e.getMessage(), e);
            return false;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "copyFile: close IOException " + e.getMessage(), e);
            }
        }
        return true;
    }
}
