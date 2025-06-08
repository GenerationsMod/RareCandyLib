package generations.gg.rarecandylib.common.client.model

import generations.gg.rarecandylib.common.client.texture.MinecraftTextureLoader
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

class CompiledModelLoader : ResourceManagerReloadListener {
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        MinecraftTextureLoader.initialize(resourceManager)
        SpriteRegistry.onResourceManagerReload(resourceManager)
        ModelRegistry.clear()
    }
}
