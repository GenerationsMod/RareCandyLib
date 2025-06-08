package generations.gg.rarecandylib.common.client.texture

import gg.generations.rarecandy.renderer.loading.ITexture
import net.minecraft.resources.ResourceLocation

interface ITextureWithResourceLocation: ITexture {
    var location: ResourceLocation
}