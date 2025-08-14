因为SVSDKMedia引用到了SVSDKLocalMedia的类，如果只提供SVSDKMedia的 jar包，会导致运行时错误(SVSDKLocalMedia的类会找不到)。
因此提供SVSDKMedia的 jar包的话，也必须同时提供SVSDKLocalMedia的 jar包，不太合适给到外部使用。
因此新增这个SDK的包是专门用来替代 SVSDKMedia，主要是为了提供单一的 jar 给外部使用。
使用方式：
        DsvSDKMediaManager.getInstance().initialize(this);
        DsvSDKMediaManager.getInstance().registerMediaInfoCallback();
        服务连接回调成功后，可以使用DsvSDKMediaManager.getInstance().play()等进行操作。