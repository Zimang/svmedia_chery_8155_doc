package com.desaysv.moduledab.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABLogo;
import com.desaysv.moduledab.common.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * created by ZNB on 2022-12-17
 * 用来根据某些规则拆分列表
 */
public class ListUtils {

    public static final String TAG = "ListUtils";


    /**
     * 将完整的 DAB列表，按照类别分类划分，将不同类别显示在列表中，相同的只显示第一个
     * @param allList
     * @return
     */
    public static List<RadioMessage> collectWithProgramType(List<RadioMessage> allList){
        List<RadioMessage> temp = new ArrayList<>();
        HashMap<Integer ,Integer> ctgMap = new HashMap<>();

        for (RadioMessage radioMessage : allList){
            if (!ctgMap.containsKey(radioMessage.getDabMessage().getProgramType())){
                ctgMap.put(radioMessage.getDabMessage().getProgramType(),radioMessage.getDabMessage().getProgramType());
                temp.add(radioMessage);
            }
        }
        return temp;
    }



    /**
     * 将完整的 DAB列表，按照集合分类划分，将不同集合显示在列表中，相同的只显示第一个
     * @param allList
     * @return
     */
    public static List<RadioMessage> collectWithEnsemble(List<RadioMessage> allList){
        List<RadioMessage> temp = new ArrayList<>();
        HashMap<Integer ,Integer> ctgMap = new HashMap<>();
        for (RadioMessage radioMessage : allList){
            if (!ctgMap.containsKey(radioMessage.getDabMessage().getEnsembleId())){
                ctgMap.put(radioMessage.getDabMessage().getEnsembleId(),radioMessage.getDabMessage().getEnsembleId());
                temp.add(radioMessage);
            }
        }
        return temp;
    }



    /**
     * 将完整的 DAB列表，按照类别分类划分，显示当前符合类别的项
     * @param allList
     * @param programType
     * @return
     */
    public static List<RadioMessage> filterWithProgramType(List<RadioMessage> allList, int programType){
        Log.d(TAG,"filterWithProgramType，programType: "+ programType + "allList size:"+allList.size());
        List<RadioMessage> temp = new ArrayList<>();
        for (RadioMessage radioMessage : allList){
            if (radioMessage.getDabMessage().getProgramType() == programType){
                temp.add(radioMessage);
            }
        }
        return temp;
    }




    /**
     * 将完整的 DAB列表，按照集合分类划分，显示当前符合类别的项
     * @param allList
     * @param ensembleId
     * @return
     */
    public static List<RadioMessage> filterWithEnsembleId(List<RadioMessage> allList, int ensembleId){
        Log.d(TAG,"filterWithEnsembleId，ensembleId: "+ ensembleId + "allList size:"+allList.size());
        List<RadioMessage> temp = new ArrayList<>();
        for (RadioMessage radioMessage : allList){
            if (radioMessage.getDabMessage().getEnsembleId() == ensembleId){
                temp.add(radioMessage);
            }
        }
        return temp;
    }

    /**
     * 保存当前播放列表的类型 和参数
     * @param listType，列表类型
     * @param tag，ensembleId or programType
     */
    public static void savePlayListTag(Context context, int listType, int tag){
        Log.d(TAG,"savePlayListTag,listType:"+listType + ",tag:"+tag);
        SharedPreferences spDABPlayList = context.getSharedPreferences(Constant.SP_PLAYLIST_NAME,MODE_PRIVATE);
        SharedPreferences.Editor edDABPlayList = spDABPlayList.edit();
        edDABPlayList.putInt(Constant.SP_PLAYLIST_TYPE_KEY,listType);
        edDABPlayList.putInt(Constant.SP_PLAYLIST_TAG_KEY,tag);
        edDABPlayList.commit();
    }

    /**
     * 根据保存的Key，从完整的列表中拆分出符合条件的播放列表
     * @param context
     * @return
     */
    public static List<RadioMessage> getCurrentPlayList(Context context){
        SharedPreferences spDABPlayList = context.getSharedPreferences(Constant.SP_PLAYLIST_NAME,MODE_PRIVATE);

        int playListType = spDABPlayList.getInt(Constant.SP_PLAYLIST_TYPE_KEY,Constant.LIST_TYPE_ALL);
        int playListTag = spDABPlayList.getInt(Constant.SP_PLAYLIST_TAG_KEY,Constant.LIST_TYPE_ALL);

        Log.d(TAG,"getCurrentPlayList,playListType:"+playListType+",playListTag:"+playListTag);

        if (playListType == Constant.LIST_TYPE_ALL){
            List<RadioMessage> temp = new ArrayList<>(RadioList.getInstance().getDABEffectRadioMessageList());
            return temp;
        }else if (playListType == Constant.LIST_TYPE_COLLECT){
            List<RadioMessage> temp = new ArrayList<>(RadioList.getInstance().getDABCollectRadioMessageList());
            return temp;
        }else if (playListType == Constant.LIST_TYPE_ENSEMBLE){
            List<RadioMessage> temp = new ArrayList<>();
            for (RadioMessage radioMessage : RadioList.getInstance().getDABEffectRadioMessageList()){
                if (radioMessage.getDabMessage().getEnsembleId() == playListTag){
                    temp.add(radioMessage);
                }
            }
            return temp;
        }else if (playListType == Constant.LIST_TYPE_CATEGORY){
            List<RadioMessage> temp = new ArrayList<>();
            for (RadioMessage radioMessage : RadioList.getInstance().getDABEffectRadioMessageList()){
                if (radioMessage.getDabMessage().getProgramType() == playListTag){
                    temp.add(radioMessage);
                }
            }
            return temp;
        }

        return RadioList.getInstance().getDABEffectRadioMessageList();
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
}
