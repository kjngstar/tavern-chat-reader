# Tavern Chat Reader for Android

这是酒馆聊天记录查看器的原生 Android 外壳。它将根目录的单文件阅读器内置到 WebView 中，保持离线运行；TXT、正则、皮肤、字体等均通过 Android 系统文件选择器导入。

## 本地构建

1. 用 Android Studio 打开本目录。
2. 确保安装 Android SDK Platform 35 和 JDK 17。
3. 等待 Gradle 同步完成，执行 `Build > Build APK(s)`。
4. 生成的调试包位于 `app/build/outputs/apk/debug/app-debug.apk`。

首次启动后，从页面里导入 TXT 即可。手机点击正文中央仍会唤出阅读器原有的上下控制栏。

## 兼容性

- Android 7.0（API 24）及以上。
- 采用系统 Android WebView；建议用户保持 WebView 组件更新。
- 书架、阅读偏好、导入皮肤与字体由页面的本地存储保存，卸载应用会清除这些数据。
