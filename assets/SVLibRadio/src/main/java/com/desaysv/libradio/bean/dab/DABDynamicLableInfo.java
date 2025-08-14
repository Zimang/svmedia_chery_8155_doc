package com.desaysv.libradio.bean.dab;

import java.util.List;

/**
 * @author uidq1846
 * @desc DynamicLable信息 RT文本
 * @time 2022-10-10 15:22
 */
public class DABDynamicLableInfo {

    private int[] dynamicLabel;
    private List<DynamicPlusLabel> dynamicPlusLabelList;

    public int[] getDynamicLabel() {
        return dynamicLabel;
    }

    public void setDynamicLabel(int[] dynamicLabel) {
        this.dynamicLabel = dynamicLabel;
    }

    public List<DynamicPlusLabel> getDynamicPlusLabelList() {
        return dynamicPlusLabelList;
    }

    public void setDynamicPlusLabelList(List<DynamicPlusLabel> dynamicPlusLabelList) {
        this.dynamicPlusLabelList = dynamicPlusLabelList;
    }

    public static class DynamicPlusLabel {
        public int[] label;

        public int[] getLabel() {
            return label;
        }

        public void setLabel(int[] label) {
            this.label = label;
        }
    }
}
