/*
 * 灵犀跳跃 —— 基于 gkd-kit/gkd 修改 (GPL-3.0-only)
 *
 * 赞赏页：纯本地两张收款码，不打点、不上传、不拦截功能。
 * 文案原则：说清钱花在哪、给具体锚点、不催不闹。
 */
package li.gkd.app.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import li.gkd.app.resources.Res
import li.gkd.app.resources.reward_alipay
import li.gkd.app.resources.reward_wechat
import li.gkd.app.ui.MainViewModel
import li.gkd.app.ui.home.LxColor
import li.gkd.app.ui.home.LxDivider
import li.gkd.app.ui.home.LxGroup
import li.gkd.app.ui.home.LxPage
import li.gkd.app.ui.home.LxRow
import org.jetbrains.compose.resources.painterResource

@Composable
fun SupportPage() {
    val mainVm = MainViewModel.requireCurrent()
    LxPage(title = "赞赏", onBack = mainVm.navigator::pop) {
        LxGroup("为什么会有这一页") {
            LxRow(
                title = "完全免费，功能不加锁",
                value = "赞赏随意",
            )
            LxDivider()
            Text(
                "广告拦截、灵犀喂养、订阅规则——所有能力都不收费、不看广告、不需要登录。\n" +
                    "这一页只是因为有人问「怎么支持你」，所以放上来了。",
                fontSize = 12.sp,
                color = LxColor.TextSub,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        LxGroup("钱会花在哪") {
            LxRow("订阅与规则维护", "每月手测几十个应用的开屏")
            LxDivider()
            LxRow("测试机", "不同厂商的机型和系统版本")
            LxDivider()
            LxRow("剩下的", "买咖啡，继续写代码")
        }

        LxGroup("公众号") {
            LxRow(title = "趣味暖心", value = "微信搜索关注")
            LxDivider()
            val clipboard = LocalClipboardManager.current
            LxRow(
                title = "复制公众号名",
                value = "点这里",
                accent = true,
                chevron = true,
            ) {
                clipboard.setText(AnnotatedString("趣味暖心"))
            }
            LxDivider()
            Text(
                "更新说明、规则变动、踩过的坑都发在这里。不推广、不带货。",
                fontSize = 12.sp,
                color = LxColor.TextSub,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        LxGroup("微信赞赏") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.reward_wechat),
                    contentDescription = "微信赞赏码",
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .width(220.dp)
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp)),
                )
                Text(
                    "微信扫一扫 · 金额随意",
                    fontSize = 12.sp,
                    color = LxColor.TextSub,
                )
            }
        }

        LxGroup("支付宝") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.reward_alipay),
                    contentDescription = "支付宝收款码",
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .width(220.dp)
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp)),
                )
                Text(
                    "支付宝扫一扫 · 支持花呗/信用卡",
                    fontSize = 12.sp,
                    color = LxColor.TextSub,
                )
            }
        }

        Text(
            "赞赏不会解锁任何功能，也不会改变使用体验。\n" +
                "不赞赏同样可以完整使用。",
            fontSize = 12.sp,
            color = LxColor.Foot,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
        )
    }
}

@Composable
private fun LxDivider() {
    li.gkd.app.ui.home.LxDivider()
}
