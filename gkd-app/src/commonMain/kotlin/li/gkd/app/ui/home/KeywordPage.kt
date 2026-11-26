/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 灵犀喂养页：文案按"一看就懂"重写。
 *   · 顶部一句话说它干什么
 *   · 中间只有一个输入框（不填就不生效）
 *   · 下面三行说明：什么时候动手 / 什么不点 / 点了没反应怎么办
 *   · 时机参数收进「高级」，默认折叠
 */
package li.gkd.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import li.gkd.app.keyword.KeywordSettings
import li.gkd.app.settings.SettingsRepository
import li.gkd.app.ui.MainViewModel

@Composable
fun KeywordPage() {
    val mainVm = MainViewModel.requireCurrent()
    val settings by SettingsRepository.keywordSettings.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf(settings.text) }
    var showAdvanced by remember { mutableStateOf(false) }

    // 停止输入 600ms 后再写盘并重建规则，避免每敲一个字都编译一遍选择器
    LaunchedEffect(draft) {
        if (draft == settings.text) return@LaunchedEffect
        delay(600)
        SettingsRepository.updateKeywordSettings { it.copy(text = draft) }
    }

    // 用户可能在 600ms 内就退出页面，此时 LaunchedEffect 会被取消、输入丢失。
    // 离开时把未保存的内容补写一次。
    DisposableEffect(Unit) {
        onDispose {
            val pending = draft
            if (pending != SettingsRepository.keywordSettings.value.text) {
                SettingsRepository.updateKeywordSettings { it.copy(text = pending) }
            }
        }
    }

    fun patch(transform: (KeywordSettings) -> KeywordSettings) =
        SettingsRepository.updateKeywordSettings(transform)

    val count = settings.entries.size

    LxPage(title = "灵犀喂养", onBack = mainVm.navigator::pop) {
        LxGroup(null) {
            LxSwitchRow(
                title = "灵犀喂养",
                checked = settings.enabled,
                subtitle = "只帮你点开屏广告上的「跳过」",
                onText = "开着",
                offText = "关着",
            ) { on -> patch { it.copy(enabled = on) } }
        }

        LxGroup(if (count == 0) "关键词 · 还没填，现在不会动手" else "关键词 · 已填 $count 个") {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .height(96.dp),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 15.sp),
                placeholder = {
                    Text("例如：跳过 关闭广告", fontSize = 15.sp, color = LxColor.Foot)
                },
            )
            LxDivider()
            Text(
                "想跳哪些字就填哪些字，多个词用空格隔开。填「跳过」就够了。",
                fontSize = 12.sp,
                color = LxColor.TextSub,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        LxGroup("它怎么工作") {
            LxRow("什么时候动手", "应用刚打开、你还没碰屏幕的那几秒")
            LxDivider()
            LxRow("什么不点", "输入框、列表里的字、搜索/设置/详情页")
            LxDivider()
            LxRow("点了没反应", "半秒内自己发现，不弹提示，再补一次")
        }

        LxGroup(null) {
            LxRow(
                if (showAdvanced) "高级设置   收起" else "高级设置",
                "时机与次数",
                chevron = true,
            ) { showAdvanced = !showAdvanced }
        }

        if (showAdvanced) {
            LxGroup(null) {
                LxRow(
                    title = "应用打开后多久内动手",
                    value = if (settings.matchTimeMs <= 0) "一直" else "${settings.matchTimeMs / 1000} 秒",
                    accent = true,
                    chevron = true,
                ) { patch { it.copy(matchTimeMs = nextMatchTime(it.matchTimeMs)) } }
                LxDivider()
                LxRow(
                    title = "同一屏最多点几次",
                    value = if (settings.actionMaximum <= 0) "不限" else "${settings.actionMaximum} 次",
                    accent = true,
                    chevron = true,
                ) { patch { it.copy(actionMaximum = nextMaximum(it.actionMaximum)) } }
                LxDivider()
                LxRow(
                    title = "两次尝试间隔",
                    value = if (settings.actionCdMs <= 0) "不限" else "${settings.actionCdMs / 1000} 秒",
                    accent = true,
                    chevron = true,
                ) { patch { it.copy(actionCdMs = nextCd(it.actionCdMs)) } }
            }

            LxGroup("词的写法（一般不用）") {
                LxRow("跳过", "整段文字等于它，或带「秒/括号」这类装饰")
                LxDivider()
                LxRow("+跳过", "文字以它开头")
                LxDivider()
                LxRow("-跳过", "文字以它结尾")
                LxDivider()
                LxRow("=跳过", "只认完全一样")
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
        Text(
            "不填关键词就不会动作。订阅规则里的开屏规则自己会跑，两边不冲突。",
            fontSize = 12.sp,
            color = LxColor.Foot,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

private val MATCH_TIMES = listOf(3000L, 5000L, 8000L, 12000L, 0L)
private val MAXIMUMS = listOf(1, 2, 3, 0)
private val CDS = listOf(1000L, 2000L, 3000L, 5000L, 0L)

private fun nextMatchTime(current: Long): Long =
    MATCH_TIMES[(MATCH_TIMES.indexOf(current).takeIf { it >= 0 } ?: 1).let { (it + 1) % MATCH_TIMES.size }]

private fun nextMaximum(current: Int): Int =
    MAXIMUMS[(MAXIMUMS.indexOf(current).takeIf { it >= 0 } ?: 0).let { (it + 1) % MAXIMUMS.size }]

private fun nextCd(current: Long): Long =
    CDS[(CDS.indexOf(current).takeIf { it >= 0 } ?: 2).let { (it + 1) % CDS.size }]
