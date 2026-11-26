/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 灵犀喂养的防误触，移植自灵犀跳跃 9.00 —— 只取"防误触"，不取它的文字识别。
 *
 * 文字识别仍是我们原来的选择器（=精确 / +开头 / -结尾 / 等于+装饰）。
 * 这里只做 9.00 的否决条件：
 *   · small        控件必须是"小控件"（宽≤85%屏、高≤32%屏、面积≤38%屏）——整屏容器不是按钮
 *   · blockedByList 祖先里有 ScrollView/RecyclerView/ListView/GridView/NestedScrollView，
 *                   且控件中心落在屏高 16%~84% —— 信息流里的文字不算
 *   · isInput      3 层内是 EditText —— 输入框不碰
 *   · neverSplash  类名含 search/setting/detail/webview/login/chat 的页面整页不处理
 *   · BAD_PKG      微信/和平/王者/自己
 *   · 手指按住不动手，抬起 750ms 恢复；点不动连续 2 次冷却 60 秒
 *
 * 只作用于关键词规则（subsId = -1000），订阅规则完全不受影响。
 */
package li.gkd.app.matching.guard

import android.graphics.Rect
import android.os.SystemClock
import kotlinx.coroutines.delay
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale
import li.gkd.app.keyword.KeywordRules
import li.gkd.app.matching.input.currentTopActivity
import li.gkd.app.rule.ResolvedRule

object KeywordGuard {
    /** 完全不处理的包 */
    private val BAD_PKG =
        listOf(
            "com.tencent.mm",
            "com.tencent.tmgp.pubgmhd",
            "com.tencent.tmgp.sgame",
            "io.github.nanomuse.app",
        )

    /** 这些类名里出现的页面不是开屏 */
    private val NEVER = listOf("search", "setting", "detail", "webview", "login", "chat")

    private val SCROLLERS =
        listOf("ScrollView", "RecyclerView", "ListView", "GridView", "NestedScrollView")

    @Volatile private var fingerDown = false
    private var fingerDownAt = 0L

    private var failKey: String? = null
    private var failCount = 0
    private var failUntil = 0L

    fun isKeywordRule(rule: ResolvedRule): Boolean = rule.subsItem.id == KeywordRules.SUBS_ID

    fun noteInteraction() {
        fingerDown = true
        fingerDownAt = SystemClock.elapsedRealtime()
    }

    fun noteInteractionEnd() {
        fingerDown = false
    }

    private fun fingerHeld(): Boolean {
        if (!fingerDown) return false
        if (SystemClock.elapsedRealtime() - fingerDownAt > FINGER_HOLD_MS) {
            fingerDown = false
            return false
        }
        return true
    }

    // ---------------------------------------------------------------- 页面级

    fun allowPage(rule: ResolvedRule): Boolean {
        if (!isKeywordRule(rule)) return true
        val appId = currentTopActivity.appId
        if (appId.isNotEmpty() && isBadPackage(appId)) return false
        if (fingerHeld()) return false
        if (coolingDown(rule)) return false
        val activityId = currentTopActivity.activityId
        if (activityId != null && neverSplash(activityId)) return false
        return true
    }

    private fun isBadPackage(pkg: String): Boolean = BAD_PKG.any { pkg == it || pkg.startsWith("$it.") }

    /** 只看类名本身（9.00 踩过的坑：包路径里带 login 会把正常的 SplashActivity 误杀） */
    private fun neverSplash(activityId: String): Boolean {
        val simple = activityId.substringAfterLast('.').lowercase(Locale.ROOT)
        return NEVER.any { simple.contains(it) }
    }

    // ---------------------------------------------------------------- 控件级

    /**
     * 决定"到底点哪个节点"。
     *
     * 文字节点经常不是按钮：腾讯视频的「跳过」是个 TextView，本身不可点、点中心也没反应，
     * 真正挂点击监听的是它外面那层容器。这里向上找最近的可点击祖先，
     * 但要求两者面积接近（≤6 倍），避免把整屏广告容器当成按钮点。
     */
    fun adjustTarget(rule: ResolvedRule, node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (!isKeywordRule(rule)) return node
        if (!allowNode(rule, node)) return null
        if (runCatching { node.isClickable }.getOrDefault(false)) return node
        val nodeRect = rectOf(node) ?: return node
        if (nodeRect.isEmpty) return node
        val nodeArea = nodeRect.width().toLong() * nodeRect.height()
        var current: AccessibilityNodeInfo? = runCatching { node.parent }.getOrNull()
        var depth = 0
        while (depth < MAX_CLICK_ANCESTOR_DEPTH) {
            val candidate = current ?: break
            val clickable = runCatching { candidate.isClickable }.getOrDefault(false)
            if (clickable) {
                val rect = rectOf(candidate)
                if (rect != null && !rect.isEmpty &&
                    rect.width().toLong() * rect.height() <= nodeArea * MAX_CLICK_AREA_RATIO
                ) {
                    return candidate
                }
            }
            current = runCatching { candidate.parent }.getOrNull()
            depth++
        }
        return node
    }

