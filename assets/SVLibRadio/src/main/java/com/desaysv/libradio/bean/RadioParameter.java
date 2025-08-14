package com.desaysv.libradio.bean;

/**
 * 设置/获取底层搜台条件参数
 */
public class RadioParameter {


    public RadioParameter() {
    }

    //FM 6个参数
    private int level;
    private int nsn;
    private int wam;
    private int offset;
    private int bdw;
    private int mode;

    //DAB 1个参数
    private int Qlevel;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getNsn() {
        return nsn;
    }

    public void setNsn(int nsn) {
        this.nsn = nsn;
    }

    public int getWam() {
        return wam;
    }

    public void setWam(int wam) {
        this.wam = wam;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getBdw() {
        return bdw;
    }

    public void setBdw(int bdw) {
        this.bdw = bdw;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getQlevel() {
        return Qlevel;
    }

    public void setQlevel(int qlevel) {
        Qlevel = qlevel;
    }

    @Override
    public String toString() {
        return "RadioParameter{" +
                "level=" + level +
                ", nsn=" + nsn +
                ", wam=" + wam +
                ", offset=" + offset +
                ", bdw=" + bdw +
                ", mode=" + mode +
                ", Qlevel=" + Qlevel +
                '}';
    }
}
