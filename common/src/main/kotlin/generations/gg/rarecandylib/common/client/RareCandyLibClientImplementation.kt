package generations.gg.rarecandylib.common.client

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener

interface RareCandyLibClientImplementation {
    fun registerResourceReloader(
        identifier: ResourceLocation,
        reloader: PreparableReloadListener,
        dependencies: Collection<ResourceLocation>,
    )

    fun isModLoaded(modid: String): Boolean
}
