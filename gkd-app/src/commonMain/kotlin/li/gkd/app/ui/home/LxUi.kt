/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 全应用统一的视觉组件：所有页面（首页 / 订阅 / 应用 / 设置 / 二级页）都用这一套，
 * 不各写一套。色值与「灵犀跳跃 9.00」一致：
 *   BG #E9F3EC / 卡片 0xD6FFFFFF / 主文字 #14241B / 次要 #6B7C72
 *   分隔线 #E8EFE9 / 强调 #1B713D / 深绿 #0F592F→#1B713D
 */
package li.gkd.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object LxColor {
    val Bg = Color(0xFFE9F3EC)
    val Card = Color(0xD6FFFFFF)
    val Deep = Color(0xFF0F592F)
    val Green = Color(0xFF1B713D)
    val TextMain = Color(0xFF14241B)
    val TextSub = Color(0xFF6B7C72)
    val Line = Color(0xFFE8EFE9)
    val HeadSub = Color(0xFFCFE3D6)
    val Foot = Color(0xFF8A9A90)

    /** 失败/危险色（低饱和砖红，和整体克制的调子一致） */
    val Warn = Color(0xFFB4543F)
}

/** 二级页外壳：顶部返回栏 + 竖向滚动内容 */
@Composable
fun LxPage(
    title: String,
    onBack: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(LxColor.Bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Text(
                    "‹",
                    fontSize = 26.sp,
                    color = LxColor.TextMain,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                )
            } else {
                Box(Modifier.padding(start = 12.dp))
            }
            Text(
                title,
                fontSize = 18.sp,
                color = LxColor.TextMain,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        ) {
            content()
        }
    }
}

/** 卡片组：可选组标题 + 白色圆角卡片 */
@Composable
fun LxGroup(title: String? = null, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        if (!title.isNullOrEmpty()) {
            Text(
                title,
                fontSize = 12.sp,
                color = LxColor.TextSub,
                modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 6.dp),
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LxColor.Card),
        ) {
            content()
        }
    }
}

@Composable
fun LxRow(
    title: String,
    value: String = "",
    accent: Boolean = false,
    chevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 15.sp, color = LxColor.TextMain, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 13.sp, color = if (accent) LxColor.Green else LxColor.TextSub)
        }
        if (onClick != null && chevron) {
            Text("›", fontSize = 16.sp, color = LxColor.Foot, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
fun LxDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(1.dp)
            .background(LxColor.Line),
    )
}

/** 开关行（右侧显示 开/关 文案，点整行切换） */
@Composable
fun LxSwitchRow(
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    onText: String = "显示",
    offText: String = "隐藏",
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, color = LxColor.TextMain)
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = LxColor.Foot, maxLines = 2)
            }
        }
        Text(
            if (checked) onText else offText,
            fontSize = 13.sp,
            color = if (checked) LxColor.Green else LxColor.TextSub,
            maxLines = 1,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}


/** 单选行：整行可点，左侧标题 + 说明，右侧勾选标记 */
@Composable
fun LxChoice(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp,
                color = if (enabled) LxColor.TextMain else LxColor.Foot,
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = LxColor.TextSub)
        }
        Text(
            if (selected) "✓" else "",
            fontSize = 16.sp,
            color = LxColor.Green,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

/** 说明文字块（卡片内） */
@Composable
fun LxNote(text: String, warn: Boolean = false) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = if (warn) LxColor.Warn else LxColor.TextSub,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}
