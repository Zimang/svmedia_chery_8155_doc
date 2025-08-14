package com.desaysv.svlibfileoperation.task;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibfileoperation.iinterface.IFileOperationListener;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportTask extends BaseOperationTask{

    public ExportTask(Context mContext, IFileOperationListener listener, List<FileMessage> fileMessageList) {
        super(mContext, listener, fileMessageList);
    }

    @Override
    public int operation() {
        if (isEnoughSpace(list)){//预先估算空间是否足够
            String[] paths = new String[list.size()];
            String[] mimetypes = new String[list.size()];
            for (int i = 0; i < list.size();i++){
                if (copyFile(list.get(i).getPath(),list.get(i).getFileName()) == FileOperationManager.OPERATION_STATE_SUCCESS){
                    //拷贝成功，通知界面刷新
                    paths[i] = FileOperationManager.EXPORT_PATH + File.separator + list.get(i).getFileName();
                    mimetypes[i] = "image/*";
                    Log.d(TAG,"paths[i]:"+paths[i]);
                    Log.d(TAG,"mimetypes[i]:"+mimetypes[i]);
                    publishProgress(i+1);
                }else {
                    return FileOperationManager.OPERATION_STATE_ERROR_IO;
                }
                if (isCancelled()){
                    Log.d(TAG,"cancel");
                    return FileOperationManager.OPERATION_STATE_CANCEL;
                }
            }
            final int[] count = {0};
            MediaScannerConnection.scanFile(mContext, paths, mimetypes, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.d(TAG,"onScanCompleted,uri:"+uri+", path:"+path);
                    count[0]++;
                }
            });
            while (count[0] > (list.size() -1)){//等待扫描完成再执行下一步，从实际来看，单个文件扫描完毕就会收到回调了。所以应该要计数处理
                Log.d(TAG,"wait ScanCompleted");
            }
            return FileOperationManager.OPERATION_STATE_SUCCESS;
        }else {
            if (DeviceStatusBean.getInstance().isUSB1Connect()){
                return FileOperationManager.OPERATION_STATE_ERROR_SPACE;
            }else {
                return FileOperationManager.OPERATION_STATE_ERROR_IO;
            }

        }
    }

    @Override
    public void notifyOperation(int state, int current, int total) {
        if (listener != null){
            listener.onExportState(state,current,total);
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
        File file = new File(FileOperationManager.EXPORT_PATH);
        if (!file.exists()){
            file.mkdirs();
        }
        Log.d(TAG,"isEnoughSpace,total:"+total+",free:"+file.getFreeSpace());
        return total < file.getFreeSpace();
    }

    /**
     * 拷贝文件
     * @param path
     * @param sourceName
     * @return
     */
    private int copyFile(String path, String sourceName){
        if (path != null){
            deleteFileIfNeed(sourceName);
            return copyFile(new File(path), new File(FileOperationManager.EXPORT_PATH + File.separator + sourceName));
        }
        return FileOperationManager.OPERATION_STATE_ERROR;
    }

    /**
     * 判断当前拷贝的文件是否已经存在，存在的话则删除
     * @param sourceName 需要拷贝的文件名
     * @return 返回是否文件存在
     */
    private void deleteFileIfNeed(String sourceName){
        File file = new File(FileOperationManager.EXPORT_PATH+ File.separator + sourceName);
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
                return FileOperationManager.OPERATION_STATE_SUCCESS;
            }catch (IOException e){
                Log.d(TAG,e.toString());
                return FileOperationManager.OPERATION_STATE_ERROR_IO;
            }
        }
        return FileOperationManager.OPERATION_STATE_ERROR;
    }
}
