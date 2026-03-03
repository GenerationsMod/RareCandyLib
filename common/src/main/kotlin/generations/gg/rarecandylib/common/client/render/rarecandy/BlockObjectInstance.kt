package generations.gg.rarecandylib.common.client.render.rarecandy

import generations.gg.rarecandylib.common.client.model.ModelContextProviders.TintProvider
import gg.generations.rarecandy.renderer.rendering.ObjectInstance
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f

class BlockObjectInstance(transformationMatrix: Matrix4f, normalMatrix: Matrix3f, variant: String) : ObjectInstance(transformationMatrix, normalMatrix, variant), BlockLightValueProvider, TintProvider {
    override var light = 0x000000
    override var tint: Vector3f? = null
}
