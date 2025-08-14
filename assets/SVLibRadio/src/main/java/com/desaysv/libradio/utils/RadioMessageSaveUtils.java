package com.desaysv.libradio.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.radio.RadioManager;
import android.util.Log;

import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

/**
 * Created by LZM on 2019-8-6.
 * Comment 收音信息保存工具类
 */
public class RadioMessageSaveUtils {

    private static final String TAG = "RadioMessageSaveUtils";

    private static RadioMessageSaveUtils instance;

    public static RadioMessageSaveUtils getInstance() {
        if (instance == null) {
            synchronized (RadioMessageSaveUtils.class) {
                if (instance == null) {
                    instance = new RadioMessageSaveUtils();
                }
            }
        }
        return instance;
    }

    private SharedPreferences spRadio;
    private SharedPreferences.Editor edRadio;
    private Gson mGson;

    /**
     * 初始化，在RadioControlRegister里面已经实现了初始化
     *
     * @param context AppBase.mContext
     */
    @SuppressLint("CommitPrefEdits")
    public void initialize(Context context) {
        mGson = new Gson();
        spRadio = context.getSharedPreferences("spRadio", 0);
        edRadio = spRadio.edit();
    }

    /**
     * 保存收音的信息，方法里面会区分AM和FM，自己去自适应保存
     *
     * @param radioMessage 保存的收音信息
     */
    public void saveRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "saveRadioMessage: radioMessage = " + radioMessage);
        try {
            saveCurrentRadioMessage(radioMessage);
            if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
                saveDABRadioMessage(radioMessage);
                saveDABorFMRadioMessage(radioMessage);
            } else if (radioMessage.getRadioBand() == RadioManager.BAND_FM) {
                saveFMRadioMessage(radioMessage);
                saveDABorFMRadioMessage(radioMessage);
            } else if (radioMessage.getRadioBand() == RadioManager.BAND_AM) {
                saveAMRadioMessage(radioMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String CURRENT_RADIO_MESSAGE = "current_radio_message_obj";

    /**
     * 保存当前的收音信息
     *
     * @param radioMessage 当前的收音信息
     */
    private void saveCurrentRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "saveCurrentRadioMessage: radioMessage = " + radioMessage);
        saveObject(CURRENT_RADIO_MESSAGE, radioMessage);
    }

    /**
     * 获取当前的收音信息
     *
     * @return RadioMessage
     */
    public RadioMessage getCurrentRadioMessage() {
        //这一块收音默认可能需要根据收音区域的默认值修改
        RadioMessage radioMessage = getObject(CURRENT_RADIO_MESSAGE, RadioMessage.class);
        Log.d(TAG, "getCurrentRadioMessage: radioMessage = " + radioMessage);
        if (radioMessage == null) {
            radioMessage = new RadioMessage(RadioManager.BAND_FM, RadioConfig.FM_MIN);
        }
        return radioMessage;
    }

    private static final String AM_RADIO_MESSAGE = "am_radio_message_obj";

    /**
     * 保存AN的收音信息
     *
     * @param radioMessage 收音信息
     */
    private void saveAMRadioMessage(RadioMessage radioMessage) {
        saveObject(AM_RADIO_MESSAGE, radioMessage);
    }

    /**
     * 获取AM的收音信息
     *
     * @return RadioMessage
     */
    public RadioMessage getAMRadioMessage() {
        RadioMessage radioMessage = getObject(AM_RADIO_MESSAGE, RadioMessage.class);
        if (radioMessage == null) {
            //如果为null，则创建默认值
            radioMessage = new RadioMessage(RadioManager.BAND_AM, RadioConfig.AM_MIN);
        }
        Log.d(TAG, "getAMRadioMessage: radioMessage = " + radioMessage);
        return radioMessage;
    }

    private static final String FM_RADIO_MESSAGE = "fm_radio_message_obj";

    /**
     * 保存FM的收音信息
     *
     * @param radioMessage 收音信息
     */
    private void saveFMRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "saveFMRadioMessage: radioMessage = " + radioMessage);
        saveObject(FM_RADIO_MESSAGE, radioMessage);
    }

    /**
     * 获取FN的收音信息
     *
     * @return RadioMessage
     */
    public RadioMessage getFMRadioMessage() {
        RadioMessage radioMessage = getObject(FM_RADIO_MESSAGE, RadioMessage.class);
        if (radioMessage == null) {
            radioMessage = new RadioMessage(RadioManager.BAND_FM, RadioConfig.FM_MIN);
        }
        Log.d(TAG, "getFMRadioMessage: radioMessage = " + radioMessage);
        return radioMessage;
    }

    //初始化Tuner的时候，无法通过DAB来进行，需要记忆切换到DAB之前的是哪个FM/AM
    //这样才能打开
    public static final String PRE_RADIO_MESSAGE = "pre_radio_message_obj";

    public void savePreRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "savePreRadioMessage: radioMessage = " + radioMessage);
        try {
            saveObject(PRE_RADIO_MESSAGE, radioMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取FN的收音信息
     *
     * @return RadioMessage
     */
    public RadioMessage getPreRadioMessage() {
        RadioMessage radioMessage = getObject(PRE_RADIO_MESSAGE, RadioMessage.class);
        if (radioMessage == null) {
            radioMessage = new RadioMessage(RadioManager.BAND_FM, RadioConfig.FM_MIN);
        }
        Log.d(TAG, "getPreRadioMessage: radioMessage = " + radioMessage);
        return radioMessage;
    }


    private static final String DAB_RADIO_MESSAGE = "dab_radio_message_obj";

    /**
     * 保存DAB的收音信息
     *
     * @param radioMessage 收音信息
     */
    private void saveDABRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "saveDABRadioMessage: radioMessage = " + radioMessage);
        saveObject(DAB_RADIO_MESSAGE, radioMessage);
    }

    /**
     * 获取DAB的收音信息
     *
     * @return RadioMessage
     */
    public RadioMessage getDABRadioMessage() {
        RadioMessage radioMessage = getObject(DAB_RADIO_MESSAGE, RadioMessage.class);
        if (radioMessage == null) {
            DABMessage dabMessage = new DABMessage(174928, 0xD313, 0);
            radioMessage = new RadioMessage(dabMessage);
        }
        radioMessage.setRadioType(RadioMessage.DAB_TYPE);
        Log.d(TAG, "getDABRadioMessage: radioMessage = " + radioMessage);
        return radioMessage;
    }



    private static final String DAB_FM_RADIO_MESSAGE = "dab_fm_radio_message_obj";

    /**
     * 保存DAB/FM的收音信息
     *
     * @param radioMessage 收音信息
     */
    private void saveDABorFMRadioMessage(RadioMessage radioMessage) {
        saveObject(DAB_FM_RADIO_MESSAGE, radioMessage);
    }

    /**
     * 获取DAB/FM的收音信息
     *
     * @return RadioMessage
     */
    public RadioMessage getDABorFMRadioMessage() {
        RadioMessage radioMessage = getObject(DAB_FM_RADIO_MESSAGE, RadioMessage.class);
        if (radioMessage == null) {
            //如果为null，则创建默认值
            radioMessage = new RadioMessage(RadioManager.BAND_FM, RadioConfig.FM_MIN);
        }
        Log.d(TAG, "getDABorFMRadioMessage: radioMessage = " + radioMessage);
        return radioMessage;
    }





    /**
     * 存储对象到本地
     *
     * @param key    key
     * @param object object
     */
    private void saveObject(String key, Object object) {
        edRadio.putString(key, mGson.toJson(object)).apply();
        /*if (object instanceof Serializable) {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(object);//把对象写到流里
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                if (edRadio != null) {
                    edRadio.putString(key, temp).apply();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oos != null) {
                        oos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (baos != null) {
                        baos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new Exception("Object must implements Serializable");
        }*/
    }

    /**
     * 获取存储的对象
     *
     * @param key key
     * @return Object
     */
    @Nullable
    private <T> T getObject(String key, Class<T> classOfT) {
        String temp = spRadio.getString(key, null);
        if (null == temp) {
            return null;
        }
        Log.d(TAG, "getObject: key = " + key + "  temp = " + temp);
        return mGson.fromJson(temp, classOfT);

       /* Object readObject = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        String temp = "";
        try {
            if (spRadio != null) {
                temp = spRadio.getString(key, "");
            }
            bais = new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
            ois = new ObjectInputStream(bais);
            readObject = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return readObject;*/
    }
}
