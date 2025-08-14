MediaObserverManager

具体来说，`MediaObserverManager` 通过注册多个 `IMediaObserver` 观察者来监听媒体信息的变更，包括播放状态、专辑图、当前播放内容等，并在这些信息发生变化时通知所有已注册的观察者。它还支持界面切换时的通知功能，确保主界面能够实时显示各个媒体模块的播放信息。

该类采用单例模式进行管理，提供了添加和移除观察者的方法，并通过一系列设置方法（如 `setPlayStatus`、`setAlbum`、`setMediaInfo` 等）来更新当前媒体信息，并将这些变化广播给所有注册的观察者。此外，它还提供了一个获取当前媒体信息的方法 `getCurrentMediaInfo`，用于在界面初始化时获取后台播放的内容。

总之，`MediaObserverManager` 的作用是充当一个媒体数据变更的通知中心，确保各个模块能够及时感知并更新其显示内容。

- `addObserver`  : [[MainActivity]] 
  - MainActivity内部存在一个observer，在initViewListener这个地方，在onCreate的时候设置
  - 意义在于，MainActivity可以以此订阅这里的信息源
- setPlayStatus [[RadioStatusUtils]]  [[MusicStatusUtils]]
	- 