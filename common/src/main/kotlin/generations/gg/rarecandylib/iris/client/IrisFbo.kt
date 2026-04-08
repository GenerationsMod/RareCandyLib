package generations.gg.rarecandylib.iris.client

import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.instanceOrNull
import generations.gg.rarecandylib.iris.mixin.client.ExtendedShaderAccessor
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer
import net.irisshaders.iris.pipeline.IrisRenderingPipeline
import net.irisshaders.iris.pipeline.programs.ExtendedShader
import net.irisshaders.iris.pipeline.programs.ShaderKey

object IrisFbo {
    fun getFbo(pipeline: IrisRenderingPipeline): GlFramebuffer? {
        val key = if (pipeline.isBeforeTranslucent) {
            ShaderKey.ENTITIES_SOLID
        } else {
            ShaderKey.ENTITIES_TRANSLUCENT
        }

        return pipeline.shaderMap.getShader(key).instanceOrNull<IrisShader>()?.fbo(pipeline)
    }
}
