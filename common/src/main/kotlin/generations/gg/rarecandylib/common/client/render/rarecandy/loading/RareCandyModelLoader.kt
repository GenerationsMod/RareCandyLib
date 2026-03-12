package generations.gg.rarecandylib.common.client.render.rarecandy.loading

import generations.gg.rarecandylib.common.client.RareCandyLibClient
import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftObjectInstance
import generations.gg.rarecandylib.common.client.render.rarecandy.CompiledModel
import gg.generations.rarecandy.pokeutils.PixelAsset
import gg.generations.rarecandy.renderer.components.MeshObject
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.loading.ModelLoader
import gg.generations.rarecandy.renderer.model.GLModel
import org.joml.Matrix3f
import org.joml.Matrix4f
import java.io.InputStream
import java.util.function.Supplier

class RareCandyModelLoader(numThreads: Int) : ModelLoader(numThreads) {
    fun compiledModelMethod(
        model: CompiledModel,
        stream: InputStream,
        supplier: Supplier<MeshObject>,
        name: String?
    ): MultiRenderObject<MeshObject> {
        return createObject<MeshObject>(
            { PixelAsset(stream, name) },
            { gltfModel, animResources, textures, config, `object` ->
                val glCalls = ArrayList<Runnable>()
                try {
                    processModel(
                        `object`,
                        gltfModel,
                        animResources,
                        textures,
                        config,
                        glCalls,
                        supplier
                    ) { vertexBuffer, indexBuffer, glCalls, indexSize, gltType, attributes ->
                        GLModel(
                            vertexBuffer,
                            indexBuffer,
                            glCalls,
                            indexSize,
                            gltType,
                            attributes
                        )
                    }
                } catch (e: Exception) {
                    RareCandyLibClient.LOGGER.error("Oh no! Model : $name didn't properly load!")
                    e.printStackTrace()
                }
                glCalls
            },
            { `object`: MultiRenderObject<MeshObject?>? ->
                model.guiInstance = MinecraftObjectInstance(Matrix4f(), Matrix3f(), null)
                model.guiInstance!!.link(`object`)
                if (`object`!!.scale == 0f) `object`.scale = 1.0f
            }
        )
    }
}