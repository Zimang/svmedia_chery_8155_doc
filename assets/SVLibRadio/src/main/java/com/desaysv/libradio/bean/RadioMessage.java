package com.desaysv.libradio.bean;

import android.hardware.radio.RadioManager;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.rds.RDSRadioText;

import java.io.Serializable;

/**
 * @author LZM
 * @date 2019-7-9
 * Comment 收音信息的具体对象类，包括了收音的频段，频率，以及收藏状态
 */
public class RadioMessage implements Serializable {
    private static final String TAG = "RadioMessage";
    public static final int FM_AM_TYPE = 0;
    public static final int DAB_TYPE = 1;
    public static final int DAB_BAND = 10;
    //收音的频段 RadioManager.BAND_AM RadioManager.BAND_FM RadioManager.BAND_AM_HD RadioManager.BAND_FM_HD
    private int radioBand = RadioManager.BAND_AM;
    private int radioType = FM_AM_TYPE;
    private int radioFrequency = -1;
    private boolean isCollect = false;
    private DABMessage dabMessage;
    private RDSRadioText rdsRadioText;

    private boolean isTP = false;//是否为交通电台的标志

    private boolean isST = false;//是否为立体音的标志

    private String sortName;

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public RadioMessage() {
    }

    public RadioMessage(int radioBand, int radioFrequency) {
        Log.d(TAG, "RadioMessage: radioBand = " + radioBand + " radioFrequency = " + radioFrequency);
        this.radioBand = radioBand;
        setRadioType(FM_AM_TYPE);
        setRadioFrequency(radioFrequency);
    }

    public RadioMessage(int radioBand, int radioFrequency, boolean isCollect) {
        this(radioBand, radioFrequency, isCollect, false);
        Log.d(TAG, "RadioMessage: radioBand = " + radioBand + " radioFrequency = " + radioFrequency + " isCollect = " + isCollect);
    }

    public RadioMessage(int radioBand, int radioFrequency, boolean isCollect, boolean playByVr) {
        Log.d(TAG, "RadioMessage: radioBand = " + radioBand + " radioFrequency = " + radioFrequency);
        this.radioBand = radioBand;
        setRadioType(FM_AM_TYPE);
        if (playByVr) {
            this.radioFrequency = radioFrequency;
        } else {
            setRadioFrequency(radioFrequency);
        }
        this.isCollect = isCollect;
    }

    public RadioMessage(DABMessage dabMessage) {
        Log.d(TAG, "RadioMessage: dabMessage = " + dabMessage);
        setRadioType(DAB_TYPE);
        this.radioBand = DAB_BAND;
        setDabMessage(dabMessage);
    }

    public RadioMessage(DABMessage dabMessage, boolean isCollect) {
        Log.d(TAG, "RadioMessage: radioBand = " + radioBand + " radioFrequency = " + radioFrequency + " isCollect = " + isCollect);
        setRadioType(DAB_TYPE);
        this.isCollect = isCollect;
        this.radioBand = DAB_BAND;
        setDabMessage(dabMessage);
    }

    /**
     * 是否是收藏状态，这个收藏状态直接对比收藏列表
     *
     * @return true 收藏；false 不是收藏
     */
    public boolean isCollect() {
        return isCollect = RadioList.getInstance().checkIsCollect(this);
    }

