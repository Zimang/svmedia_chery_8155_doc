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
import com.desaysv.usbbaselib.statussubject.USB1PictureDataSubject;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.constant.Constant;
import com.desaysv.usbpicture.observer.IScanObserver;
import com.desaysv.usbpicture.observer.USB1ScanObserver;

import java.util.List;

public class USB1PictureListFragment extends BaseUSBPictureListFragment{

    @Override
    protected boolean checkUSBStatus() {
        return DeviceStatusBean.getInstance().isUSB1Connect();
    }

    @Override
    public void updateViewWithScanStatus() {
        if (USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() == SearchType.SEARCHED_HAVE_DATA) {
            Log.d(TAG,"SEARCHED_HAVE_DATA,mLoadingDialog dismiss");
//            mLoadingDialog.dismiss();
            startLoadingAni(false);
            if (!hadShowToast) {
                hadShowToast = true;
                //ToastUtil.showToast(getContext(), getString(R.string.loading_ok));
            }
        }else if (USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() == SearchType.NO_DATA){
            Log.d(TAG,"SEARCHED_NO_DATA,mLoadingDialog dismiss");
//            mLoadingDialog.dismiss();
            startLoadingAni(false);
        }else if (MediaKey.SUPPORT_SUBSECTION_LOADING && USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() == SearchType.SEARCHING_HAVE_DATA){
            Log.d(TAG,"SEARCHING_HAVE_DATA,mLoadingDialog dismiss");
//            mLoadingDialog.dismiss();
            if (!hadShowToast) {//确保这个提示只显示一次
                hadShowToast = true;
                //ToastUtil.showToast(getContext(), getString(R.string.loading_ok));
            }
            startLoadingAni(false);
        }else {
            Log.d(TAG,"SEARCHED_SEARCHING,mLoadingDialog show");
            startLoadingAni(true);
        }
    }

    @Override
    public void updateQueryWithScanStatus() {
        QueryManager.getInstance().startQueryPictureWithUSB1Scan(USB1ScanObserver.getInstance().getScanStatus(), folderAdapter.getCurrentFolderPath());
    }

