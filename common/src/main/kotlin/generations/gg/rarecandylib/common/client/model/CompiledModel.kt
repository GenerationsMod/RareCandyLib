package generations.gg.rarecandylib.common.client.model

import generations.gg.rarecandylib.common.client.util.TaskQueue
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject
import gg.generations.rarecandy.renderer.components.MeshObject
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.rendering.ObjectInstance
import gg.generations.rarecandy.renderer.storage.ObjectManager
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import java.util.function.Supplier

/**
 * Represents a compiled model which can be rendered
 */
class CompiledModel @JvmOverloads constructor(
    val name: ResourceLocation,
    stream: Resource?,
    supplier: Supplier<MeshObject>,
    requiresVariantTexture: Boolean = true,
) {
    @JvmField
    var renderObject: MultiRenderObject<MeshObject>? = null
    val map: List<String> = ArrayList()

    init {
        queue.addTask {
//            if(GenerationsCore.CONFIG.client.logModelLoading) GenerationsCore.LOGGER.info("Loading PK: $name")
            renderObject = loader.compiledModelMethod(this, stream?.open(), supplier, name.toString(), requiresVariantTexture)
        }
    }

    fun render(instance: ObjectInstance, source: MultiBufferSource) {
        if (renderObject == null) return
        if (!renderObject!!.isReady) return

//        if(GenerationsCore.CONFIG.client.useVanilla) {
//            renderVanilla(instance, source)
//        } else {
        renderRareCandy(instance, ModelRegistry.worldRareCandy.objectManager)
//        }
    }

//    private fun renderVanilla(instance: ObjectInstance, source: MultiBufferSource) {
//        var list = renderObject!!.objects
//
//        var transforms = instance.takeIf { it is AnimatedObjectInstance }?.let { it as AnimatedObjectInstance }?.transforms ?: AnimationController.NO_ANIMATION
//
//        list.forEach {
//            var model = it.model.takeIf { it is VanilaRenderModel }?.let { it as VanilaRenderModel } ?: return
//
//            model.bufferSource = source
//
//            model.render(instance, it, transforms)
//        }
//    }

    private fun renderRareCandy(instance: ObjectInstance, objectManager: ObjectManager) {
        objectManager.add(renderObject!!, instance)
    }

    fun delete() {
//        if(GenerationsCore.CONFIG.client.logModelLoading) System.out.println("Deleting GPU Resources for: $name")
        renderObject?.close()
    }

    companion object {
        val loader = GenerationsModelLoader(
            2
        )

        val queue = TaskQueue()

        @JvmStatic
        fun init() {}

        @JvmStatic
        fun of(pair: ResourceLocation, resource: Resource): CompiledModel {
            return try {
                CompiledModel(pair, resource, { AnimatedMeshObject() })
            } catch (e: Exception) {
                val path = pair.toString()
                if (path.endsWith(".smdx") || path.endsWith(".pqc")) throw RuntimeException("Tried reading a 1.12 .smdx or .pqc")
                throw RuntimeException("Failed to load $path", e)
            }
        }

    }
}