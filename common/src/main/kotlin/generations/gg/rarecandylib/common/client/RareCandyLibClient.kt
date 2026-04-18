package generations.gg.rarecandylib.common.client

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import generations.gg.rarecandylib.common.RareCandyLib.id
import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftClientGameProvider
import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftObjectInstance
import generations.gg.rarecandylib.common.client.render.rarecandy.ModelRegistry
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.components.DummyVAO
import gg.generations.rarecandy.renderer.components.InstanceDetails
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.rendering.RenderStage
import gg.generations.rarecandy.renderer.rendering.StateManager
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import org.joml.Matrix4f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MatrixCache {
    @JvmStatic var projectionMatrix = Matrix4f()
    @JvmStatic var viewMatrix = Matrix4f()
}

object RareCandyLibClient {
    private lateinit var vao: DummyVAO

    @JvmField
    var LOGGER: Logger = LoggerFactory.getLogger("RareCandyLib")
    private val renderDebugEvents = mutableSetOf<String>()
    var isIrisRenderingImpl: () -> Boolean = { false }
    @JvmStatic val isIrisRendering: Boolean get() = isIrisRenderingImpl.invoke()
    lateinit var TOGGLE_SHADING: KeyMapping

    fun onInitialize(implementation: RareCandyLibClientImplementation) {
        InstanceDetails.size = MinecraftObjectInstance.SIZE

            try {
                System.loadLibrary("renderdoc")
            } catch (e: UnsatisfiedLinkError) {
                LOGGER.warn("Attempted to use renderdoc without renderdoc installed.")
            }

        ITextureLoader.setInstance(TextureLoader)

        implementation.registerResourceReloader("model_registry".id(), CompiledModelLoader(), emptyList())

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
            vao = DummyVAO()
            Pipelines.onInitialize(event.resourceManager)

        })
    }

    private fun shouldRenderFpsPie(): Boolean {
        return Minecraft.getInstance().options.reducedDebugInfo()
            .get() /*&& Minecraft.getInstance().options.renderDebugCharts*/ && !Minecraft.getInstance().options.hideGui
    }

    fun secondRenderPass() {
        renderRareCandyTransparent()

//        ModelRegistry.worldRareCandy.end()

        ModelRegistry.tick(MinecraftClientGameProvider.timePassed)
    }

    fun firstRenderPass() {
//        ModelRegistry.worldRareCandy.update()
        renderRareCandySolid()
    }


    fun renderRareCandySolid() {
        render(
            RenderStage.SOLID_DEPTH_CULL,
            RenderStage.SOLID_DEPTH_NOCULL,
            RenderStage.SOLID_NODEPTH_CULL,
            RenderStage.SOLID_NODEPTH_NOCULL
        )
    }

    fun renderRareCandyTransparent() {
        render(
            RenderStage.TRANSPARENT_DEPTH_CULL,
            RenderStage.TRANSPARENT_DEPTH_NOCULL,
            RenderStage.TRANSPARENT_NODEPTH_CULL,
            RenderStage.TRANSPARENT_NODEPTH_NOCULL
        )
    }

    fun render(stage1: RenderStage, stage2: RenderStage, stage3: RenderStage, stage4: RenderStage) {
        BufferUploader.reset()

        val pipeline = Pipelines.pipeline ?: return;
        vao.bind()
        pipeline.useProgram()
        pipeline.bindGlobal()

        ModelRegistry.render(pipeline, stage1, stage2, stage3, stage4)
//        for (obj in ModelRegistry.render()worldRareCandy.objects) {
//            pipeline.bindModel(obj, null)
//
//            render(pipeline, obj, stage1)
//            render(pipeline, obj, stage2)
//            render(pipeline, obj, stage3)
//            render(pipeline, obj, stage4)
//        }

        vao.unbind()

    }

//    private fun render(
//        pipeline: TraditionalPipeline,
//        obj: MultiRenderObject,
//        renderStage: RenderStage
//    ) {
//        manager.toggle(renderStage)
//        obj.render(pipeline, renderStage)
//    }
//
//    fun renderRareCandy(stage1: RenderStage, stage2: RenderStage, stage3: RenderStage, stage4: RenderStage) {
//                val startTime = System.currentTimeMillis()
//
//
//        if (shouldRenderFpsPie()) LOGGER.warn("RareCandy render took " + (System.currentTimeMillis() - startTime) + "ms")
//    }
}
