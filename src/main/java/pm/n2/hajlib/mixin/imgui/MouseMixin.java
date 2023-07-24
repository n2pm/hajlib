package pm.n2.hajlib.mixin.imgui;

import imgui.ImGui;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(at = @At("HEAD"), method = "onMouseButton", cancellable = true)
    private void onKey(long window, int button, int action, int mods, CallbackInfo ci) {
        var io = ImGui.getIO();
        if (io.getWantCaptureMouse()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "onMouseScroll", cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        var io = ImGui.getIO();
        if (io.getWantCaptureMouse()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "onCursorPos", cancellable = true)
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        var io = ImGui.getIO();
        if (io.getWantCaptureMouse()) ci.cancel();
    }
}
