package com.desaysv.modulebtmusic.utils;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.base.VDValue;
import com.desaysv.ivi.vdb.event.id.bt.VDEventBT;
import com.desaysv.ivi.vdb.event.id.bt.VDValueBT;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTConnect;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTDevice;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTMusicInfo;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTPair;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.bean.SVDevice;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;

import java.util.ArrayList;

/**
 * 通用解析工具类
 */
public class ParseUtils {
    private static final String TAG = Constants.TAG + "ParseUtils";

    public static ArrayList<SVDevice> getSVConnectedList(int svProfile) {
        ArrayList<SVDevice> svList = new ArrayList<>();
        ArrayList<VDBTDevice> vdList = getVDConnectedList(svProfile);
        if (vdList == null) {
            Log.w(TAG, "getSVConnectedList: vdList == null");
            return svList;
        }
        for (VDBTDevice vdbtDevice : vdList) {
            if (vdbtDevice != null) {
                svList.add(ParseUtils.parseVDDevice(vdbtDevice));
            }
        }
        return svList;
    }

    public static ArrayList<VDBTDevice> getVDConnectedList(int svProfile) {
        Bundle payload = new Bundle();
        payload.putInt(VDKey.TYPE, ParseUtils.parseSVProfileType(svProfile));
        VDEvent event = VDBus.getDefault().getOnce(new VDEvent(VDEventBT.SETTING_CONNECT_LIST, payload));
        return VDBTDevice.getList(event);
    }

    public static String getConnectedAddress(int svProfileType) {
        ArrayList<SVDevice> svConnectedList = getSVConnectedList(svProfileType);
        if (svConnectedList == null || svConnectedList.isEmpty()) {
            Log.w(TAG, "getConnectedAddress: svConnectedList is empty");
            return null;
        }
        return svConnectedList.get(0).getAddress();
    }

    public static SVDevice getConnectedDevice(int svProfileType) {
        ArrayList<SVDevice> svConnectedList = getSVConnectedList(svProfileType);
        if (svConnectedList == null || svConnectedList.isEmpty()) {
            Log.w(TAG, "getConnectedDevice: svConnectedList is empty");
            return null;
        }
        for (SVDevice device : svConnectedList) {
            if (TextUtils.equals(device.getAddress(), getConnectedAddress(svProfileType))) {
                return device;
            }
        }
        return null;
    }

    public static int getSVConnectStatus(int profile) {
        int vdConnectStatus = getVDConnectStatus(profile);
        switch (vdConnectStatus) {
            case VDValue.ConnectStatus.CONNECTED:
                return Constants.ProfileConnectionState.STATE_CONNECTED;
            case VDValue.ConnectStatus.DISCONNECTED:
                return Constants.ProfileConnectionState.STATE_DISCONNECTED;
            default:
                return -1;
        }
    }

    public static int getVDConnectStatus(int profile) {
        VDEvent event = VDBus.getDefault().getOnce(parseSVProfileToConnectEvent(profile));
        VDBTConnect vdbtConnect = VDBTConnect.getValue(event);
        if (vdbtConnect == null) {
            Log.w(TAG, "getVDConnectStatus: vdbtConnect == null");
            return -1;
        }
        return vdbtConnect.getConnectStatus();
    }


    public static int parseVDPowerState(int vdPowerState) {
        switch (vdPowerState) {
            case VDValue.EnableStatus.DISABLED:
                return Constants.BtSwitchState.STATE_OFF;
            case VDValue.EnableStatus.OPENING:
                return Constants.BtSwitchState.STATE_TURNING_ON;
            case VDValue.EnableStatus.ENABLED:
                return Constants.BtSwitchState.STATE_ON;
            case VDValue.EnableStatus.CLOSING:
                return Constants.BtSwitchState.STATE_TURNING_OFF;
            default:
                return -1;
        }
    }

    public static int parseVDBondedState(int vdBondedState) {
        switch (vdBondedState) {
            case VDValueBT.PairStatus.PAIRED:
                return Constants.PairState.BOND_BONDED;
            case VDValueBT.PairStatus.UNPAIRED:
                return Constants.PairState.BOND_NONE;
            case VDValueBT.PairStatus.PAIRING:
                return Constants.PairState.BOND_BONDING;
            default:
                return -1;
        }
    }

    public static int parseSVProfileType(int svProfileType) {
        switch (svProfileType) {
            case Constants.ProfileType.HEADSET_CLIENT:
                return VDValueBT.ProfileType.HEADSET_CLIENT;
            case Constants.ProfileType.PBAP_CLIENT:
                return VDValueBT.ProfileType.PBAP_CLIENT;
            case Constants.ProfileType.MAP_CLIENT:
                return VDValueBT.ProfileType.MAP_CLIENT;
            case Constants.ProfileType.A2DP_SINK:
                return VDValueBT.ProfileType.A2DP_SINK;
            case Constants.ProfileType.AVRCP_CONTROLLER:
                return VDValueBT.ProfileType.AVRCP_CONTROLLER;
            default:
                return -1;
        }
    }

