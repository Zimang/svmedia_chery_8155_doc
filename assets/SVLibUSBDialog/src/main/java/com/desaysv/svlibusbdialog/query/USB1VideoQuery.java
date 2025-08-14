package com.desaysv.svlibusbdialog.query;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.manager.QueryStateManager;
import com.desaysv.svlibusbdialog.manager.ScanStateManager;
import com.desaysv.svlibusbdialog.utils.MimeMatcher;

import java.util.Arrays;
import java.util.List;

public class USB1VideoQuery extends BaseQuery {

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
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " ) ";

    public USB1VideoQuery(Context mContext) {
        super(mContext);
    }

    @Override
    protected Uri getQueryUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String getQuerySelection() {
        // 增加过滤U盘系统文件夹扫描
        String typeSelection;
        if (isDataType) {
            typeSelection = DATA_SELECTION;
        } else {
            typeSelection = MimeMatcher.VideoMatcher.getMimeTypeVideo();
        }
        return MediaStore.Video.VideoColumns.RELATIVE_PATH + " != 'System Volume Information/' and " +
                MediaStore.Video.VideoColumns.DATA + " like ? and " + typeSelection;
    }

    /**
     * 返回条件
     * 修改后条件 [%/storage/usb0%, %.avi, %.mp4, %.m4v, %.3gp, %.mov, %.mkv, %.mpeg, %.mpg, %.vob, %.asf, %.wmv, %.rm, %.rmvb, %.flv]
     * @return
     */
    @Override
    protected String[] getArgs() {
        List<String> list;
        if(isDataType){
            String[] suffixArr = new String[]{
                    "%.avi",//avi
                    "%.mp4", //mp4
                    "%.m4v",//m4v
                    "%.3gp",//3gp
                    "%.mov",//mov
                    "%.mkv",//mkv
                    "%.mpeg",//mpeg
                    "%.mpg",//mpg
                    "%.vob",//vob
                    "%.asf", //asf
                    "%.wmv", //wmv
                    "%.rm", //rm
                    "%.rmvb",//rmvb
                    "%.flv"//flv
            };
            list = Arrays.asList(suffixArr);
        } else {
            list = MimeMatcher.VideoMatcher.getMediaVideoType();
        }

        String[] str = new String[list.size() + 1];
        str[0] = "%" + getPath() + "%";
        for (int i = 1; i <= list.size(); i++) {
            str[i] = list.get(i - 1);
        }
        return str;
    }

    @Override
    protected String getPath() {
        return Constant.PATH.PATH_USB1;
    }

    @Override
    protected int getScanState() {
        return ScanStateManager.getInstance().getUsb1State();
    }

    @Override
    protected void notifyQueryState(int state) {
        QueryStateManager.getInstance().setUsb1VideoState(state);
    }

    @SuppressLint("Range")
    @Override
    protected String getData(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
    }

    @Override
    protected boolean filter(Cursor mCursor, int scanState) {
        //查后缀不需要过滤
        if(isDataType) {
            return false;
        } else {
            @SuppressLint("Range") String mimeType = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
            return !MimeMatcher.VideoMatcher.isSupport(mimeType);// 不支持的即表示需要过滤
        }
    }
}
