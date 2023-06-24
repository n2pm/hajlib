package pm.n2.hajlib.event

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

class EventManager {
    private val handlers: MutableMap<Class<*>, MutableList<EventTarget>> = mutableMapOf()
    private val functions: MutableMap<Class<*>, MutableList<(obj: Any) -> Unit>> = mutableMapOf()

    private fun internalClass(clazz: KClass<*>, obj: Any, register: Boolean) {
        val methods = clazz.declaredFunctions.filter { func -> func.annotations.any { it is EventHandler } }

        for (method in methods) {
            val paramType = method.parameters[1].type.javaType as Class<*>
            val list = handlers.getOrPut(paramType) { mutableListOf() }

            if (register) {
                list.add(
                    EventTarget(
                        obj,
                        method.javaMethod!!
                    )
                )
            } else {
                list.removeIf { it.obj == obj && it.method == method.javaMethod!! }
            }
        }
    }

    fun internalFunc(
        type: Class<*>,
        func: (obj: Any) -> Unit,
        register: Boolean
    ): (Any) -> Unit {
        val list = functions.getOrPut(type) { mutableListOf() }

        if (register) {
            list.add(func)
        } else {
            list.remove(func)
        }

        return func
    }

    fun registerClass(obj: KClass<*>) = internalClass(obj, obj.objectInstance!!, true)
    fun registerClass(obj: Any) = internalClass(obj::class, obj, true)
    fun unregisterClass(obj: KClass<*>) = internalClass(obj, obj.objectInstance!!, false)
    fun unregisterClass(obj: Any) = internalClass(obj::class, obj, false)
    inline fun <reified T> registerFunc(noinline func: (Any) -> Unit) =
        internalFunc(T::class.java, func, true)

    inline fun <reified T> unregisterFunc(noinline func: (Any) -> Unit) = internalFunc(T::class.java, func, false)

    fun dispatch(event: Any) {
        val targetHandlers = handlers[event.javaClass] ?: listOf()
        for (target in targetHandlers) {
            target.invoke(event)
        }

        val targetFunctions = functions[event.javaClass] ?: listOf()
        for (func in targetFunctions) {
            func(event)
        }
    }

    inline fun <reified T : Any> waitForEvent(obj: T): T {
        var value: T? = null
        val receivedSignal = Mutex(true)
        val func = { e: Any ->
            value = e as T
            receivedSignal.unlock()
        }

        registerFunc<T>(func)
        runBlocking {
            receivedSignal.lock()
        }
        unregisterFunc<T>(func)

        return value!!
    }
}
