package pm.n2.hajlib.mixin.imgui;

import imgui.ImGui;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(at = @At("HEAD"), method = "onKey", cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        var io = ImGui.getIO();
        if (io.getWantCaptureKeyboard()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "onChar", cancellable = true)
    private void onChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        var io = ImGui.getIO();
        if (io.getWantCaptureKeyboard()) ci.cancel();
    }
}
