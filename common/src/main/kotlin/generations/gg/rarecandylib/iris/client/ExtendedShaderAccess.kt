package generations.gg.rarecandylib.iris.client

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer

interface ExtendedShaderAccess {
    fun getFrameBuffer(beforeTranslucent: Boolean): GlFramebuffer
}