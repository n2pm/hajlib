package pm.n2.hajlib.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pm.n2.hajlib.imgui.ImGuiManager;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(at = @At("HEAD"), method = "flipFrame")
    private static void flipFrame(long window, CallbackInfo ci) {
        MinecraftClient.getInstance().getProfiler().push("ImGui");
        ImGuiManager.INSTANCE.onFrameRender$hajlib();
        MinecraftClient.getInstance().getProfiler().pop();
    }
}
