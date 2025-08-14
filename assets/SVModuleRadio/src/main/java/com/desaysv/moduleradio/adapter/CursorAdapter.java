package com.desaysv.moduleradio.adapter;

import android.hardware.radio.RadioManager;

import com.desaysv.moduleradio.view.cursor.Adapter;

import java.util.List;

public class CursorAdapter extends Adapter {
    private final String TAG = getClass().getSimpleName();

    private List<Float> frequencyList;

    private int band = RadioManager.BAND_FM;

    public void setFrequencyList(List<Float> frequencyList, int band) {
        this.frequencyList = frequencyList;
        this.band = band;
    }

    @Override
    public int getItemCount() {
        return frequencyList == null ? 0 : frequencyList.size();
    }

    @Override
    public List<Float> getItemList() {
        return frequencyList;
    }

    @Override
    public int getBand() {
        return band;
    }
}
