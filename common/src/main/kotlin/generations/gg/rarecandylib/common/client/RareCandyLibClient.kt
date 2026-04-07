package generations.gg.rarecandylib.common.client

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import generations.gg.rarecandylib.common.RareCandyLib.id
import generations.gg.rarecandylib.common.client.render.RenderStateRecord
import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftClientGameProvider
import generations.gg.rarecandylib.common.client.render.rarecandy.ModelRegistry
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.rendering.RareCandy
import gg.generations.rarecandy.renderer.rendering.RenderStage
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.joml.Matrix4f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MatrixCache {
    @JvmStatic var projectionMatrix = Matrix4f()
    @JvmStatic var viewMatrix = Matrix4f()
}

object RareCandyLibClient {
    @JvmField
    var LOGGER: Logger = LoggerFactory.getLogger("RareCandyLib")
    var isIrisRenderingImpl: () -> Boolean = { false }
    @JvmStatic val isIrisRendering: Boolean get() = isIrisRenderingImpl.invoke()
    lateinit var TOGGLE_SHADING: KeyMapping

    fun onInitialize(implementation: RareCandyLibClientImplementation) {
//            try {
//                System.loadLibrary("renderdoc")
//            } catch (e: UnsatisfiedLinkError) {
//                LOGGER.warn("Attempted to use renderdoc without renderdoc installed.")
//            }
//        }

        ITextureLoader.setInstance(TextureLoader)

        implementation.registerResourceReloader("model_registry".id(), CompiledModelLoader(), emptyList())

        RareCandy.DEBUG_THREADS = true

//        if(implementation.isModLoaded("cobbelmon")) generations.gg.rarecandylib.cobblemon.client.CobblemonModule.initialize()

//        VaryingModelRepository.registerFactory(".pk", { resourceLocation, resource) -> new Pair<>(, b -> (Bone) new ModelPart(RareCandyBone.Companion.getCUBE_LIST(), Map.of("root", new RareCandyBone(resourceLocation))}));

    }


    fun onTick() {
        if (::TOGGLE_SHADING.isInitialized && TOGGLE_SHADING.consumeClick()) {
            Pipelines.toggleRendering()
        }
    }


    fun setupClient(event: Minecraft) {

        event.tell({

            ModelRegistry.init()

            Pipelines.onInitialize(event.resourceManager,)

        })
    }

    private fun shouldRenderFpsPie(): Boolean {
        return Minecraft.getInstance().options.reducedDebugInfo()
            .get() /*&& Minecraft.getInstance().options.renderDebugCharts*/ && !Minecraft.getInstance().options.hideGui
    }

    fun secondRenderPass() {

            RenderStateRecord.push()
            RenderSystem.enableDepthTest()
            RenderSystem.defaultBlendFunc()
            RenderSystem.enableBlend()
            renderRareCandyTransparent()
            RenderStateRecord.pop()

        ModelRegistry.worldRareCandy.end()

        ModelRegistry.tick()
    }

    fun firstRenderPass() {
        ModelRegistry.worldRareCandy.update(MinecraftClientGameProvider.timePassed)


        RenderStateRecord.push()

        RenderSystem.enableDepthTest()
        RenderSystem.depthMask(true)
        Pipelines.bindFunction.invoke(false)
        renderRareCandySolid()
        RenderSystem.depthMask(false)
        Pipelines.bindFunction.invoke(true)
        renderRareCandyTransparent()

        RenderStateRecord.pop()
    }


    fun renderRareCandySolid() {
        renderRareCandy(RenderStage.SOLID)
    }

    fun renderRareCandyTransparent() {
        renderRareCandy(RenderStage.TRANSPARENT)
    }

    fun renderRareCandy(stage: RenderStage) {

        val startTime = System.currentTimeMillis()
        RenderSystem.enableDepthTest()
        BufferUploader.reset()

        ModelRegistry.worldRareCandy.render(stage, false)
        if (shouldRenderFpsPie()) LOGGER.warn("RareCandy render took " + (System.currentTimeMillis() - startTime) + "ms")
    }
}
