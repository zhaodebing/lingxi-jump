package li.gkd.app.rule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import li.gkd.app.app.AppInfoRepository
import li.gkd.app.app.applicationScope
import li.gkd.app.app.launcherAppIdFlow
import li.gkd.app.keyword.KeywordRules
import li.gkd.app.keyword.KeywordSettings
import li.gkd.app.settings.SettingsRepository
import li.gkd.app.model.AppInfo
import li.gkd.app.state.Loadable
import li.gkd.app.subscription.RawSubscription
import li.gkd.app.subscription.SubscriptionRepository
import li.gkd.app.subscription.SubscriptionSnapshot
import li.gkd.app.subscription.UsedSubsEntry
import li.gkd.db.SubsItem
import li.gkd.db.SubscriptionConfigSnapshot
import li.gkd.db.SubscriptionConfigStore

data class RuleGroupSnapshot(val groups: RuleGroupSummary, val apps: Map<String, AppInfo>)

data class RuleAppEnvironment(val apps: Map<String, AppInfo>, val launcherAppId: String)

object RuleGroupSources {
    fun usedSubscriptions(
        items: List<SubsItem>,
        subscriptions: Map<Long, RawSubscription>,
    ): List<UsedSubsEntry> = items.mapNotNull { item ->
        subscriptions[item.id]
            ?.takeIf { item.enable && it.hasRule }
            ?.let { UsedSubsEntry(item, it) }
    }

    fun observe(
        subscriptions: Flow<Loadable<SubscriptionSnapshot>>,
        environment: Flow<RuleAppEnvironment>,
        configs: Flow<SubscriptionConfigSnapshot>,
        keywords: Flow<KeywordSettings> = kotlinx.coroutines.flow.flowOf(KeywordSettings()),
    ): Flow<Loadable<RuleGroupSnapshot>> =
        combine(subscriptions, environment, configs, keywords) { loaded, input, config, keyword ->
                when (loaded) {
                    Loadable.Loading -> Loadable.Loading
                    is Loadable.Failure -> Loadable.Failure(loaded.cause)
                    is Loadable.Ready ->
                        Loadable.Ready(
                            RuleGroupSnapshot(
                                RuleGroupSummaryBuilder.build(
                                    usedSubscriptions(config.subsItems, loaded.value.subscriptions) +
                                        // 关键词通道：编译成一条 GKD 全局规则组，走引擎原生匹配
                                        KeywordRules.entry(keyword),
                                    input.apps,
                                    config.appConfigs,
                                    config.appGroupConfigs + config.globalGroupConfigs,
                                    config.categoryConfigs,
                                    input.launcherAppId,
                                ),
                                input.apps,
                            ),
                        )
                }
            }
            .catch { emit(Loadable.Failure(it)) }
            .flowOn(Dispatchers.Default)
}

val ruleGroupState: StateFlow<Loadable<RuleGroupSnapshot>> by lazy {
    RuleGroupSources.observe(
            SubscriptionRepository.snapshotFlow,
            combine(AppInfoRepository.appInfoMapFlow, launcherAppIdFlow) { apps, launcher ->
                RuleAppEnvironment(apps, launcher)
            },
            SubscriptionConfigStore.observe(),
            SettingsRepository.keywordSettings,
        )
        .stateIn(applicationScope(), SharingStarted.Eagerly, Loadable.Loading)
}
