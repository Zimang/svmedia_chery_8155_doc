注册媒体播放器的接口，这个接口在应用调用register的时候就需要传入实例，供库获取应用提供的mediaplayer

![[assets/{902D9212-2414-4927-95C7-18769CC230D2}.png]]
在多个地方被调用
1. [[MediaControlAction]]
2. [[MediaControlRegister]]
3. [[ModuleUSBMusicTrigger]]
4. [[BaseVideoPlayActivity]]