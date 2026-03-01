package generations.gg.rarecandylib.common.client.render.rarecandy

import generations.gg.rarecandylib.common.client.render.rarecandy.loading.GenerationsModelLoader
import generations.gg.rarecandylib.common.util.TaskQueue
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject
import gg.generations.rarecandy.renderer.components.MeshObject
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.rendering.ObjectInstance
import gg.generations.rarecandy.renderer.storage.ObjectManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import java.util.function.Supplier

/**
 * Represents a compiled model which can be rendered
 */
class CompiledModel @JvmOverloads constructor(
    val name: ResourceLocation,
    stream: Resource,
    supplier: Supplier<MeshObject>,
    requiresVariantTexture: Boolean = true,
) {
    @JvmField
    var renderObject: MultiRenderObject<MeshObject>? = null
    val map: List<String> = ArrayList()

    @JvmField
    var guiInstance: CobblemonInstance? = null

    init {
        queue.addTask {
//            if(GenerationsCore.CONFIG.client.logModelLoading) GenerationsCore.LOGGER.info("Loading PK: $name")
            renderObject = loader.compiledModelMethod(this, stream.open(), supplier, name.toString(), requiresVariantTexture)
        }
    }

    fun render(instance: ObjectInstance, source: MultiBufferSource) {
        if (renderObject == null) return
        if (!renderObject!!.isReady) return

        renderRareCandy(instance, ModelRegistry.worldRareCandy.objectManager)
    }

    private fun renderRareCandy(instance: ObjectInstance, objectManager: ObjectManager) {
        if (!instance.isLinked) {
            objectManager.add(renderObject!!, instance)
        }
        instance.use()
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
        fun of(key: ResourceLocation): CompiledModel {
            return try {
                val resource = Minecraft.getInstance().resourceManager.getResourceOrThrow(key)

                CompiledModel(key, resource, { AnimatedMeshObject() })
            } catch (e: Exception) {
                val path = key.toString()
                if (path.endsWith(".smdx") || path.endsWith(".pqc")) throw RuntimeException("Tried reading a 1.12 .smdx or .pqc")
                throw RuntimeException("Failed to load $path", e)
            }
        }

    }
}