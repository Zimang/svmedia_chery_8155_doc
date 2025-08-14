package com.desaysv.moduledab.utils;

import com.desaysv.libradio.bean.RadioMessage;

/**
 * created by ZNB on 2022-10-17
 * 比较工具，用于相等、排序等的比较
 */
public class CompareUtils {

    /**
     * 比较两个 DAB RadioMessage 是否相等
     * @param source
     * @param target
     * @return
     */
    public static boolean isSameDAB(RadioMessage source, RadioMessage target){

        if (source == null || target == null){
            return false;
        }
        if (source.getDabMessage() == null || target.getDabMessage() == null){
            return false;
        }

        //先比较serviceID
        if (source.getDabMessage().getServiceId() == target.getDabMessage().getServiceId()){
            //再比较频点值
            if (source.getDabMessage().getFrequency() == target.getDabMessage().getFrequency()){
                //再比较组件ID，因为子电台和主电台前两者是一样的
                if (source.getDabMessage().getServiceComponentId() == target.getDabMessage().getServiceComponentId()){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }else {
            return false;
        }
    }



    /**
     * 比较两个 DAB Ensemble 是否相等
     * @param source
     * @param target
     * @return
     */
    public static boolean isSameDABEnsemble(RadioMessage source, RadioMessage target){

        if (source == null || target == null){
            return false;
        }
        if (source.getDabMessage() == null || target.getDabMessage() == null){
            return false;
        }

        //先比较serviceID
        if (source.getDabMessage().getEnsembleId() == target.getDabMessage().getEnsembleId()){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 比较两个 DAB programType 是否相等
     * @param source
     * @param target
     * @return
     */
    public static boolean isSameDABType(RadioMessage source, RadioMessage target){

        if (source == null || target == null){
            return false;
        }
        if (source.getDabMessage() == null || target.getDabMessage() == null){
            return false;
        }

        //先比较serviceID
        if (source.getDabMessage().getProgramType() == target.getDabMessage().getProgramType()){
            return true;
        }else {
            return false;
        }
    }

}
