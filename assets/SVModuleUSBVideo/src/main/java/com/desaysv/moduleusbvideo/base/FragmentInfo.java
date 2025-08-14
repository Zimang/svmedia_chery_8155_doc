package com.desaysv.moduleusbvideo.base;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

/**
 * @author xiaohuiy
 * @email xiaohuiy@kotei-info.com
 * @since 2020-9-30
 */
public class FragmentInfo implements Serializable, Comparable {

    private static final long serialVersionUID = 5659302358558105374L;

    /**
     * 装载控件ID，Fragment所在FrameLayout的ID
     */
    private final int contentID;

    /**
     * 自定义FragmentID,Fragment的唯一标识,一般用fragment的布局id做唯一标识
     */
    private int id;

    /**
     * Fragment对象
     */
    private final Fragment fragment;

    private FragmentInfo(Builder builder) {
        this.contentID = builder.contentID;
        this.id = builder.id;
        this.fragment = builder.fragment;
    }

    /**
     * 获取装载控件ID，Fragment所在FrameLayout的ID
     */
    public int getContentID() {
        return contentID;
    }

    /**
     * 获取自定义FragmentID，Fragment的唯一标识
     */
    public int getId() {
        return id;
    }

    /**
     * 设置自定义FragmentID，Fragment的唯一标识
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取Fragment对象
     */
    public Fragment getFragment() {
        return fragment;
    }

    /**
     * 设置Fragment对象
     */

    @NonNull
    @Override
    public String toString() {
        return "FragmentInfo{" +
                "contentID=" + contentID +
                ", id=" + id +
                ", fragment=" + fragment +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        FragmentInfo fragmentInfo = (FragmentInfo) o;
        return Integer.compare(fragmentInfo.id, id);
    }

    public static class Builder {
        private int contentID;
        private int id;
        private Fragment fragment;

        public Builder setContentID(int contentID) {
            this.contentID = contentID;
            return this;
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setFragment(Fragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public FragmentInfo build() {
            return new FragmentInfo(this);
        }
    }
}
