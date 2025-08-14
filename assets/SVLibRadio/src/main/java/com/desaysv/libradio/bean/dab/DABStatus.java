package com.desaysv.libradio.bean.dab;

public class DABStatus {

    private int seek_scan_status = -1;

    public int getSeek_scan_status() {
        return seek_scan_status;
    }

    public void setSeek_scan_status(int seek_scan_status) {
        this.seek_scan_status = seek_scan_status;
    }

    private int play = -1;

    public int getPlay() {
        return play;
    }

    public void setPlay(int play) {
        this.play = play;
    }


    private int mute = -1;

    public int getMute() {
        return mute;
    }

    public void setMute(int mute) {
        this.mute = mute;
    }

    private int rssi = -1;

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    private int signalStatus = -1;

    public int getSignalStatus() {
        return signalStatus;
    }

    public void setSignalStatus(int signalStatus) {
        this.signalStatus = signalStatus;
    }
}
