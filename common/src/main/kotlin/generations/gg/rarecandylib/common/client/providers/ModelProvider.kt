package generations.gg.rarecandylib.common.client.providers

import net.minecraft.resources.ResourceLocation

interface ModelProvider {
    val model: ResourceLocation?

    val isAnimated: Boolean
        get() = false

    val animation: String
        get() = ""
}