    fun allowNode(rule: ResolvedRule, node: AccessibilityNodeInfo): Boolean {
        if (!isKeywordRule(rule)) return true
        if (!node.isVisibleToUser) return false
        if (isInput(node)) return false

        val metrics = android.content.res.Resources.getSystem().displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        if (width <= 0 || height <= 0) return true
        if (!small(node, width, height)) return false
        if (blockedByList(node, height)) return false
        return true
    }

    private fun rectOf(node: AccessibilityNodeInfo): Rect? =
        runCatching {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                rect
            }
            .getOrNull()

    /** 小控件：宽 ≤85% 屏、高 ≤32% 屏、面积 ≤38% 屏（9.00 的 small） */
    private fun small(node: AccessibilityNodeInfo, width: Int, height: Int): Boolean {
        val rect = rectOf(node) ?: return true
        if (rect.isEmpty) return true
        return rect.width() <= width * 0.85f &&
            rect.height() <= height * 0.32f &&
            rect.width().toLong() * rect.height() <= width.toLong() * height * 0.38f
    }

    private fun isInput(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (depth < 3) {
            val target = current ?: break
            val className = runCatching { target.className?.toString().orEmpty() }.getOrDefault("")
            if (className.endsWith("EditText")) return true
            current = runCatching { target.parent }.getOrNull()
            depth++
        }
        return false
    }

    /** 信息流中段的文字不算按钮（9.00 的 blockedByList） */
    private fun blockedByList(node: AccessibilityNodeInfo, screenHeight: Int): Boolean {
        if (!inList(node)) return false
        val rect = rectOf(node) ?: return false
        if (rect.isEmpty) return false
        val center = (rect.top + rect.bottom) / 2f
        return center >= screenHeight * 0.16f && center <= screenHeight * 0.84f
    }

    private fun inList(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (depth < 12) {
            val target = current ?: break
            val className = runCatching { target.className?.toString() }.getOrNull()
            if (className != null && SCROLLERS.any { className.endsWith(it) }) return true
            current = runCatching { target.parent }.getOrNull()
            depth++
        }
        return false
    }

    // ---------------------------------------------------------------- 点后验证

    /**
     * 点击之后回头看一眼：同一个窗口里、"跳过"这几个字还在，说明根本没跳掉。
     *
     * 引擎判定"成功"的依据只是 performAction 的返回值 / 手势是否发出，
     * 广告自绘的按钮经常是"点得到、不响应"，于是会出现"实际没跳过却弹了已跳过提示"。
     * 这里用「原节点是否还在」做一次廉价复核：还在 → 不算成功。
     */
    suspend fun verifyAfterClick(
        node: AccessibilityNodeInfo,
        rootProvider: () -> AccessibilityNodeInfo?,
    ): Boolean {
        val windowId = runCatching { node.windowId }.getOrDefault(-1)
        val label =
            runCatching { node.text?.toString() }.getOrNull()
                ?: runCatching { node.contentDescription?.toString() }.getOrNull()
        if (windowId < 0 || label.isNullOrEmpty()) return true
        val activityId = currentTopActivity.activityId
        delay(VERIFY_DELAY_MS)
        // 页面已经换了 = 广告确实被关掉，直接算成功（避免旧根节点还没释放造成误判）
        if (currentTopActivity.activityId != activityId) return true
        val root = runCatching { rootProvider() }.getOrNull() ?: return true
        val nodes = runCatching { root.findAccessibilityNodeInfosByText(label) }.getOrNull() ?: return true
        for (candidate in nodes) {
            val sameWindow = runCatching { candidate.windowId }.getOrDefault(-1) == windowId
            if (!sameWindow) continue
            val sameLabel =
                runCatching { candidate.text?.toString() ?: candidate.contentDescription?.toString() }
                    .getOrNull() == label
            val visible = runCatching { candidate.isVisibleToUser }.getOrDefault(false)
            if (sameLabel && visible) return false
        }
        return true
    }

    // ---------------------------------------------------------------- 结果回灌

    @Synchronized
    fun noteResult(rule: ResolvedRule, success: Boolean) {
        if (!isKeywordRule(rule)) return
        val key = currentTopActivity.appId + ":" + rule.key
        if (success) {
            failKey = null
            failCount = 0
            failUntil = 0L
            return
        }
        if (failKey == key) failCount++ else {
            failKey = key
            failCount = 1
        }
        if (failCount >= FAIL_THRESHOLD) failUntil = SystemClock.elapsedRealtime() + FAIL_COOLDOWN_MS
    }

    @Synchronized
    private fun coolingDown(rule: ResolvedRule): Boolean {
        val until = failUntil
        if (until == 0L) return false
        if (SystemClock.elapsedRealtime() >= until) {
            failUntil = 0L
            failKey = null
            failCount = 0
            return false
        }
        return failKey == currentTopActivity.appId + ":" + rule.key
    }

    /** 点完等多久再复核；太短会在广告还没响应时误判 */
    /** 向上找可点击祖先的最大层数 */
    private const val MAX_CLICK_ANCESTOR_DEPTH = 3

    /** 祖先面积最多是文字节点的几倍，超过就不算"同一个按钮" */
    private const val MAX_CLICK_AREA_RATIO = 6L

    private const val VERIFY_DELAY_MS = 500L
    private const val FINGER_HOLD_MS = 750L
    private const val FAIL_THRESHOLD = 2
    private const val FAIL_COOLDOWN_MS = 60_000L
}