    @Override
    protected int getUSBType() {
        return Constant.USBType.TYPE_USB1;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void initData() {
        if (USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() != SearchType.SEARCHED_HAVE_DATA ) {
            rl_pictureList_root.setVisibility(View.GONE);
        }
        USB1ScanObserver.getInstance().attachObserver(usb1ScanStatusObserver);
        PictureListManager.getInstance().attachUSB1Observer(usb1ListObserver);
        USB1PictureDataSubject.getInstance().attachObserver(TAG,usb1SearchingTypeObserver);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void updateData() {
        if (checkUSBStatus()) {// USB连接上才启动查询
            if (MediaKey.SUPPORT_SUBSECTION_LOADING && !isFirst){
                updateList();
            }else {
                isFirst = false;
                QueryManager.getInstance().startQueryPictureWithUSB1();
            }
            updateViewWithUSBStatus(true);
        }else {
            updateViewWithUSBStatus(false);
            updateList();
        }
    }

    @Override
    public void forceUpdateData() {
        QueryManager.getInstance().startQueryPictureWithUSB1();
    }

    @Override
    public String getUSBPath() {
        return USBConstants.USBPath.USB0_PATH;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean handleShowAllPictures() {
        if (folderAdapter == null){
            return false;
        }
        folderAdapter.updateStyleType(STYLE_TYPE_ALL_PICTURE);
        styleType = STYLE_TYPE_ALL_PICTURE;
        updateListAdapter(PictureListManager.getInstance().getAllUSB1PictureList(),null,null);
        updateViewWithCurrentPath(USBConstants.USBPath.USB0_PATH);
        updateViewWithContent();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean handleShowFolderPictures() {
        if (folderAdapter == null){
            return false;
        }
        folderAdapter.updateStyleType(STYLE_TYPE_FOLDER);
        styleType = STYLE_TYPE_FOLDER;
        if (MediaKey.SUPPORT_SUBSECTION_LOADING) {
            updateListAdapter(PictureListManager.getInstance().getCurrentUSB1PictureList(USBConstants.USBPath.USB0_PATH), PictureListManager.getInstance().getCurrentUSB1FolderList(USBConstants.USBPath.USB0_PATH)
            ,PictureListManager.getInstance().getCurrentUSB1EmptyFolderList(USBConstants.USBPath.USB0_PATH));
        }else {
            updateListAdapter(PictureListManager.getInstance().getUSB1RootPictureList(),PictureListManager.getInstance().getUSB1RootFolderList(),PictureListManager.getInstance().getCurrentUSB1EmptyFolderList(USBConstants.USBPath.USB0_PATH));
        }
        updateViewWithCurrentPath(USBConstants.USBPath.USB0_PATH);
        updateViewWithContent();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void updateList() {
        if (PictureListManager.getInstance().getAllUSB1PictureList() != null){
            Log.d(TAG,"updateList");
            if (styleType == STYLE_TYPE_FOLDER) {
                if (MediaKey.SUPPORT_SUBSECTION_LOADING){
                    updateListAdapter(PictureListManager.getInstance().getCurrentUSB1PictureList(folderAdapter.getCurrentFolderPath()), PictureListManager.getInstance().getCurrentUSB1FolderList(folderAdapter.getCurrentFolderPath())
                            , PictureListManager.getInstance().getCurrentUSB1EmptyFolderList(folderAdapter.getCurrentFolderPath()));
                    updateViewWithCurrentPath(folderAdapter.getCurrentFolderPath());
                }else {
                    updateListAdapter(PictureListManager.getInstance().getCurrentUSB1PictureList(), PictureListManager.getInstance().getCurrentUSB1FolderList(), PictureListManager.getInstance().getCurrentUSB1EmptyFolderList(folderAdapter.getCurrentFolderPath()));
                    if (PictureListManager.getInstance().getCurrentUSB1FolderList().size() > 0) {
                        updateViewWithCurrentPath(PictureListManager.getInstance().getCurrentUSB1FolderList().get(0).getParentPath());
                    } else {
                        updateViewWithCurrentPath(folderAdapter.getCurrentFolderPath());
                    }
                }
            }else {
                updateListAdapter(PictureListManager.getInstance().getAllUSB1PictureList(),null,null);
            }
        }
        updateViewWithContent();
    }

    @Override
    public void onStart() {
        //这个要放到super之前，因为update操作在 super，注册在之后会导致不更新回调
//        PictureListManager.getInstance().attachUSB1Observer(usb1ListObserver);
//        USB1PictureDataSubject.getInstance().attachObserver(TAG,usb1SearchingTypeObserver);
        super.onStart();
//        QueryManager.getInstance().registerPictureListener(getContext(),mListener);
    }

    @Override
    public void onStop() {
        super.onStop();
//        QueryManager.getInstance().unregisterPictureListener(getContext(),mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        USB1ScanObserver.getInstance().detachObserver(usb1ScanStatusObserver);
        PictureListManager.getInstance().detachUSB1Observer(usb1ListObserver);
        USB1PictureDataSubject.getInstance().detachObserver(TAG);
    }

    @Override
    public void updateViewWithContent() {
        if (MediaKey.SUPPORT_SUBSECTION_LOADING){
            if (USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() == SearchType.SEARCHING){
                return;
            }

        }else if (USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() != SearchType.SEARCHED_HAVE_DATA){
            return;
        }

        if (styleType == STYLE_TYPE_ALL_PICTURE) {
            if (PictureListManager.getInstance().getAllUSB1PictureList() == null || PictureListManager.getInstance().getAllUSB1PictureList().size() < 1) {
                updateViewWithNoContent(false);
            } else {
                updateViewWithNoContent(true);
            }
        }else {
            if ((PictureListManager.getInstance().getAllUSB1PictureList() == null || PictureListManager.getInstance().getAllUSB1PictureList().size() < 1)
                && (PictureListManager.getInstance().getCurrentUSB1EmptyFolderList() == null || PictureListManager.getInstance().getCurrentUSB1EmptyFolderList().size() < 1)){
                updateViewWithNoContent(false);
            } else {
                updateViewWithNoContent(true);
            }
        }
    }

    @Override
    protected void updateCurrentPictureList(List<FileMessage> pictureList) {
        PictureListManager.getInstance().addCurrentUSB1PictureList(pictureList);
    }

    @Override
    protected void shouldUpdate() {
        shouldUpdate = USB1PictureDataSubject.getInstance().getUSB1PictureSearchType() != SearchType.SEARCHED_HAVE_DATA;
    }

    private Observer usb1ListObserver = new Observer() {
       @Override
       public void onUpdate() {
           Log.d(TAG,"usb1ListObserver onUpdate");
           myHandler.removeMessages(MSG_HANDLE_CLICK_ALL_PICTURE);
           myHandler.removeMessages(MSG_HANDLE_CLICK_SHOW_FOLDER);
           myHandler.removeMessages(MSG_CLICK_FOLDER);
           myHandler.removeMessages(MSG_CLICK_PREVIEW);
           myHandler.removeMessages(MSG_UPDATE_LIST);
           myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
       }
   };

   private Observer usb1SearchingTypeObserver = new Observer() {
       @Override
       public void onUpdate() {
           Log.d(TAG,"usb1SearchingTypeObserver onUpdate");
           myHandler.removeMessages(MSG_UPDATE_SEARCHING);
           myHandler.sendEmptyMessage(MSG_UPDATE_SEARCHING);
       }
   };


    private IScanObserver usb1ScanStatusObserver = new IScanObserver() {
        @Override
        public void onUpdate(int scanStatus) {
            Log.d(TAG,"usb1ScanStatusObserver onUpdate,scanStatus:"+scanStatus);
            myHandler.removeMessages(MSG_UPDATE_SCAN_STATUS);
            myHandler.sendEmptyMessage(MSG_UPDATE_SCAN_STATUS);
        }
    };

    @Override
    public boolean needUpdateWithUSBStatus(String path, boolean status) {
        Log.d(TAG,"needUpdateWithUSBStatus:"+ path);
        if (USBConstants.USBPath.USB0_PATH.equals(path)){
            return true;
        }
        return false;
    }
}
