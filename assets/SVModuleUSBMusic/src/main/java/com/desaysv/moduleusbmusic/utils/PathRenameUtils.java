package com.desaysv.moduleusbmusic.utils;

import android.util.Log;

import java.io.File;

/**
 * @author uidq1846
 * @desc 文件名称重新命名的工具
 * @time 2022-8-2 15:28
 */
public class PathRenameUtils {
    private static final String TAG = PathRenameUtils.class.getSimpleName();

    /**
     * @param path mediaFile 文件路径
     * @return String
     */
    public static String getRenameTempPath(String path) {
        int index = path.lastIndexOf(".");
        String tempString;
        //StringIndexOutOfBoundsException: String index out of range: -1
        if (index == -1) {
            tempString = "";
        } else {
            tempString = path.substring(0, index) + ".svtemp";
        }
        Log.d(TAG, "getRenameNoMediaFile: mediaFilePath = " + path + " tempString = " + tempString);
        return tempString;
    }

    /**
     * 重命名
     *
     * @param oldPath oldPath
     * @param newPath newPath
     * @return 是否成功改名
     */
    public static boolean renamePath(String oldPath, String newPath) {
        File file = new File(oldPath);
        boolean rename = false;
        if (file.exists()) {
            rename = file.renameTo(new File(newPath));
            Log.d(TAG, "renamePath: oldPath = " + oldPath + " newPath = " + newPath + " rename = " + rename);
        } else {
            Log.w(TAG, "renamePath: oldPath is no exists. oldPath = " + oldPath);
        }
        return rename;
    }
}
