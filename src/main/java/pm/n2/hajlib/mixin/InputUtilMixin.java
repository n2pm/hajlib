package pm.n2.hajlib.mixin;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pm.n2.hajlib.imgui.ImGuiManager;

@Mixin(InputUtil.class)
public class InputUtilMixin {
    // We can't mixin into the Window init to install our callbacks because InputUtil steals them after
    // This method is called last in the MinecraftClient constructor, so we can install our callbacks here
    @Inject(method = "setKeyboardCallbacks", at = @At("TAIL"))
    private static void setKeyboardCallbacks(long handle, GLFWKeyCallbackI keyCallback, GLFWCharModsCallbackI charModsCallback, CallbackInfo ci) {
        ImGuiManager.INSTANCE.onGLFWInit$hajlib(handle);
    }
}
