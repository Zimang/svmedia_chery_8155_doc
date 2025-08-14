package com.desaysv.libradio.bean.rds;

/**
 * @author uidq1846
 * @desc 底层通知上层RDS的状态标志的信息
 * @time 2022-10-11 13:50
 */
public class RDSFlagInfo {
    /**
     * 频点信息
     */
    private int frequency;

    /**
     * af
     */
    private int af;

    /**
     * tp
     */
    private int tp;

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getAf() {
        return af;
    }

    public void setAf(int af) {
        this.af = af;
    }

    public int getTp() {
        return tp;
    }

    public void setTp(int tp) {
        this.tp = tp;
    }

    @Override
    public String toString() {
        return "RDSFlagInfo{" +
                "frequency=" + frequency +
                ", af=" + af +
                ", tp=" + tp +
                '}';
    }
}
