package generations.gg.rarecandylib.common.client.model;

import com.mojang.blaze3d.systems.RenderSystem
import generations.gg.rarecandylib.common.client.providers.ModelProvider
import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.rendering.RareCandy
import gg.generations.rarecandy.shaded.caffeine.cache.CacheLoader
import gg.generations.rarecandy.shaded.caffeine.cache.Caffeine
import gg.generations.rarecandy.shaded.caffeine.cache.RemovalCause
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

object ModelRegistry {
    private const val DUMMY = "dummy"

    private val LOADER = Caffeine.newBuilder().executor(Minecraft.getInstance()).expireAfterAccess(2, TimeUnit.MINUTES)
        .removalListener { key: ResourceLocation, value: CompiledModel, cause: RemovalCause ->
//            if(GenerationsCore.CONFIG.client.logModelLoading) GenerationsCore.LOGGER.info(String.format("%s was removed from cache. Deleting GPU Resouces next.", key, cause))
            RenderSystem.recordRenderCall { value.delete() }
        }
        .buildAsync<ResourceLocation, CompiledModel>(
            CacheLoader { key ->
                val resourceManager = Minecraft.getInstance().resourceManager
                try {
                    return@CacheLoader CompiledModel.of(key, resourceManager.getResourceOrThrow(key))
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            })
    @JvmStatic
    operator fun get(modelProvider: ModelProvider): CompiledModel? {
        return modelProvider.model?.let(ModelRegistry::get)
    }

    @JvmStatic
    fun cache() = LOADER

    @JvmStatic
    fun clear() = LOADER.asMap().clear()

    fun tick() = /*CACHE.tick()*/ {}

    @JvmStatic
    operator fun get(location: ResourceLocation): CompiledModel? {
        return try {
            LOADER[location].getNow(null)
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun init() {
        CompiledModel.init()
    }

    @JvmStatic
    val worldRareCandy: RareCandy = RareCandy().also {
        Animation.animationModifier = BiConsumer { animation: Animation, s: String ->
            if (s == "gfb") animation.ticksPerSecond = 60000f // 60 fps. 1000 ticks per frame?
        }
    }
}
