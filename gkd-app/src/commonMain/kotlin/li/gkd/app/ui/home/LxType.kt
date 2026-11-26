/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 统一的排版与形状：所有页面（包括上游继承下来的订阅页、应用页、各设置二级页）
 * 都从这里取字号和圆角，避免"几个页面几种排版"。
 *
 * 取法参考「灵犀跳跃 9.00」：主文 15sp、次要 12~13sp、小字 11sp、卡片 16dp 圆角。
 */
package li.gkd.app.ui.home

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LxShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private fun TextStyle.lx(size: Int, weight: FontWeight = FontWeight.Normal, line: Int = (size * 1.45f).toInt()) =
    copy(
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = line.sp,
    )

/** 只覆盖会被页面实际用到的几档，其余沿用默认 */
val LxTypography = Typography().run {
    copy(
        displayLarge = displayLarge.lx(28, FontWeight.SemiBold),
        headlineMedium = headlineMedium.lx(22, FontWeight.SemiBold),
        headlineSmall = headlineSmall.lx(20, FontWeight.SemiBold),
        titleLarge = titleLarge.lx(18, FontWeight.SemiBold),
        titleMedium = titleMedium.lx(15, FontWeight.Medium),
        titleSmall = titleSmall.lx(14, FontWeight.Medium),
        bodyLarge = bodyLarge.lx(15),
        bodyMedium = bodyMedium.lx(13),
        bodySmall = bodySmall.lx(12),
        labelLarge = labelLarge.lx(13, FontWeight.Medium),
        labelMedium = labelMedium.lx(12),
        labelSmall = labelSmall.lx(11),
    )
}
