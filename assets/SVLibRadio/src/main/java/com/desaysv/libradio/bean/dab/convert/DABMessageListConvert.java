package com.desaysv.libradio.bean.dab.convert;

import com.desaysv.libradio.bean.dab.convert.DABMessageConvert;

import java.util.List;

/**
 * created by ZNB on 2022-12-20
 * HAL回传的数据结构类型可能会变动
 * 因此增加一层转接头直接对接HAL的数据
 * 再把数据转成APP需要的类型
 * 举个栗子：
 * HAL给的数据结构是 int[]，但是应用要用的是 String，不想在用到的地方都去修改的话，做个数据转接层来保证数据结构的稳定性就很有必要
 * 如果HAL给的数据变动，那么只要修改转接层的处理就行，无须修改应用层繁多的引用
 */
public class DABMessageListConvert {
    private List<DABMessageConvert> dabStationList;

    public List<DABMessageConvert> getDabMessageList() {
        return dabStationList;
    }

    public void setDabMessageList(List<DABMessageConvert> dabStationList) {
        this.dabStationList = dabStationList;
    }
}
