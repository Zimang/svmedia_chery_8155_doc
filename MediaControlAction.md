这是媒体动作的执行类
包含一个[[IRequestMediaPlayer]]的mRequestMediaPlayer

初始化时通过`initMediaPlayer`获取`mMediaPlayer`,我们知道的mRequestMediaPlayer是一个接口，这意味它可以不停的改变，可以是（usb1,usb2,local,recent）之流