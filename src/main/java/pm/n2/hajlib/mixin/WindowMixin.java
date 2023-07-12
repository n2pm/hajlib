package pm.n2.hajlib.mixin;

import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pm.n2.hajlib.imgui.ImGuiManager;

@Mixin(Window.class)
public class WindowMixin {
    @Final
    @Shadow
    private long handle;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, String videoMode, String title, CallbackInfo ci) {
        ImGuiManager.INSTANCE.onGLFWInit$hajlib(handle);
    }
}
