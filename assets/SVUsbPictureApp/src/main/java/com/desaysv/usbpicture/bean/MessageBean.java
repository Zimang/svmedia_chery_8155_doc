package com.desaysv.usbpicture.bean;

/**
 * Created by zhaoqingjing on 2019-11-05.
 * Comment :USB音乐的app
 */
public class MessageBean {
    private int Type;
    private boolean flag;
    private String folderName;
    private int status ;
    private boolean clickAction;
    public static final int FILE_OR_DIRECTORY_MODE = 1;
    public static final int TAB_SWITCH_BACK_BUTTON = 2;
    public static final int CLICK_BACK_BUTTON = 3;
    public static final int CURRENT_FOLDER_NAME = 4;
    public static final int PICTURE_USB_1_DEVICE_CONNECT_STATUS = 5;
    public static final int PICTURE_USB_2_DEVICE_CONNECT_STATUS = 6;


    public static final int USB_DEVICE_CONNECTED = 7;
    public static final int USB_DEVICE_DISCONNECT = 8;
    public static final int BACK_TO_MAIN_ACTIVITY = 9;

    public static final int CAN_NOT_ENLARGE_PHOTO = 9;
    public static final int CAN_NOT_NARROW_PHOTO = 10;
    public static final int USB1_NO_CONTENT = 11;
    public static final int USB2_NO_CONTENT = 12;
    public static final int USB1_HAS_CONTENT = 13;
    public static final int USB2_HAS_CONTENT = 14;
    public static final int GO_TO_PREVIEW = 15;

    public MessageBean(int type, boolean flag) {
        Type = type;
        this.flag = flag;
    }

    public MessageBean(boolean isClick) {
        clickAction = isClick;
    }

    public boolean isClickAction() {
        return clickAction;
    }

    public void setClickAction(boolean clickAction) {
        this.clickAction = clickAction;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public MessageBean(int type, boolean flag, String folderName) {
        Type = type;
        this.flag = flag;
        this.folderName = folderName;
    }

    public MessageBean(int type, String folderName) {
        Type = type;
        this.folderName = folderName;
    }

    public MessageBean(int type, int status){
        Type =type;
        this.status = status;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getStatus(){
        return status;
    }
}
