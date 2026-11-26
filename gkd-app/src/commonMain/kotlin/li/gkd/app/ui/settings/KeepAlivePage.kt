package li.gkd.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import li.gkd.app.META
import li.gkd.app.permission.AppPermission
import li.gkd.app.resources.*
import li.gkd.app.settings.SettingsRepository
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.component.GkAlertDialog
import li.gkd.app.ui.component.GkIcon
import li.gkd.app.ui.component.GkIconButton
import li.gkd.app.ui.component.GkIcons
import li.gkd.app.ui.component.GkPageBottomSpace
import li.gkd.app.ui.component.GkScaffold
import li.gkd.app.ui.component.GkSettingItem
import li.gkd.app.ui.component.GkTextSwitch
import li.gkd.app.ui.component.GkTopAppBar
import li.gkd.app.ui.component.gkTopAppBarScroll
import li.gkd.app.ui.home.LxDivider
import li.gkd.app.ui.home.LxGroup
import li.gkd.app.ui.home.LxPage
import li.gkd.app.ui.home.LxRow
import li.gkd.app.ui.home.LxSwitchRow
import li.gkd.app.ui.home.dashboardPlatformState
import li.gkd.app.ui.navigation.PrivilegeServiceRoute
import li.gkd.app.ui.platform.LocalUiHost
import li.gkd.app.ui.platform.ignoresBatteryOptimizations
import li.gkd.app.ui.platform.openAppDetails
import li.gkd.app.ui.platform.openRecents
import li.gkd.app.ui.platform.setStatusServiceEnabled
import li.gkd.app.ui.share.launchUiAction
import li.gkd.app.ui.style.titleItemPadding
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun KeepAlivePage() {
    val host = LocalUiHost.current
    val mainVm = MainViewModel.requireCurrent()
    val vm = viewModel { KeepAliveViewModel() }
    val scope = rememberCoroutineScope()
    val store by SettingsRepository.settings.collectAsStateWithLifecycle()
    val platform = dashboardPlatformState()
    val batteryUnrestricted = ignoresBatteryOptimizations() == true
    val showKeepAlive by vm.showKeepAlive.collectAsStateWithLifecycle()
    if (showKeepAlive) {
        GkAlertDialog(
            onDismissRequest = { vm.setShowKeepAlive(false) },
            title = { Text(stringResource(Res.string.keep_alive_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        stringResource(Res.string.keep_alive_tile_description, META.appName).trim(),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(Res.string.keep_alive_setup_heading).trim(),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(stringResource(Res.string.keep_alive_setup_open_tiles).trim())
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.width(64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = org.jetbrains.compose.resources.painterResource(
                                        li.gkd.app.resources.Res.drawable.mascot,
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                )
                                Text(
                                    META.appName,
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Text(
                                stringResource(Res.string.keep_alive_setup_add_tile, META.appName)
                                    .trim(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Text(stringResource(Res.string.keep_alive_setup_place_tile))
                    }
                    if (!platform.writeSecureSettings) {
                        Text(
                            stringResource(Res.string.keep_alive_permission_missing).trimStart() +
                                stringResource(Res.string.keep_alive_permission_description),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.setShowKeepAlive(false)
                        if (!platform.writeSecureSettings)
                            mainVm.navigator.navigate(PrivilegeServiceRoute)
                    },
                ) {
                    Text(
                        stringResource(
                            if (platform.writeSecureSettings) Res.string.action_understood
                            else Res.string.settings_go_to,
                        ),
                    )
                }
            },
            dismissButton =
                if (platform.writeSecureSettings) null
                else {
                    {
                        TextButton(
                            onClick = {
                                vm.setShowKeepAlive(false)
                            },
                        ) {
                            Text(stringResource(Res.string.action_close))
                        }
                    }
                },
        )
    }

    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }

    LxPage(
        title = stringResource(Res.string.keep_alive_strategy),
        onBack = mainVm.navigator::pop,
    ) {
        LxGroup("应用内") {
            LxSwitchRow(
                "常驻通知",
                platform.statusRunning && store.enableStatusService,
            ) { host.setStatusServiceEnabled(it) }
            LxDivider()
            LxSwitchRow(
                "状态悬浮窗",
                store.enableStatusOverlay,
            ) { vm.requestStatusOverlayEnabled(it) }
        }

        LxGroup("系统设置") {
            LxRow(
                "忽略电池优化",
                if (batteryUnrestricted) "已忽略" else "去设置",
                accent = !batteryUnrestricted,
                chevron = true,
            ) {
                if (batteryUnrestricted) {
                    openAppDetails()
                } else {
                    scope.launch {
                        mainVm.permissions.ensurePermissions(AppPermission.IgnoreBatteryOptimizations)
                    }
                }
            }
            LxDivider()
            LxRow("允许自启动", "去设置", chevron = true) {
                pendingAction = PendingAction.Autostart
            }
            LxDivider()
            LxRow("锁定后台（最近任务）", "去设置", chevron = true) {
                pendingAction = PendingAction.Recents
            }
        }

        LxGroup("更多") {
            LxRow("保活说明", "查看", chevron = true) { vm.setShowKeepAlive(true) }
            LxDivider()
            LxRow("特权服务", "去设置", chevron = true) {
                mainVm.navigator.navigate(PrivilegeServiceRoute)
            }
        }
    }

    pendingAction?.let { action ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = {
                Text(
                    if (action == PendingAction.Autostart) "去系统设置开启自启动"
                    else "去最近任务锁定后台",
                    fontSize = 16.sp,
                )
            },
            text = {
                Text(
                    if (action == PendingAction.Autostart)
                        "国产 ROM 不允许应用自己开自启动，需要你在系统设置里手动允许。点「去设置」会直接打开本应用的系统详情页。"
                    else
                        "在最近任务里长按本应用卡片并加锁，可以避免被系统清理。点「去设置」会打开最近任务。",
                    fontSize = 13.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingAction = null
                    if (action == PendingAction.Autostart) {
                        openAppDetails()
                    } else {
                        scope.launch { openRecents() }
                    }
                }) { Text("去设置") }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) { Text("取消") }
            },
        )
    }
}

private enum class PendingAction { Autostart, Recents }
