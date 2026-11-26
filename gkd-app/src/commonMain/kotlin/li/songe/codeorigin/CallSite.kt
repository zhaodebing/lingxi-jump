// 本地替身：上游用 li.songe.codeorigin 插件在编译期注入调用点位置（@CallSite("{file}") loc = "..."）。
// 该插件的字节码目标为 JVM 21，沙箱无法运行；这里提供同名注解的源码替身，
// 调用点保留默认值即可 —— 不改变任何运行时行为（只影响日志里的位置字符串）。
package li.songe.codeorigin

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class CallSite(val value: String = "")
