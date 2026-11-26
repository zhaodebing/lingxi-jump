package li.gkd.app.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import li.gkd.app.META
import li.gkd.app.a11y.observeEnabledGestureServices
import li.gkd.app.crash.CrashData
import li.gkd.app.permission.PermissionRequester
import li.gkd.app.rule.RuleGroupTarget
import li.gkd.app.subscription.RawSubscription
import li.gkd.app.ui.component.DialogRequests
import li.gkd.app.ui.component.ShareLogState
import li.gkd.app.ui.component.TextDialogState
import li.gkd.app.ui.navigation.AppNavigator
import li.gkd.app.ui.navigation.CrashReportRoute
import li.gkd.app.ui.share.DeletionTarget
import li.gkd.app.ui.share.confirmDelete as confirmRuleDeletion
import li.gkd.app.ui.share.launchUi
import li.gkd.app.ui.state.BaseViewModel
import li.gkd.app.ui.subscription.RuleControlDialogState
import li.gkd.app.ui.subscription.RuleGroupState
import li.gkd.app.ui.subscription.SubsLinkDialogState
import li.gkd.app.ui.subscription.SubsSheetState
import li.gkd.app.ui.update.UpdateStatus
import li.gkd.app.ui.upload.GithubUploadState

class MainViewModel(val permissions: PermissionRequester) : BaseViewModel() {
    companion object {
        private var current: MainViewModel? = null

        fun requireCurrent(): MainViewModel = checkNotNull(current) { "Main UI is not initialized" }
    }

    fun registerCurrent() {
        // 灵犀：Activity 重建/闪屏重启时会再注册一次，上游用 check() 直接抛
        // IllegalStateException（"Main UI is already registered"）导致冷启动崩一次。
        // 这里改成幂等：后注册者生效，旧实例的 closeable 会自行让位。
        current = this
    }

    init {
        addCloseable {
            if (current === this) current = null
        }
    }

    val navigator = AppNavigator(scope)

    val dialogRequests = DialogRequests()

    fun confirmDelete(
        title: String,
        text: String,
        targets: () -> Set<DeletionTarget> = { emptySet() },
        dismiss: () -> Unit = {},
        delete: suspend () -> Unit,
    ) = scope.launchUi {
        confirmRuleDeletion(
            dialogRequests,
            navigator,
            subsSheet,
            ruleGroupState,
            ruleControlDialog,
            title,
            text,
            targets,
            dismiss,
            delete,
        )
    }

    val updateStatus = if (META.updateEnabled) UpdateStatus(scope) else null

    val githubUpload =
        GithubUploadState(
            scope = scope,
            navigator = navigator,
        )

    val shareLog =
        ShareLogState(
            scope = scope,
            githubUpload = githubUpload,
        )

    val textDialog = TextDialogState()

    val subsLinkDialog =
        SubsLinkDialogState(
            navigator = navigator,
            permissions = permissions,
            showText = textDialog::showText,
        )

    val subsSheet =
        SubsSheetState(
            navigator = navigator,
            openUrl = textDialog::showUrl,
            requestUrl = subsLinkDialog::request,
            confirmDelete = ::confirmDelete,
        )

    val ruleGroupState = RuleGroupState(dialogRequests)

    fun showRuleGroup(
        subscriptionId: Long,
        appId: String?,
        group: RawSubscription.RawGroupProps,
        pageAppId: String?,
    ) = scope.launch {
        withContext(Dispatchers.Default) { group.cacheStr }
        ruleGroupState.showGroup(
            if (group is RawSubscription.RawGlobalGroup) {
                RuleGroupTarget.Global(subscriptionId, group.key, pageAppId)
            } else {
                RuleGroupTarget.App(subscriptionId, requireNotNull(appId), group.key)
            },
        )
    }

    val ruleControlDialog = RuleControlDialogState()

    val gestureServices = observeEnabledGestureServices().stateLoadable()

    private var pendingCrashes = emptyList<CrashData>()

    fun showCrashReports(records: List<CrashData>) {
        pendingCrashes = records
        if (records.isNotEmpty()) navigator.navigate(CrashReportRoute)
    }

    fun takeCrashDataList(): List<CrashData> = pendingCrashes.also { pendingCrashes = emptyList() }
}
