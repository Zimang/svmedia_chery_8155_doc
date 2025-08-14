package com.desaysv.moduleusbmusic.dataPoint;

/**
 * @author uidq1846
 * @desc USB的数据埋点操作
 * @time 2023-4-12 21:58
 */
public class LocalMusicPoint extends BaseMusicPoint implements ILocalMusicPoint {
    private static final class PointHolder {
        static final LocalMusicPoint point = new LocalMusicPoint();
    }

    public static ILocalMusicPoint getInstance() {
        return LocalMusicPoint.PointHolder.point;
    }

    @Override
    public void open(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.OpenClick
                , getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Open), content));
    }

    @Override
    public void close(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.CloseClick
                , getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Close), content));
    }

    @Override
    public void play(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Play), content));
    }

    @Override
    public void pause(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Pause), content));
    }

    @Override
    public void pre(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Pre), content));
    }

    @Override
    public void next(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Next), content));
    }

    @Override
    public void seekForward(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.SeekForward), content));
    }

    @Override
    public void seekBackward(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.SeekBackward), content));
    }

    @Override
    public void delete(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Delete), content));
    }

    @Override
    public void randomMode(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.PlayMode
                , getContentString(new ContentData(PointValue.Field.PlayMode, PointValue.PlayModeValue.Random), content));
    }

    @Override
    public void singleMode(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.PlayMode
                , getContentString(new ContentData(PointValue.Field.PlayMode, PointValue.PlayModeValue.Single), content));
    }

    @Override
    public void cycleMode(ContentData... content) {
        upload(PointValue.LocalMusicKeyName.PlayMode
                , getContentString(new ContentData(PointValue.Field.PlayMode, PointValue.PlayModeValue.Cycle), content));
    }
}
