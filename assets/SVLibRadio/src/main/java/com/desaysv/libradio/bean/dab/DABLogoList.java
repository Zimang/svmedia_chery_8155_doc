package com.desaysv.libradio.bean.dab;

import java.util.List;

/**
 * @author uidq4079
 * @desc 底层解析当前获取到的全部DAB logo，应用做对应保存，显示时从对应列表取出
 * @time 2024-02-06
 */
public class DABLogoList {
    //电台名称
    private List<DABLogo> dabLogoList;

    public List<DABLogo> getDabLogoList() {
        return dabLogoList;
    }

    public void setDabLogoList(List<DABLogo> dabLogoList) {
        this.dabLogoList = dabLogoList;
    }
}
