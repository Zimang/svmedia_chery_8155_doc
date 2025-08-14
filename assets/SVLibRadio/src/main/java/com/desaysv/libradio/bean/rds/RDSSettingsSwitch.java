package com.desaysv.libradio.bean.rds;

/**
 * @author uidq1846
 * @desc  RDS设置开关
 * @time 2022-9-26 14:02
 */
public class RDSSettingsSwitch {
    //开/关信息 0：关，1：开
    private int rds = -1;
    //开/关信息 0：关，1：开
    private int ta = -1;
    //开/关信息 0：关，1：开
    private int af = -1;
    //开/关信息 0：关，1：开
    private int pty = -1;
    //开/关信息 0：关，1：开
    private int reg = -1;
    //开/关信息 0：关，1：开
    private int eon = -1;

    public RDSSettingsSwitch(){}

    public int getRds() {
        return rds;
    }

    public void setRds(int rds) {
        this.rds = rds;
    }

    public int getTa() {
        return ta;
    }

    public void setTa(int ta) {
        this.ta = ta;
    }

    public int getAf() {
        return af;
    }

    public void setAf(int af) {
        this.af = af;
    }

    public int getPty() {
        return pty;
    }

    public void setPty(int pty) {
        this.pty = pty;
    }

    public int getReg() {
        return reg;
    }

    public void setReg(int reg) {
        this.reg = reg;
    }

    public int getEon() {
        return eon;
    }

    public void setEon(int eon) {
        this.eon = eon;
    }

    @Override
    public String toString() {
        return "RDSSettingsSwitch{" +
                "rds=" + rds +
                ", ta=" + ta +
                ", af=" + af +
                ", pty=" + pty +
                ", reg=" + reg +
                ", eon=" + eon +
                '}';
    }
}
