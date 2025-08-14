package com.desaysv.modulebtmusic.bean;

import java.util.List;

/**
 * @Description: SVDataListBean
 * Common公共bean
 */
public class SVDataListBean<T> {
    boolean isSuccess = false;
    List<T> list;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}