/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 关于页：只保留「版本 / 内核 / 隐私 / 许可」，视觉与首页一致。
 * 上游这里的更新渠道、捐赠、彩蛋、反馈等入口已移除。
 */
package li.gkd.app.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import li.gkd.app.META
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.home.LxColor
import li.gkd.app.ui.home.LxDivider
import li.gkd.app.ui.home.LxGroup
import li.gkd.app.ui.home.LxPage
import li.gkd.app.ui.home.LxRow
import li.gkd.app.ui.navigation.SupportRoute

private const val GKD_REPO = "https://github.com/gkd-kit/gkd"
private const val LINGXI_REPO = "https://github.com/zhaodebing/lingxi-jump"
private const val LINGXI_RELEASES = "$LINGXI_REPO/releases/latest"
private const val LINGXI_ISSUES = "$LINGXI_REPO/issues"

@Composable
fun AboutPage() {
    val mainVm = MainViewModel.requireCurrent()
    var showPrivacy by remember { mutableStateOf(false) }
    var showLicense by remember { mutableStateOf(false) }

    LxPage(title = "关于", onBack = { mainVm.navigator.pop() }) {
        LxGroup(null) {
            LxRow("灵犀跳跃", "v${META.versionName}")
            LxDivider()
            LxRow("包名", META.appId)
            LxDivider()
            LxRow("内核", "与 GKD 深度融合", chevron = true) {
                mainVm.navigator.openWebPage(GKD_REPO)
            }
        }

        Spacer(Modifier.height(14.dp))

        LxGroup("开源") {
            LxRow("源码仓库", "github.com/zhaodebing/lingxi-jump", chevron = true) {
                mainVm.navigator.openWebPage(LINGXI_REPO)
            }
            LxDivider()
            LxRow("检查更新", "GitHub Releases", chevron = true) {
                mainVm.navigator.openWebPage(LINGXI_RELEASES)
            }
            LxDivider()
            LxRow("问题反馈", "提 Issue", chevron = true) {
                mainVm.navigator.openWebPage(LINGXI_ISSUES)
            }
            LxDivider()
            LxRow("开源许可", "GPL-3.0", chevron = true) { showLicense = true }
        }

        Spacer(Modifier.height(14.dp))

        LxGroup("隐私") {
            LxRow("数据收集", "无", chevron = true) { showPrivacy = true }
            LxDivider()
            LxRow("联网行为", "仅订阅更新", chevron = true) { showPrivacy = true }
        }

        Spacer(Modifier.height(14.dp))

        LxGroup("支持") {
            LxRow("赞赏", "请我喝咖啡", accent = true, chevron = true) {
                mainVm.navigator.navigate(SupportRoute)
            }
            LxDivider()
            LxRow("公众号", "趣味暖心", chevron = true) {
                mainVm.navigator.navigate(SupportRoute)
            }
        }

        Spacer(Modifier.height(14.dp))

        Spacer(Modifier.height(14.dp))
        Text(
            "灵犀跳跃与 GKD 深度融合运行，规则匹配内核来自 gkd-kit/gkd。\n" +
                "按 GPL-3.0-only 授权分发，不得用于商业用途。",
            fontSize = 11.sp,
            color = LxColor.Foot,
        )
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("隐私说明", fontSize = 16.sp) },
            text = {
                Text(
                    "· 不采集设备信息，不埋点，不上报任何数据\n" +
                        "· 规则、白名单、触发记录只存在本机\n" +
                        "· 唯一的联网行为：你手动添加订阅并点「更新」时访问该链接\n" +
                        "· 没有账号体系、没有云同步",
                    fontSize = 13.sp,
                )
            },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("好") } },
        )
    }

    if (showLicense) {
        AlertDialog(
            onDismissRequest = { showLicense = false },
            title = { Text("开源许可", fontSize = 16.sp) },
            text = {
                Text(
                    "灵犀跳跃是 gkd-kit/gkd 的衍生作品。\n\n" +
                        "上游版本：v1.13.2（commit 301e7e0a）\n" +
                        "上游许可：GPL-3.0-only\n" +
                        "本作品同样以 GPL-3.0-only 分发。\n\n" +
                        "分发时须保留 LICENSE 与版权声明，并同样开源。",
                    fontSize = 13.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLicense = false
                    mainVm.navigator.openWebPage(GKD_REPO)
                }) { Text("查看上游") }
            },
            dismissButton = { TextButton(onClick = { showLicense = false }) { Text("关闭") } },
        )
    }
}
