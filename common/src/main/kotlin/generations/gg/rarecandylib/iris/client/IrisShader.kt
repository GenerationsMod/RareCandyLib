package generations.gg.rarecandylib.iris.client;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer
import net.irisshaders.iris.pipeline.IrisRenderingPipeline

public interface IrisShader {
    fun fbo(pipeline: IrisRenderingPipeline): GlFramebuffer?
}
