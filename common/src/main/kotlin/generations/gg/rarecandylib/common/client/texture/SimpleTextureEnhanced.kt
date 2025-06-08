package generations.gg.rarecandylib.common.client.texture

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.resources.ResourceLocation
import org.lwjgl.opengl.GL13C

class SimpleTextureEnhanced(override var location: ResourceLocation) : SimpleTexture(location),
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
}