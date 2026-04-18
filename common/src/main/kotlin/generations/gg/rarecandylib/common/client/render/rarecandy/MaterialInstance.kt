package generations.gg.rarecandylib.common.client.render.rarecandy

import org.joml.Matrix3f
import org.joml.Matrix4f

class MaterialInstance(transformationMatrix: Matrix4f, normalMatrix: Matrix3f, variant: Int) : MinecraftObjectInstance(transformationMatrix, normalMatrix, variant) {
    var material: String? = null
}
