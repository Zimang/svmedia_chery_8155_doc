#SVLibUSBDialog 

内置一个myHandler

在initDialog的地方注册 3( 图&视&听) * 2(usb 1 & 2) 即六个观察者回调

这个项目不支持usb2 所以实际上部分情况是不存在的

myHandler处理6种情况
更具判断条件来判断是否展示dialog


内置三个[[SourceDialog]]  dialog , dialogUSB1,dialogUSB2
因为不支持usb2 所以实际上使用的是dialog
最开始需要initDialog
1. 初始化myHandler
2. 注册对设备插拔状态的监听
	1. [[USB2DeviceStateObserver]]
	2. [[USB1DeviceStateObserver]]
	3. 
3. 注册媒体查询状态的监听 2 * 3
	1. [[USB1QueryStateObserver]]
	2. [[USB2QueryStateObserver]]