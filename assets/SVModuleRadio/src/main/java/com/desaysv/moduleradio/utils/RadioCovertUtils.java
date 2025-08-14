package com.desaysv.moduleradio.utils;

import android.content.Context;
import android.util.Log;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABLogo;
import com.desaysv.libradio.bean.rds.RDSRadioName;
import com.desaysv.libradio.utils.PinyinComparator;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleradio.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RadioCovertUtils {

    public static final String TAG = "RadioCovertUtils";

    /**
     * 将RDS的类型转为对应的字串
     * @param rdsType
     * @return
     */
    public static String changeTypeToString(Context context , int rdsType){
        Log.d(TAG,"changeTypeToString rdsType: "+ rdsType);
        String type = "";
        switch (rdsType){
            case 0:
                type = "---"/*context.getResources().getString(R.string.radio_rds_type_0)*/;//PQM说前方路试那边建议调整为Golf一致
                break;
            case 1:
                type = context.getResources().getString(R.string.radio_rds_type_1);
                break;
            case 2:
                type = context.getResources().getString(R.string.radio_rds_type_2);
                break;
            case 3:
                type = context.getResources().getString(R.string.radio_rds_type_3);
                break;
            case 4:
                type = context.getResources().getString(R.string.radio_rds_type_4);
                break;
            case 5:
                type = context.getResources().getString(R.string.radio_rds_type_5);
                break;
            case 6:
                type = context.getResources().getString(R.string.radio_rds_type_6);
                break;
            case 7:
                type = context.getResources().getString(R.string.radio_rds_type_7);
                break;
            case 8:
                type = context.getResources().getString(R.string.radio_rds_type_8);
                break;
            case 9:
                type = context.getResources().getString(R.string.radio_rds_type_9);
                break;
            case 10:
                type = context.getResources().getString(R.string.radio_rds_type_10);
                break;
            case 11:
                type = context.getResources().getString(R.string.radio_rds_type_11);
                break;
            case 12:
                type = context.getResources().getString(R.string.radio_rds_type_12);
                break;
            case 13:
                type = context.getResources().getString(R.string.radio_rds_type_13);
                break;
            case 14:
                type = context.getResources().getString(R.string.radio_rds_type_14);
                break;
            case 15:
                type = context.getResources().getString(R.string.radio_rds_type_15);
                break;
            case 16:
                type = context.getResources().getString(R.string.radio_rds_type_16);
                break;
            case 17:
                type = context.getResources().getString(R.string.radio_rds_type_17);
                break;
            case 18:
                type = context.getResources().getString(R.string.radio_rds_type_18);
                break;
            case 19:
                type = context.getResources().getString(R.string.radio_rds_type_19);
                break;
            case 20:
                type = context.getResources().getString(R.string.radio_rds_type_20);
                break;
            case 21:
                type = context.getResources().getString(R.string.radio_rds_type_21);
                break;
            case 22:
                type = context.getResources().getString(R.string.radio_rds_type_22);
                break;
            case 23:
                type = context.getResources().getString(R.string.radio_rds_type_23);
                break;
            case 24:
                type = context.getResources().getString(R.string.radio_rds_type_24);
                break;
            case 25:
                type = context.getResources().getString(R.string.radio_rds_type_25);
                break;
            case 26:
                type = context.getResources().getString(R.string.radio_rds_type_26);
                break;
            case 27:
                type = context.getResources().getString(R.string.radio_rds_type_27);
                break;
            case 28:
                type = context.getResources().getString(R.string.radio_rds_type_28);
                break;
            case 29:
                type = context.getResources().getString(R.string.radio_rds_type_29);
                break;
            case 30:
                type = context.getResources().getString(R.string.radio_rds_type_30);
                break;
            case 31:
                type = context.getResources().getString(R.string.radio_rds_type_31);
                break;
        }

        return type;
    }

    /**
     * 从组合列表返回搜索结果
     * @param searchText
     * @param isSearchCollect 是否在收藏列表里面搜索
     * @return
     */
    public static List<RadioMessage> getSearchResultList(String searchText, boolean isSearchCollect){
        Log.d(TAG,"getSearchResultList,: "+searchText);
        List<RadioMessage> resultList = new ArrayList<>();
        List<RadioMessage> searchOriList;
        if (isSearchCollect) {
            searchOriList = RadioList.getInstance().getCurrentCollectRadioMessageList();
        } else {
            searchOriList = RadioList.getInstance().getMultiRadioMessageList();
        }
        for (RadioMessage radioMessage : searchOriList){
            if (radioMessage.getDabMessage() != null){
                if (radioMessage.getDabMessage().getShortProgramStationName().toLowerCase().contains(searchText.toLowerCase())){
                    resultList.add(radioMessage);
                }
            }else {
                String listName = String.format(Locale.ENGLISH, AppBase.mContext.getResources().getString(R.string.radio_fm_item_title),radioMessage.getRadioFrequency() / 1000.0);
                Log.d(TAG,"getSearchResultList,listName: "+listName);
                if (listName != null && listName.toLowerCase().contains(searchText.toLowerCase())){
                    resultList.add(radioMessage);
                }else {
                    String rdsName = getOppositeRDSName(radioMessage);
                    if (rdsName != null && rdsName.toLowerCase().contains(searchText.toLowerCase())){
                        resultList.add(radioMessage);
                    }
                }
            }
        }
        Log.d(TAG,"getSearchResultList:"+resultList);
        return resultList;
    }

    public static String getOppositeRDSName(RadioMessage radioMessage){
        for (RDSRadioName rdsRadioName : RadioList.getInstance().getRdsRadioNameList()){
            if (radioMessage.getRadioFrequency() == rdsRadioName.getFrequency()){
                Log.d(TAG,"getOppositeRDSName has one: "+radioMessage.getRadioFrequency() + " freq");
                Log.d(TAG,"getOppositeRDSName has one: "+rdsRadioName.getProgramStationName() + " NAME");
                return rdsRadioName.getProgramStationName();
            }
        }
        Log.d(TAG,"getOppositeRDSName has none");
        return null;
    }

    public static byte[] getOppositeDABLogo(RadioMessage radioMessage){
        if (radioMessage.getDabMessage() != null) {
            for (DABLogo dabLogo : RadioList.getInstance().getDabLogoList()) {
                if (radioMessage.getDabMessage().getFrequency() == dabLogo.getFrequency() && radioMessage.getDabMessage().getServiceId() == dabLogo.getServiceId()) {
                    Log.d(TAG,"getOppositeDABLogo has one");
                    return dabLogo.getLogoDataList();
                }
            }
        }
        Log.d(TAG,"getOppositeDABLogo has null");
        return null;
    }

    public static List<RadioMessage> sortWithName(Context context, List<RadioMessage> currentList){
        List<RadioMessage> tempList = new ArrayList<>();
        for (RadioMessage radioMessage : currentList) {
            if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
                String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage);
                if (radioName == null || radioName.trim().length() < 1){
                    radioName = String.format(Locale.ENGLISH,context.getResources().getString(com.desaysv.moduleradio.R.string.radio_fm_item_title), radioMessage.getRadioFrequency() / 1000.0);
                }
                radioMessage.setSortName(radioName);
            }else {
                radioMessage.setSortName(radioMessage.getDabMessage().getProgramStationName());
            }
            tempList.add(radioMessage);
        }
        // 当前目录文件夹的排序
        Collections.sort(tempList, new Comparator<RadioMessage>() {
            Comparator<String> comparator = new PinyinComparator();

            @Override
            public int compare(RadioMessage o1, RadioMessage o2) {
                if (o1.getSortName() != null && o2.getSortName() != null && o1.getSortName().startsWith(context.getResources().getString(com.desaysv.moduleradio.R.string.radio_fm))
                    && o2.getSortName().startsWith(context.getResources().getString(com.desaysv.moduleradio.R.string.radio_fm))){
                        return o1.getRadioFrequency() > o2.getRadioFrequency() ? 1 : -1;
                }
                return (o1.getSortName() == null || o2.getSortName() == null) ? 0 : comparator.compare(o1.getSortName(), o2.getSortName());
            }
        });

        return tempList;
    }
}
