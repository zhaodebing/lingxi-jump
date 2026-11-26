/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 灵犀喂养在「投喂规则」列表里的卡片：和订阅卡片同一套容器、字号、开关位置。
 */
package li.gkd.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import li.gkd.app.keyword.KeywordSettings

@Composable
fun KeywordSubsCard(
    settings: KeywordSettings,
    onOpen: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    val count = settings.entries.size
    val subtitle =
        when {
            count == 0 -> "未填写关键词，不会动作"
            settings.enabled -> "已开启 · $count 个词"
            else -> "$count 个词 · 已关闭"
        }
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp)
                .clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LxColor.Card),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "灵犀喂养",
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = LxColor.TextMain,
                        ),
                )
                Text(
                    text = subtitle,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(color = LxColor.TextSub),
                )
            }
            Switch(
                checked = settings.enabled,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
