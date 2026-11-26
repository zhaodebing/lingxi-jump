/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 视觉 1:1 对齐「灵犀跳跃 9.00」（参考工程 MainActivity.java）：
 *   BG #E9F3EC / 卡片 0xD6FFFFFF / 深绿渐变 #0F592F→#1B713D
 *   正文 #14241B / 次要 #6B7C72 / 分隔线 #E8EFE9 / 强调 #1B713D
 *   hero 卡片 22dp 圆角、内边距 20dp；标语 20sp 粗体；卡片标题 12sp；
 *   行：左侧 15sp、右侧 13sp（强调色），内边距 16/15，分隔线左侧缩进 16dp。
 *
 * 功能全部走上游实现（订阅、规则组、辅助服务、动作日志…），这里只是把首页换掉。
 */
package li.gkd.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import li.gkd.app.META
import li.gkd.app.rule.RuleGroupSummary
import li.gkd.app.rule.ruleGroupState
import li.gkd.app.resources.*
import li.gkd.app.settings.SettingsRepository
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.navigation.ActionLogRoute
import li.gkd.app.ui.navigation.KeywordRoute
import li.gkd.app.ui.navigation.SupportRoute
import li.gkd.app.ui.navigation.KeepAliveRoute
import li.gkd.app.ui.navigation.PrivilegeServiceRoute
import li.gkd.app.ui.platform.LocalUiHost
import li.gkd.app.ui.platform.setStatusServiceEnabled
import li.gkd.app.ui.platform.switchAutomator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// 参考工程的取色
private val Bg = Color(0xFFE9F3EC)
private val Card = Color(0xD6FFFFFF)
private val Deep = Color(0xFF0F592F)
private val Green = Color(0xFF1B713D)
private val TextMain = Color(0xFF14241B)
private val TextSub = Color(0xFF6B7C72)
private val Line = Color(0xFFE8EFE9)
private val HeadSub = Color(0xFFCFE3D6)
private val FootText = Color(0xFF8A9A90)

private const val LINK = "https://v.v8l.cn/s/x5ft8Ed"

