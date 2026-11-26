package li.gkd.app.ui.style

import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import li.gkd.app.app
import li.gkd.app.ui.share.LocalDarkTheme
import li.gkd.app.ui.share.LocalIsTalkbackEnabled
import li.gkd.app.ui.theme.GkTheme
import li.gkd.app.ui.theme.rememberAppearance
import li.gkd.app.util.AndroidTarget

// 灵犀跳跃配色：绿底白卡，与首页 LxUi 取色完全一致
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B713D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE8D6),
    onPrimaryContainer = Color(0xFF07240F),
    secondary = Color(0xFF4F6353),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E8DA),
    onSecondaryContainer = Color(0xFF0B2015),
    background = Color(0xFFE9F3EC),
    onBackground = Color(0xFF14241B),
    surface = Color(0xFFE9F3EC),
    onSurface = Color(0xFF14241B),
    surfaceVariant = Color(0xFFDBE9DF),
    onSurfaceVariant = Color(0xFF6B7C72),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4FAF5),
    surfaceContainer = Color(0xFFF8FCF9),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = Color(0xFF8A9A90),
    outlineVariant = Color(0xFFE8EFE9),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7FD6A1),
    onPrimary = Color(0xFF06280F),
    primaryContainer = Color(0xFF12502A),
    onPrimaryContainer = Color(0xFFCDE8D6),
    background = Color(0xFF101813),
    onBackground = Color(0xFFE2EDE5),
    surface = Color(0xFF101813),
    onSurface = Color(0xFFE2EDE5),
    surfaceVariant = Color(0xFF223028),
    onSurfaceVariant = Color(0xFFB9C9BE),
    surfaceContainer = Color(0xFF18231C),
    surfaceContainerHigh = Color(0xFF1E2B23),
    surfaceContainerHighest = Color(0xFF243329),
    outline = Color(0xFF8A9A90),
    outlineVariant = Color(0xFF2C3B31),
)

@Composable
fun AppTheme(
    invertedTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val appearance by rememberAppearance()
    val darkTheme = appearance.isDark(isSystemInDarkTheme(), invertedTheme)
    val enableDynamicColor = false   // 灵犀跳跃固定深绿配色，不跟随系统取色
    val colorScheme =
        when {
            AndroidTarget.S && enableDynamicColor && darkTheme -> dynamicDarkColorScheme(app)
            AndroidTarget.S && enableDynamicColor && !darkTheme -> dynamicLightColorScheme(app)
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    val activity = LocalActivity.current
    if (activity != null) {
        LaunchedEffect(darkTheme) {
            // https://github.com/gkd-kit/gkd/pull/421
            WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
        }
        val bg = colorScheme.background.toArgb()
        LaunchedEffect(darkTheme, bg) {
            activity.window.decorView.setBackgroundColor(bg)
        }
    }

    var isTalkbackEnabled by remember { mutableStateOf(app.a11yManager.isTouchExplorationEnabled) }
    DisposableEffect(null) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener {
            isTalkbackEnabled = it
        }
        app.a11yManager.addTouchExplorationStateChangeListener(listener)
        onDispose {
            app.a11yManager.removeTouchExplorationStateChangeListener(listener)
        }
    }
    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalIsTalkbackEnabled provides isTalkbackEnabled,
    ) {
        GkTheme(
            colorScheme = colorScheme,
            typography = li.gkd.app.ui.home.LxTypography,
            shapes = li.gkd.app.ui.home.LxShapes,
            content = content,
        )
    }
}
