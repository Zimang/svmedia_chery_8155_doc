内部向[[MediaObserverManager]]注册了一个回调，所有的回调出发都是向内部的`mHandler`发送信息`0`
```java
//类似如下代码
@Override  
public void onPlayStatusChanged(String source, boolean isPlaying) {  
    Log.d(TAG, "onPlayStatusChanged,source:" + source + ",isPlaying:" + isPlaying);  
    mHandler.sendEmptyMessage(0);  
}
```

被动触发回调后，MainActivity通过`updateMiniPlayer`主动更新UI
updateMiniPlayer通过[[MediaObserverManager]].getCurrentMediaInfo()更新UI
因为涉及到图片更新，所以用到了`glide`库

  