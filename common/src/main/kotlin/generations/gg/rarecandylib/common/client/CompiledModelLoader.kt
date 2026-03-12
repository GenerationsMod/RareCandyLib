package generations.gg.rarecandylib.common.client

import generations.gg.rarecandylib.common.client.render.rarecandy.ModelRegistry
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

class CompiledModelLoader : ResourceManagerReloadListener {
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        TextureLoader.initialize(resourceManager)
        ModelRegistry.clear()
    }
}
