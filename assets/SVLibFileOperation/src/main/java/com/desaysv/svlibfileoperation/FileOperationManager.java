package com.desaysv.svlibfileoperation;

import android.content.Context;
import android.util.Log;

import com.desaysv.svlibfileoperation.dialog.DialogUtils;
import com.desaysv.svlibfileoperation.iinterface.IFileOperationListener;
import com.desaysv.svlibfileoperation.task.DeleteTask;
import com.desaysv.svlibfileoperation.task.ExportTask;
import com.desaysv.svlibpicturebean.bean.FileMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by ZNB on 2022-08-08
 * 把文件操作抽取到Lib形成公版
 */
public class FileOperationManager implements IFileOperationListener{

    private static final String TAG = "FileOperationManager";
    public static final int OPERATION_STATE_ERROR = -1;//默认错误，例如路径为空等
    public static final int OPERATION_STATE_SUCCESS = 0;//导出/删除成功
    public static final int OPERATION_STATE_PROGRESS = 1;//正在导出/删除
    public static final int OPERATION_STATE_ERROR_IO = 2;//导出/删除失败，导出过程中U盘被拔出
    public static final int OPERATION_STATE_ERROR_SPACE = 3;//导出/删除失败，目标路径空间不足
    public static final int OPERATION_STATE_ERROR_EXIST = 4;//导出/删除失败，目标文件已经存在
    public static final int OPERATION_STATE_CANCEL = 5;//取消操作
    public static final String EXPORT_PATH = "/storage/usb0/CVBOX";//导出到U盘的路径
    private Map<String,IFileOperationListener> listenerMap = new HashMap<>();
    private ExportTask exportTask;
    private DeleteTask deleteTask;
    private Context mContext;

    private static FileOperationManager mInstance;

    public static FileOperationManager getInstance(){
        if (mInstance == null){
            synchronized (FileOperationManager.class){
                if (mInstance == null){
                    mInstance = new FileOperationManager();
                }
            }
        }
        return mInstance;
    }


    public void init(Context context){
        this.mContext = context;
        DialogUtils.getInstance().init(context);
    }

    /**
     * 注册文件操作进度的观察者
     * @param listener 监听者
     * @param className 监听者类名
     */
    public void registerOperationListener(IFileOperationListener listener, String className){
        if (!listenerMap.containsKey(className)){
            listenerMap.put(className,listener);
        }
    }

    /**
     * 注销文件操作进度的观察者
     * @param className 类名
     */
    public void unregisterOperationListener(String className){
        listenerMap.remove(className);
    }


    /**
     * 通知观察者导出操作有更新
     * @param state
     * @param current
     * @param total
     */
    private void notifyExportState(int state,int current, int total){
        Log.d(TAG,"notifyExportState:"+state);
        for (Map.Entry<String, IFileOperationListener> listener : listenerMap.entrySet()){
            listener.getValue().onExportState(state,current,total);
        }
    }

    /**
     * 通知观察者删除操作有更新
     * @param state
     * @param current
     * @param total
     */
    private void notifyDeleteState(int state,int current, int total){
        Log.d(TAG,"notifyExportState:"+state);
        for (Map.Entry<String, IFileOperationListener> listener : listenerMap.entrySet()){
            listener.getValue().onDeleteState(state,current,total);
        }
    }


    /**
     * 启动文件导出操作，导出目录是固定目录，后续再扩展传参目录
     * @param fileMessageList
     */
    public void startExportFiles(Context context, List<FileMessage> fileMessageList){
            exportTask = new ExportTask(context,this,fileMessageList);
            exportTask.execute();
    }

    /**
     * 取消导出操作
     */
    public void cancelExport(){
        exportTask.cancel(true);
    }

    /**
     * 启动文件删除操作，导出目录是固定目录，后续再扩展传参目录
     * @param fileMessageList
     */
    public void startDeleteFiles(Context context,List<FileMessage> fileMessageList){
        deleteTask = new DeleteTask(context,this,fileMessageList);
        deleteTask.execute();
    }

    /**
     * 取消删除操作
     */
    public void cancelDelete(){
        deleteTask.cancel(true);
    }




    @Override
    public void onExportState(int state, int current, int total) {
        Log.d(TAG,"onExportState, state:"+state+",current:"+current+",total:"+total);
        notifyExportState(state,current,total);
        DialogUtils.getInstance().updateExportDialog(state,current,total);
    }

    @Override
    public void onDeleteState(int state, int current, int total) {
        Log.d(TAG,"onDeleteState, state:"+state+",current:"+current+",total:"+total);
        notifyDeleteState(state,current,total);
        DialogUtils.getInstance().updateDeleteDialog(state,current,total);
    }
}
