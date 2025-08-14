package com.desaysv.svlibpicturebean.manager;
/**
 * Created by ZNB on 2022-01-12
 * 存储查询到的图片和文件夹列表
 * 这个类只处理查询到的列表，只和查询操作有关
 */

import android.util.Log;

import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibpicturebean.utils.PinyinComparator;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB1PictureDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2PictureDataSubject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PictureListManager {
    private static final String TAG = "PictureListManager";
    private static PictureListManager mInstance;
    private List<FileMessage> mAllPictureList = new ArrayList<>();
    private List<FileMessage> mCurrentUSB1PictureList = new ArrayList<>();
    private List<FileMessage> mCurrentUSB2PictureList = new ArrayList<>();
    private List<FileMessage> mAllUSB1PictureList = new ArrayList<>();
    private List<FileMessage> mAllUSB2PictureList = new ArrayList<>();
    private List<FolderMessage> mUSB1RootFolderList = new ArrayList<>();
    private List<FolderMessage> mUSB2RootFolderList = new ArrayList<>();
    private List<FileMessage> mUSB1RootPictureList = new ArrayList<>();
    private List<FileMessage> mUSB2RootPictureList = new ArrayList<>();
    private List<FolderMessage> mCurrentUSB1FolderList = new ArrayList<>();
    private List<FolderMessage> mCurrentUSB2FolderList = new ArrayList<>();

    private HashMap<String, List<FileMessage>> cacheUSB1PictureList = new HashMap<String, List<FileMessage>>();
    private HashMap<String, List<FolderMessage>> cacheUSB1FolderList = new HashMap<String, List<FolderMessage>>();
    private HashMap<String, List<FileMessage>> cacheUSB2PictureList = new HashMap<String, List<FileMessage>>();
    private HashMap<String, List<FolderMessage>> cacheUSB2FolderList = new HashMap<String, List<FolderMessage>>();

    private HashMap<String, FolderMessage> cacheUSB1Folder = new HashMap<String, FolderMessage>();
    private HashMap<String, FolderMessage> cacheUSB2Folder = new HashMap<String, FolderMessage>();

    private HashMap<String, List<FolderMessage>> cacheUSB1EmptyFolderList = new HashMap<String, List<FolderMessage>>();
    private HashMap<String, List<FolderMessage>> cacheUSB2EmptyFolderList = new HashMap<String, List<FolderMessage>>();
    private List<FolderMessage> mCurrentUSB1EmptyFolderList = new ArrayList<>();
    private List<FolderMessage> mCurrentUSB2EmptyFolderList = new ArrayList<>();

    private List<String> mCurrentBadList = new ArrayList<>();//维护一个破损图片的播放列表

    public static PictureListManager getInstance() {
        synchronized (PictureListManager.class) {
            if (mInstance == null) {
                mInstance = new PictureListManager();
            }
            return mInstance;
        }
    }


    /**
     * 将查询到的当前目录图片列表缓存
     * @param directory
     * @param pictureList
     */
    public void addCacheUSB1PictureList(String directory, List<FileMessage> pictureList){
        if (cacheUSB1PictureList.containsKey(directory)){
            cacheUSB1PictureList.remove(directory);
        }
        cacheUSB1PictureList.put(directory,pictureList);
    }
    public List<FileMessage> getCacheUSB1PictureList(String directory){
        return cacheUSB1PictureList.get(directory);
    }

    /**
     * 将查询到的当前目录文件夹列表缓存
     * @param directory
     * @param pictureList
     */
    public void addCacheUSB1FolderList(String directory, List<FolderMessage> pictureList){
        if (cacheUSB1FolderList.containsKey(directory)){
            cacheUSB1FolderList.remove(directory);
        }
        cacheUSB1FolderList.put(directory,pictureList);
    }
    public List<FolderMessage> getCacheUSB1FolderList(String directory){
        return cacheUSB1FolderList.get(directory);
    }

    /**
     * 将查询到的当前目录图片列表缓存
     * @param directory
     * @param pictureList
     */
    public void addCacheUSB2PictureList(String directory, List<FileMessage> pictureList){
        if (cacheUSB2PictureList.containsKey(directory)){
            cacheUSB2PictureList.remove(directory);
        }
        cacheUSB2PictureList.put(directory,pictureList);
    }
    public List<FileMessage> getCacheUSB2PictureList(String directory){
        return cacheUSB2PictureList.get(directory);
    }

    /**
     * 将查询到的当前目录文件夹列表缓存
     * @param directory
     * @param pictureList
     */
    public void addCacheUSB2FolderList(String directory, List<FolderMessage> pictureList){
        if (cacheUSB2FolderList.containsKey(directory)){
            cacheUSB2FolderList.remove(directory);
        }
        cacheUSB2FolderList.put(directory,pictureList);
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
        return (cacheUSB1PictureList.containsKey(directory) || cacheUSB1FolderList.containsKey(directory));
    }

    /**
     * 查看是否已经存在缓存
     * @param directory
     * @return
     */
    public boolean getHasUSB2Cache(String directory){
        return (cacheUSB2PictureList.get(directory) != null || cacheUSB2FolderList.get(directory) != null);
    }

    public void clearUSB1Cache(){
        cacheUSB1PictureList.clear();
        cacheUSB1FolderList.clear();
        cacheUSB1Folder.clear();
        mCurrentBadList.clear();
    }

    public void clearUSB2Cache(){
        cacheUSB2PictureList.clear();
        cacheUSB2FolderList.clear();
        cacheUSB2Folder.clear();
        mCurrentBadList.clear();
    }


    /**
     * 所有图片的列表
     *
     * @param allPictureList
     */
    public void addAllPictureList(List<FileMessage> allPictureList) {
        mAllPictureList.clear();
        mAllPictureList.addAll(allPictureList);
    }

    public List<FileMessage> getAllPictureList() {
        return mAllPictureList;
    }

    /**
     * 所有USB1图片的列表
     *
     * @param allPictureList
     */
    public void addAllUSB1PictureList(List<FileMessage> allPictureList) {
        mAllUSB1PictureList.clear();
        mAllUSB1PictureList.addAll(allPictureList);
        Log.d(TAG,"addAllUSB1PictureList");
    }

    public List<FileMessage> getAllUSB1PictureList() {
        return mAllUSB1PictureList;
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
    public void addUSB1RootPictureList(List<FileMessage> folderList) {
        mUSB1RootPictureList.clear();
        mUSB1RootPictureList.addAll(folderList);
    }

    public List<FileMessage> getUSB1RootPictureList() {
        return mUSB1RootPictureList;
    }

    /**
     * 切换Folder类型的时候，直接从根目录开始
     * 因此保存根目录下的文件夹和文件，以免多次查询
     *
     * @param folderList
     */
    public void addUSB2RootPictureList(List<FileMessage> folderList) {
        mUSB2RootPictureList.clear();
        mUSB2RootPictureList.addAll(folderList);
    }

    public List<FileMessage> getUSB2RootPictureList() {
        return mUSB2RootPictureList;
    }


    /**
     * 所有USB2图片的列表
     *
     * @param allPictureList
     */
    public void addAllUSB2PictureList(List<FileMessage> allPictureList) {
        mAllUSB2PictureList.clear();
        mAllUSB2PictureList.addAll(allPictureList);
    }

    public List<FileMessage> getAllUSB2PictureList() {
        return mAllUSB2PictureList;
    }


    /**
     * 当前目录的图片列表
     * 使用 addCurrentUSB1List 替代
     * @param currentPictureList
     */
    @Deprecated
    public void addCurrentUSB1PictureList(List<FileMessage> currentPictureList) {
        mCurrentUSB1PictureList.clear();
        mCurrentUSB1PictureList.addAll(currentPictureList);
    }

    public List<FileMessage> getCurrentUSB1PictureList() {
        return mCurrentUSB1PictureList;
    }

    /**
     * 当前目录的图片列表
     * 使用 addCurrentUSB2List 替代
     * @param currentPictureList
     */
    @Deprecated
    public void addCurrentUSB2PictureList(List<FileMessage> currentPictureList) {
        mCurrentUSB2PictureList.clear();
        mCurrentUSB2PictureList.addAll(currentPictureList);
    }

    public List<FileMessage> getCurrentUSB2PictureList() {
        return mCurrentUSB2PictureList;
    }

    /**
     * 当前目录的文件夹列表
     *
     * @param currentFolderList
     */
    public void addCurrentUSB1List(List<FileMessage> currentPictureList, List<FolderMessage> currentFolderList) {
        mCurrentUSB1PictureList.clear();
        mCurrentUSB1PictureList.addAll(currentPictureList);

        mCurrentUSB1FolderList.clear();
        mCurrentUSB1FolderList.addAll(currentFolderList);
        Log.d(TAG,"addCurrentUSB1List");
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
    public void addCurrentUSB2List(List<FileMessage> currentPictureList, List<FolderMessage> currentFolderList) {
        mCurrentUSB2PictureList.clear();
        mCurrentUSB2PictureList.addAll(currentPictureList);

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
        mAllPictureList.clear();
        mCurrentUSB1PictureList.clear();
        mCurrentUSB2PictureList.clear();
        mCurrentUSB1FolderList.clear();
        mCurrentUSB2FolderList.clear();
        mAllUSB1PictureList.clear();
        mAllUSB2PictureList.clear();
        mUSB1RootFolderList.clear();
        mUSB2RootFolderList.clear();
        mUSB1RootPictureList.clear();
        mUSB2RootPictureList.clear();
        cacheUSB1PictureList.clear();
        cacheUSB1FolderList.clear();
        cacheUSB2PictureList.clear();
        cacheUSB2FolderList.clear();
        usb1ScanStatus = USBConstants.ProviderScanStatus.SCAN_FINISHED;
        usb2ScanStatus = USBConstants.ProviderScanStatus.SCAN_FINISHED;
        cacheUSB1Folder.clear();
        cacheUSB2Folder.clear();
        tempUSB1Folder.clear();
        tempUSB2Folder.clear();
        cacheUSB1EmptyFolderList.clear();
        cacheUSB2EmptyFolderList.clear();
        mCurrentUSB1EmptyFolderList.clear();
        mCurrentUSB2EmptyFolderList.clear();
        mCurrentBadList.clear();
        Log.d(TAG,"clearAllList");
        notifyUSB1Observer();
        notifyUSB2Observer();
    }

    /**
     * 清空所有USB1列表
     * 例如USB断开等
     */
    public void clearAllUSB1List(){
        mAllUSB1PictureList.clear();
        mUSB1RootFolderList.clear();
        mUSB1RootPictureList.clear();
        cacheUSB1PictureList.clear();
        cacheUSB1FolderList.clear();
        mCurrentUSB1PictureList.clear();
        mCurrentUSB1FolderList.clear();
        cacheUSB1Folder.clear();
        tempUSB1Folder.clear();
        cacheUSB1EmptyFolderList.clear();
        mCurrentUSB1EmptyFolderList.clear();
        mCurrentBadList.clear();
        usb1ScanStatus = USBConstants.ProviderScanStatus.SCAN_FINISHED;
        Log.d(TAG,"clearAllUSB1List");
        notifyUSB1Observer();
    }

    /**
     * 清空所有USB2列表
     * 例如USB断开等
     */
    public void clearAllUSB2List(){
        mAllUSB2PictureList.clear();
        mUSB2RootFolderList.clear();
        mUSB2RootPictureList.clear();
        cacheUSB2PictureList.clear();
        cacheUSB2FolderList.clear();
        mCurrentUSB2PictureList.clear();
        mCurrentUSB2FolderList.clear();
        cacheUSB2Folder.clear();
        tempUSB2Folder.clear();
        cacheUSB2EmptyFolderList.clear();
        mCurrentUSB2EmptyFolderList.clear();
        usb2ScanStatus = USBConstants.ProviderScanStatus.SCAN_FINISHED;
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
        if (usb1ScanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            if (mAllUSB1PictureList.size() > 0) {
                Log.d(TAG,"SearchType.SEARCHED_HAVE_DATA");
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
            }else {
                Log.d(TAG,"SearchType.NO_DATA");
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.NO_DATA);
            }
        }else {
            if (mAllUSB1PictureList.size() > 0) {
                Log.d(TAG,"SearchType.SEARCHING_HAVE_DATA");
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHING_HAVE_DATA);
            }else {
                Log.d(TAG,"SearchType.SEARCHING");
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHING);
            }
        }

        for (Observer observer : usb1ObserverList){
            observer.onUpdate();
        }

    }

    /**
     * 通知 USB2 扫描数据更新
     */
    private void notifyUSB2Observer(){

        if (usb2ScanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            if (mAllUSB2PictureList.size() > 0) {
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
            }else {
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.NO_DATA);
            }
        }else {
            if (mAllUSB2PictureList.size() > 0) {
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHING_HAVE_DATA);
            }else {
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHING);
            }
        }

        for (Observer observer : usb2ObserverList){
            observer.onUpdate();
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
     * 有可能刚好 addAllUSB1PictureList() 的时候，
     * 执行 mAllUSB1PictureList.clear的时候拿数据，可能会出现显示为空的情况，
     * 可以考虑加锁，也可以考虑使用延迟
     * @param directory
     * @return
     */
    public List<FileMessage> getCurrentUSB1PictureList(String directory) {
        if (directory == null){
            directory = USBConstants.USBPath.USB0_PATH;
        }
        Log.d(TAG,"znbtest directory: " + directory);

        if (cacheUSB1PictureList.containsKey(directory)){
            return cacheUSB1PictureList.get(directory);
        }

        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB1PictureList);
        List<FileMessage> tempCurrentList = new ArrayList<>();

        for (int i = 0; i < tempAllList.size(); i++) {
            String str = tempAllList.get(i).getPath();
            if (str.startsWith(directory+"/")) {
                String subStr = str.substring(directory.length() + 1);//subStr 最开头是 ‘/’, 所以 +1，获得要查询目录下的子文件(夹)路径
                if (subStr.contains("/") || !subStr.contains(".")) {//表示是一个文件夹，不包含.表示这是上一级目录的文件

                } else {//表示是个图片(音乐、视频)文件
                    tempCurrentList.add(tempAllList.get(i));
                }
            }
        }
        if (usb1ScanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            cacheUSB1PictureList.put(directory,tempCurrentList);
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
        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB1PictureList);
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


    public List<FileMessage> getCurrentUSB2PictureList(String directory) {
        if (directory == null){
            directory = USBConstants.USBPath.USB1_PATH;
        }

        Log.d(TAG,"getCurrentUSB2PictureList directory: " + directory);

        if (cacheUSB2PictureList.containsKey(directory)){
            return cacheUSB2PictureList.get(directory);
        }
        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB2PictureList);
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
            cacheUSB2PictureList.put(directory,tempCurrentList);
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
        List<FileMessage> tempAllList = new ArrayList<>(mAllUSB2PictureList);
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

    /**
     * 根据路径，和含有图片的列表比较
     * @param directory
     * @param folder
     * @return
     */
    private boolean isEmpty(String directory, File folder){
        Log.d(TAG,"isEmpty,directory:"+directory+",folder:"+folder.getPath());
        List<FolderMessage> tempCurrentList = getCurrentUSB1FolderList(directory);
        for (FolderMessage folderMessage: tempCurrentList){
            if (folder.getPath().equals(folderMessage.getPath())){
                return false;
            }
        }
        return true;
    }

    /**
     * 根据目录，获取对应目录的空文件夹列表
     * 奇瑞的奇葩设计
     * @param directory
     * @return
     */
    public List<FolderMessage> getCurrentUSB1EmptyFolderList(String directory) {
        if (directory == null){
            directory = USBConstants.USBPath.USB0_PATH;
        }
        Log.d(TAG,"getCurrentUSB1EmptyFolderList directory: " + directory);
        if (cacheUSB1EmptyFolderList.containsKey(directory)){
            Log.d(TAG,"getCurrentUSB1EmptyFolderList cache");
            mCurrentUSB1EmptyFolderList.clear();
            mCurrentUSB1EmptyFolderList.addAll(cacheUSB1EmptyFolderList.get(directory));
            return cacheUSB1EmptyFolderList.get(directory);
        }
        List<FolderMessage> tempCurrentList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        if (files != null) {
            for (File file : files) {//列举当前目录下的文件
                if (file.isDirectory()) {//如果是文件夹
                    if (isEmpty(directory, file)) {//判断是否含有图片
                        FolderMessage folderMessage = new FolderMessage();
                        folderMessage.setPath(file.getAbsolutePath());
                        folderMessage.setName(file.getName());
                        folderMessage.setParentPath(directory);
                        tempCurrentList.add(folderMessage);
                    }
                }
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
            cacheUSB1EmptyFolderList.put(directory,tempCurrentList);
        }
        mCurrentUSB1EmptyFolderList.clear();
        mCurrentUSB1EmptyFolderList.addAll(tempCurrentList);
        return tempCurrentList;
    }

    /**
     * 根据目录，获取对应目录的空文件夹列表
     * 奇瑞的奇葩设计
     * @param directory
     * @return
     */
    public List<FolderMessage> getCurrentUSB2EmptyFolderList(String directory) {
        if (directory == null){
            directory = USBConstants.USBPath.USB1_PATH;
        }
        Log.d(TAG,"getCurrentUSB2EmptyFolderList directory: " + directory);
        if (cacheUSB2EmptyFolderList.containsKey(directory)){
            mCurrentUSB2EmptyFolderList.clear();
            mCurrentUSB2EmptyFolderList.addAll(cacheUSB2EmptyFolderList.get(directory));
            return cacheUSB2EmptyFolderList.get(directory);
        }
        List<FolderMessage> tempCurrentList = new ArrayList<>();
        for (File file : new File(directory).listFiles()){//列举当前目录下的文件
            if (file.isDirectory()){//如果是文件夹
                if (isEmpty(directory,file)){//判断是否含有图片
                    FolderMessage folderMessage = new FolderMessage();
                    folderMessage.setPath(file.getAbsolutePath());
                    folderMessage.setName(file.getName());
                    tempCurrentList.add(folderMessage);
                }
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
            cacheUSB2EmptyFolderList.put(directory,tempCurrentList);
        }
        mCurrentUSB2EmptyFolderList.clear();
        mCurrentUSB2EmptyFolderList.addAll(tempCurrentList);
        return tempCurrentList;
    }

    public List<FolderMessage> getCurrentUSB1EmptyFolderList(){
        return mCurrentUSB1EmptyFolderList;
    }

    public List<FolderMessage> getCurrentUSB2EmptyFolderList(){
        return mCurrentUSB2EmptyFolderList;
    }

    public List<String> getCurrentBadList() {
        return mCurrentBadList;
    }

    public void setCurrentBadList(FileMessage fileMessage) {
        if (!this.mCurrentBadList.contains(fileMessage.getPath())){
            this.mCurrentBadList.add(fileMessage.getPath());
        }

    }
}
