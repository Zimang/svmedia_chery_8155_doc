package com.desaysv.libradio.bean.dab;

/**
 * @author uidq1846
 * @desc 配置进入头条的页面
 * @time 2022-9-26 19:43
 */
public class DABHeadlineAction {
    //请求类型
    private int action;
    //进入下一级页面菜单子项序号 ：0--31
    private int bodyOption;
    //数据服务id
    private int dataSid;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getBodyOption() {
        return bodyOption;
    }

    public void setBodyOption(int bodyOption) {
        this.bodyOption = bodyOption;
    }

    public int getDataSid() {
        return dataSid;
    }

    public void setDataSid(int dataSid) {
        this.dataSid = dataSid;
    }
}
