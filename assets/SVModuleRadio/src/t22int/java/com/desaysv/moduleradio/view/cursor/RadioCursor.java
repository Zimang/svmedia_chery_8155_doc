package com.desaysv.moduleradio.view.cursor;

import static com.desaysv.libradio.bean.RadioConfig.AM_MAX;
import static com.desaysv.libradio.bean.RadioConfig.AM_MIN;
import static com.desaysv.libradio.bean.RadioConfig.AM_STEP;
import static com.desaysv.libradio.bean.RadioConfig.FM_MAX;
import static com.desaysv.libradio.bean.RadioConfig.FM_MIN;
import static com.desaysv.libradio.bean.RadioConfig.FM_STEP;

import android.content.Context;
import android.hardware.radio.RadioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.adapter.CursorAdapter;

import java.util.List;

public class RadioCursor extends LinearLayout {
    private static final String TAG = "RadioCursor";

    OnFrequencyChangedListener onFrequencyChangedListener;

    HorizontalCursorView fmCursorView;
    HorizontalCursorView amCursorView;

    CursorAdapter fmCursorAdapter, amCursorAdapter;

    private int band = RadioManager.BAND_FM;

    public RadioCursor(@NonNull Context context) {
        super(context);
    }

    public RadioCursor(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadioCursor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.radio_cuesor, this, false);
        fmCursorView = view.findViewById(R.id.fmCursorView);
        amCursorView = view.findViewById(R.id.amCursorView);

        fmCursorView.setOnFrequencyChangedListener(frequencyChangedListener);
        amCursorView.setOnFrequencyChangedListener(frequencyChangedListener);

        fmCursorAdapter = new CursorAdapter();
        amCursorAdapter = new CursorAdapter();

        reset();

        addView(view);
    }

    /**
     * 重置刻度尺的刻度列表数据
     */
    public void reset() {
        float fmMin = FM_MIN / 1000F;
        float fmMax = FM_MAX / 1000F;
        float fmStep = FM_STEP / 1000F;
        List<Float> fmList = RadioConfig.getInstance().createFrequencyList(fmMin, fmMax, fmStep);
        fmCursorAdapter.setFrequencyList(fmList, RadioManager.BAND_FM);

        List<Float> amList = RadioConfig.getInstance().createFrequencyList(AM_MIN, AM_MAX, AM_STEP);
        amCursorAdapter.setFrequencyList(amList, RadioManager.BAND_AM);

        fmCursorView.setAdapter(fmCursorAdapter);
        amCursorView.setAdapter(amCursorAdapter);
    }

    OnFrequencyChangedListener frequencyChangedListener = new OnFrequencyChangedListener() {
        @Override
        public void onChanged(int band, float frequency) {
            Log.d(TAG, "onChanged: band = " + band + ", frequency = " + frequency);
            if (onFrequencyChangedListener != null) {
                if (band == RadioManager.BAND_FM) {
                    frequency *= 1000;
                }
                onFrequencyChangedListener.onChanged(band, frequency);
            }
        }

        @Override
        public void onChangedAndOpenIt(int band, float frequency) {
            Log.d(TAG, "onChangedAndOpenIt: band = " + band + ", frequency = " + frequency);
            if (onFrequencyChangedListener != null) {
                if (band == RadioManager.BAND_FM) {
                    frequency *= 1000;
                }
                onFrequencyChangedListener.onChangedAndOpenIt(band, frequency);
            }
        }
    };

    public void setOnFrequencyChangeListener(OnFrequencyChangedListener onFrequencyChangeListener) {
        this.onFrequencyChangedListener = onFrequencyChangeListener;
    }

    /**
     * 设置刻度尺当前要显示的刻度值
     *
     * @param freq 频率刻度值
     */
    public void setFrequency(float freq) {
        Log.d(TAG, "setFrequency: freq = " + freq);
        if (band == RadioManager.BAND_FM) {
            freq = freq / 1000F;
            fmCursorView.scrollToFrequency(freq);
        } else {
            amCursorView.scrollToFrequency(freq);
        }
    }

    public void setBand(int band) {
        this.band = band;
        if (band == RadioManager.BAND_FM) {
            fmCursorView.setVisibility(VISIBLE);
            amCursorView.setVisibility(GONE);
        } else {
            fmCursorView.setVisibility(GONE);
            amCursorView.setVisibility(VISIBLE);
        }
    }

    public void updateRadioMessage(RadioMessage radioMessage) {
        if (radioMessage.getRadioBand() == RadioManager.BAND_AM) {
            if (amCursorView != null) {
                amCursorView.scrollToFrequency(radioMessage.getRadioFrequency());
            }
        } else if (fmCursorView != null) {
            fmCursorView.scrollToFrequency(radioMessage.getRadioFrequency() / 1000F);
        }
    }

    public void notifyRadioRegionChanged() {
        reset();
        if (fmCursorView != null) {
            fmCursorView.notifyDataSetChanged();
        }
        if (amCursorView != null) {
            amCursorView.notifyDataSetChanged();
        }
    }

}
