package com.desaysv.mediacommonlib.bean;

/**
 * Created by uidp5370 on 2019-6-12.
 * 原因变化 Reason：打log的时候可以确定是什么原因导致的，Prority：优先级，判断当前命令能否冲掉前一个命令
 */

public class ChangeReason {

    private String Reason;
    private int Prority;

    public ChangeReason(String reason, int prority) {
        Reason = reason;
        Prority = prority;
    }

    public String getReason() {
        return Reason;
    }

    public int getPrority() {
        return Prority;
    }

    @Override
    public String toString() {
        return "ChangeReason{" +
                "Reason='" + Reason + '\'' +
                ", Prority=" + Prority +
                '}';
    }
}
