package li.gkd.selector

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 关键词通道生成的选择器必须满足两条：
 * 1. 能通过引擎编译（语法合法）；
 * 2. 只做 等于 / 开头 / 结尾（且前缀后缀必须是"装饰 + 关键词"），
 *    绝不做"包含"、也不做裸的"以关键词结尾"，否则「不跳过」「跳过广告领奖励」会被误点。
 */
class KeywordSelectorTest {
    // 与 li.gkd.app.keyword.KeywordRules.selectorsOf("跳过") 保持一致
    private val skipSelectors: List<String> =
        listOf(
            "[text=\"跳过\"]",
            "[desc=\"跳过\"]",
            "[text^=\"跳过 \"]",
            "[text^=\"跳过(\"]",
            "[text^=\"跳过（\"]",
            "[text^=\"跳过:\"]",
            "[text^=\"跳过：\"]",
            "[text^=\"跳过|\"]",
            "[text^=\"跳过｜\"]",
            "[text\$=\"|跳过\"]",
            "[text\$=\"｜跳过\"]",
            "[text\$=\")跳过\"]",
            "[text\$=\"）跳过\"]",
            "[text\$=\"s跳过\"]",
            "[text\$=\"S跳过\"]",
            "[text\$=\"秒跳过\"]",
        )

    private fun node(text: String? = null, desc: String? = null) =
        TestNode(
            key = "n",
            name = "android.widget.TextView",
            attributes = buildMap {
                if (text != null) put("text", text)
                if (desc != null) put("desc", desc)
            },
        )

    private fun matchAny(source: String, target: TestNode): Boolean =
        Selector.compile(source).value.match(target, TestNodeAdapter) != null

    @Test
    fun everySelectorCompiles() {
        skipSelectors.forEach { source ->
            assertNotNull(Selector.compile(source).value, "编译失败: $source")
        }
    }

    @Test
    fun matchesExactAndDecoratedText() {
        listOf("跳过", "跳过 5s", "跳过(3)", "跳过: 2", "|跳过", "3s跳过", "5S跳过").forEach { text ->
            assertTrue(skipSelectors.any { matchAny(it, node(text = text)) }, "应命中: $text")
        }
        assertTrue(skipSelectors.any { matchAny(it, node(desc = "跳过")) }, "无障碍描述应命中")
    }

    @Test
    fun doesNotMatchSentencesContainingKeyword() {
        listOf("跳过广告领奖励", "不跳过", "跳过说明", "已跳过", "点击跳过").forEach { text ->
            assertTrue(skipSelectors.none { matchAny(it, node(text = text)) }, "不该命中: $text")
        }
    }

    @Test
    fun equalsSyntaxOnlyMatchesExactly() {
        val exact = Selector.compile("[text=\"跳过\"]").value
        assertNotNull(exact.match(node(text = "跳过"), TestNodeAdapter))
        assertNull(exact.match(node(text = "跳过 5s"), TestNodeAdapter))
    }

    @Test
    fun pageVetoSelectorMatchesWholeSentence() {
        val veto = Selector.compile("[text*=\"青少年模式\"]").value
        assertNotNull(veto.match(node(text = "青少年模式"), TestNodeAdapter))
        assertNotNull(veto.match(node(text = "已开启青少年模式"), TestNodeAdapter))
    }
}
