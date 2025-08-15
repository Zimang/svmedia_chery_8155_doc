#SVLibUSBDialog #单例 
查询媒体库状态的观察者



该类使用Map<String,[[IQueryPictureStateChangedListener]]> 来管理观察者，而共有三组
1. music
2. picture
3. video


统一使用notifyPicture来publish, attachXXXObserver来add Observer

