package com.desaysv.querypicture.query;

import android.content.Context;
import android.util.Log;

import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.manager.AlbumListManager;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CVBOX的相册数据查询
 * 目前分工是由雄狮实现“相册”，所以这部分不做实现
 * 只是保留框架代码，留待以后有需要时使用
 */
public class AlbumQuery{
    private static final String TAG = "AlbumQuery";
    private Context mContext;

    public AlbumQuery(Context context) {
        mContext = context;
    }

    /**
     * 执行查询和更新List操作
     */
    public void doInBackGround(){
        AlbumListManager.getInstance().updateAlbumList(test());
    }

    private CopyOnWriteArrayList<FileMessage> test(){

        CopyOnWriteArrayList<FileMessage> testList = new CopyOnWriteArrayList<>();
        File file = new File("/storage/emulated/0/LocalPicture");

        if (file.isDirectory()){
            for (File file1 : file.listFiles()){
                FileMessage fileMessage = new FileMessage();
                fileMessage.setPath(file1.getAbsolutePath());
                fileMessage.setName(file1.getName());
                fileMessage.setFileName(file1.getName());
                Log.d(TAG,"name:"+file1.getName());
                testList.add(fileMessage);
            }
        }

        return testList;
    }

}
