实现了一个媒体播放状态管理器，用于监听不同音源（如本地、USB0、USB1）的状态变化，并在状态改变时触发相应的回调方法。


包含四个[[IMediaStatusChange]]类型的成员变量
1. mLocalMediaStatusChange
2. mUSB1MediaStatusChange
3. mUSB2MediaStatusChange
4. mRecentMediaStatusChange
这四个回调注册在[[ModuleUSBMusicTrigger]]中的四个不同类型的[[IGetControlTool]]中
这些回调存在调用[[MusicStatusUtils]]的行为