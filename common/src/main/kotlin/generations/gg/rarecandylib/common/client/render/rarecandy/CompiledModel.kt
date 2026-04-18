package generations.gg.rarecandylib.common.client.render.rarecandy

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import generations.gg.rarecandylib.common.client.render.rarecandy.loading.RareCandyModelLoader
import generations.gg.rarecandylib.common.util.TaskQueue
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.loading.ModelLoader
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.rendering.ObjectInstance
import gg.generations.rarecandy.renderer.rendering.RareCandy
import gg.generations.rarecandy.renderer.rendering.RenderStage
import gg.generations.rarecandy.shaded.commons.compress.archivers.sevenz.SevenZArchiveEntry
import gg.generations.rarecandy.shaded.commons.compress.archivers.sevenz.SevenZFile
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Represents a compiled model which can be rendered
 */
class CompiledModel(
    val name: ResourceLocation,
    stream: Resource
) {
    val empty: Boolean
        get() = renderObject?.isEmpty ?: false

    @JvmField
    var renderObject: MultiRenderObject? = null

    @JvmField
    var guiInstance: MinecraftObjectInstance? = null

    init {
        ModelRegistry.runLater {
            renderObject = RareCandyModelLoader.createObject(this, stream)
        }
    }

    fun render(instance: ObjectInstance) {
        if (renderObject == null) return

        renderObject?.run {
            this.add(instance)
            instance.use()
        }
    }

    private fun renderRareCandy(instance: ObjectInstance, objectManager: RareCandy) {
        if (!instance.isLinked) {
            objectManager.add(renderObject!!, instance)
        }
        instance.use()
    }

    fun delete() {
//        if(.CONFIG.client.logModelLoading) println("Deleting GPU Resources for: $name")
        renderObject?.run {
            this.close()
            this.imageNameToId.keys.forEach {
                ITextureLoader.instance().remove(it)
            }
        }
    }

    fun render(pipeline: TraditionalPipeline, stage: RenderStage) {
        renderObject?.render(pipeline, stage)
    }

    fun update(time: Double) {
        renderObject?.update(time)
    }

    companion object {
        val queue = TaskQueue()

        @JvmStatic
        fun init() {}

        @JvmStatic
        fun of(key: ResourceLocation): CompiledModel {
            return try {
                val resource = Minecraft.getInstance().resourceManager.getResourceOrThrow(key)

                CompiledModel(key, resource)
            } catch (e: Exception) {
                val path = key.toString()
                if (path.endsWith(".smdx") || path.endsWith(".pqc")) throw RuntimeException("Tried reading a 1.12 .smdx or .pqc")
                throw RuntimeException("Failed to load $path", e)
            }
        }

    }

    class Model(names: ModelLoader.Names) : MultiRenderObject(names) {
        override fun render(pipeline: TraditionalPipeline, stage: RenderStage) {
            drawBuffer[stage]?.render()
        }

        override fun targetVertexStride(): Int = 32

    }

    class Reader(resource: Resource) : gg.generations.rarecandy.pokeutils.resource.ResourceReader {
        private val entries = HashMap<String, ByteArray>()

        init {
            var entry: SevenZArchiveEntry?
            SevenZFile(SeekableInMemoryByteChannel(resource.open().readAllBytes())).use { file ->
                while ((file.nextEntry.also { entry = it }) != null) {
                    if (!entry!!.isDirectory) {
                        this.entries[entry.name] = file.getInputStream(entry).readAllBytes()
                    }
                }
            }
        }

        override fun getName(): String {
            return ""
        }

        @Throws(IOException::class)
        override fun getFile(key: String?): ByteArray? {
            if (!this.entries.containsKey(key)) {
                throw IOException("Entry not found: " + key)
            } else {
                return this.entries.get(key)
            }
        }

        override fun getFileNames(): MutableSet<String> {
            return this.entries.keys
        }

        override fun hasFile(key: String?): Boolean {
            return this.entries.containsKey(key)
        }

        @Throws(IOException::class)
        override fun getInputStream(key: String?): InputStream {
            return ByteArrayInputStream(this.getFile(key))
        }
    }
}