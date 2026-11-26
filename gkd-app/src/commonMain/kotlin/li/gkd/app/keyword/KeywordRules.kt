/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 把用户关键词编译成一条 GKD 全局规则组，走引擎原生管线：
 *   选择器编译 → 快查(findAccessibilityNodeInfosByText) → 匹配 → 动作 → 触发记录
 *
 * 关键词语义沿用我们原来的（李跳跳式）：
 *   跳过   等于 / 跳过+装饰 / 装饰+跳过     （装饰 = 空格 ( （ : ： | ｜ / | ｜ ) ） s S 秒）
 *   +跳过  开头      -跳过  结尾      =跳过  完全相等
 *
 * 9.00 只贡献"防误触"，在 KeywordGuard 里，不在这里。
 * 没有内置词：用户不填就不生成规则。
 */
package li.gkd.app.keyword

import li.gkd.app.subscription.RawSubscription
import li.gkd.app.subscription.UsedSubsEntry
import li.gkd.db.SubsItem

object KeywordRules {
    /** 保留的本地订阅 id：负数不会与线上订阅冲突 */
    const val SUBS_ID = -1000L
    const val GROUP_KEY = 1
    const val GROUP_NAME = "灵犀喂养"

    /** 广告容器 id：自绘开屏的「跳过」是容器里一个没有文字的可点 View */
    private val AD_CONTAINERS =
        listOf(
            "ad_container",
            "splash_container",
            "splash_root",
            "splash_layout",
            "ad_root",
            "ad_view_layout",
            "advertising_container",
            "adContainer",
            "ksad_splash_root_container",
            "adsFl",
            "pyAdLayout",
        )

    /** 关键词后面常见的装饰（"跳过 5s" / "跳过(3)" / "跳过:2"） */
    private val PREFIX_DECORATIONS = listOf(" ", "(", "（", ":", "：", "|", "｜")

    /** 关键词前面常见的装饰（"5s | 跳过" / "3) 跳过"） */
    private val SUFFIX_DECORATIONS = listOf("|", "｜", ")", "）", "s", "S", "秒")

    fun entry(settings: KeywordSettings): List<UsedSubsEntry> {
        val subscription = build(settings) ?: return emptyList()
        return listOf(
            UsedSubsEntry(
                SubsItem(id = SUBS_ID, enable = true, order = -1),
                subscription,
            ),
        )
    }

    fun build(settings: KeywordSettings): RawSubscription? {
        // 必须用户自己填：不填 = 不生成任何规则
        if (!settings.active) return null
        val rules =
            settings.entries.mapIndexed { index, entry ->
                RawSubscription.RawGlobalRule(
                    key = index + 1,
                    name = entry,
                    actionCd = settings.actionCdMs.takeIf { it > 0 },
                    actionDelay = null,
                    fastQuery = true,
                    matchRoot = false,
                    matchDelay = null,
                    matchTime = null,
                    actionMaximum = settings.actionMaximum.takeIf { it > 0 },
                    resetMatch = null,
                    actionCdKey = null,
                    actionMaximumKey = null,
                    priorityTime = null,
                    priorityActionMaximum = null,
                    order = null,
                    forcedTime = null,
                    snapshotUrls = null,
                    excludeSnapshotUrls = null,
                    exampleUrls = null,
                    preKeys = null,
                    action = null,
                    position = null,
                    swipeArg = null,
                    // 注意：matches 是"全部命中"(AND)，anyMatches 才是"任一命中"(OR)。
                    // 一个词会展开成十几条选择器，必须放 anyMatches，否则永远不可能匹配。
                    matches = null,
                    anyMatches = selectorsOf(entry),
                    excludeMatches = null,
                    excludeAllMatches = null,
                    apps = null,
                )
            } + listOf(containerRule(settings, key = settings.entries.size + 1))
        val group =
            RawSubscription.RawGlobalGroup(
                key = GROUP_KEY,
                name = GROUP_NAME,
                desc = "用户关键词，空格分隔；=精确 +开头 -结尾",
                enable = true,
                scopeKeys = null,
                actionCd = settings.actionCdMs.takeIf { it > 0 },
                actionDelay = null,
                fastQuery = true,
                matchRoot = false,
                matchDelay = null,
                matchTime = settings.matchTimeMs.takeIf { it > 0 },
                actionMaximum = settings.actionMaximum.takeIf { it > 0 },
                resetMatch = "activity",
                actionCdKey = null,
                actionMaximumKey = null,
                priorityTime = null,
                priorityActionMaximum = null,
                order = null,
                forcedTime = null,
                snapshotUrls = null,
                excludeSnapshotUrls = null,
                exampleUrls = null,
                matchAnyApp = true,
                matchSystemApp = false,
                matchLauncher = false,
                disableIfAppGroupMatch = null,
                rules = rules,
                apps = null,
            )
        return RawSubscription(
            id = SUBS_ID,
            name = GROUP_NAME,
            version = 1,
            globalGroups = listOf(group),
        )
    }

