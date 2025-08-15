相当于一个[[IGetControlTool]],[[IControlTool]],[[IStatusTool]]的管理中心

具体逻辑是别的地方注册tool到该成员变量中,其他地方要用的时候再在这个地方获取

[[MusicStatusUtils]] ---注册回调---> [[ModuleUSBMusicTrigger]]  ---高层调用---> 

 ![[assets/{D006A6EB-46DD-453D-84B7-25C8D3B31E96} 1.png]]

高层调用包括
1. [[MusicVrManager]]
2. [[MediaKeyActionManager]]
	1. 这里在handlerKeyAction方法中进行派发指令，不同的keyAction来调用