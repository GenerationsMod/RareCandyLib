package generations.gg.rarecandylib.common.client.render.rarecandy

import generations.gg.rarecandylib.common.client.model.ModelContextProviders.TintProvider
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f

class BlockAnimatedObjectInstance(transformationMatrix: Matrix4f, normalMatrix: Matrix3f, materialId: Int) : AnimatedObjectInstance(transformationMatrix, normalMatrix,  materialId), BlockLightValueProvider, TintProvider {
    override var light: Int = 0x000000
    override var tint: Vector3f? = null
}
