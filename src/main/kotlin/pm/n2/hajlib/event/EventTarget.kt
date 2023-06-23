package pm.n2.hajlib.event

import java.lang.reflect.Method

class EventTarget(val obj: Any?, val method: Method) {
    fun invoke(event: Any) {
        method.invoke(obj, event)
    }
}
