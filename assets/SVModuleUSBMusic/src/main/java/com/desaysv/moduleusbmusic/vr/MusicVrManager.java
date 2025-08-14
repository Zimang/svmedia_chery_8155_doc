package com.desaysv.moduleusbmusic.vr;

import static com.desaysv.libusbmedia.control.MediaControlTool.NEED_TO_FIND_THE_SAVE_MEDIA;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.VDValueVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRUpload;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.localmediasdk.bean.PackageConfig;
import com.desaysv.localmediasdk.sdk.bean.Constant;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.businesslogic.control.CopyDeleteControl;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.ui.fragment.MusicMainFragment;
import com.desaysv.svcommonutils.LanguageManager;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.google.gson.Gson;

import java.util.List;

/**
 * @author uidq1846
 * @desc 音乐部分语音状态监听和响应管理类
 * @time 2023-1-17 11:41
 */
public class MusicVrManager implements IVrTool, IVrControl, IVrResponse {
    private static final String TAG = MusicVrManager.class.getSimpleName();
    private Handler handler;
    private Context context;
    public static final String VR_PLAY_LIST_TAG = "VR_PLAY_LIST_TAG";
    public static final int VR_PLAY_LIST_NULL = -1;
    public static final int VR_PLAY_LIST_OPEN = 0x01;
    public static final int VR_PLAY_LIST_CLOSE = VR_PLAY_LIST_OPEN + 1;
    private static boolean isEUVersion;
    private boolean isFg = false;
    private Gson mGson;

    private MusicVrManager() {
    }

    private static final class MusicVrManagerHolder {
        @SuppressLint("StaticFieldLeak")
        static final MusicVrManager musicVrManager = new MusicVrManager();
    }

    public static IVrTool getInstance() {
        return MusicVrManagerHolder.musicVrManager;
    }

    @Override
    public void init(Context context) {
        this.context = context;
        this.mGson = new Gson();
        isEUVersion = CarConfigUtil.getDefault().isEUVersionByPartNum();
        initHandlerThread();
        initVDService();
    }

    @Override
    public IVrControl getControl() {
        return MusicVrManagerHolder.musicVrManager;
    }

    @Override
    public IVrResponse getResponse() {
        return MusicVrManagerHolder.musicVrManager;
    }

