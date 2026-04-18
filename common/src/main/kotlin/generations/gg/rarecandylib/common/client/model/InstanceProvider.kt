package generations.gg.rarecandylib.common.client.model

import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftObjectInstance
import gg.generations.rarecandy.renderer.rendering.ObjectInstance

interface InstanceProvider {
    fun generateInstance(): MinecraftObjectInstance?

    var instanceArray: Array<MinecraftObjectInstance?>?
}
