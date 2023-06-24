package pm.n2.hajlib.internal

internal sealed class InternalEvent {
    object PreTick : InternalEvent()
    object PostTick : InternalEvent()
}