    /**
     * 自绘开屏的兜底：广告容器里那个"没有文字、可点击、没有子节点"的 View 就是跳过按钮。
     * 用容器 id 锚定，不做全页空按钮猜测（9.00 试过全页猜测，知乎/微博/百度都误触过）。
     */
    private fun containerRule(settings: KeywordSettings, key: Int): RawSubscription.RawGlobalRule =
        RawSubscription.RawGlobalRule(
            key = key,
            name = "容器里的跳过按钮",
            actionCd = settings.actionCdMs.takeIf { it > 0 },
            actionDelay = null,
            fastQuery = true,
            matchRoot = false,
            matchDelay = null,
            matchTime = null,
            actionMaximum = settings.actionMaximum.takeIf { it > 0 },
            resetMatch = null,
            actionCdKey = null,
            actionMaximumKey = null,
            priorityTime = null,
            priorityActionMaximum = null,
            order = null,
            forcedTime = null,
            snapshotUrls = null,
            excludeSnapshotUrls = null,
            exampleUrls = null,
            preKeys = null,
            action = null,
            position = null,
            swipeArg = null,
            matches = null,
            anyMatches =
                AD_CONTAINERS.map {
                    "[vid=\"$it\"] > [clickable=true][text=null][desc=null][childCount=0]"
                },
            excludeMatches = null,
            excludeAllMatches = null,
            apps = null,
        )

    /**
     * 一个词条 → 选择器。全部是"字面量 + 比较运算符"，
     * 快查用 findAccessibilityNodeInfosByText(子串) 拿候选后再按运算符精确过滤。
     */
    fun selectorsOf(entry: String): List<String> {
        val word = entry.trim()
        if (word.isEmpty()) return emptyList()
        return when {
            // =xxx 精确
            word.startsWith("=") && word.length > 1 ->
                listOf(textSelector("=", escape(word.drop(1))))
            // +xxx 开头
            word.startsWith("+") && word.length > 1 ->
                listOf(textSelector("^=", escape(word.drop(1))))
            // -xxx 结尾（带装饰，避免命中「不跳过」这类反面按钮）
            word.startsWith("-") && word.length > 1 -> {
                val value = escape(word.drop(1))
                SUFFIX_DECORATIONS.map { textSelector("$=", escape(it) + value) }
            }
            else -> {
                val value = escape(word)
                val bounded =
                    "[text^=\"$value\"][text.length<=${word.length + 3}]" +
                        "[text!*=\"联网\"][text!*=\"片头\"][text!*=\"引导\"][text!*=\"登录\"]"
                buildList {
                    // 第一位放"最常见的那一枪"：跳过 / 跳过3 / 跳过 3 / 跳过广告 都能一次命中，
                    // 后面那些只在它没中时才会被查询，不会拖慢常规开屏。
                    add(bounded)
                    add(textSelector("=", value))
                    PREFIX_DECORATIONS.forEach { add(textSelector("^=", value + escape(it))) }
                    SUFFIX_DECORATIONS.forEach { add(textSelector("$=", escape(it) + value)) }
                    add(descSelector("=", value))
                }.distinct()
            }
        }
    }

    private fun textSelector(operator: String, value: String) = "[text$operator\"$value\"]"

    private fun descSelector(operator: String, value: String) = "[desc$operator\"$value\"]"

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")
}
