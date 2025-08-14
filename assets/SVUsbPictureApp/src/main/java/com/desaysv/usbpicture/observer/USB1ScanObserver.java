package com.desaysv.usbpicture.observer;

import android.util.Log;

import com.desaysv.usbbaselib.bean.USBConstants;


public class USB1ScanObserver extends BaseScanObserver{

    private static USB1ScanObserver mInstance;

    public static USB1ScanObserver getInstance(){
        if (mInstance == null) {
            synchronized (USB1ScanObserver.class) {
                if (mInstance == null) {
                    mInstance = new USB1ScanObserver();
                }
            }
        }
        return mInstance;
    }


    @Override
    public void notifyObserverUpdate() {
        Log.d(TAG, "notifyObserverUpdate: observers size = " + observers.size());
        readLock.lock();
        try {
            for (IScanObserver observer : observers) {
                observer.onUpdate(scanStatus);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int getObserverSize() {
        return observers.size();
    }

    public int getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(int scanStatus) {
        this.scanStatus = scanStatus;
        notifyObserverUpdate();
    }

    private int scanStatus = USBConstants.ProviderScanStatus.SCAN_FINISHED;
}
