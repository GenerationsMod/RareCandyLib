package generations.gg.rarecandylib.common.client.render

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem

object RenderStateRecord {
    private var readOnly = false

    @JvmStatic var blendEnabled: Boolean = false
    @JvmStatic var srcRgb: Int = 0
    @JvmStatic var dstRgb: Int = 0
    @JvmStatic var srcAlpha: Int = 0
    @JvmStatic var dstAlpha: Int = 0

    @JvmStatic var depthTestEnabled: Boolean = false
    @JvmStatic var depthMask: Boolean = false
    @JvmStatic var depthFunc: Int = 0

    @JvmStatic var cullEnabled: Boolean = false

    @JvmStatic val isActive: Boolean
        get() = readOnly

    fun push() {
        readOnly = true
    }

    fun pop() {
        readOnly = false

        // Blend
        if (blendEnabled) RenderSystem.enableBlend() else RenderSystem.disableBlend()
        RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha)

        // Depth
        if (depthTestEnabled) RenderSystem.enableDepthTest() else RenderSystem.disableDepthTest()
        RenderSystem.depthMask(depthMask)
        RenderSystem.depthFunc(depthFunc)

        // Cull
        if (cullEnabled) RenderSystem.enableCull() else RenderSystem.disableCull()
    }
}