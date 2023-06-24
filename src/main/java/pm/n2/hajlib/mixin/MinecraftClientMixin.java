package pm.n2.hajlib.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pm.n2.hajlib.internal.InternalEvent;
import pm.n2.hajlib.task.TaskContext;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick$pre(CallbackInfo ci) {
        TaskContext.Companion.getEventManager().dispatch(InternalEvent.PreTick.INSTANCE);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick$post(CallbackInfo ci) {
        TaskContext.Companion.getEventManager().dispatch(InternalEvent.PostTick.INSTANCE);
    }
}
