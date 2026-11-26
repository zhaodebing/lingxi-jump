/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 界面：只留语言。本版固定浅色（灵犀跳跃绿），所以不再提供深浅色切换 ——
 * 之前那个开关对自定义页面本来就不生效，属于"点了没反应"，直接去掉。
 */
package li.gkd.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import li.gkd.app.model.findOption
import li.gkd.app.resources.*
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.component.GkTextMenu
import li.gkd.app.ui.home.LxGroup
import li.gkd.app.ui.home.LxNote
import li.gkd.app.ui.home.LxPage
import li.gkd.app.ui.option.LanguageOption
import org.jetbrains.compose.resources.stringResource

@Composable
fun InterfaceSettingsPage() {
    val mainVm = MainViewModel.requireCurrent()
    val vm = viewModel { InterfaceSettingsViewModel() }
    val selectedLanguage by vm.selectedLanguage.collectAsStateWithLifecycle()

    LxPage(title = stringResource(Res.string.settings_interface), onBack = mainVm.navigator::pop) {
        LxGroup("语言") {
            GkTextMenu(
                title = stringResource(Res.string.language_title),
                option = LanguageOption.objects.findOption(selectedLanguage),
                onOptionChange = { vm.setLanguage(it.value) },
            )
        }
        LxGroup(null) {
            LxNote("本版界面固定为灵犀跳跃配色，不提供深浅色切换。")
        }
    }
}
