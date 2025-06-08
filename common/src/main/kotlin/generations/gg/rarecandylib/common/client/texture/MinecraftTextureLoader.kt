package generations.gg.rarecandylib.common.client.texture

import com.cobblemon.mod.common.util.asResource
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import generations.gg.rarecandylib.common.client.model.SpriteRegistry
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.loading.ITexture
import net.minecraft.client.Minecraft
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager

object MinecraftTextureLoader : ITextureLoader() {
    val REGULAR = mutableMapOf<String, ResourceLocation>()
    val CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
    val RARE_CANDY = FileToIdConverter("textures", "rare_candy_texture.json")
    val gson = Gson()


    fun initialize(manager: ResourceManager) {
        clear()
        try {
            RARE_CANDY.listMatchingResourceStacks(manager).forEach { name, list ->
                list.forEach { resource ->
                    val obj = resource.openAsReader().use { SpriteRegistry.GSON.fromJson(it, JsonObject::class.java) }
                    val map = CODEC.decode(JsonOps.INSTANCE, obj).orThrow.first

                    if(map.isNotEmpty()) {
                        map.forEach { (key, value) ->
                            register(key, SimpleTextureEnhanced(value.let { "${it.namespace}:textures/${it.path}.png" }.asResource()))
                        }
                    }
                }
            }

//            RARE_CANDY.listMatchingResources(manager).values.forEach { resouce ->
//                resouce.openAsReader().use { GsonHelper.fromJson(gson, it, RARE_CANDY_TYPE) }.forEach { (key, value) ->
//                    register(key, SimpleTextureEnhanced(value.asResource().let { "${it.namespace}:textures/${it.path}.png" }.asResource()))
//                }
//            }
        } catch (e: Exception) { throw RuntimeException(e)
        }
    }

    override fun getTexture(s: String?): ITexture? {
        val texture = REGULAR.getOrDefault(s, null)?.let { Minecraft.getInstance().textureManager.getTexture(it, null) }.takeIf { it is ITextureWithResourceLocation } ?: return MissingTextureProxy

        return texture as ITexture
    }

    override fun register(s: String, iTexture: ITexture) {
        if(iTexture is ITextureWithResourceLocation) REGULAR.putIfAbsent(s, iTexture.location)
    }

    override fun register(id: String, name: String, data: ByteArray) {
        SimpleTextureIndependentData.read(data, name)?.let { register(id, it) }
    }


    override fun remove(s: String) {
        REGULAR.remove(s)?.run { Minecraft.getInstance().textureManager.release(this) }
    }

    fun clear() {
        val iterator = REGULAR.iterator()
        while(iterator.hasNext()) {
            val entry = iterator.next()
            iterator.remove()
            Minecraft.getInstance().textureManager.release(entry.value)
        }
    }

    override fun getDarkFallback(): ITexture? = getTexture("dark")

    override fun getBrightFallback(): ITexture? = getTexture("bright")

    override fun getNuetralFallback(): ITexture? = getTexture("neutral")

    override fun getTextureEntries(): Set<String> = REGULAR.keys

    fun has(texture: String?): Boolean = REGULAR.containsKey(texture)

    fun getLocation(material: String?): ResourceLocation? = REGULAR.getOrDefault(material, null)
    fun getTextureOrNull(name: String?): ITexture? {
        return if(has(name)) getTexture(name) else null
    }

}