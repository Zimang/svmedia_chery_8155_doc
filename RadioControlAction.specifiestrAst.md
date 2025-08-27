
若radioMessage为空则返回
静音

searchMode置为true


setSearching 调用onSearchStatusChange回调，举例搜索圈圈 （BaseRadioListFragment在start注册好了）

比较请求ast的频率与实际当前播放的频率
1. 不同 switch触发
	1. 切换radioMessage为当前请求ast频率
	2. 通知底层打开收音的某个频点和频段`tuneAndSetConfiguration`
	3. isSearchSwitch置为false
	4. waitSearchMode()
	5. ast
2. 相同 切换音源
	1. ast