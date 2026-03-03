package generations.gg.rarecandylib.common.util

import com.mojang.blaze3d.vertex.PoseStack
import gg.generations.rarecandy.renderer.rendering.ObjectInstance

public fun ObjectInstance.set(last: PoseStack.Pose) {
    modelMatrix().set(last.pose())
    normalMatrix().set(last.normal())
}

class Utils {
}