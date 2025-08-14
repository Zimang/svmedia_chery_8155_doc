package com.desaysv.libusbmedia.interfaze;

import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.libusbmedia.bean.MediaAction;

import java.util.List;

/**
 * Created by uidp5370 on 2019-6-12.
 * 对外的控制逻辑
 */

public interface IControlTool {

    /**
     * 控制入口，接口对外
     * @param mediaAction 执行的动作
     * @param changeReason 执行的原因
     */
    void processCommand(MediaAction mediaAction, ChangeReason changeReason);

    /**
     * 控制入口，接口对外
     * @param mediaAction 执行的动作
     * @param changeReason 执行的原因
     * @param data 执行的需要的参数，如播放的位置，跳转的位置等
     */
    void processCommand(MediaAction mediaAction, ChangeReason changeReason, int data);

    /**
     * 设置播放的列表，接口对外，这个接口要和open一起用
     * @param playList 播放的列表
     * @param currentPlayListType 播放列表的模式（艺术家，专辑，收藏，全部，文件夹）
     */
    void setPlayList(List<FileMessage> playList, CurrentPlayListType currentPlayListType );

    /**
     * 设置视频的播放速率
     * 仅用于设置播放速率，不设置也可以
     * @param speed speed
     */
    void setPlaySpeed(float speed);
}
