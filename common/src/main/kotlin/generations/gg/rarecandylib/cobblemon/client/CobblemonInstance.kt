package generations.gg.rarecandylib.cobblemon.client

import generations.gg.rarecandylib.cobblemon.TeraProvider
import generations.gg.rarecandylib.common.client.providers.BlockLightValueProvider
import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.animation.AnimationInstance
import gg.generations.rarecandy.renderer.animation.Transform
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import org.joml.Matrix4f
import org.joml.Vector3f

class CobblemonInstance(transformationMatrix: Matrix4f, viewMatrix: Matrix4f, materialId: String?) :
    AnimatedObjectInstance(transformationMatrix, viewMatrix, materialId), BlockLightValueProvider, TeraProvider {
    lateinit var matrixTransforms: Array<Matrix4f>
    var offsets: Transform? = null
    val tint: Vector3f = Vector3f()
    override var teraActive: Boolean = false

    fun getOffset(material: String?): Transform? {
        return if (this.offsets != null) currentAnimation!!.getOffset(material) else null
    }

    override var light: Int = 0

    override fun changeAnimation(newAnimation: AnimationInstance) {
    }

    fun setAnimation(animation: Animation) {
        if (currentAnimation == null) currentAnimation = StaticAnimationInstance(animation)
        (currentAnimation as StaticAnimationInstance).setAnimation(animation)
    }

    override fun getTransforms(): Array<Matrix4f> {
        return matrixTransforms
    }

}