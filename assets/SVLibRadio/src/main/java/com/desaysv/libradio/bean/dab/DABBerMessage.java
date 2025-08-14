package com.desaysv.libradio.bean.dab;

/**
 * @author uidq1846
 * @desc 获取电台的BER信息
 * @time 2022-9-27 10:16
 */
public class DABBerMessage {
    private int berSig;
    private int berExp;

    public int getBerSig() {
        return berSig;
    }

    public void setBerSig(int berSig) {
        this.berSig = berSig;
    }

    public int getBerExp() {
        return berExp;
    }

    public void setBerExp(int berExp) {
        this.berExp = berExp;
    }
}
