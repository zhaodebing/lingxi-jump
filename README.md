# 灵犀跳跃（Lingxi Jump）

> 开屏广告跳过工具 · 基于 [gkd-kit/gkd](https://github.com/gkd-kit/gkd) 1.13.2 二次开发 · GPL-3.0-only
> 下载：[Releases](https://github.com/zhaodebing/lingxi-jump/releases/latest)


开屏广告跳过工具。基于 [gkd-kit/gkd](https://github.com/gkd-kit/gkd) 1.13.2 二次开发，
以 **GPL-3.0-only** 分发。

- 包名：`li.songe.gkd`（沿用上游，不能与官方 GKD 共存）
- 当前版本：1.9.4（versionCode 160）

## 这是什么

一个只做一件事的工具：**跳过应用开屏广告**。不推送、不埋点、不上传数据，所有规则与记录只存在本机。

两条跳过通道，共用一个匹配引擎：

1. **订阅规则**：兼容 GKD 订阅格式，可导入任意 GKD 订阅链接；订阅里的规则原样工作。
2. **灵犀喂养**：用户自己填关键词（如「跳过」），由本仓库自己实现的一条合成订阅参与匹配，
   额外带一层「点后验证」——点完 500ms 回头检查按钮是否还在，还在就不算成功、不弹提示、不计数。

## 与上游 GKD 的差异

| 位置 | 改动 |
|---|---|
| `gkd-app/.../keyword/` | 新增：把用户关键词编译成一条 `RawSubscription(id=-1000)` 全局规则组 |
| `gkd-app/.../matching/guard/KeywordGuard.kt` | 新增：灵犀喂养的防误触与点后验证（只作用于 subsId=-1000 的规则） |
| `gkd-app/.../matching/query/A11yQueryContext.kt` | `queryRule` 前后各一道闸；命中后把点击目标升级为"可点击祖先" |
| `gkd-app/.../matching/AndroidMatchHost.kt` | 关键词规则的点击结果先验证再提交（提示/计数/记录） |
| `gkd-app/.../matching/query/RuleQuery.kt` | 灵犀喂养命中后结束本轮遍历，避免订阅规则补刀 |
| `gkd-app/.../matching/ForegroundRuleSnapshot.kt` | 规则排序：priority → 灵犀喂养 → 带字面量 |
| `gkd-app/.../rule/RuleExecutionParameters.kt` | `fastQuery` 默认开启 |
| `gkd-app/.../matching/query/RuleSelectorMatcher.kt` | 快查无结果时回退整树一次，保证覆盖率 |
| `gkd-app/.../ui/home/` | 全新的首页 / 设置 / 灵犀喂养页面（Lx* 组件） |
| `gkd-app/.../ui/settings/SupportPage.kt` | 赞赏页（两张收款码，纯本地） |
| `gkd-android/res/` | 自有图标（海豚）、闪屏、单磁贴、通知小图标 |

上游的快照、调试、事件日志等页面在本分支中已移除。

## 构建

环境：JDK 17、Android SDK（compileSdk 37）。

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :gkd-android:assembleGkdRelease
```

产物：`gkd-android/build/outputs/apk/gkd/release/gkd-android-gkd-release.apk`

> 上游的打包流程还会做 dex 隐藏 API 重映射（见上游 `docs/`）。本仓库交付的 APK 使用
> `gkd-a11y-fixture` 与常规 gradle 流程构建，未做额外 remap；如需与上游一致的隐藏 API 处理，请参考上游脚本。

## 许可

- 本仓库为 gkd-kit/gkd 的衍生作品，同样以 **GPL-3.0-only** 分发，见 [LICENSE](./LICENSE)。
- 再分发时须保留 `LICENSE`、上游版权声明，并提供本仓库源码。
- 上游名称「GKD」及其商标不属于本项目，不得用于宣传本衍生作品。
- 应用内的赞赏入口为自愿捐赠，不解锁任何功能，也不构成对软件的售卖。

## 隐私

- 不采集设备信息、不埋点、不上报任何数据。
- 唯一联网行为：你手动添加订阅并点「更新」时访问该订阅链接。
- 触发记录、规则、设置均保存在本机数据库与私有目录。
