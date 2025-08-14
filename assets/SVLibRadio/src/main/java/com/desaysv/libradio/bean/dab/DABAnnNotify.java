package com.desaysv.libradio.bean.dab;

/**
 * @author uidq1846
 * @desc Ann的提示通知
 * @time 2022-10-10 14:03
 */
public class DABAnnNotify {
    /**
     * 显示状态 int	0：hide 1：show
     */
    private int status;

    /**
     * 通知类型 int 0:无效; 1:alarm; 2:traffic; 3:other;
     */
    private int announceType;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAnnounceType() {
        return announceType;
    }

    public void setAnnounceType(int announceType) {
        this.announceType = announceType;
    }

    public DABAnnNotify() {
    }
}
