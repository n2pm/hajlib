package pm.n2.hajlib.imgui

import imgui.ImFontConfig
import imgui.ImGui
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiKey
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import net.fabricmc.loader.api.FabricLoader
import org.lwjgl.glfw.GLFW
import pm.n2.hajlib.event.EventManager
import kotlin.properties.Delegates

/**
 * A wrapper around ImGui, ported from [imgui-quilt](https://git.gaycatgirl.sex/evie/imgui-quilt).
 * Subscribe to the Tick event via the [EventManager] to draw your UI.
 * @see ImGuiScreen
 */
object ImGuiManager {
    val eventManager = EventManager()

    private val imguiGLFW = ImGuiImplGlfw()
    private val imguiGL3 = ImGuiImplGl3()
    private var windowHandle by Delegates.notNull<Long>()

    internal fun onGLFWInit(handle: Long) {
        ImGui.createContext()
        val io = ImGui.getIO()

        io.iniFilename = FabricLoader.getInstance()
                .configDir
                .resolve("imgui.ini")
                .toString()
        io.addConfigFlags(
                ImGuiConfigFlags.NavEnableKeyboard
                        or ImGuiConfigFlags.DockingEnable
                        or ImGuiConfigFlags.ViewportsEnable
        )

        val fontAtlas = io.fonts
        val fontConfig = ImFontConfig()
        fontAtlas.addFontDefault()

        fontConfig.mergeMode = true
        fontConfig.pixelSnapH = true
        fontConfig.destroy()

        // APPLE MOMENT
        val isTimCooksBitch = System.getProperty("os.name").lowercase().contains("mac")
        imguiGL3.init(if (isTimCooksBitch) "#version 140" else "#version 130")
        imguiGLFW.init(handle, true)

        windowHandle = handle

        ImPlot.createContext()
    }

    internal fun onFrameRender() {
        imguiGLFW.newFrame()
        ImGui.newFrame()

        eventManager.dispatch(ImGuiEvent.Draw)

        ImGui.render()

        imguiGL3.renderDrawData(ImGui.getDrawData())

        val backupWindowPtr = GLFW.glfwGetCurrentContext()
        ImGui.updatePlatformWindows()
        ImGui.renderPlatformWindowsDefault()
        GLFW.glfwMakeContextCurrent(backupWindowPtr)
    }
}
