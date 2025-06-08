package generations.gg.rarecandylib.common.client

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.mojang.blaze3d.vertex.BufferUploader
import dev.architectury.registry.ReloadListenerRegistry
import generations.gg.rarecandylib.common.client.model.RareCandyBone
import generations.gg.generations.core.generationscore.common.client.model.RunnableKeybind
import generations.gg.rarecandylib.cobblemon.Pipelines
import generations.gg.rarecandylib.common.RareCandyLib.asRclResource
import generations.gg.rarecandylib.common.client.util.MinecraftClientGameProvider
import generations.gg.rarecandylib.cobblemon.client.GenerationsClientMolangFunctions
import generations.gg.rarecandylib.common.client.model.CompiledModelLoader
import generations.gg.rarecandylib.common.client.model.ModelRegistry
import generations.gg.rarecandylib.common.client.texture.MinecraftTextureLoader
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.rendering.RareCandy
import gg.generations.rarecandy.renderer.rendering.RenderStage
import net.minecraft.client.Minecraft
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.world.phys.Vec3
import org.lwjgl.glfw.GLFW
import java.io.File
import java.util.function.Function

private operator fun BlockPos.minus(pos: BlockPos): BlockPos {
    return this.subtract(pos)
}

private operator fun Vec3.times(scale: Float): Vec3 {
    return this.multiply(scale.toDouble(), scale.toDouble(), scale.toDouble())
}

private operator fun Vec3.minus(vec3: Vec3): Vec3 {
    return this.subtract(vec3)
}

object RareCandyLibClient {
    fun onInitialize(minecraft: Minecraft) {

        ModelRegistry.init()

        ITextureLoader.setInstance(MinecraftTextureLoader)

        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, CompiledModelLoader(), "model_registry".asRclResource())

        setupClient(minecraft)

        GenerationsClientMolangFunctions.addAnimationFunctions()

        RareCandy.DEBUG_THREADS = true

        VaryingModelRepository.registerFactory(".pk", { resourceLocation, resource ->
            ResourceLocation.fromNamespaceAndPath(resourceLocation.namespace, File(resourceLocation.path).getName()) to
                    Function<Boolean, Bone> { bool ->
                        (ModelPart(
                            RareCandyBone.CUBE_LIST,
                            mapOf("root" to RareCandyBone(resourceLocation))
                        )) as Bone
                    }
        })
    }


    private fun setupClient(event: Minecraft) {
        RunnableKeybind.create("toggleShaderRendering", GLFW.GLFW_KEY_P, "rendering", Pipelines::toggleRendering)

        event.tell {
            Pipelines.REGISTER.register(Pipelines::initGenerationsPipelines)

            Pipelines.onInitialize(event.resourceManager)
        }
    }

    fun renderRareCandySolid() {
        renderRareCandy(RenderStage.SOLID, false)
    }

    fun renderRareCandyTransparent(clear: Boolean = false) {
        renderRareCandy(RenderStage.TRANSPARENT, clear)
    }

    private fun renderRareCandy(stage: RenderStage, clear: Boolean) {
        BufferUploader.reset()

        ModelRegistry.worldRareCandy.render(stage, clear, MinecraftClientGameProvider.getTimePassed())
    }
}

