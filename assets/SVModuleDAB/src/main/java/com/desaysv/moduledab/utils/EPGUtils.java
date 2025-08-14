package com.desaysv.moduledab.utils;

import android.util.Log;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.dab.DABTime;

import java.util.ArrayList;
import java.util.List;

public class EPGUtils {
    private static final String TAG = "EPGUtils";

    /**
     * 将完整的 EPG列表，按照日期划分，将不同日期显示在列表中，相同的只显示第一个
     * 因为UI的设计，这里只是用于显示日期
     * @param DABEPGScheduleList
     * @return
     */
    public static List<DABEPGSchedule> collectWithDate(List<DABEPGSchedule> DABEPGScheduleList){

        List<DABEPGSchedule> temp = new ArrayList<>();

        String year = "";
        String month = "";
        String date = "";

        for (DABEPGSchedule DABEPGSchedule : DABEPGScheduleList){
            if (year.equals(DABEPGSchedule.getYear()) && month.equals(DABEPGSchedule.getMonth()) && date.equals(DABEPGSchedule.getDay())){

            }else {
                year = DABEPGSchedule.getYear();
                month = DABEPGSchedule.getMonth();
                date = DABEPGSchedule.getDay();
                temp.add(DABEPGSchedule);
            }
        }

        return temp;
    }



    /**
     * 将完整的 EPG列表，按照日期划分，显示当前符合日期的项
     * @param DABEPGScheduleList
     * @return
     */
    public static List<DABEPGSchedule> filterWithDate(List<DABEPGSchedule> DABEPGScheduleList, DABEPGSchedule schedule){
        Log.d(TAG,"DABEPGScheduleList size:"+DABEPGScheduleList.size());
        List<DABEPGSchedule> temp = new ArrayList<>();
        for (DABEPGSchedule DABEPGSchedule : DABEPGScheduleList){
            if (schedule.getYear().equals(DABEPGSchedule.getYear()) && schedule.getMonth().equals(DABEPGSchedule.getMonth()) && schedule.getDay().equals(DABEPGSchedule.getDay())){
                temp.add(DABEPGSchedule);
            }
        }
        return temp;
    }


    /**
     * 根据点击的EPG预约信息，转换成DAB信息，达到打开对应DAB电台的目的
     * @param currentShowSubscribeEPG
     * @return
     */
    public static RadioMessage convertToDABMessage(DABEPGSchedule currentShowSubscribeEPG){

        DABMessage dabMessage = new DABMessage(currentShowSubscribeEPG.getFreq(),currentShowSubscribeEPG.getServiceId(),currentShowSubscribeEPG.getServiceComponentId());
        RadioMessage radioMessage = new RadioMessage(dabMessage);
        Log.d(TAG,"convertToDABMessage:"+radioMessage);
        return radioMessage;
    }


    /**
     * 根据传入的DAB时间，和 订阅的EPG列表比较，确认是否需要弹窗提示预约的节目已经到达
     * @param preShow 上一个显示内容
     * @return 返回应该显示的EPG订阅数据
     */
    public static DABEPGSchedule needShowReminder(DABEPGSchedule preShow, DABTime time){
        Log.d(TAG,"needShowReminder,DABTime:"+time);
        //使用一个新的List对象，避免ConcurrentModificationException异常
        List<DABEPGSchedule> allList = new ArrayList<>();
        allList.addAll(RadioList.getInstance().getEPGSubscribeList());


        for (DABEPGSchedule temp : allList){
            if (onTime(temp,time)){//如果存在预约点，那就继续
                //有预约弹窗出现，那就比较是不是同一个预约内容
                Log.d(TAG,"needShowReminder,onTime:"+time);
                if (temp.isHadShow()){//同一个预约内容，那就跳出

                }else {//不是同一个预约内容，那就重新赋值
                   return temp;
                }
            }
        }
        return null;
    }

