/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 设置页只放"首页没有的东西"：跳过提示、连接方式、其他。
 * 与首页重复的入口（无障碍开关、常驻通知、自启与省电、订阅、应用列表）
 * 一律不在这里出现 —— 一个功能只有一个入口。
 */
package li.gkd.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import li.gkd.app.META
import li.gkd.app.settings.SettingsRepository
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.navigation.AboutRoute
import li.gkd.app.ui.navigation.CrashReportRoute
import li.gkd.app.ui.navigation.InterfaceSettingsRoute
import li.gkd.app.ui.navigation.NotificationTextRoute
import li.gkd.app.ui.navigation.TouchOptimizationRoute
import li.gkd.app.ui.navigation.WorkModeRoute

@Composable
fun settingsPage(
    vm: HomeViewModel,
    onBack: (() -> Unit)? = null,
): ScaffoldExt {
    val mainVm = MainViewModel.requireCurrent()
    val store by SettingsRepository.settings.collectAsStateWithLifecycle()
    val platform = dashboardPlatformState()

    val modeText = if (store.useA11y) "无障碍" else "Shizuku / ADB"

    return ScaffoldExt(
        navItem = BottomNavItem.Settings,
        topBar = {},
        content = {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(LxColor.Bg),
            ) {
                LxPage(title = "设置", onBack = onBack) {
                    LxGroup("跳过提示") {
                        LxSwitchRow(
                            title = "显示提示",
                            checked = store.toastWhenClick,
                            subtitle = "跳过时提示一下",
                            onText = "显示",
                            offText = "隐藏",
                        ) { on ->
                            SettingsRepository.updateSettings { it.copy(toastWhenClick = on) }
                        }
                        LxDivider()
                        LxSwitchRow(
                            title = "提示方式",
                            checked = store.useSystemToast,
                            subtitle = "关 = 无障碍悬浮窗",
                            onText = "系统",
                            offText = "悬浮窗",
                        ) { on ->
                            SettingsRepository.updateSettings { it.copy(useSystemToast = on) }
                        }
                    }

                    LxGroup("连接方式") {
                        LxRow("工作模式", modeText, accent = !store.useA11y, chevron = true) {
                            mainVm.navigator.navigate(WorkModeRoute)
                        }
                        LxDivider()
                        LxRow("触摸优化", "", chevron = true) {
                            mainVm.navigator.navigate(TouchOptimizationRoute)
                        }
                    }

                    LxGroup("其他") {
                        LxRow("通知文案", "", chevron = true) {
                            mainVm.navigator.navigate(NotificationTextRoute)
                        }
                        LxDivider()
                        LxRow("界面", "语言", chevron = true) {
                            mainVm.navigator.navigate(InterfaceSettingsRoute)
                        }
                        LxDivider()
                        LxRow("崩溃记录", "", chevron = true) {
                            mainVm.navigator.navigate(CrashReportRoute)
                        }
                        LxDivider()
                        LxRow("关于", "v${META.versionName}", chevron = true) {
                            mainVm.navigator.navigate(AboutRoute)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        "灵犀跳跃 · 基于 GKD 1.13.2 内核 · GPL-3.0",
                        fontSize = 11.sp,
                        color = LxColor.Foot,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                    )
                }
            }
        },
    )
}
