package com.desaysv.moduledab.utils;

import android.content.Context;
import android.util.Log;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduledab.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CtgUtils {
    private static final String TAG = "CtgUtils";

    /**
     * 将完整的 DAB列表，按照类别划分，将不同类别显示在列表中，相同的只显示第一个
     * 因为UI的设计，这里只是用于显示类型
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
     * 将完整的 DAB列表，按照类型划分，显示当前符合类型的项
     * @param allList
     * @param programType
     * @return
     */
    public static List<RadioMessage> filterWithProgramType(List<RadioMessage> allList,int programType){
        Log.d(TAG,"programType: "+ programType);
        Log.d(TAG,"allList size:"+allList.size());
        List<RadioMessage> temp = new ArrayList<>();
        for (RadioMessage radioMessage : allList){
            if (radioMessage.getDabMessage().getProgramType() == programType){
                temp.add(radioMessage);
            }
        }
        return temp;
    }



    /**
     * 将完整的 DAB列表，按照类型划分，显示当前符合类型的项的数量
     * @param allList
     * @param programType
     * @return
     */
    public static String getCountWithProgramType(List<RadioMessage> allList,int programType){
        Log.d(TAG,"programType: "+ programType);
        Log.d(TAG,"allList size:"+allList.size());
        int count = 0;
        for (RadioMessage radioMessage : allList){
            if (radioMessage.getDabMessage().getProgramType() == programType){
                count++;
            }
        }
        return "("+count+")";
    }


    /**
     * 根据DAB-Json协议文档，将类型值转换为对应的类型名
     * @param context
     * @param programType
     * @return
     */
    public static String changeTypeToString(Context context , int programType){
        Log.d(TAG,"changeTypeToString programType: "+ programType);
        String type = "";
        switch (programType){
            case 0:
                type = "---";
                break;
            case 1:
                type = context.getResources().getString(R.string.program_type_1);
                break;
            case 2:
                type = context.getResources().getString(R.string.program_type_2);
                break;
            case 3:
                type = context.getResources().getString(R.string.program_type_3);
                break;
            case 4:
                type = context.getResources().getString(R.string.program_type_4);
                break;
            case 5:
                type = context.getResources().getString(R.string.program_type_5);
                break;
            case 6:
                type = context.getResources().getString(R.string.program_type_6);
                break;
            case 7:
                type = context.getResources().getString(R.string.program_type_7);
                break;
            case 8:
                type = context.getResources().getString(R.string.program_type_8);
                break;
            case 9:
                type = context.getResources().getString(R.string.program_type_9);
                break;
            case 10:
                type = context.getResources().getString(R.string.program_type_10);
                break;
            case 11:
                type = context.getResources().getString(R.string.program_type_11);
                break;
            case 12:
                type = context.getResources().getString(R.string.program_type_12);
                break;
            case 13:
                type = context.getResources().getString(R.string.program_type_13);
                break;
            case 14:
                type = context.getResources().getString(R.string.program_type_14);
                break;
            case 15:
                type = context.getResources().getString(R.string.program_type_15);
                break;
            case 16:
                type = context.getResources().getString(R.string.program_type_16);
                break;
            case 17:
                type = context.getResources().getString(R.string.program_type_17);
                break;
            case 18:
                type = context.getResources().getString(R.string.program_type_18);
                break;
            case 19:
                type = context.getResources().getString(R.string.program_type_19);
                break;
            case 20:
                type = context.getResources().getString(R.string.program_type_20);
                break;
            case 21:
                type = context.getResources().getString(R.string.program_type_21);
                break;
            case 22:
                type = context.getResources().getString(R.string.program_type_22);
                break;
            case 23:
                type = context.getResources().getString(R.string.program_type_23);
                break;
            case 24:
                type = context.getResources().getString(R.string.program_type_24);
                break;
            case 25:
                type = context.getResources().getString(R.string.program_type_25);
                break;
            case 26:
                type = context.getResources().getString(R.string.program_type_26);
                break;
            case 27:
                type = context.getResources().getString(R.string.program_type_27);
                break;
            case 28:
                type = context.getResources().getString(R.string.program_type_28);
                break;
            case 29:
                type = context.getResources().getString(R.string.program_type_29);
                break;
            case 30:
                type = context.getResources().getString(R.string.program_type_30);
                break;
            case 31:
                type = context.getResources().getString(R.string.program_type_31);
                break;
        }

        return type;

    }

}
