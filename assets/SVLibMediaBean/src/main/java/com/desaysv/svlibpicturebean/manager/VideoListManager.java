package com.desaysv.svlibpicturebean.manager;
/**
 * Created by ZNB on 2022-01-12
 * 存储查询到的视频和文件夹列表
 */

import android.util.Log;

import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibpicturebean.utils.PinyinComparator;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB1VideoDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2VideoDataSubject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class VideoListManager {
    private static final String TAG = "VideoListManager";
    private static VideoListManager mInstance;
    private List<FileMessage> mAllVideoList = new ArrayList<>();
    private List<FileMessage> mCurrentUSB1VideoList = new ArrayList<>();
    private List<FileMessage> mCurrentUSB2VideoList = new ArrayList<>();
    private List<FileMessage> mAllUSB1VideoList = new ArrayList<>();
    private List<FileMessage> mAllUSB2VideoList = new ArrayList<>();
    private List<FolderMessage> mUSB1RootFolderList = new ArrayList<>();
    private List<FolderMessage> mUSB2RootFolderList = new ArrayList<>();
    private List<FileMessage> mUSB1RootVideoList = new ArrayList<>();
    private List<FileMessage> mUSB2RootVideoList = new ArrayList<>();
    private List<FolderMessage> mCurrentUSB1FolderList = new ArrayList<>();
    private List<FolderMessage> mCurrentUSB2FolderList = new ArrayList<>();

    private HashMap<String, List<FileMessage>> cacheUSB1VideoList = new HashMap<String, List<FileMessage>>();
    private HashMap<String, List<FolderMessage>> cacheUSB1FolderList = new HashMap<String, List<FolderMessage>>();
    private HashMap<String, List<FileMessage>> cacheUSB2VideoList = new HashMap<String, List<FileMessage>>();
    private HashMap<String, List<FolderMessage>> cacheUSB2FolderList = new HashMap<String, List<FolderMessage>>();

    private HashMap<String, FolderMessage> cacheUSB1Folder = new HashMap<String, FolderMessage>();
    private HashMap<String, FolderMessage> cacheUSB2Folder = new HashMap<String, FolderMessage>();


    public static VideoListManager getInstance() {
        synchronized (VideoListManager.class) {
            if (mInstance == null) {
                mInstance = new VideoListManager();
            }
            return mInstance;
        }
    }


    /**
     * 将查询到的当前目录视频列表缓存
     * @param directory
     * @param VideoList
     */
    public void addCacheUSB1VideoList(String directory, List<FileMessage> VideoList){
        if (cacheUSB1VideoList.containsKey(directory)){
            cacheUSB1VideoList.remove(directory);
        }
        cacheUSB1VideoList.put(directory,VideoList);
    }
    public List<FileMessage> getCacheUSB1VideoList(String directory){
        return cacheUSB1VideoList.get(directory);
    }

    /**
     * 将查询到的当前目录文件夹列表缓存
     * @param directory
     * @param VideoList
     */
    public void addCacheUSB1FolderList(String directory, List<FolderMessage> VideoList){
        if (cacheUSB1FolderList.containsKey(directory)){
            cacheUSB1FolderList.remove(directory);
        }
        cacheUSB1FolderList.put(directory,VideoList);
    }
    public List<FolderMessage> getCacheUSB1FolderList(String directory){
        return cacheUSB1FolderList.get(directory);
    }

    /**
     * 将查询到的当前目录视频列表缓存
     * @param directory
     * @param VideoList
     */
    public void addCacheUSB2VideoList(String directory, List<FileMessage> VideoList){
        if (cacheUSB2VideoList.containsKey(directory)){
            cacheUSB2VideoList.remove(directory);
        }
        cacheUSB2VideoList.put(directory,VideoList);
    }
    public List<FileMessage> getCacheUSB2VideoList(String directory){
        return cacheUSB2VideoList.get(directory);
    }

    /**
     * 将查询到的当前目录文件夹列表缓存
     * @param directory
     * @param VideoList
     */
    public void addCacheUSB2FolderList(String directory, List<FolderMessage> VideoList){
        if (cacheUSB2FolderList.containsKey(directory)){
            cacheUSB2FolderList.remove(directory);
        }
        cacheUSB2FolderList.put(directory,VideoList);
    }
    public List<FolderMessage> getCacheUSB2FolderList(String directory){
        return cacheUSB2FolderList.get(directory);
    }
    /**
     * 查看是否已经存在缓存
     * @param directory
     * @return
     */
    public boolean getHasUSB1Cache(String directory){
        return cacheUSB1VideoList.containsKey(directory) || cacheUSB1FolderList.containsKey(directory);
    }

    /**
     * 查看是否已经存在缓存
     * @param directory
     * @return
     */
    public boolean getHasUSB2Cache(String directory){
        return cacheUSB2VideoList.get(directory) != null || cacheUSB2FolderList.get(directory) != null;
    }

    public void clearUSB1Cache(){
        cacheUSB1VideoList.clear();
        cacheUSB1FolderList.clear();
        cacheUSB1Folder.clear();
    }

    public void clearUSB2Cache(){
        cacheUSB2VideoList.clear();
        cacheUSB2FolderList.clear();
        cacheUSB2Folder.clear();
    }


    /**
     * 所有视频的列表
     *
     * @param allVideoList
     */
    public void addAllVideoList(List<FileMessage> allVideoList) {
        mAllVideoList.clear();
        mAllVideoList.addAll(allVideoList);
    }

    public List<FileMessage> getAllVideoList() {
        return mAllVideoList;
    }

    /**
     * 所有USB1视频的列表
     *
     * @param allVideoList
     */
    public void addAllUSB1VideoList(List<FileMessage> allVideoList) {
        mAllUSB1VideoList.clear();
        mAllUSB1VideoList.addAll(allVideoList);
        notifyUSB1Observer();
    }

    public List<FileMessage> getAllUSB1VideoList() {
        return mAllUSB1VideoList;
    }


    /**
     * 切换Folder类型的时候，直接从根目录开始
     * 因此保存根目录下的文件夹和文件，以免多次查询
     *
     * @param folderList
     */
    public void addUSB1RootFolderList(List<FolderMessage> folderList) {
        mUSB1RootFolderList.clear();
        mUSB1RootFolderList.addAll(folderList);
    }

    public List<FolderMessage> getUSB1RootFolderList() {
        return mUSB1RootFolderList;
    }

    /**
     * 切换Folder类型的时候，直接从根目录开始
     * 因此保存根目录下的文件夹和文件，以免多次查询
     *
     * @param folderList
     */
    public void addUSB2RootFolderList(List<FolderMessage> folderList) {
        mUSB2RootFolderList.clear();
        mUSB2RootFolderList.addAll(folderList);
    }

    public List<FolderMessage> getUSB2RootFolderList() {
        return mUSB2RootFolderList;
    }

    /**
     * 切换Folder类型的时候，直接从根目录开始
     * 因此保存根目录下的文件夹和文件，以免多次查询
     *
     * @param folderList
     */
    public void addUSB1RootVideoList(List<FileMessage> folderList) {
        mUSB1RootVideoList.clear();
        mUSB1RootVideoList.addAll(folderList);
    }

    public List<FileMessage> getUSB1RootVideoList() {
        return mUSB1RootVideoList;
    }

    /**
     * 切换Folder类型的时候，直接从根目录开始
     * 因此保存根目录下的文件夹和文件，以免多次查询
     *
     * @param folderList
     */
    public void addUSB2RootVideoList(List<FileMessage> folderList) {
        mUSB2RootVideoList.clear();
        mUSB2RootVideoList.addAll(folderList);
    }

    public List<FileMessage> getUSB2RootVideoList() {
        return mUSB2RootVideoList;
    }


    /**
     * 所有USB2视频的列表
     *
     * @param allVideoList
     */
    public void addAllUSB2VideoList(List<FileMessage> allVideoList) {
        mAllUSB2VideoList.clear();
        mAllUSB2VideoList.addAll(allVideoList);
        notifyUSB2Observer();
    }

    public List<FileMessage> getAllUSB2VideoList() {
        return mAllUSB2VideoList;
    }


    /**
     * 当前目录的视频列表
     *
     * @param currentVideoList
     */
    public void addCurrentUSB1VideoList(List<FileMessage> currentVideoList) {
        mCurrentUSB1VideoList.clear();
        mCurrentUSB1VideoList.addAll(currentVideoList);
    }

    public List<FileMessage> getCurrentUSB1VideoList() {
        return mCurrentUSB1VideoList;
    }

    /**
     * 当前目录的视频列表
     *
     * @param currentVideoList
     */
    public void addCurrentUSB2VideoList(List<FileMessage> currentVideoList) {
        mCurrentUSB2VideoList.clear();
        mCurrentUSB2VideoList.addAll(currentVideoList);
    }

    public List<FileMessage> getCurrentUSB2VideoList() {
        return mCurrentUSB1VideoList;
    }


    /**
     * 当前目录的文件夹列表
     *
     * @param currentFolderList
     */
    public void addCurrentUSB1List(List<FileMessage> currentVideoList, List<FolderMessage> currentFolderList) {
        mCurrentUSB1VideoList.clear();
        mCurrentUSB1VideoList.addAll(currentVideoList);

        mCurrentUSB1FolderList.clear();
        mCurrentUSB1FolderList.addAll(currentFolderList);

        notifyUSB1Observer();
    }

    public List<FolderMessage> getCurrentUSB1FolderList() {
        return mCurrentUSB1FolderList;
    }

    /**
     * 当前目录的文件夹列表
     *
     * @param currentFolderList
     */
    public void addCurrentUSB2List(List<FileMessage> currentVideoList, List<FolderMessage> currentFolderList) {
        mCurrentUSB2VideoList.clear();
        mCurrentUSB2VideoList.addAll(currentVideoList);

        mCurrentUSB2FolderList.clear();
        mCurrentUSB2FolderList.addAll(currentFolderList);

        notifyUSB2Observer();
    }

    public List<FolderMessage> getCurrentUSB2FolderList() {
        return mCurrentUSB2FolderList;
    }

    /**
     * 清空所有列表
     * 例如USB断开等
     */
    public void clearAllList(){
        mAllVideoList.clear();
        mCurrentUSB1VideoList.clear();
        mCurrentUSB2VideoList.clear();
        mCurrentUSB1FolderList.clear();
        mCurrentUSB2FolderList.clear();
        mAllUSB1VideoList.clear();
        mAllUSB2VideoList.clear();
        mUSB1RootFolderList.clear();
        mUSB2RootFolderList.clear();
        mUSB1RootVideoList.clear();
        mUSB2RootVideoList.clear();
        cacheUSB1VideoList.clear();
        cacheUSB1FolderList.clear();
        cacheUSB2VideoList.clear();
        cacheUSB2FolderList.clear();
        tempUSB1Folder.clear();
        tempUSB2Folder.clear();
    }

    /**
     * 清空所有USB1列表
     * 例如USB断开等
     */
    public void clearAllUSB1List(){
        mAllUSB1VideoList.clear();
        mUSB1RootFolderList.clear();
        mUSB1RootVideoList.clear();
        cacheUSB1VideoList.clear();
        cacheUSB1FolderList.clear();
        mCurrentUSB1VideoList.clear();
        mCurrentUSB1FolderList.clear();
        tempUSB1Folder.clear();
        notifyUSB1Observer();
    }

    /**
     * 清空所有USB2列表
     * 例如USB断开等
     */
    public void clearAllUSB2List(){
        mAllUSB2VideoList.clear();
        mUSB2RootFolderList.clear();
        mUSB2RootVideoList.clear();
        cacheUSB2VideoList.clear();
        cacheUSB2FolderList.clear();
        mCurrentUSB2VideoList.clear();
        mCurrentUSB2FolderList.clear();
        tempUSB2Folder.clear();
        notifyUSB2Observer();
    }

    private List<Observer> usb1ObserverList = new ArrayList<>();

    /**
     * 添加USB1列表变化观察者
     * @param observer
     */
    public void attachUSB1Observer(Observer observer){
        usb1ObserverList.add(observer);
    }

    /**
     * 移除USB1列表变化观察者
     * @param observer
     */
    public void detachUSB1Observer(Observer observer){
        usb1ObserverList.remove(observer);
    }

    private List<Observer> usb2ObserverList = new ArrayList<>();

    /**
     * 添加USB2列表变化观察者
     * @param observer
     */
    public void attachUSB2Observer(Observer observer){
        usb2ObserverList.add(observer);
    }

    /**
     * 移除USB2列表变化观察者
     * @param observer
     */
    public void detachUSB2Observer(Observer observer){
        usb2ObserverList.remove(observer);
    }

    /**
     * 通知 USB1 扫描数据更新
     */
    private void notifyUSB1Observer(){
        for (Observer observer : usb1ObserverList){
            observer.onUpdate();
        }
        if (USBConstants.ProviderScanStatus.SCANNING == usb1ScanStatus){
            if (mAllUSB1VideoList.size() > 0){
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.SEARCHING_HAVE_DATA);
            }else {
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.SEARCHING);
            }
        }else if (USBConstants.ProviderScanStatus.SCAN_FINISHED == usb1ScanStatus){
            if (mAllUSB1VideoList.size() > 0){
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.SEARCHED_HAVE_DATA);
            }else {
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.NO_DATA);
            }
        }
    }

    /**
     * 通知 USB2 扫描数据更新
     */
    private void notifyUSB2Observer(){
        for (Observer observer : usb2ObserverList){
            observer.onUpdate();
        }
        if (USBConstants.ProviderScanStatus.SCANNING == usb2ScanStatus){
            if (mAllUSB2VideoList.size() > 0){
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.SEARCHING_HAVE_DATA);
            }else {
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.SEARCHING);
            }
        }else if (USBConstants.ProviderScanStatus.SCAN_FINISHED == usb2ScanStatus){
            if (mAllUSB2VideoList.size() > 0){
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.SEARCHED_HAVE_DATA);
            }else {
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.NO_DATA);
            }
        }
    }

    private int usb1ScanStatus = USBConstants.ProviderScanStatus.SCAN_FINISHED;
    private int usb2ScanStatus = USBConstants.ProviderScanStatus.SCAN_FINISHED;

    public void setUsb1ScanStatus(int usb1ScanStatus) {
        this.usb1ScanStatus = usb1ScanStatus;
    }

    public void setUsb2ScanStatus(int usb2ScanStatus) {
        this.usb2ScanStatus = usb2ScanStatus;
    }

    public int getUsb1ScanStatus() {
        return usb1ScanStatus;
    }

    public int getUsb2ScanStatus() {
        return usb2ScanStatus;
    }

    /**
     * 将查询到的当前目录文件夹缓存
     * @param path
     * @param folderMessage
     */
    public void addCacheUSB1Folder(String path, FolderMessage folderMessage){
        cacheUSB1Folder.put(path,folderMessage);
    }

    public void addCacheUSB2Folder(String path, FolderMessage folderMessage){
        cacheUSB1Folder.put(path,folderMessage);
    }

    /**
     * 查看是否已经存在缓存
     * @param path
     * @return
     */
    public boolean getHasUSB1FolderCache(String path){
        return cacheUSB1Folder.containsKey(path);
    }

    public boolean getHasUSB2FolderCache(String path){
        return cacheUSB2Folder.containsKey(path);
    }


    /**
     * 根据目录，通过当前查询到的全部文件，计算得到对应的文件
     * 因为查询媒体库是分段进行的，所以需要用临时对象来处理，否则会导致查询操作和获取操作 使用 同一个列表对象，
     * 导致并发修改的 ConcurrentModificationException
     * @param directory
     * @return
     */
    public List<FileMessage> getCurrentUSB1VideoList(String directory) {
        if (directory == null){
            directory = USBConstants.USBPath.USB0_PATH;
        }
        Log.d(TAG,"getCurrentUSB1VideoList directory: " + directory);

        if (cacheUSB1VideoList.containsKey(directory)){
            return cacheUSB1VideoList.get(directory);
        }

        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB1VideoList);
        List<FileMessage> tempCurrentList = new ArrayList<>();

        for (int i = 0; i < tempAllList.size(); i++) {
            String str = tempAllList.get(i).getPath();
            if (str.startsWith(directory)) {
                String subStr = str.substring(directory.length() + 1);//subStr 最开头是 ‘/’, 所以 +1，获得要查询目录下的子文件(夹)路径
                if (subStr.contains("/")) {//表示是一个文件夹

                } else {//表示是个图片(音乐、视频)文件
                    tempCurrentList.add(tempAllList.get(i));
                }
            }
        }
        if (usb1ScanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            cacheUSB1VideoList.put(directory,tempCurrentList);
        }
        return tempCurrentList;
    }

    /**
     * 根据目录，通过当前查询到的全部文件，计算得到对应的文件夹
     * 因为查询媒体库是分段进行的，所以需要用临时对象来处理，否则会导致查询操作和获取操作 使用 同一个列表对象，
     * 导致并发修改的 ConcurrentModificationException
     * @param directory
     * @return
     */
    public List<FolderMessage> getCurrentUSB1FolderList(String directory) {

        if (directory == null){
            directory = USBConstants.USBPath.USB0_PATH;
        }
        Log.d(TAG,"getCurrentUSB1FolderList directory: " + directory);
        if (cacheUSB1FolderList.containsKey(directory)){
            return cacheUSB1FolderList.get(directory);
        }
        tempUSB1Folder.clear();
        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB1VideoList);
        List<FolderMessage> tempCurrentList = new ArrayList<>();

        for (int i = 0; i < tempAllList.size(); i++) {
            String str = tempAllList.get(i).getPath();
            if (str.startsWith(directory)) {
                String subStr = str.substring(directory.length() + 1);//subStr 最开头是 ‘/’, 所以 +1，获得要查询目录下的子文件(夹)路径
                String filterStr = str.substring(directory.length());//因为用的是 Like 的查询语句，如果不是 "/" 开头，说明这个路径下有拷贝的同名文件，需要剔除
                if (filterStr.startsWith("/")) {
                    if (subStr.contains("/")) {//表示是一个文件夹
                        if (subStr != null && subStr.contains("/")) {//表示是一个文件夹
                            FolderMessage folderMessage = new FolderMessage();
                            String folderName = subStr.split("/")[0];
                            String path = directory + "/" + folderName;
                            folderMessage.setName(folderName);
                            folderMessage.setPath(path);
                            folderMessage.setParentPath(directory);
                            if (!getHasUSB1FolderTemp(path)) {
                                addTempUSB1Folder(path,folderMessage);
                                tempCurrentList.add(folderMessage);
                            }

                        } else {//表示是个图片(音乐、视频)文件

                        }
                    }
                }
            }
        }

        // 再起一个循环，获取每个 folder 下有多少个图片(音乐、视频)
        if (tempCurrentList != null && tempCurrentList.size() > 0) {
            for (FolderMessage folderMessage : tempCurrentList) {
                int picCount = 0;
                for (FileMessage fileMessage : tempAllList) {
                    if (fileMessage.getPath().startsWith(folderMessage.getPath())) {
                        String subString = fileMessage.getPath().substring(folderMessage.getPath().length());
                        if (subString.startsWith("/")) {
                            picCount++;
                        }
                    }
                }
                folderMessage.setCount(picCount);
            }
        }

        // 当前目录文件夹的排序
        Collections.sort(tempCurrentList, new Comparator<FolderMessage>() {
            Comparator<String> comparator = new PinyinComparator();

            @Override
            public int compare(FolderMessage o1, FolderMessage o2) {
                return comparator.compare(o1.getName(), o2.getName());
            }
        });

        if (usb1ScanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            cacheUSB1FolderList.put(directory,tempCurrentList);
        }
        return tempCurrentList;
    }


    public List<FileMessage> getCurrentUSB2VideoList(String directory) {
        if (directory == null){
            directory = USBConstants.USBPath.USB1_PATH;
        }

        Log.d(TAG,"getCurrentUSB2VideoList directory: " + directory);

        if (cacheUSB2VideoList.containsKey(directory)){
            return cacheUSB2VideoList.get(directory);
        }
        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB2VideoList);
        List<FileMessage> tempCurrentList = new ArrayList<>();

        for (int i = 0; i < tempAllList.size(); i++) {
            String str = tempAllList.get(i).getPath();
            if (str.startsWith(directory)) {
                String subStr = str.substring(directory.length() + 1);//subStr 最开头是 ‘/’, 所以 +1，获得要查询目录下的子文件(夹)路径
                if (subStr.contains("/")) {//表示是一个文件夹

                } else {//表示是个图片(音乐、视频)文件
                    tempCurrentList.add(tempAllList.get(i));
                }
            }
        }
        if (usb2ScanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            cacheUSB2VideoList.put(directory,tempCurrentList);
        }
        return tempCurrentList;
    }

    public List<FolderMessage> getCurrentUSB2FolderList(String directory) {

        if (directory == null){
            directory = USBConstants.USBPath.USB1_PATH;
        }
        Log.d(TAG,"getCurrentUSB2FolderList directory: " + directory);

        if (cacheUSB2FolderList.containsKey(directory)){
            return cacheUSB2FolderList.get(directory);
        }

        tempUSB2Folder.clear();
        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB2VideoList);
        List<FolderMessage> tempCurrentList = new ArrayList<>();

        for (int i = 0; i < tempAllList.size(); i++) {
            String str = tempAllList.get(i).getPath();
            if (str.startsWith(directory)) {
                String subStr = str.substring(directory.length() + 1);//subStr 最开头是 ‘/’, 所以 +1，获得要查询目录下的子文件(夹)路径
                String filterStr = str.substring(directory.length());//因为用的是 Like 的查询语句，如果不是 "/" 开头，说明这个路径下有拷贝的同名文件，需要剔除
                if (filterStr.startsWith("/")) {
                    if (subStr.contains("/")) {//表示是一个文件夹
                        if (subStr != null && subStr.contains("/")) {//表示是一个文件夹
                            FolderMessage folderMessage = new FolderMessage();
                            String folderName = subStr.split("/")[0];
                            String path = directory + "/" + folderName;
                            folderMessage.setName(folderName);
                            folderMessage.setPath(path);
                            folderMessage.setParentPath(directory);
                            if (!getHasUSB2FolderTemp(path)) {
                                addTempUSB2Folder(path,folderMessage);
                                tempCurrentList.add(folderMessage);
                            }

                        } else {//表示是个图片(音乐、视频)文件

                        }
                    }
                }
            }
        }

        // 再起一个循环，获取每个 folder 下有多少个图片(音乐、视频)
        if (tempCurrentList != null && tempCurrentList.size() > 0) {
            for (FolderMessage folderMessage : tempCurrentList) {
                int picCount = 0;
                for (FileMessage fileMessage : tempAllList) {
                    if (fileMessage.getPath().startsWith(folderMessage.getPath())) {
                        String subString = fileMessage.getPath().substring(folderMessage.getPath().length());
                        if (subString.startsWith("/")) {
                            picCount++;
                        }
                    }
                }
                folderMessage.setCount(picCount);
            }
        }
        // 当前目录文件夹的排序
        Collections.sort(tempCurrentList, new Comparator<FolderMessage>() {
            Comparator<String> comparator = new PinyinComparator();

            @Override
            public int compare(FolderMessage o1, FolderMessage o2) {
                return comparator.compare(o1.getName(), o2.getName());
            }
        });

        if (usb2ScanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            cacheUSB2FolderList.put(directory,tempCurrentList);
        }

        return tempCurrentList;
    }

    private HashMap<String, FolderMessage> tempUSB1Folder = new HashMap<String, FolderMessage>();
    private HashMap<String, FolderMessage> tempUSB2Folder = new HashMap<String, FolderMessage>();

    /**
     * 将查询到的当前目录文件夹缓存
     * @param path
     * @param folderMessage
     */
    public void addTempUSB1Folder(String path, FolderMessage folderMessage){
        tempUSB1Folder.put(path,folderMessage);
    }

    public void addTempUSB2Folder(String path, FolderMessage folderMessage){
        tempUSB2Folder.put(path,folderMessage);
    }

    /**
     * 查看是否已经存在缓存
     * @param path
     * @return
     */
    public boolean getHasUSB1FolderTemp(String path){
        return tempUSB1Folder.containsKey(path);
    }

    public boolean getHasUSB2FolderTemp(String path){
        return tempUSB2Folder.containsKey(path);
    }
    
}
