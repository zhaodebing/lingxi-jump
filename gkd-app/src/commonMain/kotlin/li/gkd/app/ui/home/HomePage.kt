/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 没有底部导航：首页就是唯一入口，其它页面从首页的行进入，
 * 系统返回键 / 左上返回箭头回到首页。
 */
package li.gkd.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.platform.GkBackHandler

@Composable
fun HomePage() {
    val navigator = MainViewModel.requireCurrent().navigator
    val vm = viewModel { HomeViewModel() }
    DisposableEffect(vm, navigator) {
        navigator.bindHomeState(vm.homeState)
        onDispose { navigator.unbindHomeState(vm.homeState) }
    }
    val latest = vm.latestState.collectAsStateWithLifecycle()
    val apps = vm.appsState.collectAsStateWithLifecycle()
    val subscriptions = vm.subscriptionsState.collectAsStateWithLifecycle()
    val selected by vm.homeState.selectedTab.collectAsStateWithLifecycle()
    val backToHome: () -> Unit = { vm.homeState.selectTab(BottomNavItem.Dashboard) }
    GkBackHandler(enabled = selected != BottomNavItem.Dashboard) { backToHome() }
    GkHomeHost(vm.homeState) { tab ->
        when (tab) {
            BottomNavItem.Dashboard -> dashboardPage(vm)
            BottomNavItem.AppList -> appListPage(vm, apps.value, onBack = backToHome)
            BottomNavItem.SubsManage -> subsManagePage(vm, subscriptions.value, onBack = backToHome)
            BottomNavItem.Settings -> settingsPage(vm, onBack = backToHome)
        }
    }
}
