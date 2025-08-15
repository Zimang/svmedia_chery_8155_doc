addObserver  管理[[IMediaObserver]]
实际上也只有一个地方（[[MainActivity]].initViewListener）调用了这个接口

这个对象会被 [[RadioStatusUtils]]  [[MusicStatusUtils]]调用

内部存在一个[[MediaInfoBean]]变量，currentMediaInfoBean，也是会被[[MusicStatusUtils]]在[[ModuleUSBMusicTrigger]]中注册的回调所调用