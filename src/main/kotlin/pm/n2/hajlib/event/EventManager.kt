package pm.n2.hajlib.event

import kotlinx.coroutines.sync.Mutex
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

typealias UnregisterFunc = () -> Unit
typealias EventHandlerFunc<T> = (obj: T, unregister: UnregisterFunc) -> Any?
typealias EventTarget = Pair<Any?, Method>

/**
 * A simple event bus, similar to Bukkit and Orbit.
 *
 * There are two ways to receive events:
 * - Register instances of classes with [registerClass], adding functions annotated with [EventHandler].
 * - Register functions directly with [registerFunc].
 *
 * Define custom events in your own enum-like class, or use existing types (e.g. packet classes).
 *
 * ```
 * sealed class MyEvents {
 *     object SomethingHappened : MyEvents()
 *     class SomethingElseHappened(val data: String) : MyEvents()
 * }
 * ```
 */
@Suppress("UNCHECKED_CAST")
class EventManager {
    private val handlers: MutableMap<Class<*>, MutableList<EventTarget>> = mutableMapOf()
    private val functions: MutableMap<Class<*>, MutableList<EventHandlerFunc<*>>> = mutableMapOf()

    private fun registerClassInternal(clazz: KClass<*>, obj: Any, register: Boolean) {
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
                list.removeIf { it.first == obj && it.second == method.javaMethod!! }
            }
        }
    }

    private fun <T : Any> dispatchClassInternal(event: T): List<Any?> {
        val targetHandlers = handlers[event.javaClass] ?: listOf()
        val ret = mutableListOf<Any?>()

        for (target in targetHandlers) {
            val response = target.second.invoke(target.first, event)
            ret.add(response)
        }

        return ret
    }

    private fun <T : Any> dispatchFuncInternal(event: T): List<Any?> {
        val targetFunctions = functions[event.javaClass] ?: listOf()
        val ret = mutableListOf<Any?>()

        // Clone it so we don't ConcurrentModificationException
        for (func in targetFunctions.toMutableList()) {
            val funcCasted = func as EventHandlerFunc<T>
            var shouldUnregister = false
            val response = funcCasted(event) { shouldUnregister = true }
            ret.add(response)

            if (shouldUnregister) {
                // Could call the internal function to remove it, but eh
                functions[event.javaClass]?.remove(func)
            }
        }

        return ret
    }

    /**
     * Do not call this function directly. Use [registerFunc] and [unregisterFunc] instead.
     * It is public because of inlined functions.
     */
    fun <T : Any, C : KClass<T>> registerFuncInternal(
        type: C,
        func: EventHandlerFunc<T>,
        register: Boolean
    ): EventHandlerFunc<T> {
        val list = functions.getOrPut(type.java) { mutableListOf() }

        if (register) {
            list.add(func)
        } else {
            list.remove(func)
        }

        return func
    }

    /**
     * Registers all functions with the [EventHandler] annotation in the specified class.
     *
     * ```
     * class MyClass {
     *   @EventHandler
     *   fun exampleFunction(event: String) {
     *      // Do something
     *   }
     * }
     *
     * val clazz = MyClass()
     * val eventHandler = EventManager()
     *
     * eventHandler.registerClass(clazz)
     * eventHandler.dispatch("Hello, world!")
     * ```
     *
     * @param obj The class to register functions in.
     */
    fun registerClass(obj: Any) = registerClassInternal(obj::class, obj, true)

    /**
     * Unregisters all functions with the [EventHandler] annotation in the specified class.
     *
     * @param obj The class to unregister functions in.
     */
    fun unregisterClass(obj: Any) = registerClassInternal(obj::class, obj, false)

    /**
     * Registers a function to receive events of the specified type.
     *
     * The function will receive the event along with an unregister function. This can be called in your event handler
     * logic to stop receiving events (e.g. one-time event handlers).
     *
     * ```
     * val eventManager = EventManager()
     *
     * eventManager.registerFunc(String::class) { str, _ ->
     *    println("String received (func one): $str")
     * }
     *
     * eventManager.registerFunc(String::class) { str, unregister ->
     *    println("String received (func two): $str")
     *    unregister() // This function will no longer be called after this event
     * }
     *
     * eventManager.dispatch("Hello, world!")
     * eventManager.dispatch("Hello again, world!")
     * ```
     *
     * @param clazz The class of the event to receive.
     * @param func The function to call when an event is dispatched.
     */
    inline fun <reified T : Any, reified C : KClass<T>> registerFunc(clazz: C, noinline func: EventHandlerFunc<T>) =
        registerFuncInternal(clazz, func, true)

    /**
     * Unregisters a function receiving events of the specified type. This is effectively the same as calling the
     * unregister function inside the event handler.
     *
     * @param clazz The class of the event to stop receiving.
     * @param func The associated event handler function.
     */
    inline fun <reified T : Any, reified C : KClass<T>> unregisterFunc(clazz: C, noinline func: EventHandlerFunc<T>) =
        registerFuncInternal(clazz, func, false)

    /**
     * Dispatches an event to all registered handlers, returning a list of all return values from each handler.
     * Use the [filterIsInstance] function to filter to a specific type.
     *
     * @param event The event to dispatch.
     * @return A list of all return values from each handler.
     */
    fun <T : Any> dispatch(event: T): List<Any?> {
        val ret = mutableListOf<Any?>()
        ret.addAll(dispatchClassInternal(event))
        ret.addAll(dispatchFuncInternal(event))
        return ret
    }

    /**
     * Waits for an event of the specified type and returns it.
     *
     * @param obj The class of the event to wait for.
     */
    suspend inline fun <reified T : Any, reified C : KClass<T>> waitForEvent(obj: C): T {
        var value: T? = null
        // This is a very fun way to solve this, I think
        val receivedSignal = Mutex(true)

        val func = { e: T, unregister: UnregisterFunc ->
            value = e
            receivedSignal.unlock()
            unregister()
        }

        registerFunc(obj, func)
        receivedSignal.lock()

        return value!!
    }
}
