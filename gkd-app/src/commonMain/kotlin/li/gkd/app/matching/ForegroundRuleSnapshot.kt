package li.gkd.app.matching

import li.gkd.app.keyword.KeywordRules
import li.gkd.app.rule.ResolvedRule
import li.gkd.app.rule.RuleStatus
import li.gkd.app.rule.RuleSummary
import li.gkd.app.rule.TopActivity

@kotlinx.serialization.Serializable
enum class RuleAvailability {
    Loading,
    Ready,
    Failed,
}

class ForegroundRuleSnapshot(
    val topActivity: TopActivity = TopActivity(),
    val blockMatch: Boolean,
    val ruleSummary: RuleSummary = RuleSummary(),
    val ruleAvailability: RuleAvailability = RuleAvailability.Ready,
) {
    val appRules = ruleSummary.appIdToRules[topActivity.appId] ?: emptyList()
    val activityRules =
        if (blockMatch) emptyList()
        else
            appRules.filter { rule ->
                rule.matchActivity(topActivity.appId, topActivity.activityId)
            }
    val globalRules =
        if (blockMatch) emptyList()
        else
            ruleSummary.globalRules.filter { r ->
                r.matchActivity(topActivity.appId, topActivity.activityId)
            }

    val currentRules = (activityRules + globalRules).sortedBy { it.order }
    val emptyReason: String
        get() =
            when {
                ruleAvailability == RuleAvailability.Loading -> "rules_loading"
                ruleAvailability == RuleAvailability.Failed -> "rules_load_failed"
                blockMatch -> "app_blocked"
                else -> "no_rules_for_activity"
            }

    val hasPriorityRule = currentRules.size > 1 && currentRules.any { it.priorityEnabled }
    val activePriority: Boolean
        get() = hasPriorityRule && currentRules.any { it.isPriority() }

    /** 排序结果缓存：快照按事件创建，但 priorityRules 可能被访问多次，不能每次重排 */
    private val priorityRulesCache: List<ResolvedRule> by lazy {
            if (currentRules.size <= 1) return@lazy currentRules
            val anyFast = currentRules.any { it.hasFastLiteral }
            if (!hasPriorityRule && !anyFast) return@lazy currentRules
            // 灵犀：priority 规则仍然最优先（订阅作者明确要求的）；
            // 其次是灵犀喂养（字面量 + 保守判定，通常是这一屏最先能判定的那条），
            // 最后按"带字面量（vid/id/text）"排前面——这类规则走快查，1~2 次 IPC 就能判定。
            currentRules.sortedWith(
                compareBy(
                    { if (it.isPriority()) 0 else 1 },
                    { if (it.subsItem.id == KeywordRules.SUBS_ID) 0 else 1 },
                    { if (it.hasFastLiteral) 0 else 1 },
                ),
            )
    }

    val priorityRules: List<ResolvedRule>
        get() = priorityRulesCache

    val skipMatch: Boolean
        get() {
            return currentRules.all { r -> !r.status.ok }
        }

    val skipConsumeEvent: Boolean
        get() {
            return currentRules.all { r -> !r.status.alive }
        }

    val hasFeatureAction: Boolean
        get() = currentRules.any { r ->
            r.checkForced() && (r.status == RuleStatus.Ready || r.status == RuleStatus.Cooldown)
        }
}
