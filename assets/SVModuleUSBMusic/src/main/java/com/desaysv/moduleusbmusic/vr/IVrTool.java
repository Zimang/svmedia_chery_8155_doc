package com.desaysv.moduleusbmusic.vr;

import android.content.Context;

/**
 * @author uidq1846
 * @desc Vr 初始化工具
 * @time 2023-1-17 11:34
 */
public interface IVrTool {

    /**
     * 初始化
     */
    void init(Context context);

    /**
     * 获取控制器
     *
     * @return IVrControl
     */
    IVrControl getControl();

    /**
     * 获取响应器
     *
     * @return IVrResponse
     */
    IVrResponse getResponse();
}
