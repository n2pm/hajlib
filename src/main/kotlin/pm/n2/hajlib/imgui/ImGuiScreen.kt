package pm.n2.hajlib.imgui

import imgui.ImGui
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import pm.n2.hajlib.event.UnregisterFunc

/**
 * A screen that draws ImGui.
 * Extend this class and insert your ImGui code into the [drawImGui] function,
 * and it will be drawn on the screen.
 */
open class ImGuiScreen(title: Text) : Screen(title) {
    var initialized: Boolean = false // init() gets called on resize, too

    private val drawLambda = { _: ImGuiEvent.Draw, _: UnregisterFunc ->
        this.drawImGui()
    }

    open fun drawImGui() {}

    override fun init() {
        if (!initialized) {
            ImGuiManager.eventManager.registerFunc(ImGuiEvent.Draw::class, drawLambda)
            initialized = true
        }
    }

    override fun removed() {
        if (initialized) {
            ImGuiManager.eventManager.unregisterFunc(ImGuiEvent.Draw::class, drawLambda)
            initialized = false
        }

        super.removed()
    }
}
