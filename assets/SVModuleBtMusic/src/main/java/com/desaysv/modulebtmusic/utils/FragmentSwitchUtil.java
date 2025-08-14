package com.desaysv.modulebtmusic.utils;

public class FragmentSwitchUtil {
    private static final String TAG = "FragmentSwitchUtil";

    private static volatile FragmentSwitchUtil mInstance;
    private final ObserverBuilder<OnFragmentSwitchListener> mObserverBuilder = new ObserverBuilder<>();

    public static final int BT_MUSIC_HOME = 1;
    public static final int BT_MUSIC_PLAY = 2;

    public static FragmentSwitchUtil getInstance() {
        if (mInstance == null) {
            synchronized (FragmentSwitchUtil.class) {
                if (mInstance == null) {
                    mInstance = new FragmentSwitchUtil();
                }
            }
        }
        return mInstance;
    }

    public void registerListener(OnFragmentSwitchListener listener) {
        mObserverBuilder.registerObserver(listener);
    }

    public void unregisterListener(OnFragmentSwitchListener listener) {
        mObserverBuilder.unregisterObserver(listener);
    }

    public interface OnFragmentSwitchListener {
        void onFragmentSwitch(int type);

        default void switchFragment(int type){}
    }

    public void notifyFragmentSwitch(int type) {
        mObserverBuilder.notifyObservers((ObserverBuilder.IListener<OnFragmentSwitchListener>)
                observer -> observer.onFragmentSwitch(type));
    }

    public void requestSwitchFragment(int type) {
        mObserverBuilder.notifyObservers((ObserverBuilder.IListener<OnFragmentSwitchListener>)
                observer -> observer.switchFragment(type));
    }
}
