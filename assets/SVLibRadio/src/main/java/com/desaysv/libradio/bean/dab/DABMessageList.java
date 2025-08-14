package com.desaysv.libradio.bean.dab;

import java.util.List;

/**
 * @author uidq1846
 * @desc DAB电台信息列表
 * @time 2022-9-29 20:14
 */
public class DABMessageList {
    private List<DABMessage> dabMessageList;

    public List<DABMessage> getDabMessageList() {
        return dabMessageList;
    }

    public void setDabMessageList(List<DABMessage> dabMessageList) {
        this.dabMessageList = dabMessageList;
    }
}
