# http://developer.android.com/guide/developing/tools/proguard.html

-dontwarn **

# 隐藏 API 的 Binder 回调（CompatTaskStackListener / UserService / ProxyUiAutomationConnection …）
#
# 注意：不能用 `-keep class * extends android.os.Binder { *; }`。
# 这些类的父类（android.app.ITaskStackListener$Stub 等）来自 gkd-hidden-api，
# 而它是 compileOnly —— R8 阶段父类不存在，无法判定继承关系，整条规则一条都匹配不上，
# 方法会被当成死代码删掉，Binder 回调时抛 AbstractMethodError（Binder 线程 → 崩进程）。
#
# 所以直接按包保留：这些包里放的全是需要被系统回调的实现。
-keep class li.gkd.app.priv.** { *; }
-keep class li.gkd.app.platform.** { *; }
-keep class li.gkd.aidl.** { *; }
-keep class li.gkd.app.keyword.** { *; }
-keep class li.gkd.app.matching.guard.** { *; }

# 崩溃日志里保留类名，便于排查
-keepnames class li.gkd.app.priv.** { *; }
-keepnames class li.gkd.app.keyword.** { *; }
-keepnames class li.gkd.app.matching.guard.** { *; }
