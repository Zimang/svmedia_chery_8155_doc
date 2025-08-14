package com.desaysv.modulebtmusic.tracker;

import com.desaysv.presenter.tracking.common.EventInfo;

public interface ITracker {
    void trackEvent(String keyName, String field, String value);

    void release();

    default void trackEventPlatform(EventInfo eventInfo) { }
}
