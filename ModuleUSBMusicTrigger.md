#SVModuleUSBMusic 

相当于一个[[IGetControlTool]],[[IControlTool]],[[IStatusTool]]的管理中心

 内部封装多个[[IRequestMediaPlayer]]，该类变量和MediaType一起注册在[[MediaControlRegister]]中，然后存为[[IGetControlTool]]

 

具体逻辑是别的地方注册tool到该成员变量中,其他地方要用的时候再在这个地方获取

[[MusicStatusUtils]] ---注册回调---> [[ModuleUSBMusicTrigger]]  ---高层调用(特指`getCurrentIGetControlTool`)---> manager&adapter&fragment

 ![[assets/{D006A6EB-46DD-453D-84B7-25C8D3B31E96} 1.png]]


包括四组([[IGetControlTool]],[[IControlTool]],[[IStatusTool]]，[[MediaPlayer]])
分别是最近播放音乐，本地音乐，USB1, USB2, 




高层调用包括（这里仅展示`getCurrentIGetControlTool`）
1. manager  （相当于当作api调用了）
	1. [[MusicVrManager]]
	2. [[MediaKeyActionManager]]
		1. 这里在handlerKeyAction方法中进行派发指令，不同的keyAction来调用
2. Adapter (1. 获取playItem  2. 判断当前播放状态)
	1. [[MusicFolderListAdapter]]  1
	2. [[MusicListAdapter]]  1
	3. [[MusicPlayListAdapter]]  1，2都有
3. fragment
	1. [[MusicLocalFragment]] 主要用于删除文件前暂停播放

对于直接获取`IGetControlTool`，毕竟这里是`public`的，我们需要分情况讨论（`getUSB1MusicControlTool`,`getRecentMusicControlTool`,`getLocalMusicControlTool`,`getUSB2MusicControlTool`）

1. `getUSB1MusicControlTool`
	![[assets/{0AE47552-C615-4F32-AD5A-113DC5F71144}.png]]
	1. [[MediaPlaybackService]]
	2. [[USB1MusicPlayFragment]]
	3. [[MusicControlUtils]]
	4.  [[MusicStatusUtils]]
	5. [[MusicVrManager]]
	6. 
2. `getUSB2MusicControlTool`
	![[assets/{0B060330-432E-46AB-B5A7-49AEDFA6ECE8}.png]]
	1. [[MusicStatusUtils]]
	2. [[MusicControlUtils]]
3. `getRecentMusicControlTool`
	![[assets/{DA97B4C9-4710-4332-9E3A-024A5260657E}.png]]
	1. [[MusicPlaybackService]]
	2. [[MusicControlUtils]]
	3. [[MusicStatusUtils]]
	4. [[MusicVrManager]]
	5. [[MusicLocalFragment]]
	6. [[LocalMusicPlayFragment]]
4. `getLocalMusicControlTool`
	![[assets/{DA1466CD-8662-4130-A2F3-A1F8FD97B2B8}.png]]
	1. [[MusicPlaybackService]]
	2. [[LocalMusicPlayFragment]]
	3. [[MusicLocalFragment]]
	4. [[MusicControlUtils]]
	5. [[MusicStatusUtils]]
	6. [[MusicVrManager]]
	7. [[MusicUsbFragment]]