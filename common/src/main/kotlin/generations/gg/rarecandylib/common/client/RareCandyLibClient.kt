package generations.gg.rarecandylib.common.client

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import generations.gg.rarecandylib.common.RareCandyLib.LOGGER
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
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

private operator fun BlockPos.minus(pos: BlockPos): BlockPos {
    return this.subtract(pos)
}

private operator fun Vec3.times(scale: Float): Vec3 {
    return this.multiply(scale.toDouble(), scale.toDouble(), scale.toDouble())
}

private operator fun Vec3.minus(vec3: Vec3): Vec3 {
    return this.subtract(vec3)
}

object MatrixCache {
    var projectionMatrix = Matrix4f()
    var viewMatrix = Matrix4f()
}

object RareCandyLibClient {
    lateinit var TOGGLE_SHADING: KeyMapping

    fun onInitialize(implementation: RareCandyLibClientImplementation) {
//        if (GenerationsCore.CONFIG.client.useRenderDoc) {
            try {
                System.loadLibrary("renderdoc")
            } catch (e: UnsatisfiedLinkError) {
                LOGGER.warn("Attempted to use renderdoc without renderdoc installed.")
            }
//        }

        ITextureLoader.setInstance(GenerationsTextureLoader)

        implementation.registerResourceReloader("model_registry".id(), CompiledModelLoader(), emptyList())

        RareCandy.DEBUG_THREADS = true

        if(implementation.isModLoaded("cobbelmon")) generations.gg.rarecandylib.cobblemon.client.CobblemonModule.initialize()

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

            Pipelines.onInitialize(event.resourceManager)

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

        MatrixCache.projectionMatrix = RenderSystem.getProjectionMatrix()
        MatrixCache.viewMatrix = RenderSystem.getModelViewMatrix()

        RenderStateRecord.push()

        renderRareCandySolid()
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

        var startTime = System.currentTimeMillis()
        RenderSystem.enableDepthTest()
        BufferUploader.reset()

        ModelRegistry.worldRareCandy.render(stage, false)
        if (shouldRenderFpsPie()) LOGGER.warn("RareCandy render took " + (System.currentTimeMillis() - startTime) + "ms")
    }
}

fun <T:AbstractContainerMenu> Holder<MenuType<*>>.asValue(): MenuType<T> = this.value() as MenuType<T>
