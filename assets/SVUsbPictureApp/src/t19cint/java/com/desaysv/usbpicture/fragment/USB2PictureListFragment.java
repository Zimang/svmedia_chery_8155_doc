package com.desaysv.usbpicture.fragment;

import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.querypicture.QueryManager;
import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.manager.PictureListManager;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB2PictureDataSubject;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.constant.Constant;
import com.desaysv.usbpicture.observer.IScanObserver;
import com.desaysv.usbpicture.observer.USB2ScanObserver;

import java.util.List;

public class USB2PictureListFragment extends BaseUSBPictureListFragment{

    @Override
    protected void updateList() {
        if (PictureListManager.getInstance().getAllUSB2PictureList() != null){
            Log.d(TAG,"updateList");
            if (styleType == STYLE_TYPE_FOLDER) {
                updateListAdapter(PictureListManager.getInstance().getUSB2RootPictureList(), PictureListManager.getInstance().getUSB2RootFolderList()
                        , PictureListManager.getInstance().getCurrentUSB2EmptyFolderList(USBConstants.USBPath.USB1_PATH));
            }else {
                updateListAdapter(PictureListManager.getInstance().getAllUSB2PictureList(),null,null);
            }
        }
        updateViewWithContent();
    }

    @Override
    protected boolean checkUSBStatus() {
        return DeviceStatusBean.getInstance().isUSB2Connect();
    }

    @Override
    public void updateViewWithScanStatus() {
        if (USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() == SearchType.SEARCHED_HAVE_DATA) {
            Log.d(TAG,"SEARCHED_HAVE_DATA,mLoadingDialog dismiss");
//            mLoadingDialog.dismiss();
            startLoadingAni(false);
            //ToastUtil.showToast(getContext(),getString(R.string.loading_ok));
        }else if (USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() == SearchType.NO_DATA){
            Log.d(TAG,"SEARCHED_NO_DATA,mLoadingDialog dismiss");
//            mLoadingDialog.dismiss();
            startLoadingAni(false);
        }else if (MediaKey.SUPPORT_SUBSECTION_LOADING && USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() == SearchType.SEARCHING_HAVE_DATA){
            Log.d(TAG,"SEARCHING_HAVE_DATA,mLoadingDialog dismiss");
//            mLoadingDialog.dismiss();
            startLoadingAni(false);
        }else {
            Log.d(TAG,"SEARCHED_SEARCHING,mLoadingDialog show");
            startLoadingAni(true);
        }
    }

    @Override
    public void updateQueryWithScanStatus() {
        QueryManager.getInstance().startQueryPictureWithUSB2Scan(USB2ScanObserver.getInstance().getScanStatus(), folderAdapter.getCurrentFolderPath());
    }

    @Override
    protected int getUSBType() {
        return Constant.USBType.TYPE_USB2;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void initData() {
        if (USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() != SearchType.SEARCHED_HAVE_DATA ) {
            rl_pictureList_root.setVisibility(View.GONE);
        }
        updateData();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void updateData() {
        if (checkUSBStatus()) {// USB连接上才启动查询
            QueryManager.getInstance().startQueryPictureWithUSB2();
            updateViewWithUSBStatus(true);
        }else {
            updateViewWithUSBStatus(false);
            updateList();
        }
    }

    @Override
    public void forceUpdateData() {
    }

    @Override
    public String getUSBPath() {
        return USBConstants.USBPath.USB1_PATH;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean handleShowAllPictures() {
        if (folderAdapter == null){
            return false;
        }
        updateListAdapter(PictureListManager.getInstance().getAllUSB2PictureList(),null,null);
        updateViewWithCurrentPath(USBConstants.USBPath.USB0_PATH);
        folderAdapter.updateStyleType(STYLE_TYPE_ALL_PICTURE);
        styleType = STYLE_TYPE_ALL_PICTURE;
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean handleShowFolderPictures() {
        if (folderAdapter == null){
            return false;
        }
        updateListAdapter(PictureListManager.getInstance().getUSB2RootPictureList(),PictureListManager.getInstance().getUSB2RootFolderList()
                , PictureListManager.getInstance().getCurrentUSB2EmptyFolderList(USBConstants.USBPath.USB1_PATH));
        PictureListManager.getInstance().addCurrentUSB2PictureList(PictureListManager.getInstance().getUSB2RootPictureList());
        updateViewWithCurrentPath(USBConstants.USBPath.USB0_PATH);
        folderAdapter.updateStyleType(STYLE_TYPE_FOLDER);
        styleType = STYLE_TYPE_FOLDER;
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        PictureListManager.getInstance().attachUSB2Observer(USB2ListObserver);
        USB2PictureDataSubject.getInstance().attachObserver(TAG,USB2SearchingTypeObserver);
        USB2ScanObserver.getInstance().attachObserver(usb2ScanStatusObserver);
//        QueryManager.getInstance().registerPictureListener(getContext(),mListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        PictureListManager.getInstance().detachUSB2Observer(USB2ListObserver);
        USB2PictureDataSubject.getInstance().detachObserver(TAG);
        USB2ScanObserver.getInstance().detachObserver(usb2ScanStatusObserver);
//        QueryManager.getInstance().unregisterPictureListener(getContext(),mListener);
    }

    @Override
    public void updateViewWithContent() {
        if (!MediaKey.SUPPORT_SUBSECTION_LOADING && USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() != SearchType.SEARCHED_HAVE_DATA) {
            return;
        }
        if (PictureListManager.getInstance().getAllUSB2PictureList() == null || PictureListManager.getInstance().getAllUSB2PictureList().size() < 1){
            updateViewWithNoContent(false);
        }else {
            updateViewWithNoContent(true);
        }
    }

    @Override
    protected void updateCurrentPictureList(List<FileMessage> pictureList) {
        PictureListManager.getInstance().addCurrentUSB2PictureList(pictureList);
    }


    @Override
    protected void shouldUpdate() {
        shouldUpdate = USB2PictureDataSubject.getInstance().getUSB2PictureSearchType() != SearchType.SEARCHED_HAVE_DATA;
    }

    private Observer USB2ListObserver = new Observer() {
        @Override
        public void onUpdate() {
            Log.d(TAG,"USB2ListObserver onUpdate");
            myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
            myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
            myHandler.removeMessages(MSG_CLICK_FOLDER);
            myHandler.removeMessages(MSG_CLICK_PREVIEW);
            myHandler.removeMessages(MSG_UPDATE_LIST);
            myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
        }
    };

    private Observer USB2SearchingTypeObserver = new Observer() {
        @Override
        public void onUpdate() {
            Log.d(TAG,"USB2SearchingTypeObserver onUpdate");
            myHandler.removeMessages(MSG_UPDATE_SEARCHING);
            myHandler.sendEmptyMessage(MSG_UPDATE_SEARCHING);
        }
    };

    private IScanObserver usb2ScanStatusObserver = new IScanObserver() {
        @Override
        public void onUpdate(int scanStatus) {
            Log.d(TAG,"usb2ScanStatusObserver onUpdate,scanStatus:"+scanStatus);
            myHandler.removeMessages(MSG_UPDATE_SCAN_STATUS);
            myHandler.sendEmptyMessage(MSG_UPDATE_SCAN_STATUS);
        }
    };

    @Override
    public boolean needUpdateWithUSBStatus(String path, boolean status) {
        return false;
    }
}
