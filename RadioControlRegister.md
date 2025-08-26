
单例

初始化

```JAVA
private void initialize() {  
    RadioConfig.getInstance().initialize(RadioConfig.getInstance().getCurrentRegion());  
    RadioMessageSaveUtils.getInstance().initialize(AppBase.mContext);  
    RadioListSaveUtils.getInstance().initialize(AppBase.mContext);  
    RadioList.getInstance().initialize();  
    //焦点工具初始化  
    AudioFocusUtils.getInstance().initialize(AppBase.mContext);  
}
```

注册RadioTool

```java
public IGetControlTool registeredRadioTool() {  
    RadioControlAction.getInstance().initialize();  
    return getControlTool;  
}
```