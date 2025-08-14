package com.desaysv.svlibfileoperation.task;

import android.content.Context;
import android.os.AsyncTask;

import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibfileoperation.iinterface.IFileOperationListener;
import com.desaysv.svlibpicturebean.bean.FileMessage;

import java.util.List;


public abstract class BaseOperationTask extends AsyncTask<Object, Integer, Integer> {

    public final String TAG = this.getClass().getSimpleName();
    public Context mContext;
    public IFileOperationListener listener;
    public List<FileMessage> list;

    public BaseOperationTask(Context mContext, IFileOperationListener listener, List<FileMessage> fileMessageList) {
        this.mContext = mContext;
        this.listener = listener;
        this.list = fileMessageList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null){
            notifyOperation(FileOperationManager.OPERATION_STATE_PROGRESS,0,list.size());
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (listener != null){
            notifyOperation(integer,list.size(),list.size());
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (listener != null){
            notifyOperation(FileOperationManager.OPERATION_STATE_PROGRESS,values[0],list.size());
        }
    }

    @Override
    protected void onCancelled(Integer integer) {
        super.onCancelled(integer);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Integer doInBackground(Object... objects) {

        return operation();
    }

    /**
     * 文件操作 ，导出或者删除
     */
    public abstract int operation();

    /**
     * 文件操作的状态
     */
    public abstract void notifyOperation(int state,int current, int total);
}
