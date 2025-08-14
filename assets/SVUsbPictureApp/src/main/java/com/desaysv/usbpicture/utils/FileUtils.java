package com.desaysv.usbpicture.utils;

import android.util.Log;

import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by ZNB on 2022-08-08
 * 文件操作的工具类
 */
public class FileUtils {
    private static final String TAG ="FileUtils";
    public static final int OPERATION_STATE_ERROR = -1;//默认错误，例如路径为空等
    public static final int OPERATION_STATE_SUCCESS = 0;//导出/删除成功
    public static final int OPERATION_STATE_PROGRESS = 1;//正在导出/删除
    public static final int OPERATION_STATE_ERROR_IO = 2;//导出/删除失败，导出过程中U盘被拔出
    public static final int OPERATION_STATE_ERROR_SPACE = 3;//导出/删除失败，目标路径空间不足
    public static final int OPERATION_STATE_ERROR_EXIST = 4;//导出/删除失败，目标文件已经存在
    public static final String EXPORT_PATH = "/storage/usb0/CVBOX";//导出到U盘的路径
    private Map<String,IFileOperationListener> listenerMap = new HashMap<>();

    private static FileUtils mInstance;

    public static FileUtils getInstance(){
        if (mInstance == null){
            synchronized (FileUtils.class){
                if (mInstance == null){
                    mInstance = new FileUtils();
                }
            }
        }
        return mInstance;
    }
    
    
    public int deleteFile(List<FileMessage> pathList){
        for (int i = 0; i < pathList.size();i++){
            if (deleteFile(pathList.get(i).getPath()) == OPERATION_STATE_SUCCESS){
                notifyDeleteState(OPERATION_STATE_SUCCESS, i + 1, pathList.size());
            }else {
                notifyDeleteState(OPERATION_STATE_ERROR_IO, i + 1, pathList.size());
                return OPERATION_STATE_ERROR_IO;
            }
        }
        notifyDeleteState(OPERATION_STATE_SUCCESS, pathList.size(), pathList.size());
        return OPERATION_STATE_SUCCESS;
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
                return OPERATION_STATE_SUCCESS;
            }else {
                return OPERATION_STATE_ERROR_IO;
            }
        }else {
            return OPERATION_STATE_SUCCESS;//文件不存在默认返回true
        }
    }

    /**
     * 拷贝文件
     * @param path
     * @return
     */
    private int copyFile(String path){
        if (path != null){
            String sourceName = path.substring(USBConstants.USBPath.USB0_PATH.length());
            deleteFileIfNeed(sourceName);
            return copyFile(new File(path), new File(EXPORT_PATH + File.separator + sourceName));
        }
        return OPERATION_STATE_ERROR;
    }


    /**
     * 批量导出
     * @param pathList
     * @return
     */
    public int exportFileList(List<FileMessage> pathList){
        if (isEnoughSpace(pathList)){//预先估算空间是否足够
            for (int i = 0; i < pathList.size();i++){
                if (copyFile(pathList.get(i).getPath()) == OPERATION_STATE_SUCCESS){
                    //拷贝成功，通知界面刷新
                    notifyExportState(OPERATION_STATE_SUCCESS,i +1, pathList.size());
                }else {
                    notifyExportState(OPERATION_STATE_ERROR_IO,i +1, pathList.size());
                    return OPERATION_STATE_ERROR_IO;
                }
            }
            notifyExportState(OPERATION_STATE_SUCCESS,pathList.size(), pathList.size());
            return OPERATION_STATE_SUCCESS;
        }else {
            notifyExportState(OPERATION_STATE_ERROR_SPACE,0, pathList.size());
            return OPERATION_STATE_ERROR_SPACE;
        }
    }


    /**
     * 这里实际是计算LocalPicture这个路径是否已经存放超过最大允许的容量
     * @return 判断空间是否充足
     */
    private boolean isEnoughSpace(List<FileMessage> pathList){
        long total = 0;
        for (FileMessage path : pathList){
            File file = new File(path.getPath());
            total+=file.length();
        }
        File file = new File(EXPORT_PATH);
        Log.d(TAG,"isEnoughSpace,total:"+total+",free:"+file.getFreeSpace());
        return total < file.getFreeSpace();
    }



    /**
     * 判断当前拷贝的文件是否已经存在，存在的话则删除
     * @param sourceName 需要拷贝的文件名
     * @return 返回是否文件存在
     */
    private void deleteFileIfNeed(String sourceName){
        File file = new File(EXPORT_PATH+ File.separator + sourceName);
        if (file.exists()){//如果存在则直接返回true
            file.delete();
            Log.d(TAG,"deleteFileIfNeed,delete first:"+sourceName);
        }else {
            try {
                if (!file.getParentFile().exists()) {//如果不存在，则先判断父目录是否已经存在
                    file.getParentFile().mkdirs();//不存在则先创建父目录
                }
                file.createNewFile();//创建文件

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 拷贝文件
     * @param oldFile 源文件
     * @param newFile 目标文件
     */
    private int copyFile(File oldFile, File newFile) {
        if (oldFile != null && newFile != null) {
            Log.d(TAG, "copyFile, oldFile: "+oldFile.getPath() + ", newFile: "+newFile.getPath());
            try {

                //新建文件输入流并对它进行缓冲
                FileInputStream inputStream = new FileInputStream(oldFile);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                //新建文件输出流并对它进行缓冲
                FileOutputStream outputStream = new FileOutputStream(newFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                //缓冲数组
                byte[] bytes = new byte[1024];

                //循环写入
                while (bufferedInputStream.read(bytes) != -1) {
                    bufferedOutputStream.write(bytes);
                }
                //刷入缓冲
                bufferedOutputStream.flush();

                //关闭输入输出流
                bufferedInputStream.close();
                bufferedOutputStream.close();
                inputStream.close();
                outputStream.close();
                return OPERATION_STATE_SUCCESS;
            }catch (IOException e){
                Log.d(TAG,e.toString());
                return OPERATION_STATE_ERROR_IO;
            }
        }
        return OPERATION_STATE_ERROR;
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
     * 拷贝文件进度的监听器
     */
    public interface IFileOperationListener{
        /**
         * 导出状态的回调
         * @param state 导出处理的状态
         * @param current 正在导出第几个
         * @param total 一共需要导出多少个
         */
        void onExportState(int state,int current, int total);

        /**
         * 删除状态的回调
         * @param state 删除处理的状态
         * @param current 正在删除第几个
         * @param total 一共需要删除多少个
         */
        void onDeleteState(int state,int current, int total);
    }

}