    /**
     * 初始化语音VDS服务
     */
    private void initVDService() {
        VDBus.getDefault().init(context);
        VDBus.getDefault().registerVDBindListener(new VDBindListener() {
            @Override
            public void onVDConnected(VDServiceDef.ServiceType serviceType) {
                //执行些media vds启动时需初始化的工作
                //订阅VDB事件
                Log.d(TAG, "onVDConnected: serviceType = " + serviceType);
                if (VDServiceDef.ServiceType.VR == serviceType) {
                    addVrSubscribe();
                }
            }

            @Override
            public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
                Log.d(TAG, "onVDDisconnected: serviceType = " + serviceType);
            }
        });
        //绑定服务
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.VR);
    }

    /**
     * 添加VR订阅
     */
    private void addVrSubscribe() {
        VDBus.getDefault().addSubscribe(VDEventVR.VR_MUSIC, VDThreadType.CHILD_THREAD);
        //监听通用逻辑
        VDBus.getDefault().addSubscribe(VDEventVR.VR_MEDIA, VDThreadType.CHILD_THREAD);
        VDBus.getDefault().subscribeCommit(); // 提交订阅
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);
        Log.d(TAG, "addVrSubscribe: ");
    }

    /**
     * 初始化Handler线程
     */
    private void initHandlerThread() {
        Log.d(TAG, "initHandlerThread: ");
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handlerKeyAction(msg);
            }
        };
    }

    /**
     * 处理语音消息
     *
     * @param msg Message
     */
    private void handlerKeyAction(Message msg) {

    }

    /**
     * VDS订阅
     */
    private final VDNotifyListener mVDNotifyListener = new VDNotifyListener() {

        @Override
        public void onVDNotify(VDEvent vdEvent, int threadType) {
            int id = vdEvent.getId();
            Log.i(TAG, "onVDNotify: getId = " + id);
            if (id == VDEventVR.VR_MEDIA) {
                //如果是其它的id
                String sourceName = AudioFocusUtils.getInstance().getCurrentAudioSourceName();
                Log.d(TAG, "onVDNotify: sourceName = " + sourceName);
                if (DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName)
                        || DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName)) {
                    id = VDEventVR.VR_MUSIC;
                }
            }
            if (id == VDEventVR.VR_MUSIC) {
                //当前是本地源才响应
                VDVRPipeLine param = VDVRPipeLine.getValue(vdEvent);
                //校验
                if (param == null) {
                    Log.e(TAG, "onVDNotify: getValue is null!!");
                    return;
                }
                // 内部Json语义表定义的Key值(参考: https://docs.qq.com/sheet/DZVBUYlBFUERNTG5q?tab=o9ktwo)
                String key = param.getKey();
                // 内部Json语义表定义的Josn数据, 例: {"action":"OPEN","position":"F","type":"","value":""}
                String data = param.getValue();
                Log.d(TAG, "onVDNotify: key = " + key + " data = " + data);
                MusicVRActionBean musicVRActionBean = mGson.fromJson(data, MusicVRActionBean.class);
                Log.d(TAG, "onVDNotify: musicVRActionBean = " + musicVRActionBean.toString());
                SemanticBean semantic = musicVRActionBean.getSemantic();
                switch (key) {
                    case MusicVRKey.KEY_SKIP_MUSIC_APP:
                        String skipAction = semantic.getAction();
                        if (skipAction == null || !needToRespondOpenVr()) {
                            Log.w(TAG, "onVDNotify: KEY_SKIP_MUSIC_APP skipAction is null");
                            return;
                        }
                        if (MusicVRValue.SkipAppAction.OPEN.equals(skipAction)) {
                            int fgFlag = MusicSetting.getInstance().getInt(MusicMainFragment.MUSIC_FG_FLAG, 0);
                            Log.d(TAG, "onVDNotify: music fgFlag = " + fgFlag);
                            if (fgFlag == 0) {
                                postToVrTts(R.string.media_music_openmusicapp_open);
                                if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getPlayList() != null) {
                                    if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getPlayList().isEmpty()) {
                                        List<FileMessage> localMusicAllList = USBMusicDate.getInstance().getLocalMusicAllList();
                                        if (!localMusicAllList.isEmpty()) {
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localMusicAllList, CurrentPlayListType.ALL);
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                            return;
                                        }
                                    } else {
                                        ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                        LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                        lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                    }
                                } else {
                                    //设置下列表
                                    List<FileMessage> localMusicAllList = USBMusicDate.getInstance().getLocalMusicAllList();
                                    if (!localMusicAllList.isEmpty()) {
                                        ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localMusicAllList, CurrentPlayListType.ALL);
                                        ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                        LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                        lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                        return;
                                    }
                                }
                                //剩余判断USB的
                                if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getPlayList() != null) {
                                    if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getPlayList().isEmpty()) {
                                        List<FileMessage> usbList = USBMusicDate.getInstance().getUSB1MusicAllList();
                                        if (!usbList.isEmpty()) {
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usbList, CurrentPlayListType.ALL);
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                        }
                                    } else {
                                        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                        UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                        lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                    }
                                } else {
                                    //设置下列表
                                    List<FileMessage> usbList = USBMusicDate.getInstance().getUSB1MusicAllList();
                                    if (!usbList.isEmpty()) {
                                        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usbList, CurrentPlayListType.ALL);
                                        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                        UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                        lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                        return;
                                    }
                                }
                                //如果都没有，则拉起列表页
                                lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, Constant.OpenSourceViewType.LIST_VIEW);
                            } else {
                                postToVrTts(R.string.media_music_openmusicapp_opened);
                            }
                        }
                        break;
                    case MusicVRKey.KEY_PLAY_SPECIFIC_MUSIC:
                        String name = semantic.getName();
                        String artist = semantic.getArtist();
                        Log.i(TAG, "onVDNotify: name = " + name + " artist = " + artist);
                        //如果歌名不为空
                        if (!TextUtils.isEmpty(name)) {
                            //先判断本地的
                            List<FileMessage> localList = USBMusicDate.getInstance().getLocalMusicAllList();
                            for (int i = 0; i < localList.size(); i++) {
                                FileMessage songMessage = localList.get(i);
                                if (name.equalsIgnoreCase(songMessage.getName())) {
                                    Log.i(TAG, "onVDNotify: found song in local list songMessage = " + songMessage + " position = " + i);
                                    ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localList, CurrentPlayListType.ALL);
                                    ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, i);
                                    LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                    return;
                                }
                            }
                            //如果本地没找到，找USB的
                            List<FileMessage> usb0List = USBMusicDate.getInstance().getUSB1MusicAllList();
                            for (int i = 0; i < usb0List.size(); i++) {
                                FileMessage songMessage = usb0List.get(i);
                                if (name.equalsIgnoreCase(songMessage.getName())) {
                                    Log.i(TAG, "onVDNotify: found song in usb list songMessage = " + songMessage + " position = " + i);
                                    ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usb0List, CurrentPlayListType.ALL);
                                    ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, i);
                                    UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                    return;
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(artist)) {
                            //先判断本地的
                            List<FileMessage> localList = USBMusicDate.getInstance().getLocalMusicAllList();
                            for (int i = 0; i < localList.size(); i++) {
                                FileMessage songMessage = localList.get(i);
                                if (artist.equalsIgnoreCase(songMessage.getAuthor())) {
                                    Log.i(TAG, "onVDNotify: found artist in local list songMessage = " + songMessage + " position = " + i);
                                    ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localList, CurrentPlayListType.ALL);
                                    ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, i);
                                    LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                    return;
                                }
                            }
                            //如果本地没找到，找USB的
                            List<FileMessage> usb0List = USBMusicDate.getInstance().getUSB1MusicAllList();
                            for (int i = 0; i < usb0List.size(); i++) {
                                FileMessage songMessage = usb0List.get(i);
                                if (artist.equalsIgnoreCase(songMessage.getAuthor())) {
                                    Log.i(TAG, "onVDNotify: found artist in usb list songMessage = " + songMessage + " position = " + i);
                                    ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usb0List, CurrentPlayListType.ALL);
                                    ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, i);
                                    UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                    return;
                                }
                            }
                        }
                        //这里表示找不到曲目，如果列表有其它歌曲则播放
                        List<FileMessage> localList = USBMusicDate.getInstance().getLocalMusicAllList();
                        List<FileMessage> usb0List = USBMusicDate.getInstance().getUSB1MusicAllList();
                        if (localList.isEmpty() && usb0List.isEmpty()) {
                            postToVrTts(R.string.media_music_playsinger_empty);
                            return;
                        }
                        if (!TextUtils.isEmpty(name)) {
                            postToVrTts(R.string.media_music_playsong_playother);
                        } else {
                            postToVrTts(R.string.media_music_playsinger_playother);
                        }
                        if (!localList.isEmpty()) {
                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localList, CurrentPlayListType.ALL);
                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, 0);
                            LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                            return;
                        }
                        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usb0List, CurrentPlayListType.ALL);
                        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, 0);
                        UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                        break;
                    case MusicVRKey.KEY_CONTROL_COLLECT://歌曲收藏列表控制
                        String collectAction = semantic.getAction();
                        if (collectAction == null) {
                            Log.w(TAG, "onVDNotify: KEY_CONTROL_COLLECT collectAction is null");
                            return;
                        }
                        //只有当前播放的是USB音乐才能够收藏
                        if (!isMusicSource()) {
                            return;
                        }
                        IStatusTool statusTool1 = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool();
                        //当前没有媒体播放
                        FileMessage fileMessage = statusTool1.getCurrentPlayItem();
                        Log.d(TAG, "onVDNotify: fileMessage = " + fileMessage);
                        switch (collectAction) {
                            case MusicVRValue.CollectAction.COLLECT:
                                if (!statusTool1.isPlaying()) {
                                    postToVrTts(R.string.media_music_collect_noplaying);
                                    return;
                                }
                                //当前是本地则提示不支持
                                if (fileMessage.getPath().startsWith(USBConstants.USBPath.LOCAL_PATH)) {
                                    postToVrTts(R.string.media_music_collect_playing_nosupport);
                                    return;
                                }
                                //已经收藏
                                if (CopyDeleteControl.getInstance().getCopyControl().isCopied(fileMessage)) {
                                    postToVrTts(R.string.media_music_collect_playing_collected);
                                    return;
                                }
                                //收藏列表已满，请清理后再试
                                if (!CopyDeleteControl.getInstance().getCopyControl().isStorageAvailable(fileMessage)) {
                                    postToVrTts(R.string.media_music_collect_overrange);
                                    return;
                                }
                                postToVrTts(R.string.media_music_collect_playing_collect);
                                CopyDeleteControl.getInstance().getCopyControl().copyFile(fileMessage, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
                                break;
                            case MusicVRValue.CollectAction.CANCEL_COLLECT:
                                if (!statusTool1.isPlaying()) {
                                    postToVrTts(R.string.media_music_uncollect_playing_noplaying);
                                    return;
                                }
                                //当前播放歌曲没有收藏
                                if (!CopyDeleteControl.getInstance().getCopyControl().isCopied(fileMessage)) {
                                    postToVrTts(R.string.media_music_uncollect_playing_uncollected);
                                    return;
                                }
                                postToVrTts(R.string.media_music_uncollect_playing_collected);
                                //主动暂停播放
                                Log.d(TAG, "CANCEL_COLLECT STOP PLAY");
                                ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getControlTool().processCommand(MediaAction.STOP, ChangeReasonData.NA);
                                List<FileMessage> playList = statusTool1.getPlayList();
                                String playPath = statusTool1.getCurrentPlayItem().getPath();
                                //播放下一个播放
                                int nextComparePosition = -1;
                                if (playList != null && playList.size() > 1) {
                                    for (int position = 0; position < playList.size(); position++) {
                                        String path = playList.get(position).getPath();
                                        if (playPath.equals(path)) {
                                            nextComparePosition = position + 1;
                                            //当前是最后一个时,下一个是第一个
                                            if (nextComparePosition >= playList.size()) {
                                                nextComparePosition = 0;
                                            }
                                            //找到的下一个就是当前要删除的这个，重置-1不播放
                                            if (playPath.equals(playList.get(nextComparePosition).getPath())) {
                                                nextComparePosition = -1;
                                            }
                                            Log.d(TAG, "CANCEL_COLLECT find nextComparePosition = " + nextComparePosition);
                                            break;
                                        }
                                    }
                                    if (nextComparePosition != -1) {
                                        ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.NA, nextComparePosition);
                                    }
                                }
                                Log.d(TAG, "CANCEL_COLLECT nextComparePosition = " + nextComparePosition);
                                try {
                                    //这里休息下，否则还未来得及释放，就到文件删除了
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                String path = fileMessage.getPath();
                                String deletePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + path.substring(path.lastIndexOf("/") + 1);
                                Log.d(TAG, "onVDNotify: deletePath = " + deletePath);
                                FileMessage message = new FileMessage();
                                message.setPath(deletePath);
                                message.setFileName(fileMessage.getFileName());
                                message.setSize(fileMessage.getSize());
                                message.setName(fileMessage.getName());
                                CopyDeleteControl.getInstance().getDeleteControl().deleteFile(message);
                                break;
                            case MusicVRValue.CollectAction.PLAY:
                                //听收藏的歌曲
                                break;
                        }
                        break;
                    case MusicVRKey.KEY_PLAY_MUSIC:
                        //播放意图
                        String playAction = semantic.getAction();
                        if (playAction == null) {
                            Log.w(TAG, "onVDNotify: KEY_PLAY_MUSIC playAction is null");
                            return;
                        }
                        //用户有听音乐意向，但是没有明确说出想听的歌名或是歌手信息。（仅支持打开本地媒体源音乐，优先级为：本机-USB-蓝牙）
                        if (MusicVRValue.PlayStateAction.OPEN.equals(playAction)) {
                            String source = semantic.getSource();
                            if (source == null) {
                                Log.w(TAG, "onVDNotify: KEY_PLAY_MUSIC source is null");
                                return;
                            }
                            switch (source) {
                                case MusicVRValue.Source.FAVORITE:
                                    //收藏下载到本地，所以和本地的逻辑一致，只不过需要判断当前焦点情况
                                    if (!needToRespondOpenVr()) {
                                        return;
                                    }
                                case MusicVRValue.Source.LOCAL:
                                    if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getPlayList() != null) {
                                        if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getPlayList().isEmpty()) {
                                            List<FileMessage> list = USBMusicDate.getInstance().getLocalMusicAllList();
                                            if (!list.isEmpty()) {
                                                if (!ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().isPlaying()) {
                                                    if (source.equals(MusicVRValue.Source.FAVORITE)) {
                                                        postToVrTts(R.string.media_music_placollect_play);
                                                    } else {
                                                        postToVrTts(R.string.media_music_localmusic_source_play);
                                                    }
                                                }
                                                ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(list, CurrentPlayListType.ALL);
                                                ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                                LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                                lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                                return;
                                            }
                                        } else {
                                            if (!ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().isPlaying()) {
                                                if (source.equals(MusicVRValue.Source.FAVORITE)) {
                                                    postToVrTts(R.string.media_music_placollect_play);
                                                } else {
                                                    postToVrTts(R.string.media_music_localmusic_source_play);
                                                }
                                            }
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                            return;
                                        }
                                    } else {
                                        //设置下列表
                                        List<FileMessage> list = USBMusicDate.getInstance().getLocalMusicAllList();
                                        if (!list.isEmpty()) {
                                            if (!ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().isPlaying()) {
                                                if (source.equals(MusicVRValue.Source.FAVORITE)) {
                                                    postToVrTts(R.string.media_music_placollect_play);
                                                } else {
                                                    postToVrTts(R.string.media_music_localmusic_source_play);
                                                }
                                            }
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(list, CurrentPlayListType.ALL);
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                            return;
                                        }
                                    }
                                    //bugClose 419更改
                                    //postToVrTts(context.getString(R.string.media_music_sourcemusic_source_unable));
                                    postToVrTts(R.string.media_music_openmusic_source_empty);
                                    break;
                                case MusicVRValue.Source.USB:
                                    if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getPlayList() != null) {
                                        if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getPlayList().isEmpty()) {
                                            List<FileMessage> usbList = USBMusicDate.getInstance().getUSB1MusicAllList();
                                            if (!usbList.isEmpty()) {
                                                if (!ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().isPlaying()) {
                                                    postToVrTts(R.string.media_music_localmusic_source_play);
                                                }
                                                ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usbList, CurrentPlayListType.ALL);
                                                ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                                UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                                lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                                return;
                                            }
                                        } else {
                                            if (!ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().isPlaying()) {
                                                postToVrTts(R.string.media_music_localmusic_source_play);
                                            }
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                            return;
                                        }
                                    } else {
                                        //设置下列表
                                        List<FileMessage> usbList = USBMusicDate.getInstance().getUSB1MusicAllList();
                                        if (!usbList.isEmpty()) {
                                            if (!ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().isPlaying()) {
                                                postToVrTts(R.string.media_music_localmusic_source_play);
                                            }
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usbList, CurrentPlayListType.ALL);
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                            return;
                                        }
                                    }
                                    postToVrTts(R.string.media_music_openmusic_source_empty);
                                    break;
                                case MusicVRValue.Source.ALL:
                                    if (!needToRespondOpenVr()) {
                                        return;
                                    }
                                    if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getPlayList() != null) {
                                        if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getPlayList().isEmpty()) {
                                            List<FileMessage> localMusicAllList = USBMusicDate.getInstance().getLocalMusicAllList();
                                            if (!localMusicAllList.isEmpty()) {
                                                postToVrTts(R.string.media_music_openmusic_source_play);
                                                ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localMusicAllList, CurrentPlayListType.ALL);
                                                ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                                LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                                lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                                return;
                                            }
                                        } else {
                                            postToVrTts(R.string.media_music_openmusic_source_play);
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                            return;
                                        }
                                    } else {
                                        //设置下列表
                                        List<FileMessage> localMusicAllList = USBMusicDate.getInstance().getLocalMusicAllList();
                                        if (!localMusicAllList.isEmpty()) {
                                            postToVrTts(R.string.media_music_openmusic_source_play);
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localMusicAllList, CurrentPlayListType.ALL);
                                            ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
                                            return;
                                        }
                                    }
                                    //剩余判断USB的
                                    if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getPlayList() != null) {
                                        if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getPlayList().isEmpty()) {
                                            List<FileMessage> usbList = USBMusicDate.getInstance().getUSB1MusicAllList();
                                            if (!usbList.isEmpty()) {
                                                postToVrTts(R.string.media_music_openmusic_source_play);
                                                ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usbList, CurrentPlayListType.ALL);
                                                ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                                UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                                lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                                return;
                                            }
                                        } else {
                                            postToVrTts(R.string.media_music_openmusic_source_play);
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                            return;
                                        }
                                    } else {
                                        //设置下列表
                                        List<FileMessage> usbList = USBMusicDate.getInstance().getUSB1MusicAllList();
                                        if (!usbList.isEmpty()) {
                                            postToVrTts(R.string.media_music_openmusic_source_play);
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usbList, CurrentPlayListType.ALL);
                                            ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                            lunchPlayView(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
                                            return;
                                        }
                                    }
                                    postToVrTts(R.string.media_music_openmusic_source_empty);
                                    break;
                            }
                        }
                        break;
                    case MusicVRKey.KEY_SEARCH_MUSIC://搜索并播放

                        break;
                    case MusicVRKey.KEY_CONTROL_PLAY_MODE://播放模式切换
                        if (!isMusicSource()) {
                            return;
                        }
                        String controlPlayMode = semantic.getType();
                        if (controlPlayMode == null) {
                            Log.e(TAG, "onVDNotify: getType == null");
                            return;
                        }
                        IGetControlTool modeGetControlTool = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool();
                        IControlTool modeControlTool = modeGetControlTool.getControlTool();
                        IStatusTool modeStatusTool = modeGetControlTool.getStatusTool();
                        String loopType = modeStatusTool.getLoopType();
                        Log.d(TAG, "onVDNotify: loopType = " + loopType);
                        switch (controlPlayMode) {
                            case MusicVRValue.PlayModeType.ORDER:
                            case MusicVRValue.PlayModeType.CYCLE:
                                if (controlPlayMode.equals(loopType)) {
                                    postToVrTts(R.string.media_music_playmode_list_already);
                                } else {
                                    postToVrTts(R.string.media_music_playmode_list);
                                }
                                modeControlTool.processCommand(MediaAction.CYCLE, ChangeReasonData.VR_CONTROL);
                                if (isLocalPoint()) {
                                    LocalMusicPoint.getInstance().cycleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                } else {
                                    UsbMusicPoint.getInstance().cycleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                }
                                break;
                            case MusicVRValue.PlayModeType.RANDOM:
                                if (controlPlayMode.equals(loopType)) {
                                    postToVrTts(R.string.media_music_playmode_random_already);
                                } else {
                                    postToVrTts(R.string.media_music_playmode_random);
                                }
                                modeControlTool.processCommand(MediaAction.RANDOM, ChangeReasonData.VR_CONTROL);
                                if (isLocalPoint()) {
                                    LocalMusicPoint.getInstance().randomMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                } else {
                                    UsbMusicPoint.getInstance().randomMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                }
                                break;
                            case MusicVRValue.PlayModeType.SINGLE:
                                if (controlPlayMode.equals(loopType)) {
                                    postToVrTts(R.string.media_music_playmode_single_already);
                                } else {
                                    postToVrTts(R.string.media_music_playmode_single);
                                }
                                modeControlTool.processCommand(MediaAction.SINGLE, ChangeReasonData.VR_CONTROL);
                                if (isLocalPoint()) {
                                    LocalMusicPoint.getInstance().singleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                } else {
                                    UsbMusicPoint.getInstance().singleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                }
                                break;
                        }
                        break;
                    case MusicVRKey.KEY_PLAY_MUSIC_GENRE://播放指定风格的歌曲

                        break;
                    case MusicVRKey.KEY_CONTROL_PLAY_STATE://播放控制
                        String playStateAction = semantic.getAction();
                        if (playStateAction == null) {
                            Log.e(TAG, "onVDNotify: getAction == null");
                            return;
                        }
                        //非网络项目：
                        //1.当前本地音乐有焦点：本地音乐继续播放音乐
                        //2.当前本地音乐和蓝牙音乐都没有焦点：本地音乐响应，播放默认歌曲
                        //3.当前蓝牙音乐有焦点：蓝牙音乐响应，本地音乐不处理
                        //4.没有音乐应用：回复这个还不会，还需要学习
                        if (MusicVRValue.PlayStateAction.PLAY.equals(playStateAction)) {
                            if (!controlPlayMusicRespond()) {
                                Log.d(TAG, "onVDNotify: !controlPlayMusicRespond() return");
                                return;
                            }
                        } else {
                            if (!isMusicSource()) {
                                Log.d(TAG, "onVDNotify: !isMusicSource() return");
                                return;
                            }
                        }

                        IGetControlTool controlTool = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool();
                        IStatusTool statusTool = controlTool.getStatusTool();
                        FileMessage playStateItem = statusTool.getCurrentPlayItem();
                        ContentData[] contentData = new ContentData[4];
                        contentData[0] = new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR);
                        contentData[1] = new ContentData(PointValue.Field.ProgramName, playStateItem.getName());
                        contentData[2] = new ContentData(PointValue.Field.Author, playStateItem.getAuthor());
                        contentData[3] = new ContentData(PointValue.Field.Album, playStateItem.getAlbum());
                        switch (playStateAction) {
                            case MusicVRValue.PlayStateAction.PLAY:
                                //1.音乐暂停或播放中：继续播放（无回复）
                                if (statusTool.getPlayList() != null && !statusTool.getPlayList().isEmpty()) {
                                    Log.d(TAG, "onVDNotify: controlPlay getPlayList() = " + statusTool.getPlayList().size());
                                    controlTool.getControlTool().processCommand(MediaAction.START, ChangeReasonData.VR_CONTROL);
                                    if (isLocalPoint()) {
                                        LocalMusicPoint.getInstance().play(contentData);
                                    } else {
                                        UsbMusicPoint.getInstance().play(contentData);
                                    }
                                    return;
                                }
                                //2.音乐未打开，有媒体源可播放音乐：播放默认歌曲（无回复）
                                //先判断本地音乐列表
                                List<FileMessage> localMusicAllList = USBMusicDate.getInstance().getLocalMusicAllList();
                                if (!localMusicAllList.isEmpty()) {
                                    Log.d(TAG, "onVDNotify: controlPlay localMusicAllList = " + localMusicAllList.size());
                                    ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().setPlayList(localMusicAllList, CurrentPlayListType.ALL);
                                    ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                    LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                    return;
                                }
                                //判断USB音乐列表
                                List<FileMessage> usbList = USBMusicDate.getInstance().getUSB1MusicAllList();
                                if (!usbList.isEmpty()) {
                                    Log.d(TAG, "onVDNotify: controlPlay usbList = " + usbList.size());
                                    ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().setPlayList(usbList, CurrentPlayListType.ALL);
                                    ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getControlTool().processCommand(MediaAction.OPEN, ChangeReasonData.VR_CONTROL, NEED_TO_FIND_THE_SAVE_MEDIA);
                                    UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.VR));
                                    return;
                                }
                                Log.d(TAG, "onVDNotify: controlPlay has no music response tts");
                                //3.无音乐可播放:对不起，当前没有可播放的音乐
                                postToVrTts(R.string.media_music_openmusic_source_empty);
                                break;
                            case MusicVRValue.PlayStateAction.PAUSE:
                                controlTool.getControlTool().processCommand(MediaAction.PAUSE, ChangeReasonData.VR_CONTROL);
                                if (isLocalPoint()) {
                                    LocalMusicPoint.getInstance().pause(contentData);
                                } else {
                                    UsbMusicPoint.getInstance().pause(contentData);
                                }
                                break;
                            case MusicVRValue.PlayStateAction.NEXT:
                                if (statusTool.getPlayList() == null || statusTool.getPlayList().isEmpty()) {
                                    postToVrTts(R.string.media_music_continueplaying_empty);
                                    return;
                                } else if (statusTool.getPlayList().size() == 1) {
                                    postToVrTts(R.string.media_music_next_lastone);
                                    return;
                                }
                                controlTool.getControlTool().processCommand(MediaAction.NEXT, ChangeReasonData.VR_CONTROL);
                                if (isLocalPoint()) {
                                    LocalMusicPoint.getInstance().next(contentData);
                                } else {
                                    UsbMusicPoint.getInstance().next(contentData);
                                }
                                break;
                            case MusicVRValue.PlayStateAction.PREVIOUS:
                                if (statusTool.getPlayList() == null || statusTool.getPlayList().isEmpty()) {
                                    postToVrTts(R.string.media_music_continueplaying_empty);
                                    return;
                                } else if (statusTool.getPlayList().size() == 1) {
                                    postToVrTts(R.string.media_music_previous_firstone);
                                    return;
                                }
                                controlTool.getControlTool().processCommand(MediaAction.PRE, ChangeReasonData.VR_CONTROL);
                                if (isLocalPoint()) {
                                    LocalMusicPoint.getInstance().pre(contentData);
                                } else {
                                    UsbMusicPoint.getInstance().pre(contentData);
                                }
                                break;
                            case MusicVRValue.PlayStateAction.REPEAT:
                                //当前没播放音乐
                                if (statusTool.isPlaying()) {
                                    postToVrTts(R.string.media_music_replay_playing);
                                    controlTool.getControlTool().processCommand(MediaAction.SEEKTO, ChangeReasonData.VR_CONTROL, 0);
                                } else {
                                    postToVrTts(R.string.media_music_replay_noplaying);
                                }
                                break;
                        }
                        break;
                    case MusicVRKey.KEY_CONTROL_PLAYLIST://列表歌曲播放
                        if (!isMusicSource()) {
                            Log.d(TAG, "onVDNotify: KEY_CONTROL_PLAYLIST !isMusicSource");
                            return;
                        }
                        String playListAction = semantic.getAction();
                        if (playListAction == null) {
                            Log.e(TAG, "onVDNotify: playListAction == null");
                            return;
                        }
                        IGetControlTool playListControlTool = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool();
                        IStatusTool playListStatusTool = playListControlTool.getStatusTool();
                        //当前没有可用媒体源
                        if (playListStatusTool.getPlayList() == null || playListStatusTool.getPlayList().isEmpty()) {
                            postToVrTts(R.string.media_music_openplaylist_nosource);
                            return;
                        }
                        switch (playListAction) {
                            case MusicVRValue.ControlPLayListAction.OPEN:
                                MusicSetting.getInstance().putInt(VR_PLAY_LIST_TAG, VR_PLAY_LIST_OPEN);
                                break;
                            case MusicVRValue.ControlPLayListAction.CLOSE:
                                MusicSetting.getInstance().putInt(VR_PLAY_LIST_TAG, VR_PLAY_LIST_CLOSE);
                                break;
                        }
                        toPlayListView(Constant.OpenSourceViewType.PLAY_VIEW);
                        break;
                    case MusicVRKey.KEY_GET_PLAY_STATUS://查询播放状态

                        break;
                    case MusicVRKey.KEY_GET_SONG_NAME://获取当前播放歌曲名称
                        if (!isMusicSource()) {
                            return;
                        }
                        FileMessage playItem = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem();
                        postTextToVrTts(playItem.getName());
                        break;
                    case MusicVRKey.KEY_IS_PLAY_MODE_SUPPORTED://查询播放模式
                        if (!isMusicSource()) {
                            return;
                        }
                        //反馈当前播放状态给到VR
                        String getLoopType = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getLoopType();
                        String playModeType = semantic.getType();
                        if (playModeType == null) {
                            Log.e(TAG, "onVDNotify: playModeType == null");
                            return;
                        }
                        if ((MusicVRValue.PlayModeType.CYCLE.equals(playModeType) && USBConstants.LoopType.CYCLE.equals(getLoopType))
                                || (MusicVRValue.PlayModeType.RANDOM.equals(playModeType) && USBConstants.LoopType.RANDOM.equals(getLoopType))
                                || (MusicVRValue.PlayModeType.SINGLE.equals(playModeType) && USBConstants.LoopType.SINGLE.equals(getLoopType))) {
                            //说明对应的上循环模式
                            //uploadToVR();
                        } else {
                            //对应不上循环模式
                            //uploadToVR();
                        }
                        break;
                }
            }
        }
    };

    /**
     * 当前师傅本地埋点控制器
     *
     * @return boolean
     */
    private boolean isLocalPoint() {
        int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());
        return MediaType.LOCAL_MUSIC.ordinal() == mediaType;
    }

    private void postTextToVrTts(String text) {
        Log.d(TAG, "postToVrTts: ttsText = " + text);
        // TTS播报（set）
        VDVRPipeLine param = new VDVRPipeLine();
        param.setKey(VDValueVR.VRSemanticKey.VR_CONTROL_RESPONSE);
        param.setValue(text);
        param.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_SUCCEED_AND_TTS); //根据执行结果反馈
        VDEvent event = VDVRPipeLine.createEvent(VDEventVR.VR_AIRCONDITION, param);
        VDBus.getDefault().set(event);
    }

    /**
     * 播报tts文本方法
     *
     * @param resId 需要播报的tts文本
     */
    private void postToVrTts(int resId) {
        String ttsText = getRightLanguageTTSString(context, resId);
        postTextToVrTts(ttsText);
    }

    private String getRightLanguageTTSString(Context context,int resId) {
        Log.d(TAG, "getRightLanguageTTSString:");
        return LanguageManager.getInstance().getString(context, resId);
    }

    /**
     * 状态变化通知语音
     *
     * @param uploadKey  uploadKey
     * @param uploadData uploadData
     */
    private void uploadToVR(String uploadKey, String uploadData) {
        Log.d(TAG, "uploadToVR: uploadData = " + uploadData + " uploadKey = " + uploadKey);
        VDVRUpload vdvrUpload = new VDVRUpload();
        vdvrUpload.setPkgName("com.desaysv.svaudioapp");
        vdvrUpload.setKey(uploadKey);
        vdvrUpload.setData(uploadData);
        vdvrUpload.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_NOTIFY);
        VDEvent event = VDVRUpload.createEvent(VDEventVR.VR_DATA_UPLOAD, vdvrUpload);
        VDBus.getDefault().set(event);
    }


    /**
     * 跳转播放打开列表还是关闭列表方法
     */
    private void toPlayListView(int pageFlag) {
        Intent intent = new Intent();
        intent.setClassName(PackageConfig.MUSIC_APP_PACKAGE, "com.desaysv.svaudioapp.ui.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.Source.SOURCE_KEY, AudioFocusUtils.getInstance().getCurrentAudioSourceName());
        intent.putExtra(Constants.NavigationFlag.KEY, pageFlag);
        AppBase.mContext.startActivity(intent);
    }

    @Override
    public void uploadInfo(FileMessage fileMessage) {
        Log.d(TAG, "uploadInfo: fileMessage = " + fileMessage);
        uploadToVR(MusicVRKey.VR_MUSIC_INFO_RESPONSE, mGson.toJson(getMusicVRUploadBean(fileMessage)));
    }

    @Override
    public void uploadPlayState(String source, boolean isPlaying) {
        Log.d(TAG, "uploadPlayState: source = " + source + " isPlaying = " + isPlaying);
        uploadToVR(MusicVRKey.VR_MUSIC_PLAY_RESPONSE, mGson.toJson(getMusicVRUploadBeanByPlay(isPlaying)));
    }

    @Override
    public void uploadActiveStatus(String source, boolean isActive) {
        Log.d(TAG, "uploadActiveStatus: source = " + source + " isActive = " + isActive);
        uploadToVR(MusicVRKey.VR_MUSIC_STATUS_RESPONSE, mGson.toJson(getMusicVRUploadBeanByActive(isActive)));
    }

    /**
     * 切换歌曲上传信息
     * @param fileMessage
     * @return
     */
    private MusicVRUploadBean getMusicVRUploadBean(FileMessage fileMessage){
        //赋值 responseBean
        MusicVRUploadBean.Data.DataInfo dataInfo = new MusicVRUploadBean.Data.DataInfo();
        dataInfo.setArtist(fileMessage != null ? fileMessage.getAuthor() : "");
        dataInfo.setSong(fileMessage != null ? fileMessage.getName() : "");

        MusicVRUploadBean.Data data = new MusicVRUploadBean.Data();
        data.setDataInfo(dataInfo);
        data.setActiveStatus(isFg
                ? MusicVRValue.ActiveStatus.FOREGROUND : MusicVRValue.ActiveStatus.BACKGROUND);
        data.setSceneStatus(getCurrentPlayStatus() ? MusicVRValue.SceneStatus.PLAYING : MusicVRValue.SceneStatus.PAUSED);

        MusicVRUploadBean responseBean = new MusicVRUploadBean();
        responseBean.setData(data);
        return responseBean;
    }

    /**
     * 切换播放状态上传信息
     * @param isPlaying
     * @return
     */
    private MusicVRUploadBean getMusicVRUploadBeanByPlay(boolean isPlaying){
        //赋值 responseBean
        MusicVRUploadBean.Data.DataInfo dataInfo = new MusicVRUploadBean.Data.DataInfo();
        FileMessage fileMessage = getCurrentPlayItem();
        dataInfo.setArtist(fileMessage != null ? fileMessage.getAuthor() : "");
        dataInfo.setSong(fileMessage != null ? fileMessage.getName() : "");

        MusicVRUploadBean.Data data = new MusicVRUploadBean.Data();
        data.setDataInfo(dataInfo);
        data.setActiveStatus(isFg
                ? MusicVRValue.ActiveStatus.FOREGROUND : MusicVRValue.ActiveStatus.BACKGROUND);
        data.setSceneStatus(isPlaying ? MusicVRValue.SceneStatus.PLAYING : MusicVRValue.SceneStatus.PAUSED);

        MusicVRUploadBean responseBean = new MusicVRUploadBean();
        responseBean.setData(data);
        return responseBean;
    }

    /**
     * 切换前后台上传信息
     * @param isActive
     * @return
     */
    private MusicVRUploadBean getMusicVRUploadBeanByActive(boolean isActive){
        this.isFg = isActive;
        //赋值 responseBean
        MusicVRUploadBean.Data.DataInfo dataInfo = new MusicVRUploadBean.Data.DataInfo();
        FileMessage fileMessage = getCurrentPlayItem();
        dataInfo.setArtist(fileMessage != null ? fileMessage.getAuthor() : "");
        dataInfo.setSong(fileMessage != null ? fileMessage.getName() : "");

        MusicVRUploadBean.Data data = new MusicVRUploadBean.Data();
        data.setDataInfo(dataInfo);
        data.setActiveStatus(isActive ? MusicVRValue.ActiveStatus.FOREGROUND : MusicVRValue.ActiveStatus.BACKGROUND);
        data.setSceneStatus(getCurrentPlayStatus() ? MusicVRValue.SceneStatus.PLAYING : MusicVRValue.SceneStatus.PAUSED);

        MusicVRUploadBean responseBean = new MusicVRUploadBean();
        responseBean.setData(data);
        return responseBean;
    }

    private FileMessage getCurrentPlayItem(){
        IGetControlTool controlTool = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool();
        if(controlTool != null){
            IStatusTool statusTool = controlTool.getStatusTool();
            if(statusTool != null){
                return statusTool.getCurrentPlayItem();
            }
        }
        return null;
    }

    private boolean getCurrentPlayStatus(){
        IGetControlTool controlTool = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool();
        if(controlTool != null){
            IStatusTool statusTool = controlTool.getStatusTool();
            if(statusTool != null){
                return statusTool.isPlaying();
            }
        }
        return false;
    }

    /**
     * 当前是否音乐音源
     *
     * @return T F
     */
    private boolean isMusicSource() {
        String sourceName = AudioFocusUtils.getInstance().getCurrentAudioSourceName();
        Log.d(TAG, "isMusicSource: source = " + sourceName);
        return DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName)
                || DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName)
                || DsvAudioSDKConstants.USB1_MUSIC_SOURCE.equals(sourceName);
    }

    /**
     * 是否需要响应播放歌曲语音语义
     * 网联的时候播放歌曲, 判断焦点在本地音乐才响应
     * 非网联的时候播放歌曲，如果蓝牙音乐没有焦点，本地音乐响应
     *
     * @return T 响应 F 取消响应
     */
    private boolean controlPlayMusicRespond() {
        //是否网联项目
        boolean isNet = CarConfigUtil.getDefault().isNet();
        String sourceName = AudioFocusUtils.getInstance().getCurrentAudioSourceName();
        Log.i(TAG, "controlPlayMusicRespond: sourceName = " + sourceName + " isNet = " + isNet);
        if (isNet) {
            //网联项目判断有焦点响应
            return DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName) || DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName);
        } else {
            //非网联项目判断焦点不在蓝牙,本地音乐响应
            return !DsvAudioSDKConstants.BT_MUSIC_SOURCE.equals(sourceName);
        }
    }

    /**
     * 启动播放页面
     *
     * @param source source 具体的音源
     */
    private void lunchPlayView(String source, int viewType) {
        Log.d(TAG, "lunchPlayView: source = " + source + " viewType = " + viewType);
        Intent intent = new Intent();
        intent.setClassName(PackageConfig.MUSIC_APP_PACKAGE, "com.desaysv.svaudioapp.ui.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.Source.SOURCE_KEY, source);
        intent.putExtra(Constants.NavigationFlag.KEY, viewType);
        AppBase.mContext.startActivity(intent);
    }

    /**
     * 启动播放页面
     *
     * @param source source 具体的音源
     */
    private void lunchPlayView(String source) {
        lunchPlayView(source, Constant.OpenSourceViewType.PLAY_VIEW);
    }

//    /**
//     * 是否需要响应语音语义
//     * 当前是网联并且焦点在音乐处
//     *
//     * @return T 响应 F 取消响应
//     */
//    private boolean needToRespondVr() {
//        //是否网联项目
//        boolean isNet = CarConfigUtil.getDefault().isNet();
//        String sourceName = AudioFocusUtils.getInstance().getCurrentAudioSourceName();
//        Log.i(TAG, "needToRespondVr: sourceName = " + sourceName + " isNet = " + isNet);
//        if (!isNet) {
//            return true;
//        }
//        return DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName) || DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName);
//    }

    /**
     * 网联 欧盟也没有在线音乐，需要响应
     * @return
     */
    private boolean needToRespondOpenVr() {
        //是否网联项目
        boolean isNet = CarConfigUtil.getDefault().isNet();
        String sourceName = AudioFocusUtils.getInstance().getCurrentAudioSourceName();
        Log.i(TAG, "needToRespondVr: sourceName = " + sourceName + " isNet = " + isNet + " isEUVersion = " + isEUVersion);
        if (!isNet || isEUVersion) {
            return true;
        }
        return DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName) || DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName);
    }

}
