package pm.n2.hajlib.task

import pm.n2.hajlib.event.EventManager
import pm.n2.hajlib.internal.InternalEvent

open class TaskContext {
    internal companion object {
        val eventManager = EventManager()
    }

    open fun waitTicks(ticks: Int) {
        for (i in 0 until ticks) {
            eventManager.waitForEvent(InternalEvent.PostTick)
        }
    }
}
