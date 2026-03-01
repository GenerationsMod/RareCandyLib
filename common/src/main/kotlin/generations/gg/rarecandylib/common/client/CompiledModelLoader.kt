package generations.gg.rarecandylib.common.client

import generations.gg.rarecandylib.common.client.render.rarecandy.ModelRegistry
import generations.gg.rarecandylib.common.client.GenerationsTextureLoader
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

class CompiledModelLoader : ResourceManagerReloadListener {
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        GenerationsTextureLoader.initialize(resourceManager)
        ModelRegistry.clear()
    }
}
