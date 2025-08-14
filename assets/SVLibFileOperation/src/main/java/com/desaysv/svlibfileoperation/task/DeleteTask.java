package com.desaysv.svlibfileoperation.task;

import android.content.Context;
import android.util.Log;

import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibfileoperation.iinterface.IFileOperationListener;
import com.desaysv.svlibpicturebean.bean.FileMessage;

import java.io.File;
import java.util.List;

public class DeleteTask extends BaseOperationTask{

    public DeleteTask(Context mContext, IFileOperationListener listener, List<FileMessage> fileMessageList) {
        super(mContext, listener, fileMessageList);
    }

    @Override
    public int operation() {
        for (int i = 0; i < list.size();i++){
            if (deleteFile(list.get(i).getPath()) == FileOperationManager.OPERATION_STATE_SUCCESS){
                publishProgress(i+1);
            }else {
                return FileOperationManager.OPERATION_STATE_ERROR_IO;
            }
            if (isCancelled()){
                Log.d(TAG,"cancel");
                return FileOperationManager.OPERATION_STATE_CANCEL;
            }
        }
        return FileOperationManager.OPERATION_STATE_SUCCESS;
    }

    @Override
    public void notifyOperation(int state, int current, int total) {
        if (listener != null){
            listener.onDeleteState(state,current,total);
        }
    }

    /**
     * 删除对应路径的文件
     * @param path
     * @return 返回删除结果
     */
    private int deleteFile(String path){
        Log.d(TAG,"deleteFile:"+path);
        File file = new File(path);
        if (file.exists()){
            if (file.delete()){
                return FileOperationManager.OPERATION_STATE_SUCCESS;
            }else {
                return FileOperationManager.OPERATION_STATE_ERROR_IO;
            }
        }else {
            return FileOperationManager.OPERATION_STATE_SUCCESS;//文件不存在默认返回true
        }
    }
}
