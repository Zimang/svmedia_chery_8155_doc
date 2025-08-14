package com.desaysv.libradio.bean.dab;

import java.util.List;

/**
 * @author uidq1846
 * @desc DAB电台时间显示列表
 * @time 2022-9-26 15:50
 */
public class DABEPGScheduleList {
    //服务ID
    private long serviceId;
    //EPG列表
    private List<DABEPGSchedule> epgScheduleList;

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public List<DABEPGSchedule> getEpgScheduleList() {
        return epgScheduleList;
    }

    public void setEpgScheduleList(List<DABEPGSchedule> epgScheduleList) {
        this.epgScheduleList = epgScheduleList;
    }
}
