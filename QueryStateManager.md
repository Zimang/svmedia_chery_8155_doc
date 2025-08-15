#单例  #SVLibUSBDialog 

设置连接状态的管理器

初始化注册媒体扫描广播

```java
//私有变量
private int usb1MusicState = STATE_NO_QUERY;  
private int usb2MusicState = STATE_NO_QUERY;  
private int usb1PictureState = STATE_NO_QUERY;  
private int usb2PictureState = STATE_NO_QUERY;  
private int usb1VideoState = STATE_NO_QUERY;  
private int usb2VideoState = STATE_NO_QUERY;
```


- getter&setter
	- usb1 & usb 2
		- video & picture & music
其中get是返回变量，set则是需要通过[[USB2QueryStateObserver]],[[USB1QueryStateObserver]]来处理，此两者完全继承于[[QueryStateObserver]],换句话讲，其实就是两个类型相同，对象不同的单例

1. getUsb1MusicState & getUsb2MusicState  
	1. [[USBStateReceiver]] 中接收到MEDIA_SCAN_FINISH后进行判断是否需要更新USB1 & USB2
	2. [[SourceDialogUtil]]中的showDialog & showUSB1Dialog会调用
	3. [[SourceDialogUtil]]中的handleMessage会处理