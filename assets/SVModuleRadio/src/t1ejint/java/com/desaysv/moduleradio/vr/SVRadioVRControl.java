package com.desaysv.moduleradio.vr;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.radio.RadioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.VDValueVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRUpload;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.utils.JsonUtils;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;

import java.lang.ref.WeakReference;
import java.util.List;
import com.desaysv.moduledab.utils.ProductUtils;
/**
 * created by ZNB on 2022-12-23
 * 语音控制器，用来初始化语音VDB，注册语义监听等
 */
public class SVRadioVRControl {
    private static final String TAG = "SVRadioVRControl";

    public static final int TARGET_OPEN_FM = 0;
    public static final int TARGET_OPEN_AM = 1;
    public static final int TARGET_OPEN_DAB = 2;
    private static SVRadioVRControl instance;

    private static boolean isNet = false;
    public static SVRadioVRControl getInstance(){
        if (instance == null){
            synchronized (SVRadioVRControl.class){
                if (instance == null){
                    instance = new SVRadioVRControl();
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造方法
     */
    private SVRadioVRControl(){

    }

    /**
     * 初始化Radio语义监听
     */
    public void init(){
        VDBus.getDefault().init(AppBase.mContext);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.VR);//避免收不到语音VDB事件的问题
        VDBus.getDefault().addSubscribe(VDEventVR.VR_RADIO, VDThreadType.MAIN_THREAD);
        VDBus.getDefault().addSubscribe(VDEventVR.VR_MEDIA, VDThreadType.MAIN_THREAD);
        VDBus.getDefault().subscribeCommit(); // 提交订阅
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);

        //监听电台内容的变化
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);

        mHandler = new MyHandler(this);

        isNet = ProductUtils.isNet();
    }

    private static VDNotifyListener mVDNotifyListener = new VDNotifyListener() {
        public void onVDNotify(VDEvent event, int threadType) {
            if (threadType == VDThreadType.MAIN_THREAD) { // 主线程回调处理
                Log.d(TAG,"onVDNotify:"+event.getId());
                switch (event.getId()) {
                    case VDEventVR.VR_RADIO:
                        VDVRPipeLine param = VDVRPipeLine.getValue(event);
                        // 内部Json语义表定义的Key值(参考: https://docs.qq.com/sheet/DZVBUYlBFUERNTG5q?tab=o9ktwo)
                        String key = param.getKey();
                        Log.d(TAG,"on VR_RADIO,key:"+key);
                        // 内部Json语义表定义的Josn数据, 例: {"action":"OPEN","position":"F","type":"","value":""}
                        String data = param.getValue();
                        Log.d(TAG,"on VR_RADIO,data:"+data);
                        SVRadioVRBean svRadioVRBean = JsonUtils.generateObject(data, SVRadioVRBean.class);
                        handleVR(key,svRadioVRBean);
                        break;
                    case VDEventVR.VR_MEDIA:
                        //这个用于响应通用语义
                        if (RadioControlTool.getInstance().checkCurrentRadioSource()){
                            VDVRPipeLine param1 = VDVRPipeLine.getValue(event);
                            // 内部Json语义表定义的Key值(参考: https://docs.qq.com/sheet/DZVBUYlBFUERNTG5q?tab=o9ktwo)
                            String key1 = param1.getKey();
                            Log.d(TAG,"on VR_MEDIA,key:"+key1);
                            // 内部Json语义表定义的Josn数据, 例: {"action":"OPEN","position":"F","type":"","value":""}
                            String data1 = param1.getValue();
                            Log.d(TAG,"on VR_MEDIA,data:"+data1);
                            SVRadioVRBean svRadioVRBean1 = JsonUtils.generateObject(data1, SVRadioVRBean.class);
                            handleVR(key1,svRadioVRBean1);
                        }
                        break;
                }
            }
        }
    };

    /**
     * 根据语音进行对应操作
     * @param key
     * @param svRadioVRBean
     */
    private static void handleVR(String key,SVRadioVRBean svRadioVRBean){
        Log.d(TAG,"handleVR,key:"+key+",svRadioVRBean:"+svRadioVRBean);
        switch (key){
            case SVRadioVRConstant.Key.PLAY_BY_BAND:
                handlePlayByBand(svRadioVRBean);
                break;
            case SVRadioVRConstant.Key.SCAN_BAND:
                handleScanBand(svRadioVRBean);
                break;
            case SVRadioVRConstant.Key.CONTROL_COLLECT:
            case SVRadioVRConstant.Key.CONTROL_COLLECT_COMMON:
                if (isNet){
                    if (RadioControlTool.getInstance().checkCurrentRadioSource()){
                        handleControlCollect(svRadioVRBean);
                    }
                }else {
                    handleControlCollect(svRadioVRBean);
                }
                break;
            case SVRadioVRConstant.Key.CONTROL_PLAY:
                if (isNet){
                    if (RadioControlTool.getInstance().checkCurrentRadioSource()){
                        handleControlPlay(svRadioVRBean);
                    }
                }else {
                    handleControlPlay(svRadioVRBean);
                }
                break;
            case SVRadioVRConstant.Key.CONTROL_PLAY_LIST:
            case SVRadioVRConstant.Key.CONTROL_PLAY_LIST1:
                handleControlPlayList(svRadioVRBean);
                break;
            case SVRadioVRConstant.Key.REQUEST_STATUS:
                handleRequestStatus();
                break;
            default:
                break;
        }

    }

    /**
     * 处理 {@link SVRadioVRConstant.Key}
     * @param svRadioVRBean
     */
    private static void handlePlayByBand(SVRadioVRBean svRadioVRBean){
        Log.d(TAG,"handlePlayByBand,svRadioVRBean:"+svRadioVRBean);
        if (svRadioVRBean.getSemantic() == null){
            return;
        }
        String action = svRadioVRBean.getSemantic().getAction();
        String type = svRadioVRBean.getSemantic().getType();
        String value = svRadioVRBean.getSemantic().getValue();
        switch (action){
            case SVRadioVRConstant.Action.PLAY:
                dealPlay(type,value);
                break;
            case SVRadioVRConstant.Action.OPEN:
                dealOpen();
                break;
            default:
                break;
        }
    }

    /**
     * 实现打开电台播放的逻辑
     */
    private static void dealOpen(){
        {
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());

            if(SVRadioVRStateUtil.getInstance().isOnRadioSource()){
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_play_opened);
                handleResponseTTS(s);
            }else {
                //goto RadioHomeFragment
                Intent intent = new Intent();
                intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.Source.SOURCE_KEY, Constants.Source.SOURCE_RADIO);
                intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_MAIN);
                AppBase.mContext.startActivity(intent);
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_play_closed);
                handleResponseTTS(s);
            }
        }
    }

    /**
     * 实现播放逻辑
     * @param type
     * @param value
     */
    private static void dealPlay(String type,String value){
        Log.d(TAG,"dealPlay，type："+type+"value："+value);
        if (value != null && value.length() > 0){//确认是否带频点参数
            RadioMessage radioMessage = new RadioMessage(covertTypeToBand(type),covertValueToFreq(type, value));
            int targetState = SVRadioVRStateUtil.getInstance().checkTargetState(radioMessage);
            Log.d(TAG,"dealPlay，targetState："+targetState);
            if (targetState == SVRadioVRConstant.TargetState.STATE_FM_PLAYED){
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_fmfreq_playing);
                handleResponseTTS(s);
                return;
            }else if (targetState == SVRadioVRConstant.TargetState.STATE_AM_PLAYED){
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_amfreq_playing);
                handleResponseTTS(s);
                return;
            }else if (targetState == SVRadioVRConstant.TargetState.STATE_OVER_RANGE){
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_amfreq_overrange);
                handleResponseTTS(s);
                return;
            }else if (targetState == SVRadioVRConstant.TargetState.STATE_NOT_FM_EFFECT){
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_fmfreq_unavailable);
                handleResponseTTS(s);
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,SVRadioVRStateUtil.getInstance().getAdjacentRadioMessage());
                return;
            }else if (targetState == SVRadioVRConstant.TargetState.STATE_NOT_AM_EFFECT){
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_amfreq_unavailable);
                handleResponseTTS(s);
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,SVRadioVRStateUtil.getInstance().getAdjacentRadioMessage());
                return;
            }else {
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,radioMessage);
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_amfreq_play);
                handleResponseTTS(s);
            }

        }else {
            if (SVRadioVRConstant.Type.FM.equals(type)){
                if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_FM && ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()){
                    //当前已经在播放FM
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playfm_playing);
                    handleResponseTTS(s);
                }else {
                    //当前没有播放FM
                    RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage();
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL, radioMessage);
                    waitOpenUI(TARGET_OPEN_FM);
                }
            }else if (SVRadioVRConstant.Type.AM.equals(type)){
                if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_AM && ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()){
                    //当前已经在播放AM
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playam_playing);
                    handleResponseTTS(s);
                }else {
                    //当前没有播放AM
                    RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage();
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL, radioMessage);
                    waitOpenUI(TARGET_OPEN_AM);
                }
            }else if (SVRadioVRConstant.Type.DAB.equals(type)){
                if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getRadioBand() == RadioMessage.DAB_BAND && ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()){
                    //当前已经在播放DAB
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playdab_playing);
                    handleResponseTTS(s);
                }else {
                    //当前没有播放DAB
                    RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage();
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL, radioMessage);
                    waitOpenUI(TARGET_OPEN_DAB);
                }
            }else {
                //播放默认电台
                if (isNet){
                    if (RadioControlTool.getInstance().checkCurrentRadioSource()){
                        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                    }
                }else {
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                }}
        }
    }

    /**
     * 将 Type 转换成 Band
     * @param type
     * @return
     */
    private static int covertTypeToBand(String type){
        if (SVRadioVRConstant.Type.FM.equals(type)){
            return RadioManager.BAND_FM;
        }else if (SVRadioVRConstant.Type.AM.equals(type)){
            return RadioManager.BAND_AM;
        }else {
            return RadioManager.BAND_FM;
        }
    }

    /**
     * 将 value 转换成 频点值
     * @param type
     * @param value
     * @return
     */
    private static int covertValueToFreq(String type,String value){
        if (SVRadioVRConstant.Type.FM.equals(type)){
            return (int) (Float.parseFloat(value));//语义转换过来已经是乘以1000
        }else if (SVRadioVRConstant.Type.AM.equals(type)){
            return (int) Float.parseFloat(value);
        }
        return (int) (Float.parseFloat(value) * 1000);
    }


    /**
     * 处理 {@link SVRadioVRConstant.Key}
     * @param svRadioVRBean
     */
    private static void handleScanBand(SVRadioVRBean svRadioVRBean){
        Log.d(TAG,"handleScanBand,svRadioVRBean:"+svRadioVRBean);
        if (svRadioVRBean.getSemantic() == null){
            return;
        }
        String action = svRadioVRBean.getSemantic().getAction();
        String type = svRadioVRBean.getSemantic().getType();
        switch (action){
            case SVRadioVRConstant.Action.OPEN://开始扫描
                dealScan(type);
                break;
            case SVRadioVRConstant.Action.CLOSE://停止扫描
                dealStopScan();
                break;
            default:
                break;
        }
    }


    /**
     * 实现扫描逻辑
     * @param type
     */
    private static void dealScan(String type){
        Log.d(TAG,"dealScan，type："+type+"value");
        if ( ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSearching()||ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSeeking()){
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_scan_scanning);
            handleResponseTTS(s);
            return;
        }

        String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_scan_unscanning);
        handleResponseTTS(s);
        if (SVRadioVRConstant.Type.FM.equals(type)){
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage();
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SPECIFIES_AST, ChangeReasonData.VR_CONTROL,radioMessage);
        }else if (SVRadioVRConstant.Type.AM.equals(type)){
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage();
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SPECIFIES_AST, ChangeReasonData.VR_CONTROL,radioMessage);
        }else if (SVRadioVRConstant.Type.DAB.equals(type)){
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage();
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SPECIFIES_AST, ChangeReasonData.VR_CONTROL,radioMessage);
        }else {
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.AST, ChangeReasonData.VR_CONTROL);
        }
    }

    /**
     * 实现停止扫描逻辑
     */
    private static void dealStopScan(){
        Log.d(TAG,"dealStopScan");
        if ( ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSearching()||ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSeeking()){
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_stopscan_scanning);
            handleResponseTTS(s);
            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.VR_CONTROL);
            return;
        }
        String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_stopscan_unscanning);
        handleResponseTTS(s);
    }


    /**
     * 处理 {@link SVRadioVRConstant.Key}
     * @param svRadioVRBean
     */
    private static void handleControlCollect(SVRadioVRBean svRadioVRBean){
        Log.d(TAG,"handleControlCollect,svRadioVRBean:"+svRadioVRBean);
        if (svRadioVRBean.getSemantic() == null){
            return;
        }
        String action = svRadioVRBean.getSemantic().getAction();
        String type = svRadioVRBean.getSemantic().getType();
        switch (action){
            case SVRadioVRConstant.Action.OPEN://打开收藏列表界面
                openCollectList();
                break;
            case SVRadioVRConstant.Action.PLAY://播放收藏列表第一条
                dealPlayCollect(type);
                break;
            case SVRadioVRConstant.Action.CANCEL_COLLECT://取消收藏当前电台
                dealCollect(false);
                break;
            case SVRadioVRConstant.Action.COLLECT://收藏当前电台
                dealCollect(true);
                break;
            default:
                break;
        }
    }

    /**
     * 打开收藏列表界面
     */
    private static void openCollectList(){
        if(SVRadioVRStateUtil.getInstance().isOnRadioCollect()){
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_opencollect_opened);
            handleResponseTTS(s);
        }else {
            Intent intent = new Intent();
            intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.Source.SOURCE_KEY, Constants.Source.SOURCE_RADIO);
            intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_VR_MAIN_COLLECT);
            AppBase.mContext.startActivity(intent);
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_opencollect_open);
            handleResponseTTS(s);
        }
    }

    /**
     * 播放收藏列表，默认第一条
     * @param type
     */
    private static void dealPlayCollect(String type){

        if (RadioList.getInstance().getAllCollectRadioMessageList().size() > 0){
            RadioMessage current = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            if (current.isCollect() && ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()){
                String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playcollect_playing);
                handleResponseTTS(s);
                return;
            }

            if (SVRadioVRConstant.Type.FM.equals(type)){
                if (RadioList.getInstance().getFMCollectRadioMessageList().size() > 0){
                    RadioMessage radioMessage = RadioList.getInstance().getFMCollectRadioMessageList().get(0);
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,radioMessage);
                }
            }else if (SVRadioVRConstant.Type.AM.equals(type)){
                if (RadioList.getInstance().getAMCollectRadioMessageList().size() > 0){
                    RadioMessage radioMessage = RadioList.getInstance().getAMCollectRadioMessageList().get(0);
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,radioMessage);
                }
            }else if (SVRadioVRConstant.Type.DAB.equals(type)){
                if (RadioList.getInstance().getDABCollectRadioMessageList().size() > 0){
                    RadioMessage radioMessage = RadioList.getInstance().getDABCollectRadioMessageList().get(0);
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,radioMessage);
                }
            }else {
                RadioMessage radioMessage = RadioList.getInstance().getAllCollectRadioMessageList().get(0);
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,radioMessage);
            }
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playcollect_play);
            handleResponseTTS(s);
        }else {
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playcollect_collectempty);
            handleResponseTTS(s);
        }

    }

    /**
     * 收藏/取消收藏
     * @param collect
     */
    private static void dealCollect(boolean collect){
        boolean isPlaying = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying();
        Log.d(TAG,"dealCollect,isPlaying:"+isPlaying);
        if (!isPlaying){//没有在播放
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_collect_noplaying);
            handleResponseTTS(s);
        }else {
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            if (collect) {//开始收藏
                if (radioMessage.isCollect()){//已经被收藏
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_collect_playing_collected);
                    handleResponseTTS(s);
                }else {
                    //设计问题，FM/AM/DAB各自最多收藏30，所以需要分开判断
                    if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){
                        if (RadioList.getInstance().getDABCollectRadioMessageList().size() > 29){
                            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_collect_fulllist);
                            handleResponseTTS(s);
                            return;
                        }else {

                        }
                    }else {
                        if (RadioList.getInstance().isFMAMFullCollect()){
                            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_collect_fulllist);
                            handleResponseTTS(s);
                            return;
                        }
                    }

                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.VR_CONTROL, radioMessage);
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_collect_playing_collect);
                    handleResponseTTS(s);

                    //埋点：收藏
                    if (radioMessage.getRadioBand() == RadioManager.BAND_FM){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.FMCollect,Point.Field.CollOperType,Point.FieldValue.COLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                                ,Point.Field.RadioName,radioMessage.getCalculateFrequency()
                                ,Point.Field.Mhz,radioMessage.getCalculateFrequency());
                    }else if (radioMessage.getRadioBand() == RadioManager.BAND_AM){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.AMCollect,Point.Field.CollOperType,Point.FieldValue.COLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                                ,Point.Field.RadioName,radioMessage.getCalculateFrequency()
                                ,Point.Field.Mhz,radioMessage.getCalculateFrequency());
                    }else if (radioMessage.getDabMessage() != null){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.DABCollect,Point.Field.CollOperType,Point.FieldValue.COLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                                ,Point.Field.RadioName,radioMessage.getDabMessage().getProgramStationName()
                                ,Point.Field.Mhz,String.valueOf(radioMessage.getDabMessage().getFrequency()));
                    }

                }
            } else {
                if (radioMessage.isCollect()){
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.VR_CONTROL, radioMessage);
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_uncollect_playing_uncollect);
                    handleResponseTTS(s);

                    //埋点：取消收藏
                    if (radioMessage.getRadioBand() == RadioManager.BAND_FM){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.FMCollect,Point.Field.CollOperType,Point.FieldValue.UNCOLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                                ,Point.Field.RadioName,radioMessage.getCalculateFrequency()
                                ,Point.Field.Mhz,radioMessage.getCalculateFrequency());
                    }else if (radioMessage.getRadioBand() == RadioManager.BAND_AM){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.AMCollect,Point.Field.CollOperType,Point.FieldValue.UNCOLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                                ,Point.Field.RadioName,radioMessage.getCalculateFrequency()
                                ,Point.Field.Mhz,radioMessage.getCalculateFrequency());
                    }else if (radioMessage.getDabMessage() != null){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.DABCollect,Point.Field.CollOperType,Point.FieldValue.UNCOLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                                ,Point.Field.RadioName,radioMessage.getDabMessage().getProgramStationName()
                                ,Point.Field.Mhz,String.valueOf(radioMessage.getDabMessage().getFrequency()));
                    }

                }else {
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_uncollect_playing_uncollected);
                    handleResponseTTS(s);
                }
            }
        }
    }


    /**
     * 处理 {@link SVRadioVRConstant.Key}
     * @param svRadioVRBean
     */
    private static void handleControlPlay(SVRadioVRBean svRadioVRBean){
        Log.d(TAG,"handleControlPlay,svRadioVRBean:"+svRadioVRBean);
        if (svRadioVRBean.getSemantic() == null){
            return;
        }
        String action = svRadioVRBean.getSemantic().getAction();
        String type = svRadioVRBean.getSemantic().getType();
        String value = svRadioVRBean.getSemantic().getValue();

        RadioMessage currentRadio  = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG,"handleControlPlay,currentRadio:"+currentRadio);
        switch (action){
            case SVRadioVRConstant.Action.PAUSE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PAUSE, ChangeReasonData.VR_CONTROL);
                //埋点：暂停
                if (currentRadio.getRadioBand() == RadioManager.BAND_FM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate,Point.Field.PlayOperType,Point.FieldValue.PAUSE
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getRadioBand() == RadioManager.BAND_AM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate,Point.Field.PlayOperType,Point.FieldValue.PAUSE
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getDabMessage() != null){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate,Point.Field.PlayOperType,Point.FieldValue.PAUSE
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getDabMessage().getProgramStationName()
                            ,Point.Field.Mhz,String.valueOf(currentRadio.getDabMessage().getFrequency()));
                }
                break;
            case SVRadioVRConstant.Action.PLAY:
                if (value != null && value.length() > 0){//按照协议，value不为空，说明需要播放第value个电台
                    int position = Integer.parseInt(value) - 1;//正常说话的第一个实际是列表的第0个
                    if (position < 0){//避免有人说第0个
                        return;
                    }
                    //这里说明没有特定打开的音源，则根据当前的音源分配
                    String sourceName = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext);
                    Log.d(TAG,"handleControlPlay,sourceName:"+sourceName+",position:"+position);
                    switch (sourceName){
                        case DsvAudioSDKConstants.DAB_SOURCE:
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,RadioList.getInstance().getDABEffectRadioMessageList().get(position));
                            break;
                        case DsvAudioSDKConstants.AM_SOURCE:
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,RadioList.getInstance().getAMEffectRadioMessageList().get(position));
                            break;
                        case DsvAudioSDKConstants.FM_SOURCE:
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL,RadioList.getInstance().getFMEffectRadioMessageList().get(position));
                            break;
                    }
                    String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_openplaylist_opened);
                    handleResponseTTS(s);
                }else {
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PLAY, ChangeReasonData.VR_CONTROL);
                }
                //埋点：播放
                if (currentRadio.getRadioBand() == RadioManager.BAND_FM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate,Point.Field.PlayOperType,Point.FieldValue.PLAY
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getRadioBand() == RadioManager.BAND_AM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate,Point.Field.PlayOperType,Point.FieldValue.PLAY
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getDabMessage() != null){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate,Point.Field.PlayOperType,Point.FieldValue.PLAY
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getDabMessage().getProgramStationName()
                            ,Point.Field.Mhz,String.valueOf(currentRadio.getDabMessage().getFrequency()));
                }
                break;
            case SVRadioVRConstant.Action.NEXT:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SEEK_FORWARD, ChangeReasonData.VR_CONTROL);
                //埋点：下一曲
                if (currentRadio.getRadioBand() == RadioManager.BAND_FM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate,Point.Field.PlayOperType,Point.FieldValue.NEXT
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getRadioBand() == RadioManager.BAND_AM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate,Point.Field.PlayOperType,Point.FieldValue.NEXT
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getDabMessage() != null){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate,Point.Field.PlayOperType,Point.FieldValue.NEXT
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getDabMessage().getProgramStationName()
                            ,Point.Field.Mhz,String.valueOf(currentRadio.getDabMessage().getFrequency()));
                }
                break;
            case SVRadioVRConstant.Action.PREVIOUS:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SEEK_BACKWARD, ChangeReasonData.VR_CONTROL);
                //埋点：上一曲
                if (currentRadio.getRadioBand() == RadioManager.BAND_FM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate,Point.Field.PlayOperType,Point.FieldValue.PRE
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getRadioBand() == RadioManager.BAND_AM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate,Point.Field.PlayOperType,Point.FieldValue.PRE
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadio.getCalculateFrequency());
                }else if (currentRadio.getDabMessage() != null){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate,Point.Field.PlayOperType,Point.FieldValue.PRE
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,currentRadio.getDabMessage().getProgramStationName()
                            ,Point.Field.Mhz,String.valueOf(currentRadio.getDabMessage().getFrequency()));
                }
                break;
            default:
                break;
        }
    }

    /**
     * 处理 {@link SVRadioVRConstant.Key}
     * @param svRadioVRBean
     */
    private static void handleControlPlayList(SVRadioVRBean svRadioVRBean){
        Log.d(TAG,"handleControlPlayList,svRadioVRBean:"+svRadioVRBean);
        if (svRadioVRBean.getSemantic() == null){
            return;
        }
        String action = svRadioVRBean.getSemantic().getAction();
        String type = svRadioVRBean.getSemantic().getType();
        Intent intent = new Intent();
        intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.Source.SOURCE_KEY, Constants.Source.SOURCE_RADIO);
        if (action.equals(SVRadioVRConstant.Action.OPEN)){
            intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_VR_MAIN_LIST);
        }else {
            intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_MAIN);
        }
        AppBase.mContext.startActivity(intent);
    }

    /**
     * 处理 {@link SVRadioVRConstant.Key}
     */
    private static void handleRequestStatus(){
        Log.d(TAG,"handleRequestStatus");
        VDVRUpload param = new VDVRUpload();
        param.setPkgName(AppBase.mContext.getPackageName());
        param.setKey(SVRadioVRConstant.VRUploadRadioStatus.VR_RADIO_PLAY_RESPONSE);

        //赋值 responseBean
        SVRadioResponseBean.Data.dataInfo dataInfo = new SVRadioResponseBean.Data.dataInfo();
        dataInfo.setCode(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getCalculateFrequency());
        if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getRdsRadioText() != null) {
            dataInfo.setName(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getRdsRadioText().getProgramStationName());
        }

        SVRadioResponseBean.Data data = new SVRadioResponseBean.Data();
        data.setDataInfo(dataInfo);
        data.setActiveStatus(isAudioTop() ? SVRadioVRConstant.VRUploadRadioValue.VR_RADIO_STATUE_FG : SVRadioVRConstant.VRUploadRadioValue.VR_RADIO_STATUE_BG);
        data.setSceneStatus(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying() ? SVRadioVRConstant.VRUploadRadioValue.VR_RADIO_STATUE_PLAY:SVRadioVRConstant.VRUploadRadioValue.VR_RADIO_STATUE_PAUSE);

        SVRadioResponseBean responseBean = new SVRadioResponseBean();
        responseBean.setData(data);

        Log.d(TAG,"handleRequestStatus,responseBean:"+responseBean);
        param.setData(JsonUtils.generateJson(responseBean));
        param.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_NOTIFY);
        VDEvent event = VDVRUpload.createEvent(VDEventVR.VR_DATA_UPLOAD, param);
        VDBus.getDefault().set(event);
    }



    /**
     * 获取系统顶层界面的包名
     *
     * @return Audio应用是否前台
     */
    private static boolean isAudioTop() {
        String topPackageName;
        ActivityManager activityManager = (ActivityManager) (AppBase.mContext.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos.size() < 1){
            return false;
        }
        ComponentName componentName = runningTaskInfos.get(0).topActivity;
        topPackageName = componentName.getPackageName();
        Log.d(TAG, "getTopActivityPackageName: topPackageName = " + topPackageName);
        return "com.desaysv.svaudioapp".equals(topPackageName);
    }



    /**
     * 播放语音TTS反馈语
     * @param ttsString 当前要播报的内容
     */
    private static void handleResponseTTS(String ttsString){
        Log.d(TAG,"handleResponseTTS:"+ ttsString);
        // TTS播报（set）
        VDVRPipeLine param = new VDVRPipeLine();
        param.setKey(VDValueVR.VRSemanticKey.VR_CONTROL_RESPONSE);
        param.setValue(ttsString);
        param.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_SUCCEED_AND_TTS); //根据执行结果反馈
        VDEvent event = VDVRPipeLine.createEvent(VDEventVR.VR_RADIO, param);
        VDBus.getDefault().set(event);
    }

    private final IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG,"onCurrentRadioMessageChange");
            if (!ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSearching()) {
                handleRequestStatus();
            }
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG,"onPlayStatusChange,isPlaying:"+isPlaying);
            handleRequestStatus();
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG,"onAstListChanged");
        }

        @Override
        public void onSearchStatusChange(boolean isSearching) {
            Log.d(TAG,"onSearchStatusChange");
            handleRequestStatus();
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG,"onSeekStatusChange");
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {
            Log.d(TAG,"onAnnNotify");
        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG,"onRDSFlagInfoChange");
        }
    };


    private final IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG,"onFMCollectListChange");
            handleRequestStatus();
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG,"onAMCollectListChange");
            handleRequestStatus();
        }

        @Override
        public void onDABCollectListChange() {
            Log.d(TAG,"onDABCollectListChange");
            handleRequestStatus();
        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG,"onFMEffectListChange");
            handleRequestStatus();
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG,"onAMEffectListChange");
            handleRequestStatus();
        }

        @Override
        public void onDABEffectListChange() {
            Log.d(TAG,"onDABEffectListChange");
            handleRequestStatus();
        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG,"onFMAllListChange");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG,"onAMAllListChange");
        }

        @Override
        public void onDABAllListChange() {
            Log.d(TAG,"onDABAllListChange");
        }
    };


    private static void waitOpenUI(int target) {
        Log.d(TAG,"waitOpenUI,target:"+target);
        mHandler.sendEmptyMessageDelayed(target,150);
    }

    public void openUI(int target) {
        Log.d(TAG,"openUI,target:"+target);
        if (target == TARGET_OPEN_FM){
            //打开到列表界面
            //goto RadioHomeFragment
            Intent intent = new Intent();
            intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.Source.SOURCE_KEY, Constants.Source.SOURCE_RADIO);
            intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_VR_MAIN_FM);
            AppBase.mContext.startActivity(intent);
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playfm_unplaying);
            handleResponseTTS(s);
            //埋点：打开FM
            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.OPEN
                    ,Point.Field.OperStyle,Point.FieldValue.OpeVR);
        }else if (target == TARGET_OPEN_AM){
            //打开到列表界面
            //goto RadioHomeFragment
            Intent intent = new Intent();
            intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.Source.SOURCE_KEY, Constants.Source.SOURCE_RADIO);
            intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_VR_MAIN_AM);
            AppBase.mContext.startActivity(intent);
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playam_unplaying);
            handleResponseTTS(s);
            //埋点：打开AM
            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickFM,Point.Field.OperType,Point.FieldValue.OPEN
                    ,Point.Field.OperStyle,Point.FieldValue.OpeVR);
        }else if (target == TARGET_OPEN_DAB){
            //打开到列表界面
            //goto RadioHomeFragment
            Intent intent = new Intent();
            intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.Source.SOURCE_KEY, Constants.Source.SOURCE_RADIO);
            intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_VR_MAIN_DAB);
            AppBase.mContext.startActivity(intent);
            String s = AppBase.mContext.getResources().getString(R.string.radio_media_radio_playdab_unplaying);
            handleResponseTTS(s);
            //埋点：打开DAB
            PointTrigger.getInstance().trackEvent(Point.KeyName.OpenCloseClickDAB,Point.Field.OperType,Point.FieldValue.OPEN
                    ,Point.Field.OperStyle,Point.FieldValue.OpeVR);
        }
    }


    public static MyHandler mHandler;

    private static class MyHandler extends Handler {
        WeakReference<SVRadioVRControl> weakReference;

        MyHandler(SVRadioVRControl svRadioVRControl) {
            weakReference = new WeakReference<>(svRadioVRControl);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            weakReference.get().openUI(msg.what);
        }
    }
}
