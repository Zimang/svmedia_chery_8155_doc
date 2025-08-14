package com.desaysv.moduleusbmusic.dataPoint;

/**
 * @author uidq1846
 * @desc USB的数据埋点操作
 * @time 2023-4-12 21:58
 */
public class UsbMusicPoint extends BaseMusicPoint implements IUsbMusicPoint {

    private static final class PointHolder {
        static final UsbMusicPoint point = new UsbMusicPoint();
    }

    public static IUsbMusicPoint getInstance() {
        return PointHolder.point;
    }

    @Override
    public void open(ContentData... content) {
        upload(PointValue.USBMusicKeyName.OpenCloseClick
                , getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Open), content));
    }

    @Override
    public void close(ContentData... content) {
        upload(PointValue.USBMusicKeyName.OpenCloseClick
                , getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Close), content));
    }

    @Override
    public void openRecently(ContentData... content) {
        upload(PointValue.USBMusicKeyName.RecentOpenCloseClick
                , getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Open), content));
    }

    @Override
    public void closeRecently(ContentData... content) {
        upload(PointValue.USBMusicKeyName.RecentOpenCloseClick
                , getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Close), content));
    }

    @Override
    public void play(ContentData... content) {
        upload(PointValue.USBMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Play), content));
    }

    @Override
    public void pause(ContentData... content) {
        upload(PointValue.USBMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Pause), content));
    }

    @Override
    public void pre(ContentData... content) {
        upload(PointValue.USBMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Pre), content));
    }

    @Override
    public void next(ContentData... content) {
        upload(PointValue.USBMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Next), content));
    }

    @Override
    public void seekForward(ContentData... content) {
        upload(PointValue.USBMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.SeekForward), content));
    }

    @Override
    public void seekBackward(ContentData... content) {
        upload(PointValue.USBMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.SeekBackward), content));
    }

    @Override
    public void downloadFile(ContentData... content) {
        upload(PointValue.USBMusicKeyName.MusicOperate
                , getContentString(new ContentData(PointValue.Field.PlayOperType, PointValue.PlayOperTypeValue.Download), content));
    }

    @Override
    public void collect(ContentData... content) {

    }

    @Override
    public void cancelCollect(ContentData... content) {

    }

    @Override
    public void randomMode(ContentData... content) {
        upload(PointValue.USBMusicKeyName.PlayModeClick
                , getContentString(new ContentData(PointValue.Field.PlayMode, PointValue.PlayModeValue.Random), content));
    }

    @Override
    public void singleMode(ContentData... content) {
        upload(PointValue.USBMusicKeyName.PlayModeClick
                , getContentString(new ContentData(PointValue.Field.PlayMode, PointValue.PlayModeValue.Single), content));
    }

    @Override
    public void cycleMode(ContentData... content) {
        upload(PointValue.USBMusicKeyName.PlayModeClick
                , getContentString(new ContentData(PointValue.Field.PlayMode, PointValue.PlayModeValue.Cycle), content));
    }

    @Override
    public void downLoadFolder(ContentData... content) {
        upload(PointValue.USBMusicKeyName.DownloadClick, getContentString(null, content));
    }

    @Override
    public void insertUSB() {
        upload(PointValue.USBMusicKeyName.USBOperate, getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Open), (ContentData[]) null));
    }

    @Override
    public void removeUSB() {
        upload(PointValue.USBMusicKeyName.USBOperate, getContentString(new ContentData(PointValue.Field.OperType, PointValue.OperTypeValue.Close), (ContentData[]) null));
    }
}
