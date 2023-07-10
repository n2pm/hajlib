package pm.n2.hajlib.task

import kotlinx.coroutines.*

/**
 * A simple wrapper around Kotlin coroutines.
 */
class TaskManager<T : TaskContext>(name: String, private val clazz: T) {
    @OptIn(DelicateCoroutinesApi::class)
    private val context = newSingleThreadContext("HajLib-TaskManager-$name")
    private val scope = CoroutineScope(context)

    /**
     * Runs the specified function, passing in the task context.
     *
     * ```
     * val taskManager = TaskManager("My Task Manager", MyTaskContext)
     * taskManager.run { ctx ->
     *    ctx.waitTicks(20)
     *    println("One second has passed!")
     * }
     * ```
     *
     * @param func The function to run.
     */
    fun run(func: suspend (clazz: T) -> Unit) = scope.launch { func(clazz) }
}
