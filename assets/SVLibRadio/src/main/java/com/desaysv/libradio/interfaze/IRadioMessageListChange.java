package com.desaysv.libradio.interfaze;

/**
 * Created by LZM on 2019-8-7.
 * Comment FM和AM数据获取的列表,可以根据不同的项目需求修改
 */
public interface IRadioMessageListChange {

    void onFMCollectListChange();

    void onAMCollectListChange();

    void onDABCollectListChange();

    void onFMEffectListChange();

    void onAMEffectListChange();

    void onDABEffectListChange();

    void onFMAllListChange();

    void onAMAllListChange();

    void onDABAllListChange();

    default void onFMPlayListChange(){}

    default void onAMPlayListChange(){}

    default void onDABPlayListChange(){}
}
