package pm.n2.hajlib.event

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

class EventManager {
    private val handlers: MutableMap<Class<*>, MutableList<EventTarget>> = mutableMapOf()

    fun register(obj: KClass<*>) {
        val methods = obj.declaredFunctions.filter { func -> func.annotations.any { it is EventHandler } }

        for (method in methods) {
            val paramType = method.parameters[1].type.javaType as Class<*>
            handlers.getOrPut(paramType) { mutableListOf() }.add(
                EventTarget(
                    obj.objectInstance,
                    method.javaMethod!!
                )
            )
        }
    }

    fun unregister(obj: KClass<*>) {
        val methods = obj.declaredFunctions.filter { func -> func.annotations.any { it is EventHandler } }

        for (method in methods) {
            val paramType = method.parameters[1].type.javaType as Class<*>
            handlers.getOrPut(paramType) { mutableListOf() }
                .removeIf { it.obj == obj.objectInstance && it.method == method.javaMethod!! }
        }
    }

    fun dispatch(event: Any) {
        val targets = handlers[event.javaClass] ?: return
        for (target in targets) {
            target.invoke(event)
        }
    }
}