    public static int parseVDProfileType(int vdProfileType) {
        switch (vdProfileType) {
            case VDValueBT.ProfileType.HEADSET_CLIENT:
                return Constants.ProfileType.HEADSET_CLIENT;
            case VDValueBT.ProfileType.PBAP_CLIENT:
                return Constants.ProfileType.PBAP_CLIENT;
            case VDValueBT.ProfileType.MAP_CLIENT:
                return Constants.ProfileType.MAP_CLIENT;
            case VDValueBT.ProfileType.A2DP_SINK:
                return Constants.ProfileType.A2DP_SINK;
            case VDValueBT.ProfileType.AVRCP_CONTROLLER:
                return Constants.ProfileType.AVRCP_CONTROLLER;
            default:
                return -1;
        }
    }

    public static int parseVDProfileConnectionState(int vdConnectionState) {
        switch (vdConnectionState) {
            case VDValue.ConnectStatus.DISCONNECTED:
                return Constants.ProfileConnectionState.STATE_DISCONNECTED;
            case VDValue.ConnectStatus.CONNECTING:
                return Constants.ProfileConnectionState.STATE_CONNECTING;
            case VDValue.ConnectStatus.CONNECTED:
                return Constants.ProfileConnectionState.STATE_CONNECTED;
            case VDValue.ConnectStatus.DISCONNECTING:
                return Constants.ProfileConnectionState.STATE_DISCONNECTING;
            default:
                return -1;
        }
    }

    public static SVDevice parseVDDevice(VDBTDevice vdbtDevice) {
        SVDevice svDevice = new SVDevice();
        if (vdbtDevice == null) {
            Log.w(TAG, "parseVDDevice: vdbtDevice == null");
            return svDevice;
        }
        svDevice.setAddress(vdbtDevice.getMac());
        svDevice.setName(vdbtDevice.getName());
        svDevice.setType(vdbtDevice.getDeviceType());
        svDevice.setHfpState(parseVDProfileConnectionState(vdbtDevice.getHfpStatus()));
        svDevice.setA2dpState(parseVDProfileConnectionState(vdbtDevice.getA2dpStatus()));
        svDevice.setPbapState(parseVDProfileConnectionState(vdbtDevice.getPbapStatus()));
        svDevice.setMapState(parseVDProfileConnectionState(vdbtDevice.getMapStatus()));
        svDevice.setBondState(parseVDBondedState(vdbtDevice.getPairStatus()));
        svDevice.setDeviceClass(vdbtDevice.getDeviceClass());
        return svDevice;
    }

    public static SVDevice parseVDDevice(VDBTPair vdbtPair) {
        SVDevice svDevice = new SVDevice();
        if (vdbtPair == null) {
            Log.w(TAG, "parseVDDevice: vdbtPair == null");
            return svDevice;
        }
        int vdBondedState = parseVDBondedState(vdbtPair.getPairStatus());
        Log.i(TAG, "parseVDDevice: vdBondedState=" + vdBondedState + ",vdbtPair.getPin=" + vdbtPair.getPin());
        svDevice.setAddress(vdbtPair.getMac());
        svDevice.setName(vdbtPair.getName());
        svDevice.setBondState(vdBondedState);
        svDevice.setType(vdbtPair.getDeviceType());
        return svDevice;
    }

    public static int parseSVProfileToConnectEvent(int svProfile) {
        switch (svProfile) {
            case Constants.ProfileType.HEADSET_CLIENT:
                return VDEventBT.SETTING_CONNECT_HFP;
            case Constants.ProfileType.A2DP_SINK:
                return VDEventBT.SETTING_CONNECT_A2DP;
            case Constants.ProfileType.PBAP_CLIENT:
                return VDEventBT.SETTING_CONNECT_PBAP;
            case Constants.ProfileType.MAP_CLIENT:
                return VDEventBT.SETTING_CONNECT_MAP;
            case Constants.ProfileType.AVRCP_CONTROLLER:
                return VDEventBT.SETTING_CONNECT_AVRCP;
            default:
                return -1;
        }
    }

    public static SVMusicInfo parseVDBTMusicInfo(VDBTMusicInfo musicInfo) {
        if (musicInfo == null) {
            Log.w(TAG, "parseVDBTMusicInfo: musicInfo == null");
            return null;
        }

        SVMusicInfo svMusicInfo = new SVMusicInfo();
        svMusicInfo.setMediaTitle(musicInfo.getTitle());
        svMusicInfo.setAlbumName(musicInfo.getAlbum());
        svMusicInfo.setArtistName(musicInfo.getArtist());
        svMusicInfo.setProgress(musicInfo.getPosition());
        svMusicInfo.setDuration(musicInfo.getDuration());
        svMusicInfo.setPlayState(musicInfo.getPlayState());
        svMusicInfo.setMediaId(musicInfo.getMediaId());
        svMusicInfo.setSubtitle(musicInfo.getSubtitle());
        svMusicInfo.setDescription(musicInfo.getDescription());
        svMusicInfo.setIcon(musicInfo.getIcon());
        svMusicInfo.setIconUri(musicInfo.getIconUri());
        svMusicInfo.setMediaUri(musicInfo.getMediaUri());
        svMusicInfo.setExtras(musicInfo.getExtras());
        svMusicInfo.setAlbumCoverArtUri(musicInfo.getAlbumCoverArtUri());
        svMusicInfo.setGenre(musicInfo.getGenre());
        svMusicInfo.setTotalTrackNumber(musicInfo.getTotalTrackNumber());
        svMusicInfo.setTrackNumber(musicInfo.getTrackNumber());
        svMusicInfo.setAlbumCoverArt(musicInfo.getAlbumCoverArt());
        return svMusicInfo;
    }
}
