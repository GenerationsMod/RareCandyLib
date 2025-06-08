package generations.gg.rarecandylib.common.client.texture

import com.cobblemon.mod.common.util.asResource
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import generations.gg.rarecandylib.common.RareCandyLib.asRclResource
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import org.lwjgl.opengl.GL13C
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.random.Random

class SimpleTextureIndependentData(override var location: ResourceLocation, private val texture: ByteArray?) : SimpleTexture(location),
    ITextureWithResourceLocation {

    init {
        Minecraft.getInstance().textureManager.register(location, this)
    }

    override fun bind(slot: Int) {
        RenderSystem.activeTexture(GL13C.GL_TEXTURE0 + slot)
        RenderSystem.bindTexture(id)
    }

    override fun width(): Int {
        TODO("Not yet implemented")
    }

    override fun height(): Int {
        TODO("Not yet implemented")
    }

    override fun getTextureImage(resourceManager: ResourceManager): TextureImage {
        return load()!!
    }

    private fun load(): TextureImage? {
        return try {
            val inputStream = ByteArrayInputStream(texture)
            val nativeImage: NativeImage = try {
                NativeImage.read(inputStream)
            } catch (var9: Throwable) {
                try {
                    inputStream.close()
                } catch (var7: Throwable) {
                    var9.addSuppressed(var7)
                }
                throw var9
            }

            inputStream.close()
//                var textureMetadataSection: TextureMetadataSection? = null //TODO: See if implmenting is viable
//                try {
//                    textureMetadataSection = resource.metadata().getSection(TextureMetadataSection.SERIALIZER)
//                        .orElse(null as Any?) as TextureMetadataSection
//                } catch (var8: java.lang.RuntimeException) {
//                    LOGGER.warn("Failed reading metadata of: {}", location, var8)
//                }
            TextureImage(null, nativeImage)
        } catch (var10: IOException) {
            TextureImage(var10)
        }
    }

    companion object {
        @Throws(IOException::class)
        fun read(imageBytes: ByteArray?, name: String): SimpleTextureIndependentData? {
            if(imageBytes == null) return null

            val resource = if(name.contains(":")) {
                name.lowercase().asResource()
            } else {
                ((1..4).joinToString("") { Random.nextInt(0, 10).toString() }  + name.lowercase()).asRclResource()
            }

            return SimpleTextureIndependentData(resource, imageBytes)
        }

    }
}