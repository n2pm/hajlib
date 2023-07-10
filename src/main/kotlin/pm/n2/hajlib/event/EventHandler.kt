package pm.n2.hajlib.event

/**
 * Marks a function as an event handler.
 * The function must have a single parameter of the event type.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class EventHandler
