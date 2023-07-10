package pm.n2.hajlib.task

import pm.n2.hajlib.event.EventManager
import pm.n2.hajlib.internal.InternalEvent

/**
 * The task context gets passed as an argument to tasks created with [TaskManager]. Store mutexes and other state in the task context.
 *
 * ```
 * object MyTaskContext : TaskContext() {
 *     val myMutex = Mutex()
 * }
 * ```
 */
open class TaskContext {
    internal companion object {
        val eventManager = EventManager()
    }

    /**
     * Wait for the specified number of ticks.
     * This counts off of post ticks (also called end ticks), so this function will return after a tick ends.
     *
     * @param ticks The number of ticks to wait for.
     */
    open suspend fun waitTicks(ticks: Int) {
        for (i in 0 until ticks) {
            eventManager.waitForEvent(InternalEvent.PostTick::class)
        }
    }
}
