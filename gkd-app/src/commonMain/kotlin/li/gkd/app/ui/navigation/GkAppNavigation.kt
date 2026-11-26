package li.gkd.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import li.gkd.app.ui.ImagePreviewPage
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.PrivilegeServicePage
import li.gkd.app.ui.WebViewPage
import li.gkd.app.ui.crash.CrashReportPage
import li.gkd.app.ui.home.BlockA11ySetupPage
import li.gkd.app.ui.home.HomePage
import li.gkd.app.ui.home.KeywordPage
import li.gkd.app.ui.home.NotificationTextPage
import li.gkd.app.ui.settings.AboutPage
import li.gkd.app.ui.settings.AppWhitelistPage
import li.gkd.app.ui.settings.SupportPage
import li.gkd.app.ui.settings.InterfaceSettingsPage
import li.gkd.app.ui.settings.KeepAlivePage
import li.gkd.app.ui.settings.ServiceScopeAppListPage
import li.gkd.app.ui.settings.ServiceScopeKind
import li.gkd.app.ui.settings.TouchOptimizationPage
import li.gkd.app.ui.settings.WorkModePage
import li.gkd.app.ui.subscription.ActionLogPage
import li.gkd.app.ui.subscription.AppConfigPage
import li.gkd.app.ui.subscription.CategoryEditorPage
import li.gkd.app.ui.subscription.RuleExcludeEditorPage
import li.gkd.app.ui.subscription.SubsAppGroupListPage
import li.gkd.app.ui.subscription.SubsAppListPage
import li.gkd.app.ui.subscription.SubsCategoryGroupPage
import li.gkd.app.ui.subscription.SubsCategoryPage
import li.gkd.app.ui.subscription.SubsGlobalGroupExcludePage
import li.gkd.app.ui.subscription.SubsGlobalGroupListPage
import li.gkd.app.ui.subscription.UpsertRuleGroupPage

/** Shared production route registry for both hosts. */
@Composable
fun GkAppNavigation() {
    val mainVm = MainViewModel.requireCurrent()
    GkNavigation(mainVm.navigator.backStack, mainVm.navigator::pop) { entry ->
        val route = entry.route
        NavEntry(
            entry,
            contentKey = entry.id,
            metadata = route.transition.toMetadata(),
        ) {
            when (route) {
                is SubsAppListRoute -> SubsAppListPage(route)
                is SubsAppGroupListRoute -> SubsAppGroupListPage(route)
                is SubsGlobalGroupListRoute -> SubsGlobalGroupListPage(route)
                is SubsGlobalGroupExcludeRoute -> SubsGlobalGroupExcludePage(route)
                is SubsCategoryRoute -> SubsCategoryPage(route)
                is SubsCategoryGroupRoute -> SubsCategoryGroupPage(route)
                is RuleExcludeEditorRoute -> RuleExcludeEditorPage(route)
                is AppConfigRoute -> AppConfigPage(route)
                is ActionLogRoute -> ActionLogPage(route)
                is UpsertRuleGroupRoute -> UpsertRuleGroupPage(route)
                is CategoryEditorRoute -> CategoryEditorPage(route)
                is WebViewRoute -> WebViewPage(route)
                is ImagePreviewRoute -> ImagePreviewPage(route)

                BlockA11yAppListRoute -> ServiceScopeAppListPage(ServiceScopeKind.Disabled)
                CompanionScopeAppListRoute -> ServiceScopeAppListPage(ServiceScopeKind.Companion)
                EditBlockAppListRoute -> AppWhitelistPage()
                HomeRoute -> HomePage()
                KeywordRoute -> KeywordPage()
                NotificationTextRoute -> NotificationTextPage()
                WorkModeRoute -> WorkModePage()
                AboutRoute -> AboutPage()
                SupportRoute -> SupportPage()
                BlockA11ySetupRoute -> BlockA11ySetupPage()
                InterfaceSettingsRoute -> InterfaceSettingsPage()
                KeepAliveRoute -> KeepAlivePage()
                TouchOptimizationRoute -> TouchOptimizationPage()
                PrivilegeServiceRoute -> PrivilegeServicePage()
                CrashReportRoute -> CrashReportPage()
                // 灵犀跳跃：工具类页面（动作提示/备份/高级/调试/快照/外部调用/磁贴）已移除
                else -> Unit
            }
        }
    }
}
