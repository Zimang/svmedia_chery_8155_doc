
初始化
```java
@SuppressLint("WrongConstant")  
public synchronized void initialize() {  
    Log.d(TAG, "initialize: isInit = " + isInit);  
    //如果已经初始化了，就不用再次初始化，避免重复初始化，导致了底层芯片的异常  
    if (isInit) {  
        return;  
    }  
    isInit = true;  
    initFirst = true;  
    HandlerThread mHandlerThread = new HandlerThread("HandlerThread-For-RadioAction");  
    mHandlerThread.start();  
    mHandler = new MyHandler(this, mHandlerThread.getLooper());  
    mHandler.sendEmptyMessage(RE_INIT_RADIO_CONTROL);  
    Log.d(TAG, "initialize: initSuccess = " + initSuccess);  
}
```


`initRadio`
```java
/**  
 * 初始化底层收音  
 */  
@SuppressLint("WrongConstant")  
private void initRadio() {  
    initRadio = false;  
    initSuccess = false;  
    RadioManager mRadioManager = (RadioManager) AppBase.mContext.getSystemService(BROADCAST_RADIO);  
    Log.d(TAG, "initRadio: mRadioManager " + mRadioManager);  
    if (mRadioManager == null) {  
        reInitRadioControl();  
        return;  
    }  
    mModules.clear();  
    int status = mRadioManager.listModules(mModules);  
    Log.d(TAG, "initRadio: status = " + status);  
    if (status != RadioManager.STATUS_OK) {  
        reInitRadioControl();  
        return;  
    }  
    Log.d(TAG, "initRadio: mModules = " + mModules.size());  
    if (mModules.size() == 0) {  
        reInitRadioControl();  
        return;  
    }  
    for (RadioManager.BandDescriptor band : mModules.get(0).getBands()) {  
        Log.d(TAG, "initRadio: band = " + band);  
        if (band.isFmBand()) {  
            mFmConfig = new RadioManager.FmBandConfig.Builder((RadioManager.FmBandDescriptor) band).setStereo(true).build();  
        }  
        if (band.isAmBand()) {  
            mAmConfig = new RadioManager.AmBandConfig.Builder((RadioManager.AmBandDescriptor) band).setStereo(true).build();  
        }  
    }  
  
    Log.d(TAG, "initRadio: mFmConfig = " + mFmConfig + " mAmConfig = " + mAmConfig);  
    if (mFmConfig == null && mAmConfig == null) {  
        reInitRadioControl();  
        return;  
    }  
    //初始化电台，用来获取当前底层记忆的电台  
    RadioMessage radioMessage = mCurrentRadioInfo.getCurrentRadioMessage();  
    //如果是DAB的话，需要通过之前的FM或者AM来打开  
    if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {  
        radioMessage = mCurrentRadioInfo.getPreRadioMessage();  
        currentRadioType = RadioMessage.DAB_TYPE;  
        Log.d(TAG, "initRadio: currentRadioType is dab");  
    }  
    Log.d(TAG, "initRadio: radioMessage = " + radioMessage);  
    mRadioTuner = mRadioManager.openTuner(mModules.get(0).getId(), getRadioConfig(radioMessage.getRadioBand()), true, new RadioCallback(), null /* handler */);  
    Log.d(TAG, "initRadio: mRadioTuner = " + mRadioTuner);  
    if (mRadioTuner == null) {  
        reInitRadioControl();  
        return;  
    }  
    // 等待初始化完成、等待；初始化完成后回调 onEventNotify: eventID = 7 ，  
    waitRadioInit();  
    //初始化时赋值给DAB控制器  
    DABControlAction.getInstance().setTuner(mRadioTuner);  
    //初始化时检查是否改变了收音区域配置  
    RadioConfig.getInstance().checkAndSetPreRegion();  
    //初始化完成之后，就需要打开一次频率，这个是底层给的逻辑, 只有第一次电台打开后，才能判别为初始化成功，就绪中。  
    tuneAndSetConfiguration(radioMessage,true);  
    Log.d(TAG, "initRadio: end initSuccess = " + initSuccess);  
}
```

1. 标志位设置
2. 获取mRadioManager,获取不到重新initRadio
3. 清空`private final List<RadioManager.ModuleProperties> mModules = new ArrayList<>();`
	1. [[ArrayList]] 的clear
	2. [[RadioManager]]
4. 