package generations.gg.rarecandylib.common.client.texture

import com.mojang.blaze3d.systems.RenderSystem
import gg.generations.rarecandy.renderer.loading.ITexture
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import org.lwjgl.opengl.GL13C

object MissingTextureProxy : ITexture {

    override fun close() {
    }
    override fun bind(slot: Int) {
        var texture = Minecraft.getInstance().textureManager.getTexture(MissingTextureAtlasSprite.getLocation())

        RenderSystem.activeTexture(GL13C.GL_TEXTURE0 + slot)
        RenderSystem.bindTexture(texture.id)
    }

    override fun width(): Int = 16

    override fun height(): Int = 16

}