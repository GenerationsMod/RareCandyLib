package generations.gg.rarecandylib.common.client.render.rarecandy

import org.joml.Matrix3f
import org.joml.Matrix4f

class StatueInstance(transformationMatrix: Matrix4f, normalMatrix: Matrix3f, materialId: String) :
    CobblemonInstance(transformationMatrix, normalMatrix, materialId) {

    var material: String? = null
}
