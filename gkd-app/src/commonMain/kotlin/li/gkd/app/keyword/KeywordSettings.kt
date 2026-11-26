/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 灵犀喂养配置：开关 + 关键词 + 时机三个参数（都是我们原来的内容）。
 * 没有内置词：不填关键词就不生效。
 *
 * 关键词语法沿用我们原来的（李跳跳式）：
 *   跳过   整段文字等于关键词，或关键词+装饰（"跳过 5s"）/ 装饰+关键词
 *   +跳过  只认以它开头的文字
 *   -跳过  只认以它结尾的文字
 *   =跳过  只认完全相等
 */
package li.gkd.app.keyword

import kotlinx.serialization.Serializable

@Serializable
data class KeywordSettings(
    val enabled: Boolean = false,
    /** 关键词，空格/换行/制表符分隔；留空则不生效 */
    val text: String = "",
    /** 启动后多少毫秒内有效，0 = 不限 */
    val matchTimeMs: Long = 5000L,
    /** 每次启动最多点几次，0 = 不限 */
    val actionMaximum: Int = 1,
    /** 同一关键词两次点击最小间隔 */
    val actionCdMs: Long = 3000L,
) {
    /** 原始词条（保留 = + - 前缀，选择器生成时用） */
    val entries: List<String>
        get() =
            text.split(' ', '\n', '\r', '\t', '\u3000')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()

    val active: Boolean
        get() = enabled && entries.isNotEmpty()
}
