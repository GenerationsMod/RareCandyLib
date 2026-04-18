package generations.gg.rarecandylib.common.client

import com.cobblemon.mod.common.util.asResource
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.textures.ITexture
import gg.generations.rarecandy.renderer.textures.Texture
import net.minecraft.client.Minecraft
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import java.io.IOException
import kotlin.jvm.optionals.getOrNull

object TextureLoader : ITextureLoader() {
    val MAP = mutableMapOf<String, ITexture>();
    val CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
    val RARE_CANDY = FileToIdConverter("textures", "rare_candy_textures.json")
    val gson = Gson()

    fun initialize(manager: ResourceManager) {
        clear()
        try {
            RARE_CANDY.listMatchingResourceStacks(manager).forEach { name, list ->
                list.forEach { resource ->
                    val obj = resource.openAsReader().use {
                        gson.fromJson(it, JsonObject::class.java)
                    }
                    CODEC.parse(JsonOps.INSTANCE, obj).result().getOrNull()?.forEach { (key, value) ->
                        manager.openAsTexture("${value.namespace}:textures/${value.path}.png".asResource()).also {
                            register(key, it)
                        }

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    override fun contains(name: String): Boolean {
        return MAP.contains(name)
    }


    override fun getTexture(s: String): ITexture {
        return MAP[s]!!
    }

    override fun register(name: String, texture: ITexture) {
        MAP.computeIfAbsent(name, {
            texture
        })
    }

    override fun register(id: String, name: String, data: ByteArray) {
        Texture.read(data, name).also { MAP[id] = it }
    }


    override fun remove(name: String) {
        val value = MAP.remove(name)
        if (value != null) {
            try {
                value.close()
            } catch (ignored: IOException) {
            }
        }    }

    fun clear() {
        val iterator = MAP.iterator()
        while(iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.close()

            iterator.remove()
        }
    }

    override fun getDarkFallback(): ITexture = getTexture("dark")

    override fun getBrightFallback(): ITexture = getTexture("bright")

    override fun getNuetralFallback(): ITexture = getTexture("neutral")

    override fun getTextureEntries(): Set<String> = MAP.keys
}

private fun ResourceManager.openAsTexture(location: ResourceLocation): Texture {
    return this.open(location).readAllBytes().let { Texture.read(it, location.path) }
}
