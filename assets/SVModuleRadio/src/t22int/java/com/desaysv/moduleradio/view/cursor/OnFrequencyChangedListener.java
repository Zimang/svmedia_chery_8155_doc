package com.desaysv.moduleradio.view.cursor;

public interface OnFrequencyChangedListener {
    void onChanged(int band, float frequency);

    void onChangedAndOpenIt(int band, float frequency);
}
