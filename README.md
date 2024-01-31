##  Android 本地简易音乐播放器

```
applicationId "com.example.musicplayer"
minSdk 24
targetSdk 30
versionCode 1
versionName '1.0.1'
Gradle v7.0.2
Gradle build tool v7.0
```

**实现功能：**播放、暂停、上一曲、下一曲，点击列表进行播放。

**bug：手机系统切换深/浅色模式**会自动播放，且同时播放切换次数首歌曲。

- bug来源：添加了监听音乐播放完然后播放下一首的代码后出现，删除该段代码则消失。
- 修复方法：**不要在app运行时切换深色模式！！！**

**Thanks:**

哔哩哔哩up主：写bug的狐狸

视频地址 [https://www.bilibili.com/video/BV1oJ41197fi](https://gitee.com/link?target=https%3A%2F%2Fwww.bilibili.com%2Fvideo%2FBV1oJ41197fi)