    /**
     * add by lzm 加入一个add的方法，通过add的方式赋值，避免了对象的变化
     *
     * @param radioMessage radioMessage
     */
    public void cloneRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "cloneRadioMessage: radioMessage = " + radioMessage);
        this.radioBand = radioMessage.getRadioBand();
        this.radioFrequency = radioMessage.getRadioFrequency();
        this.isCollect = radioMessage.isCollect();
        setRadioType(radioMessage.getRadioType());
        setDabMessage(radioMessage.getDabMessage());
        setRdsRadioText(radioMessage.getRdsRadioText());
        setTP(radioMessage.isTP());
        setST(radioMessage.isST());
    }

    /**
     * 直接复制一个新的对象
     *
     * @return 新的RadioMessage对象
     */
    public RadioMessage Clone() {
        RadioMessage radioMessage = new RadioMessage(this.getRadioBand(), this.getRadioFrequency(), this.isCollect);
        radioMessage.setDabMessage(this.getDabMessage());
        radioMessage.setRadioType(this.getRadioType());
        radioMessage.setRdsRadioText(this.getRdsRadioText());
        radioMessage.setTP(this.isTP());
        radioMessage.setST(this.isST());
        return radioMessage;
    }

    /**
     * 直接复制一个新的对象
     *
     * @return 新的RadioMessage对象
     */
    public RadioMessage CloneSimply() {
        RadioMessage radioMessage = new RadioMessage(this.getRadioBand(), this.getRadioFrequency(), this.isCollect);
        radioMessage.setDabMessage(this.getDabMessage());
        radioMessage.setRadioType(this.getRadioType());
        radioMessage.setTP(this.isTP());
        radioMessage.setST(this.isST());
        return radioMessage;
    }

    public int getRadioBand() {
        return radioBand;
    }

    public void setRadioBand(int radioBand) {
        Log.d(TAG, "setRadioBand: radioBand = " + radioBand);
        this.radioBand = radioBand;
    }

    public int getRadioType() {
        return radioType;
    }

    public void setRadioType(int radioType) {
        this.radioType = radioType;
    }

    public int getRadioFrequency() {
        return radioFrequency;
    }

    /**
     * add by lzm 由于可能会出现设置频率异常，导致记忆异常，然后再导致收音打不开，然后异常了，所以在设置频率的时候，需要加入判断
     *
     * @param radioFrequency radioFrequency
     */
    public void setRadioFrequency(int radioFrequency) {
        Log.d(TAG, "setRadioFrequency: radioBand = " + radioBand + " radioFrequency = " + radioFrequency);
        switch (radioBand) {
            case RadioManager.BAND_AM:
                if (RadioConfig.AM_MIN <= radioFrequency && radioFrequency <= RadioConfig.AM_MAX) {
                    this.radioFrequency = radioFrequency;
                } else {
                    this.radioFrequency = RadioConfig.AM_MIN;
                }
                break;
            case RadioManager.BAND_FM:
                if (RadioConfig.FM_MIN <= radioFrequency && radioFrequency <= RadioConfig.FM_MAX) {
                    this.radioFrequency = radioFrequency;
                } else {
                    this.radioFrequency = RadioConfig.FM_MIN;
                }
                break;
        }
    }

    /**
     * 获取计算过的频率
     *
     * @return String frequency
     */
    public String getCalculateFrequency() {
        String frequency = "";
        if (radioBand == RadioManager.BAND_AM) {
            frequency = Integer.toString(radioFrequency);
        } else if (radioBand == RadioManager.BAND_FM) {
            frequency = ((float) radioFrequency) / 1000 + "";
        }
        Log.d(TAG, "getCalculateFrequency: frequency = " + frequency);
        return frequency;
    }


    /**
     * 获取计算过的频率和单位
     *
     * @return String 计算后的频率和单位
     */
    public String getCalculateFrequencyAndUnit() {
        /*String frequencyAndUnit = "";
        if (radioBand == RadioManager.BAND_AM) {
            frequencyAndUnit = radioFrequency + " KHz";
        } else if (radioBand == RadioManager.BAND_FM) {
            frequencyAndUnit = ((float) radioFrequency) / 1000 + " MHz";
        }
        Log.d(TAG, "getCalculateFrequencyAndUnit: frequencyAndUnit = " + frequencyAndUnit);*/
        if (radioType == FM_AM_TYPE) {
            return getCalculateFrequency();
        }else {
            return "";
        }
    }




    /**
     * 获取当前这个收音频段代表的是哪一个音源
     *
     * @return String 频段代表的音源
     */
    public String getRadioMessageSource() {
        String source = "";
        if (radioType == FM_AM_TYPE) {
            if (radioBand == RadioManager.BAND_AM) {
                source = DsvAudioSDKConstants.AM_SOURCE;
            } else {
                source = DsvAudioSDKConstants.FM_SOURCE;
            }
        }else {
            source = DsvAudioSDKConstants.DAB_SOURCE;
        }
        Log.d(TAG, "getRadioMessageSource: source = " + source);
        return source;
    }



    /**
     * 获取电台信息
     *
     * @return dabMessage
     */
    public DABMessage getDabMessage() {
        return dabMessage;
    }

    /**
     * 赋值DABMessage信息
     *
     * @param dabMessage DABMessage
     */
    public void setDabMessage(DABMessage dabMessage) {
        this.dabMessage = dabMessage;
    }


    public boolean isTP() {
        return isTP;
    }

    public void setTP(boolean TP) {
        isTP = TP;
    }

    public boolean isST() {
        return isST;
    }

    public void setST(boolean ST) {
        isST = ST;
    }

    /**
     * FM的文本信息
     *
     * @return rdsRadioText
     */
    public RDSRadioText getRdsRadioText() {
        return rdsRadioText;
    }

    /**
     * FM的文本信息
     *
     * @param rdsRadioText RDSRadioText
     */
    public void setRdsRadioText(RDSRadioText rdsRadioText) {
        this.rdsRadioText = rdsRadioText;
    }

    @Override
    public String toString() {
        return "RadioMessage{" +
                "radioBand=" + radioBand +
                ", radioType=" + radioType +
                ", radioFrequency=" + radioFrequency +
                ", dabMessage=" + dabMessage +
                '}';
    }



    /**
     * 用于获取电台名称，统一在此处理
     * @return
     */
    public String getRadioMessageName(){
        if (getRadioType() == RadioMessage.DAB_TYPE){
            if (getDabMessage() != null){
                return getDabMessage().getProgramStationName();
            }else {
                return "";
            }
        }else {
            if (getRadioBand() == RadioManager.BAND_FM){
                if (getRdsRadioText() != null){
                    if (getRdsRadioText().getProgramStationName() != null){
                        return getRdsRadioText().getProgramStationName();
                    }else {
                        return String.valueOf(getRadioFrequency());
                    }
                }else {
                    return String.valueOf(getRadioFrequency());
                }
            }else {
                return String.valueOf(getRadioFrequency());
            }
        }
    }
}
