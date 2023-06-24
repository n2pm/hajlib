package pm.n2.hajlib.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class TaskManager<T : TaskContext>(name: String, private val clazz: T) {
    @OptIn(DelicateCoroutinesApi::class)
    private val context = newSingleThreadContext("HajLib-TaskManager-$name")
    private val scope = CoroutineScope(context)

    fun run(func: suspend (clazz: T) -> Unit) {
        scope.launch {
            func(clazz)
        }
    }
}
