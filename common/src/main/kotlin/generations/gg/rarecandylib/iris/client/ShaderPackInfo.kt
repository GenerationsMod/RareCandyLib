package generations.gg.rarecandylib.iris.client

import generations.gg.rarecandylib.common.client.RareCandyLibClient
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.instanceOrNull
import generations.gg.rarecandylib.iris.client.shaderpacks.Complementary
import gg.generations.rarecandy.renderer.pipeline.Pipeline
import net.irisshaders.iris.Iris
import net.irisshaders.iris.pipeline.IrisRenderingPipeline
import net.irisshaders.iris.pipeline.programs.ShaderKey
import net.minecraft.client.Minecraft
import net.minecraft.server.packs.resources.ResourceManager
import kotlin.jvm.optionals.getOrNull

object ShaderPackInfo {
    val irisBindFunction: (Boolean) -> Unit = { Iris.getPipelineManager().pipeline?.getOrNull()?.instanceOrNull<IrisRenderingPipeline>()?.shaderMap?.getShader(
        ShaderKey.ENTITIES_TRANSLUCENT)?.instanceOrNull<ExtendedShaderAccess>()?.getFrameBuffer(it)?.bind()?.also { System.out.println("Yay!") } ?: System.out.println("What the fuck") }
    val vanillaBindFunction: (Boolean) -> Unit = {}
    val defaultPipelineFunction: (ResourceManager) -> Pair<Pipeline, Pipeline> = { Pipelines.createRegular(it) to Pipelines.createTerastal(it) }

    var isDirty = false;
    var pipelineFunction: (ResourceManager) -> Pair<Pipeline, Pipeline> = defaultPipelineFunction
    var bindFunction: (Boolean) -> Unit = vanillaBindFunction
    var isDeffered = false

    fun shadersChange(currentPackName: String, fallback: Boolean) {

        when {
            fallback -> {
                RareCandyLibClient.defferedRendering = false
                Pipelines.onInitialize(defaultPipelineFunction, vanillaBindFunction)

                System.out.println("Loading Vanilla")
            }
            currentPackName.startsWith("ComplementaryReimagined", ignoreCase = true) -> {
                RareCandyLibClient.defferedRendering = true
                Pipelines.onInitialize(Complementary.Reimagined.shader, irisBindFunction)

                System.out.println("Loading Complementary Reimagined")
            }
            else -> {
                RareCandyLibClient.defferedRendering = false
                Pipelines.onInitialize(defaultPipelineFunction, vanillaBindFunction)

                System.out.println("Loading Vanilla")
            }
        }
    }

//    fun apply() {
//        if(isDirty) {
//            Pipelines.onInitialize(Minecraft.getInstance().resourceManager, pipelineFunction, bindFunction)
//            RareCandyLibClient.defferedRendering = isDeffered
//            isDirty = false
//        }
//    }
}
