package li.gkd.app.priv

import android.app.ActivityManager
import android.app.ITaskStackListener
import android.content.ComponentName
import android.os.Parcel
import android.view.Display
import java.util.concurrent.atomic.AtomicLong
import li.gkd.app.matching.MatchingRuntime
import li.gkd.app.matching.diagnostics.MatchStage
import li.gkd.app.matching.diagnostics.MatchTaskObservation
import li.gkd.app.matching.input.ActivityScene
import li.gkd.app.matching.input.AndroidForegroundState
import li.gkd.app.matching.input.updateTopActivity
import li.gkd.app.util.AndroidTarget

object CompatTaskStackListener : ITaskStackListener.Stub() {

    // https://github.com/gkd-kit/gkd/issues/941#issuecomment-2784035441
    //
    // 真机上的 android.app.ITaskStackListener 比编译期存根多几十个方法，
    // 系统回调到我们没实现的那个（例如 onTaskMovedToFront(RunningTaskInfo) 在新版本上的变体）
    // 会在 super.onTransact 里抛 AbstractMethodError。Binder 线程里抛出会直接崩进程，
    // 所以这里一律吞掉并返回 true。
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean =
        try {
            super.onTransact(code, data, reply, flags)
        } catch (_: Throwable) {
            true
        }

    private val requests = AtomicLong()

    override fun onTaskStackChanged() = observeTask("onTaskStackChanged")

    private fun observeTask(
        callback: String,
        supplied: ComponentName? = null,
        taskId: Int? = null,
    ) {
        val request = requests.incrementAndGet()
        val diagnostics = MatchingRuntime.coordinator.diagnostics
        fun trace(reason: String, component: ComponentName? = supplied) {
            if (!diagnostics.enabled) return
            diagnostics.record(
                0,
                MatchStage.Root,
                reason,
                taskObservation =
                    MatchTaskObservation(
                        request,
                        callback,
                        component?.packageName,
                        component?.className,
                    ),
            )
        }
        trace("task_callback_received")
        val context =
            privilegeContextFlow.value
                ?: run {
                    trace("task_context_unavailable")
                    return
                }
        val page = AndroidForegroundState.currentPage
        // A blocked task Binder read must not hold up window or accessibility input.
        val cpn =
            supplied
                ?: context.topTask()?.let {
                    @Suppress("DEPRECATION")
                    if (taskId != null && it.id != taskId) {
                        trace("task_id_mismatch", it.topActivity)
                        return
                    }
                    it.topActivity
                }
                ?: run {
                    trace("task_top_unavailable")
                    return
                }
        trace("task_result_received", cpn)
        AndroidForegroundState.withTopActivityLock {
            val discarded =
                when {
                    request != requests.get() -> "task_request_superseded"
                    privilegeContextFlow.value !== context -> "task_context_changed"
                    page !== AndroidForegroundState.currentPage -> "task_page_changed"
                    else -> null
                }
            if (discarded != null) {
                trace(discarded, cpn)
                return@withTopActivityLock
            }
            trace("task_result_applied", cpn)
            updateTopActivity(cpn.packageName, cpn.className, ActivityScene.TaskStack)
        }
    }

    override fun onTaskMovedToFront(taskId: Int) =
        observeTask("onTaskMovedToFront(id)", taskId = taskId)

    override fun onTaskMovedToFront(taskInfo: ActivityManager.RunningTaskInfo) {
        if (AndroidTarget.Q && taskInfo.toHidden.displayId != Display.DEFAULT_DISPLAY) {
            return
        }
        observeTask("onTaskMovedToFront(info)", taskInfo.topActivity)
    }

    // ---------------------------------------------------------------
    // 下面这些方法在编译期存根里没有，但真机上的 android.app.ITaskStackListener
    // 会通过 Binder 回调它们。平台 Stub.onTransact 内部是 `this.onTaskXxx(...)`，
    // 只要本类里有同签名的 public 方法，动态派发就能命中，不会抛 AbstractMethodError。
    //
    // 注意：不能用 `override`（存根里没有），写成普通方法即可；在接口确实存在的
    // 系统上它们会自动成为实现。
    // ---------------------------------------------------------------

    fun onTaskCreated(taskId: Int, componentName: ComponentName?) =
        observeTask("onTaskCreated", componentName, taskId)

    /** 任务被移除：只是刷新时机，忽略 */
    fun onTaskRemoved(taskId: Int) = Unit

    fun onActivityPinned(packageName: String?, userId: Int, taskId: Int, stackId: Int) = Unit

    fun onActivityUnpinned() = Unit

    fun onActivityRestartAttempt(
        task: ActivityManager.RunningTaskInfo?,
        homeTaskVisible: Boolean,
        clearedTask: Boolean,
        wasVisible: Boolean,
    ) = Unit

    fun onActivityForcedResizable(packageName: String?, taskId: Int, reason: Int) = Unit

    fun onActivityDismissingDockedTask() = Unit

    fun onBackPressedOnTaskRoot(taskInfo: ActivityManager.RunningTaskInfo?) = Unit

    fun onActivityRequestedOrientationChanged(taskId: Int, requestedOrientation: Int) = Unit

    fun onTaskDisplayChanged(taskId: Int, newDisplayId: Int) = Unit

    fun onRecentTaskListUpdated() = Unit

    fun onTaskFocusChanged(taskId: Int, focused: Boolean) = Unit

    fun onTaskMovedToBack(taskInfo: ActivityManager.RunningTaskInfo?) = Unit

    fun onLockTaskModeChanged(mode: Int) = Unit

    fun onTaskProfileLocked(taskId: Int, userId: Int) = Unit
}
