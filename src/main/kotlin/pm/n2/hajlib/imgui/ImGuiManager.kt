package pm.n2.hajlib.imgui

import imgui.ImFontConfig
import imgui.ImGui
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
    }

    private fun fixInputs() {
        val io = ImGui.getIO()

        // Something is trying to overwrite this?
        io.setKeyMap(ImGuiKey.Tab, GLFW.GLFW_KEY_TAB)
        io.setKeyMap(ImGuiKey.LeftArrow, GLFW.GLFW_KEY_LEFT)
        io.setKeyMap(ImGuiKey.RightArrow, GLFW.GLFW_KEY_RIGHT)
        io.setKeyMap(ImGuiKey.UpArrow, GLFW.GLFW_KEY_UP)
        io.setKeyMap(ImGuiKey.DownArrow, GLFW.GLFW_KEY_DOWN)
        io.setKeyMap(ImGuiKey.PageUp, GLFW.GLFW_KEY_PAGE_UP)
        io.setKeyMap(ImGuiKey.PageDown, GLFW.GLFW_KEY_PAGE_DOWN)
        io.setKeyMap(ImGuiKey.Home, GLFW.GLFW_KEY_HOME)
        io.setKeyMap(ImGuiKey.End, GLFW.GLFW_KEY_END)
        io.setKeyMap(ImGuiKey.Insert, GLFW.GLFW_KEY_INSERT)
        io.setKeyMap(ImGuiKey.Delete, GLFW.GLFW_KEY_DELETE)
        io.setKeyMap(ImGuiKey.Backspace, GLFW.GLFW_KEY_BACKSPACE)
        io.setKeyMap(ImGuiKey.Space, GLFW.GLFW_KEY_SPACE)
        io.setKeyMap(ImGuiKey.Enter, GLFW.GLFW_KEY_ENTER)
        io.setKeyMap(ImGuiKey.Escape, GLFW.GLFW_KEY_ESCAPE)
        io.setKeyMap(ImGuiKey.KeyPadEnter, GLFW.GLFW_KEY_KP_ENTER)
        io.setKeyMap(ImGuiKey.A, GLFW.GLFW_KEY_A)
        io.setKeyMap(ImGuiKey.C, GLFW.GLFW_KEY_C)
        io.setKeyMap(ImGuiKey.V, GLFW.GLFW_KEY_V)
        io.setKeyMap(ImGuiKey.X, GLFW.GLFW_KEY_X)
        io.setKeyMap(ImGuiKey.Y, GLFW.GLFW_KEY_Y)
        io.setKeyMap(ImGuiKey.Z, GLFW.GLFW_KEY_Z)
    }

    internal fun onFrameRender() {
        imguiGLFW.newFrame()
        ImGui.newFrame()

        eventManager.dispatch(ImGuiEvent.Draw)
        fixInputs()

        ImGui.render()

        imguiGL3.renderDrawData(ImGui.getDrawData())

        val backupWindowPtr = GLFW.glfwGetCurrentContext()
        ImGui.updatePlatformWindows()
        ImGui.renderPlatformWindowsDefault()
        GLFW.glfwMakeContextCurrent(backupWindowPtr)
    }
}
