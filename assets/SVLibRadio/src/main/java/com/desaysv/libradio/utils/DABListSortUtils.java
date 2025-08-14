package com.desaysv.libradio.utils;

import android.content.Context;
import android.util.Log;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.base.AppBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DABListSortUtils {
    private static final String TAG = "DABListSortUtils";

    //Alpha/Ensemble 对应的位置
    public static final int DAB_ALPHABETICAL = 14;// 按照字母排序 DAB列表
    public static final int DAB_ENSEMBLE = 15;// 按照集合排序 DAB列表

    /**
     * 根据DAB电台名字，按字母排序
     * @param originalList
     * @return
     */
    public static List<RadioMessage> sortWithAlphabetical(List<RadioMessage> originalList) {
        if (originalList != null) {
            // 当前目录文件夹的排序
            originalList.sort(new Comparator<RadioMessage>() {
                Comparator<String> comparator = new PinyinComparator();

                @Override
                public int compare(RadioMessage o1, RadioMessage o2) {
                    return comparator.compare(o1.getDabMessage().getProgramStationName(), o2.getDabMessage().getProgramStationName());
                }
            });
        }

        return originalList;
    }

    /**
     * 根据DAB电台集合，分类排序
     * @param originalList
     * @return
     */
    public static List<RadioMessage> sortWithEnsemble(List<RadioMessage> originalList) {
        if (originalList != null) {
            // 根据原始数据，按照集合排序
            originalList.sort(new Comparator<RadioMessage>() {
                Comparator<String> comparator = new PinyinComparator();

                @Override
                public int compare(RadioMessage o1, RadioMessage o2) {
                    int result = comparator.compare(o1.getDabMessage().getEnsembleLabel(), o2.getDabMessage().getEnsembleLabel());

                    if (result == 0) {//如果相同集合，那么按照名称字母排序
                        result = comparator.compare(o1.getDabMessage().getProgramStationName(), o2.getDabMessage().getProgramStationName());
                    }

                    return result;
                }
            });
            return originalList;
        }
        return originalList;
    }


    /**
     * 根据DAB电台集合，分类排序并且返回插入集合分类的数据
     * @param originalList
     * @return
     */
    public static List<RadioMessage> sortWithEnsembleAndReturn(List<RadioMessage> originalList) {
        if (originalList != null) {
            //用一个temp来处理，是为了避免数据变化时，操作同一个对象，会导致ConstructException异常
            List<RadioMessage> temp = new ArrayList<>(originalList);
            // 根据原始数据，按照集合排序
            temp.sort(new Comparator<RadioMessage>() {
                Comparator<String> comparator = new PinyinComparator();

                @Override
                public int compare(RadioMessage o1, RadioMessage o2) {
                    int result = comparator.compare(o1.getDabMessage().getEnsembleLabel(), o2.getDabMessage().getEnsembleLabel());

                    if (result == 0) {//如果相同集合，那么按照名称字母排序
                        result = comparator.compare(o1.getDabMessage().getProgramStationName(), o2.getDabMessage().getProgramStationName());
                    }

                    return result;
                }
            });

            String preEnsembleLabel = "";
            List<Integer> positions = new ArrayList<>();
            List<String> ensembleLabels = new ArrayList<>();

            // 根据排序后的列表，获取集合分类的分界处的位置
            for (int i = 0; i < temp.size(); i++) {
                if (!preEnsembleLabel.equals(temp.get(i).getDabMessage().getEnsembleLabel())) {
                    preEnsembleLabel = temp.get(i).getDabMessage().getEnsembleLabel();
                    ensembleLabels.add(preEnsembleLabel);
                    positions.add(i);
                }
            }
            // 在集合分类的分界处，插入一个 集合类型 的数据
            // 最开始的位置，插入一个 集合类型 的数据
            for (Integer integer : positions) {
               RadioMessage RadioMessage = new RadioMessage();
               RadioMessage.getDabMessage().setProgramType(-1000);//用 -1000 表示是集合类型即可
               RadioMessage.getDabMessage().setEnsembleLabel(ensembleLabels.get(integer));
               temp.add(integer,RadioMessage);
            }

            return temp;
        }

        return originalList;
    }





    /**
     * 根据DAB电台集合，获取分类排序列表
     * 注意这里返回的只是集合类型的数据，不是真的电台类型数据
     * @param originalList
     * @return
     */
    public static List<RadioMessage> getEnsembleAndReturn(List<RadioMessage> originalList) {
        if (originalList != null) {
            //用一个temp来处理，是为了避免数据变化时，操作同一个对象，会导致ConstructException异常
            List<RadioMessage> temp = new ArrayList<>(originalList);

            List<RadioMessage> tempEnsembleLabel = new ArrayList<>();// 集合分类列表
            // 根据原始数据，获取集合分类列表
            HashMap<String ,RadioMessage> ensembleLabelMap = new HashMap<>();
            for (RadioMessage dabMessageData: temp){
                    if (!ensembleLabelMap.containsKey(dabMessageData.getDabMessage().getEnsembleLabel())){
                        RadioMessage RadioMessage = new RadioMessage();
                        RadioMessage.cloneRadioMessage(dabMessageData);
                        RadioMessage.getDabMessage().setProgramType(-1000);//用 -1000 表示是集合类型即可
                        RadioMessage.getDabMessage().setFrequency(1);
                        ensembleLabelMap.put(RadioMessage.getDabMessage().getEnsembleLabel(),RadioMessage);
                        tempEnsembleLabel.add(RadioMessage);
                    }else {
                        if (ensembleLabelMap.get(dabMessageData.getDabMessage().getEnsembleLabel()) != null) {
                            ensembleLabelMap.get(dabMessageData.getDabMessage().getEnsembleLabel()).getDabMessage().setFrequency(ensembleLabelMap.get(dabMessageData.getDabMessage().getEnsembleLabel()).getDabMessage().getFrequency() + 1);
                        }
                    }
            }


            // 根据集合列表，按照集合排序
            tempEnsembleLabel.sort(new Comparator<RadioMessage>() {
                Comparator<String> comparator = new PinyinComparator();

                @Override
                public int compare(RadioMessage o1, RadioMessage o2) {
                    int result = comparator.compare(o1.getDabMessage().getEnsembleLabel(), o2.getDabMessage().getEnsembleLabel());

                    if (result == 0) {//如果相同集合，那么按照名称字母排序
                        result = comparator.compare(o1.getDabMessage().getProgramStationName(), o2.getDabMessage().getProgramStationName());
                    }

                    return result;
                }
            });

        return tempEnsembleLabel;
    }
        return null;
    }


    /**
     * 根据DAB电台集合分类排序列表，获取对应的播放列表
     * @param originalList
     * @return
     */
    public static List<RadioMessage> getEffectListWithEnsemble(List<RadioMessage> originalList,RadioMessage ensembleData) {
        if (originalList != null && ensembleData != null) {
            //用一个temp来处理，是为了避免数据变化时，操作同一个对象，会导致ConstructException异常
            List<RadioMessage> temporiginalList = new ArrayList<>(originalList);

            List<RadioMessage> ensembleDataList = new ArrayList<>();

            // 根据总的有效列表，获取对应的集合的播放列表

            for (RadioMessage dabMessageData : temporiginalList){
                if (dabMessageData.getDabMessage().getEnsembleLabel() != null && dabMessageData.getDabMessage().getEnsembleLabel().equals(ensembleData.getDabMessage().getEnsembleLabel())){
                    ensembleDataList.add(dabMessageData);
                }
            }


            return ensembleDataList;
        }
        return null;
    }


    /**
     * 根据当前播放的 DAB，获取上一个集合列表的第一个数据
     * 这个是硬按键的 长按 pre 操作
     * 可以参考 RadioDabControlTool的 pre() 方法
     * @param currentData
     * @return
     */
    public static RadioMessage getPreEnsembleFirstData(RadioMessage currentData){
        Log.d(TAG,"getPreEnsembleFirstData,currentData: " + currentData);

        //拿到当前的集合列表
        List<RadioMessage> ensembleList = getEnsembleAndReturn(RadioList.getInstance().getDABEffectRadioMessageList());

        Log.d(TAG,"getPreEnsembleFirstData,ensembleList size: " + ensembleList.size());
        //拿到当前播放电台对应的集合
        String ensemble = currentData.getDabMessage().getEnsembleLabel();

        //根据集合列表，拿到当前集合的位置
        int position = -1;
        for (int i = 0; i < ensembleList.size(); i ++){
            if (ensemble != null && ensemble.equals(ensembleList.get(i).getDabMessage().getEnsembleLabel())){
                position = i;
            }
        }

        Log.d(TAG,"getPreEnsembleFirstData,current position: " + position);

        if (position == -1){
            return null;
        }
        if (position == 0){
            position = ensembleList.size() - 1;
        }else {
            position = position - 1;
        }
        //根据位置，拿到上一个集合
        RadioMessage preEnsemble = ensembleList.get(position);

        //根据上一个集合，返回对应集合里面列表的第一个数据
        List<RadioMessage> currentEffectList = getEffectListWithEnsemble(RadioList.getInstance().getDABEffectRadioMessageList(),preEnsemble);
        RadioMessage preData = currentEffectList != null ? currentEffectList.get(0) : null;

        Log.d(TAG,"getPreEnsembleFirstData,preData: " + preData);

        return preData;
    }


    /**
     * 根据当前播放的 DAB，获取下一个集合列表的第一个数据
     * 这个是硬按键的 长按 next 操作
     * 可以参考 RadioDabControlTool的 next() 方法
     * @param currentData
     * @return
     */
    public static RadioMessage getNextEnsembleFirstData(RadioMessage currentData){
        Log.d(TAG,"getNextEnsembleFirstData,currentData: " + currentData);

        //拿到当前的集合列表
        List<RadioMessage> ensembleList = getEnsembleAndReturn(RadioList.getInstance().getDABEffectRadioMessageList());

        Log.d(TAG,"getNextEnsembleFirstData,ensembleList size: " + ensembleList.size());

        //拿到当前播放电台对应的集合
        String ensemble = currentData.getDabMessage().getEnsembleLabel();

        //根据集合列表，拿到上一个集合的位置
        int position = -1;
        for (int i = 0; i < ensembleList.size(); i ++){
            if (ensemble != null && ensemble.equals(ensembleList.get(i).getDabMessage().getEnsembleLabel())){
                position = i;
            }
        }

        Log.d(TAG,"getNextEnsembleFirstData,current position: " + position);

        if (position == -1){
            return null;
        }
        if (position == ensembleList.size() -1){
            position = 0;
        }else {
            position = position + 1;
        }
        //根据位置，拿到上一个集合
        RadioMessage nextEnsemble = ensembleList.get(position);

        //根据上一个集合，返回对应集合里面列表的第一个数据
        List<RadioMessage> currentEffectList = getEffectListWithEnsemble(RadioList.getInstance().getDABEffectRadioMessageList(),nextEnsemble);
        RadioMessage nextData = currentEffectList != null ? currentEffectList.get(0) : null;

        Log.d(TAG,"getNextEnsembleFirstData,preData: " + nextData);

        return nextData;
    }

    /**
     * 提供统一的方式获取排序类型
     * 按照 最新的设计，默认排序需要是 Ensemble
     * 所以只需要判断当前是不是 ALPHABETICAL 排序即可
     * @return
     */
    public static boolean isSortWithEnsemble(){
        int sortType = AppBase.mContext.getSharedPreferences("dab_rds", Context.MODE_PRIVATE).getInt(String.valueOf(DABListSortUtils.DAB_ALPHABETICAL),0);
        Log.d(TAG, "isSortWithEnsemble: sortType = " + sortType);

        return sortType != 1;
    }

}
