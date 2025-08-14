package com.desaysv.libradio.bean.rds;

import java.util.List;

/**
 * @author uidq4079
 * @desc 底层解析当前获取到的全部RDS名称，应用做对应保存，显示时从对应列表取出
 * @time 2024-02-06
 */
public class RDSRadioNameList{
    //电台名称
    private List<RDSRadioName> fmStationList;


    public List<RDSRadioName> getFmStationList() {
        return fmStationList;
    }

    public void setFmStationList(List<RDSRadioName> fmStationList) {
        this.fmStationList = fmStationList;
    }
}
