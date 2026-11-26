package li.gkd.app.rule

import li.gkd.app.resources.*
import li.gkd.app.keyword.KeywordRules
import li.gkd.app.state.Loadable
import li.gkd.app.subscription.RawSubscription

data class RuleSummary(
    val globalRules: List<GlobalRule> = emptyList(),
    val globalGroups: List<ResolvedGlobalGroup> = emptyList(),
    val appIdToGlobalGroupCount: Map<String, Int> = emptyMap(),
    val appIdToRules: Map<String, List<AppRule>> = emptyMap(),
    val appIdToGroups: Map<String, List<RawSubscription.RawAppGroup>> = emptyMap(),
    val appIdToAllGroups: Map<String, List<ResolvedAppGroup>> = emptyMap(),
) {
    val appSize = appIdToRules.keys.size
    val appGroupSize = appIdToGroups.values.sumOf { it.size }

    fun statusText(actionCount: Long): String {
        // 灵犀喂养也是一条全局组，但它不是"订阅规则"，单独标注，避免和订阅混为一谈
        val keywordActive = globalGroups.any { it.subsItem.id == KeywordRules.SUBS_ID }
        val globalCount = globalGroups.count { it.subsItem.id != KeywordRules.SUBS_ID }
        // 只有灵犀喂养时，别显示成"1全局/85触发"，直接说人话
        if (keywordActive && globalCount == 0 && appGroupSize == 0) {
            return if (actionCount > 0) "灵犀喂养 · $actionCount 次触发" else "灵犀喂养"
        }
        val rules =
            listOfNotNull(
                    if (globalCount > 0)
                        Res.string.subscription_global_count.getSync(globalCount)
                    else null,
                    if (appGroupSize > 0)
                        Res.string.subscription_app_rule_counts.getSync(appSize, appGroupSize)
                    else null,
                    if (keywordActive) "灵犀喂养" else null,
                )
                .joinToString("/")
                .ifEmpty { Res.string.rules_empty.getSync() }
        return if (actionCount > 0) Res.string.action_count_summary.getSync(rules, actionCount)
        else rules
    }

    companion object {
        fun statusText(state: Loadable<RuleSummary>, actionCount: Long): String =
            when (state) {
                Loadable.Loading -> Res.string.config_loading.getSync()
                is Loadable.Failure -> Res.string.data_load_failed.getSync()
                is Loadable.Ready -> state.value.statusText(actionCount)
            }
    }
}
