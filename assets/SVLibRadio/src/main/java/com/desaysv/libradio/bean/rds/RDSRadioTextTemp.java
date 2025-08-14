package com.desaysv.libradio.bean.rds;

/**
 * @author uidq1846
 * @desc RDS电台携带的信息（中转，字符编码问题）
 * @time 2022-9-22 19:52
 */
public class RDSRadioTextTemp {
    /**
     * 频点值
     */
    private int frequency = -1;

    /**
     * 电台名称
     */
    private int[] programStationName = null;

    /**
     * 电台节目的类型，具体数值对应的类型定义见XXX说明
     */
    private int programType = -1;

    /**
     * 电台节目的文本介绍信息
     */
    private int[] radioText = null;

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int[] getProgramStationName() {
        return programStationName;
    }

    public void setProgramStationName(int[] programStationName) {
        this.programStationName = programStationName;
    }

    public int getProgramType() {
        return programType;
    }

    public void setProgramType(int programType) {
        this.programType = programType;
    }

    public int[] getRadioText() {
        return radioText;
    }

    public void setRadioText(int[] radioText) {
        this.radioText = radioText;
    }
}
