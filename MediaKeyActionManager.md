关于handlerKeyAction
```java
/**  
 * 处理按键事件  
 */  
private void handlerKeyAction(Message msg) {  
    int keyCode = msg.arg1;  
    int keyAction = msg.arg2;  
    if (keyAction == VDValueAction.ACTION_RELEASE) {  
        //短按处理  
        // TODO: 2022-12-14 这里需要根据当前音源是不是自己的源再进行处理  
        IControlTool controlTool = null;  
        String sourceName = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(context);  
        Log.d(TAG, "handlerKeyAction: sourceName = " + sourceName);  
        if (DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName)) {  
            controlTool = ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool;  
        } else if (DsvAudioSDKConstants.USB1_MUSIC_SOURCE.equals(sourceName)) {  
            controlTool = ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool;  
        } else if (DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName)) {  
            controlTool = ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool;  
        }  
        if (controlTool == null) {  
            return;  
        }  
        int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());  
        Log.d(TAG, "handlerKeyAction: mediaType = " + mediaType);  
        //当前已经是媒体源了，如果当前是最近播放控制器，需要特殊处理下  
        if (MediaType.RECENT_MUSIC.ordinal() == mediaType) {  
            controlTool = ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.getControlTool();  
        }     
		switch (keyCode) {
		// 较为复杂的逻辑，省略
		}
    }  
}
```

该代码属于[[SVModuleUSBMusic]]