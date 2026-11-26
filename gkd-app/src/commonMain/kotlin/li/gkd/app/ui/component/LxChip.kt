/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 顶栏文字动作胶囊：替代上游的图标按钮，和行式界面保持一致。
 */
package li.gkd.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import li.gkd.app.ui.home.LxColor

@Composable
fun LxChip(text: String, active: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
                .background(
                    color = LxColor.Card,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (active) LxColor.Green else LxColor.TextSub,
        )
    }
}
