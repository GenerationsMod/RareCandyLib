package generations.gg.rarecandylib.iris.client

import generations.gg.rarecandylib.common.client.RareCandyLibClient
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines
import generations.gg.rarecandylib.common.client.render.rarecandy.ShaderSet
import net.irisshaders.iris.api.v0.IrisApi
import net.irisshaders.iris.pipeline.IrisRenderingPipeline
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL40

object IrisModule {
    var shaderImpl: ShaderSet? = null

    val shader: ShaderSet?
        get() = computerShaderSet();

    private fun computerShaderSet(): ShaderSet? {
        if(!initialized) {
            Minecraft.getInstance().resourceManager?.also {
                shaderImpl = ShaderSet.create(Minecraft.getInstance().resourceManager, "iris/$packName")
                initialized = true
            }
        }

        return shaderImpl
    }

    private var initialized = false
    private var packName: String? = null;

    fun initialize() {
        RareCandyLibClient.isIrisRenderingImpl = { IrisApi.getInstance().isShaderPackInUse }
        Pipelines.alternateShaderProvider = { shader }
        RareCandyLibClient.LOGGER.info("RareCandyLib Iris Module Enabled.")
    }

    @JvmStatic
    fun loadShader(currentPackName: String) {
        shader?.destroy()

        initialized = true;
        packName = null;
        if(currentPackName.contains("SuperDuper", true)) {
            packName = "superduper"

            initialized = false
        }

        Minecraft.getInstance().resourceManager?.also {
            shaderImpl = ShaderSet.create(Minecraft.getInstance().resourceManager, "iris/$packName")
            initialized = true
        }
    }

    @JvmStatic
    fun firstPass(pipeline: IrisRenderingPipeline) {
        if(shader != null) {
            val frameBuffer = IrisFbo.getFbo(pipeline);

            if(frameBuffer != null) {
                RareCandyLibClient.LOGGER.info("First pass")
                frameBuffer.bind()
                RareCandyLibClient.firstRenderPass()
            }
        }
    }

    @JvmStatic
    fun secondPass(pipeline: IrisRenderingPipeline) {
        if(shader != null) {
            val frameBuffer = IrisFbo.getFbo(pipeline);

            if(frameBuffer != null) {
                RareCandyLibClient.LOGGER.info("Second pass")
                frameBuffer.bind()
                RareCandyLibClient.secondRenderPass()
            }
        }
    }
}