    /**
     * 匹配预约列表项的时间和DAB时间是否满足弹窗要求
     * @param scheduleListDTO
     * @param time
     * @return
     */
    private static boolean onTime(DABEPGSchedule scheduleListDTO, DABTime time){
        Log.d(TAG,"onTime,scheduleListDTO:"+scheduleListDTO);
        if (Integer.parseInt(scheduleListDTO.getYear()) == (time.getYear())){
            if (Integer.parseInt(scheduleListDTO.getMonth()) == (time.getMonth())){
                if (Integer.parseInt(scheduleListDTO.getDay()) == (time.getDay())){
                    if (Integer.parseInt(scheduleListDTO.getHour()) == (time.getHour())){
                        if ((Integer.parseInt(scheduleListDTO.getMin()) - time.getMinute() <= 1) && (Integer.parseInt(scheduleListDTO.getMin()) - time.getMinute() >= -1)){//1min之内
                            return true;
                        }else {
                            Log.d(TAG,"onTime,Minutes not in range.");
                        }
                    }else {
                        Log.d(TAG,"onTime,Hour not equal.");
                    }
                }else {
                    Log.d(TAG,"onTime,Day not equal.");
                }
            }else {
                Log.d(TAG,"onTime,Month not equal.");
                //超过一个月?一周? 的订阅数据，应该自动删除，因为没有单独的 已经订阅列表，会导致数据不断增加
                if (Integer.parseInt(scheduleListDTO.getMonth()) < (time.getMonth())){
                    //这里需要注意是否会出现操作同一个 subscribeEPGList，导致的ConcurrentModificationException异常
                    //todo
//                    ModuleRadioTrigger.getInstance().mDabStatusTool.subscribeEPG(scheduleListDTO,false);
                }
            }
        }else {
            Log.d(TAG,"onTime,Year not equal.");
        }

        return false;
    }


    /**
     * EPG列表在订阅列表中匹配到对应的内容，则需要判断当前时间是否已经超过预约时间
     * @param DABEPGSchedule EPG列表项
     * @param temp 订阅列表项
     * @return
     */
    public static boolean timeOut(DABEPGSchedule DABEPGSchedule, DABEPGSchedule temp){
        Log.d(TAG,"timeOut, DABEPGSchedule:"+DABEPGSchedule);
        Log.d(TAG,"timeOut, temp:"+temp);
        if (Integer.parseInt(DABEPGSchedule.getYear()) < Integer.parseInt(temp.getYear())){
            return true;
        }else if (Integer.parseInt(DABEPGSchedule.getYear()) == Integer.parseInt(temp.getYear())){//年相等，继续往下比较
            if (Integer.parseInt(DABEPGSchedule.getMonth()) < Integer.parseInt(temp.getMonth())){
                return true;
            }else if (Integer.parseInt(DABEPGSchedule.getMonth()) == Integer.parseInt(temp.getMonth())){//月相等，继续往下比较
                if (Integer.parseInt(DABEPGSchedule.getDay()) < Integer.parseInt(temp.getDay())){
                    return true;
                }else if (Integer.parseInt(DABEPGSchedule.getDay()) == Integer.parseInt(temp.getDay())){//日相等，继续往下比较
                    if (Integer.parseInt(DABEPGSchedule.getHour()) < Integer.parseInt(temp.getHour())){
                        return true;
                    }else if (Integer.parseInt(DABEPGSchedule.getHour()) == Integer.parseInt(temp.getHour())){//时相等，继续往下比较
                        if (Integer.parseInt(DABEPGSchedule.getMin()) < Integer.parseInt(temp.getMin())){//只比较到分，因为dab时间秒都是0
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 根据EPG项和当前DAB时间进行比较，确认该预约节目是否已经超时
     * @param dabepgSchedule
     * @param dabTime
     * @return
     */
    public static boolean isOverDue(DABEPGSchedule dabepgSchedule, DABTime dabTime){
        Log.d(TAG,"isOverDue, DABEPGSchedule:"+dabepgSchedule);
        Log.d(TAG,"isOverDue, DABTime:"+dabTime);
        if (dabTime == null || dabTime.getState() == 0){//0 表示获取的DAB时间无效
            return false;
        }
        if (Integer.parseInt(dabepgSchedule.getYear()) < dabTime.getYear()){
            return true;
        }else if (Integer.parseInt(dabepgSchedule.getYear()) == dabTime.getYear()){//年相等，继续往下比较
            if (Integer.parseInt(dabepgSchedule.getMonth()) < dabTime.getMonth()){
                return true;
            }else if (Integer.parseInt(dabepgSchedule.getMonth()) == dabTime.getMonth()){//月相等，继续往下比较
                if (Integer.parseInt(dabepgSchedule.getDay()) < dabTime.getDay()){
                    return true;
                }else if (Integer.parseInt(dabepgSchedule.getDay()) == dabTime.getDay()){//日相等，继续往下比较
                    if (Integer.parseInt(dabepgSchedule.getHour()) < dabTime.getHour()){
                        return true;
                    }else if (Integer.parseInt(dabepgSchedule.getHour()) == dabTime.getHour()){//时相等，继续往下比较
                        if (Integer.parseInt(dabepgSchedule.getMin()) < dabTime.getMinute()){//只比较到分，因为dab时间秒都是0
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


}
