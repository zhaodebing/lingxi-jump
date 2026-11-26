/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 规则包页顶栏动作：把上游的三个图标按钮换成文字胶囊（匹配中/已暂停 · 设置 · 更多），
 * 与行式界面保持一致；"更多"里放新增应用规则 / 新增全局规则。
 */
package li.gkd.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import li.gkd.app.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun RowScope.GkSubscriptionActions(
    matching: Boolean,
    onToggleMatching: () -> Unit,
    onSettings: () -> Unit,
    onAddAppRule: () -> Unit,
    onAddGlobalRule: () -> Unit,
    onMenuOpen: () -> Boolean = { true },
) {
    var expanded by remember { mutableStateOf(false) }

    LxChip(
        text = if (matching) "匹配中" else "已暂停",
        active = matching,
        onClick = onToggleMatching,
    )
    LxChip(text = "设置", onClick = onSettings)
    Box {
        LxChip(text = "更多", onClick = { expanded = onMenuOpen() })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.app_rule_add)) },
                onClick = {
                    expanded = false
                    onAddAppRule()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.global_rule_add)) },
                onClick = {
                    expanded = false
                    onAddGlobalRule()
                },
            )
        }
    }
}
