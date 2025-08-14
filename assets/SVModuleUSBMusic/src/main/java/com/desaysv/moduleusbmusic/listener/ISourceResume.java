package com.desaysv.moduleusbmusic.listener;

import com.desaysv.localmediasdk.sdk.bean.Constant;
import com.desaysv.mediacommonlib.bean.ChangeReason;

/**
 * @author uidq1846
 * @desc 音源恢复的接口
 * @time 2023-1-4 15:25
 */
public interface ISourceResume {

    /**
     * 其它音源恢复，仅需知道启动的来源，剩余需求由内部实现
     *
     * @param action {@link ResumeAction}
     */
    void openSource(ResumeAction action);

    /**
     * @param source       要恢复的音源
     * @param isForeground 是否拉起界面
     * @param flag         意图，主页面定制,查看mainActivity{@link  Constant.OpenSourceViewType}
     */
    void openSource(String source, boolean isForeground, int flag, ChangeReason changeReason);
}
