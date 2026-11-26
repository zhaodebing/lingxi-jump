/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 工作模式页：无障碍 / Shizuku·ADB 二选一，视觉与首页一致（LxPage + LxGroup + LxRow）。
 * 授权动作仍走上游实现（无障碍引导弹窗、特权服务页），没有改判定逻辑。
 */
package li.gkd.app.ui.settings

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import li.gkd.app.model.findOption
import li.gkd.app.network.AppLinks
import li.gkd.app.resources.*
import li.gkd.app.settings.AutomatorMode
import li.gkd.app.settings.SettingsRepository
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.component.GkPermissionRestrictionDialog
import li.gkd.app.ui.home.LxChoice
import li.gkd.app.ui.home.LxDivider
import li.gkd.app.ui.home.LxGroup
import li.gkd.app.ui.home.LxNote
import li.gkd.app.ui.home.LxPage
import li.gkd.app.ui.home.LxRow
import li.gkd.app.ui.home.DashboardPrivilegeStatus
import li.gkd.app.ui.home.dashboardPlatformState
import li.gkd.app.ui.navigation.PrivilegeServiceRoute
import li.gkd.app.ui.platform.RefreshPermissions
import org.jetbrains.compose.resources.stringResource

@Composable
fun WorkModePage() {
    val mainVm = MainViewModel.requireCurrent()
    val vm = viewModel { WorkModeViewModel() }
    RefreshPermissions()
    val platform = dashboardPlatformState()
    val store by SettingsRepository.settings.collectAsStateWithLifecycle()
    val automatorMode: AutomatorMode = AutomatorMode.entries.findOption(store.automatorMode)

    val automationAvailable =
        platform.privilegeAvailable && platform.privilegeCapabilities?.restricted != true
    val privilegeAction by vm.privilegeAction.collectAsStateWithLifecycle()
    if (privilegeAction != null) {
        GkPermissionRestrictionDialog(
            privilegeAvailable = platform.privilegeAvailable,
            capabilities = platform.privilegeCapabilities,
            appRestrictions = platform.appRestrictions,
            onDismiss = { vm.setPrivilegeAction(null) },
            onPrivilege = { mainVm.navigator.navigate(PrivilegeServiceRoute) },
            description = Res.string.automation_mode_privilege_notice,
            alternativeButton = {
                TextButton(
                    onClick = {
                        vm.setPrivilegeAction(null)
                        SettingsRepository.updateAutomatorMode(AutomatorMode.Automation.value)
                    },
                ) {
                    Text(stringResource(Res.string.action_switch_only))
                }
            },
        )
    }

    val privilegeConnected = platform.privilegeStatus == DashboardPrivilegeStatus.Connected

    LxPage(title = stringResource(Res.string.work_mode_title), onBack = mainVm.navigator::pop) {
        LxGroup(null) {
            LxChoice(
                title = stringResource(AutomatorMode.A11y.labelResource),
                subtitle = "系统无障碍服务 · 免 root · 够用",
                selected = automatorMode == AutomatorMode.A11y,
            ) { SettingsRepository.updateAutomatorMode(AutomatorMode.A11y.value) }
            LxDivider()
            LxChoice(
                title = stringResource(AutomatorMode.Automation.labelResource),
                subtitle = if (automationAvailable) "Shizuku / ADB 注入 · 触摸更顺"
                else "未授权 · 不可用",
                selected = automatorMode == AutomatorMode.Automation,
                enabled = automationAvailable,
            ) {
                mainVm.navigator.navigate(PrivilegeServiceRoute)
            }
        }

        LxGroup("授权状态") {
            LxRow(
                "无障碍服务",
                if (platform.a11yRunning) "已连接" else if (platform.a11yEnabled) "已开启" else "未开启",
                accent = !platform.a11yEnabled,
                chevron = true,
            ) { vm.requestA11yEnable() }
            LxDivider()
            LxRow(
                "修改安全设置",
                if (platform.writeSecureSettings) "已授予" else "未授予",
                accent = !platform.writeSecureSettings,
                chevron = true,
            ) { mainVm.navigator.navigate(PrivilegeServiceRoute) }
            LxDivider()
            LxRow(
                "Shizuku / ADB",
                if (privilegeConnected) "已连接" else if (automationAvailable) "未连接" else "不可用",
                accent = !privilegeConnected,
                chevron = true,
            ) { mainVm.navigator.navigate(PrivilegeServiceRoute) }
        }

        LxGroup("说明") {
            LxNote(
                "· 无障碍模式：无需额外授权，系统无障碍服务直接完成点击，绝大多数场景够用。\n" +
                    "· Shizuku / ADB 模式：通过 Shizuku 或一次性 ADB 授权拿到注入能力，" +
                    "点击更贴近真实触摸，个别 App 的触摸卡顿会消失。\n" +
                    "· 两种模式共用同一套规则，随时可以切换。",
            )
            LxDivider()
            LxRow("使用帮助", "查看", chevron = true) {
                mainVm.navigator.openWebPage(AppLinks.WorkModeHelp)
            }
        }
    }
}
