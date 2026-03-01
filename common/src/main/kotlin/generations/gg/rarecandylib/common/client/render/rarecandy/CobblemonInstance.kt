package generations.gg.rarecandylib.common.client.render.rarecandy

import generations.gg.rarecandylib.common.client.TeraProvider
import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.animation.AnimationInstance
import gg.generations.rarecandy.renderer.animation.Transform
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import org.joml.Matrix4f
import org.joml.Vector3f

open class CobblemonInstance(transformationMatrix: Matrix4f?, viewMatrix: Matrix4f?, materialId: String?) :
    AnimatedObjectInstance(transformationMatrix, viewMatrix, materialId), BlockLightValueProvider, TeraProvider {
    lateinit var matrixTransforms: Array<Matrix4f>
    var offsets: Transform? = null
    val tint: Vector3f = Vector3f()
    override var teraActive: Boolean = false
    val teraTint: Vector3f = Vector3f()

    fun getOffset(material: String?): Transform? {
        return if (this.offsets != null) this.currentAnimation!!.getOffset(material) else null
    }

    override var light = 0

    override fun changeAnimation(newAnimation: AnimationInstance?) {
    }

    fun setAnimation(animation: Animation?) {
        if (currentAnimation == null) currentAnimation = StaticAnimationInstance(animation)
        (currentAnimation as StaticAnimationInstance).setAnimation(animation)
    }

    override fun getTransforms(): Array<Matrix4f>? {
        return matrixTransforms
    }

    private class StaticAnimationInstance(animation: Animation?) : AnimationInstance(animation) {
        fun setAnimation(animation: Animation?) {
            this.animation = animation
        }
    }
}