@Composable
fun dashboardPage(vm: HomeViewModel): ScaffoldExt {
    val mainVm = MainViewModel.requireCurrent()
    val host = LocalUiHost.current
    val store by SettingsRepository.settings.collectAsStateWithLifecycle()
    val platform = dashboardPlatformState()
    val actionCount by SettingsRepository.actionCount.collectAsStateWithLifecycle()
    val rules by ruleGroupState.collectAsStateWithLifecycle()
    val keywords by SettingsRepository.keywordSettings.collectAsStateWithLifecycle()
    // 触发记录的真实条数（删了会变）
    val recordTotal by li.gkd.app.record.RuntimeRecordRepository.actionCountFlow.collectAsState(0)

    val bound = platform.serviceEnabled
    val granted = platform.a11yEnabled
    val statusText = when {
        bound -> "无障碍已连接"
        granted -> "无障碍已授权，未连接"
        else -> "无障碍未开启"
    }
    val ruleAppCount = rules.value?.groups?.appSize ?: 0

    return ScaffoldExt(
        navItem = BottomNavItem.Dashboard,
        topBar = {},
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg)
                    .padding(padding)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
            ) {
                // ---------------- 顶部深绿卡片 ----------------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Deep, Green)))
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "灵犀跳跃",
                                fontSize = 21.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(statusText, fontSize = 12.sp, color = HeadSub)
                        }
                        // 吉祥物 = 开关
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable {
                                    // 首次使用：系统里还没勾选本服务 → 直接去无障碍设置，
                                    // 不要先要求「写入安全设置」权限（那是给自启动用的）
                                    if (!granted) {
                                        switchAutomator()
                                        return@clickable
                                    }
                                    when (platform.authorizationRoute(bound, store)) {
                                        PrivilegeServiceRoute ->
                                            mainVm.navigator.navigate(PrivilegeServiceRoute)

                                        null -> switchAutomator()
                                        else -> platform.authorizationRoute(bound, store)?.let {
                                            mainVm.navigator.navigate(it)
                                        }
                                    }
                                },
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.mascot),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(58.dp)
                                    .alpha(if (bound) 1f else 0.34f),
                            )
                            Spacer(Modifier.height(4.dp))
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (bound) "运行中" else "已暂停",
                                fontSize = 11.sp,
                                color = if (bound) Color(0xFFD8EBDF) else Color(0x99D8EBDF),
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "开屏广告，一闪而过",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(if (bound) 1f else 0.72f),
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "只跳开屏广告，其余一概不碰",
                        fontSize = 12.sp,
                        color = HeadSub,
                        modifier = Modifier.alpha(if (bound) 1f else 0.72f),
                    )
                }

                // ---------------- 状态 ----------------
                Spacer(Modifier.height(14.dp))
                LxGroup(null) {
                    LxRow(
                        "无障碍服务",
                        if (bound) "已连接" else if (granted) "已勾选" else "未开启",
                        accent = !bound,
                        chevron = true,
                    ) {
                        if (!granted) {
                            switchAutomator()
                        } else when (platform.authorizationRoute(bound, store)) {
                            PrivilegeServiceRoute ->
                                mainVm.navigator.navigate(PrivilegeServiceRoute)

                            null -> switchAutomator()
                            else -> platform.authorizationRoute(bound, store)?.let {
                                mainVm.navigator.navigate(it)
                            }
                        }
                    }
                    LxDivider()
                    // 数字来自「触发记录」表本身（和记录页同一个数据源）：
                    // 删记录就归零，不再出现"删了还显示 40/8 次"的假数字。
                    LxRow(
                        "跳过记录",
                        if (recordTotal > 0) "$recordTotal 次" else "暂无记录",
                        accent = true,
                        chevron = true,
                    ) { mainVm.navigator.navigate(ActionLogRoute()) }
                }

                // ---------------- 规则 ----------------
                Spacer(Modifier.height(4.dp))
                LxGroup("规则") {
                    LxRow("投喂规则", "订阅 / 灵犀喂养 / 规则组", accent = true, chevron = true) {
                        vm.homeState.selectTab(BottomNavItem.SubsManage)
                    }
                    LxDivider()
                    LxRow(
                        "应用列表",
                        if (ruleAppCount > 0) "$ruleAppCount 个应用" else "暂无规则",
                        accent = ruleAppCount > 0,
                        chevron = true,
                    ) {
                        vm.homeState.selectTab(BottomNavItem.AppList)
                    }
                }

                // ---------------- 保活 ----------------
                Spacer(Modifier.height(4.dp))
                LxGroup(null) {
                    // 常驻通知的开关在「自启与省电」里，这里只留一个入口
                    LxRow("自启与省电", "常驻通知 · 电池优化", chevron = true) {
                        mainVm.navigator.navigate(KeepAliveRoute)
                    }
                }

                // ---------------- 设置（永远放最后） ----------------
                Spacer(Modifier.height(4.dp))
                LxGroup(null) {
                    LxRow("设置", "提示 · 连接方式 · 关于", chevron = true) {
                        vm.homeState.selectTab(BottomNavItem.Settings)
                    }
                }

                // ---------------- 页脚 ----------------
                Spacer(Modifier.height(22.dp))
                Text(
                    "v${META.versionName} · 平时离线 · 仅订阅更新时联网",
                    fontSize = 12.sp,
                    color = FootText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(3.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    "觉得好用？请我喝咖啡 →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Green,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mainVm.navigator.navigate(SupportRoute) }
                        .padding(vertical = 8.dp),
                )
                Text(
                    "公众号：趣味暖心",
                    fontSize = 12.sp,
                    color = FootText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mainVm.navigator.navigate(SupportRoute) }
                        .padding(vertical = 2.dp),
                )
                Text(
                    "开源 · 本地运行 · 不上传数据",
                    fontSize = 12.sp,
                    color = FootText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "获取更多神秘小工具 →",
                    fontSize = 12.sp,
                    color = Green,
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mainVm.navigator.openWebPage(LINK) },
                )
            }
        },
    )
}

// ---------------------------------------------------------------- 组件

// 上游保留：设置页与通知文案页仍在用
@Composable
fun dashboardSummary(groups: RuleGroupSummary): String? =
    listOfNotNull(
            if (groups.globalGroups.isNotEmpty())
                stringResource(
                    Res.string.subscription_global_count,
                    groups.globalGroups.size.toString(),
                )
            else null,
            if (groups.appGroupSize > 0)
                stringResource(
                    Res.string.subscription_app_rule_counts,
                    groups.appSize.toString(),
                    groups.appGroupSize.toString(),
                )
            else null,
        )
        .joinToString("/")
        .takeIf { it.isNotEmpty() }
