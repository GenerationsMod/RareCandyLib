package generations.gg.rarecandylib.common.client.render.rarecandy

import com.mojang.blaze3d.pipeline.RenderCall
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import generations.gg.rarecandylib.common.block.GenericRotatableModelBlock
import generations.gg.rarecandylib.common.block.entity.ModelProvidingBlockEntity
import generations.gg.rarecandylib.common.client.render.rarecandy.CompiledModel.Companion.of
import generations.gg.rarecandylib.common.client.model.ModelContextProviders
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.AngleProvider
import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.rendering.RareCandy
import gg.generations.rarecandy.renderer.rendering.RenderStage
import gg.generations.rarecandy.renderer.rendering.StateManager
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.BiConsumer

object ModelRegistry {
    val manager = StateManager({
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
    },
        RenderSystem::disableBlend,
        RenderSystem::enableCull,
        RenderSystem::disableCull,
        RenderSystem::enableDepthTest,
        RenderSystem::disableDepthTest
    )

    val TASKS = ConcurrentLinkedQueue<Runnable>()
    private val modelsToLoad = mutableListOf<ResourceLocation>()
    private val models = mutableMapOf<ResourceLocation, CompiledModel>()
    private val modelsToUnload = mutableListOf<ResourceLocation>()

    @JvmStatic
    operator fun get(modelProvider: ModelContextProviders.ModelProvider): CompiledModel? {
        return modelProvider.model?.let(::get)
    }

    @JvmStatic
    fun clear() {
        runLater {
            modelsToUnload.clear()
            modelsToLoad.clear()

            for (model in models.values) {
                model.delete()
            }

            models.clear()
        }
    }

    fun ensureOnRenderThread(runnable: RenderCall) {
        if (RenderSystem.isOnRenderThread()) {
            runnable.execute()
        } else {
            RenderSystem.recordRenderCall(runnable)
        }
    }

    fun tick(time: Double) {
        var task = TASKS.poll()
        while (task != null) {
            task.run()
            task = TASKS.poll()
        }

        for ((key, model) in models) {
            model.update(time)
            model.takeIf({ it.empty })?.run { modelsToUnload.add(key) }
        }

        if(modelsToLoad.isEmpty() and modelsToUnload.isEmpty()) return

        ensureOnRenderThread {
            if(modelsToLoad.isNotEmpty()) {
                for (key in modelsToLoad) {
                    models[key] = of(key);
                }
                modelsToLoad.clear()
            }

            if(modelsToLoad.isNotEmpty()) {
                for (key in modelsToUnload) {
                    models[key]?.also {
                        it.delete()

                    }
                    models.remove(key)
                }

                modelsToUnload.clear()
            }
        }
    }

    @JvmStatic
    operator fun get(location: ResourceLocation): CompiledModel? {
        return try {
            if(modelsToLoad.contains(location)) null;
            else {
                models[location] ?: run { modelsToLoad += location }.let { null }
            }
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun init() {
        CompiledModel.init()
    }

    fun render(pipeline: TraditionalPipeline, stage1: RenderStage, stage2: RenderStage, stage3: RenderStage, stage4: RenderStage) {
        models.values.filter { !it.empty }.mapNotNull { it.renderObject }.forEach {
            pipeline.bindModel(it, null)
            it.render(pipeline, stage1)
            manager.toggle(stage1)
            it.render(pipeline, stage2)
            manager.toggle(stage2)
            it.render(pipeline, stage3)
            manager.toggle(stage3)
            it.render(pipeline, stage4)
            manager.toggle(stage4)
        }
    }

    fun prepForBER(stack: PoseStack, supplier: AngleProvider) {
        stack.translate(0.5f, 0.0f, 0.5f)
        if (supplier is ModelProvidingBlockEntity) {
            val state = supplier.effectiveState
            val block = state.block

            if (block is GenericRotatableModelBlock && block.shouldRotateSpecial) {
                val forward: Direction = state.getValue(BlockStateProperties.FACING)
                val x: Int = block.getWidthValue(state)
                val z: Int = block.getLengthValue(state)
                val width: Float = block.width * 0.5f - x
                val length: Float = block.length * 0.5f - z
                when (forward) {
                    Direction.SOUTH -> stack.translate(width, 0f, -length)
                    Direction.EAST -> stack.translate(-length, 0f, -width)
                    Direction.NORTH -> stack.translate(-width, 0f, length)
                    Direction.WEST -> stack.translate(length, 0f, width)
                    else -> {}
                }
                stack.mulPose(Axis.YN.rotationDegrees(supplier.angle))
            } else {
                stack.mulPose(Axis.YN.rotationDegrees(supplier.angle))
            }
        } else {
            stack.mulPose(Axis.YN.rotationDegrees(supplier.angle))
        }
    }

    fun runLater(runnable: Runnable) {
        TASKS += runnable
    }
}
