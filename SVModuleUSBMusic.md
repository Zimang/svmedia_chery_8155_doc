[[MusicStatusUtils]]  音乐媒体状态的回调，实际上aidl最终调用的就是这个接口

这一个组件提供两个Service
1. [[MusicAidlService]]
2. [[MediaPlaybackService]]


SVModuleUSBMusic 它co [[SVSDKLocalMedia]]，也就是说他是后者的下游


关于[[MediaKeyActionManager]]，该manager为[[vdbug]]注册回调，使得按键可用