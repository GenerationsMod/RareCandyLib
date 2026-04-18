package generations.gg.rarecandylib.common.client.render.rarecandy

import com.mojang.blaze3d.vertex.VertexConsumer
import generations.gg.rarecandylib.common.client.TeraProvider
import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.animation.AnimationController
import gg.generations.rarecandy.renderer.animation.AnimationInstance
import gg.generations.rarecandy.renderer.animation.Transform
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import gg.generations.rarecandy.renderer.storage.SSBOBuffer
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f

open class MinecraftObjectInstance(transformationMatrix: Matrix4f, normalMatrix: Matrix3f, variant: Int) : AnimatedObjectInstance(transformationMatrix, normalMatrix, variant), BlockLightValueProvider, TeraProvider {
    companion object {
        // std430 layout: mat4 + padded mat3 + 220 bone mat4s + light/pad + tint vec4 + teraTint/bool slot
        val SIZE = (16 + 12 + (220 * 16) + 4 + 4 + 4) * Float.SIZE_BYTES
    }

    var matrixTransforms: Array<Matrix4f> = AnimationController.NO_ANIMATION
    var offsets: Transform? = null
    val tint: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)
    override var teraActive: Boolean = false
    val teraTint: Vector3f = Vector3f()

//    fun getOffset(material: String?): Transform? {
//        return if (this.offsets != null) this.currentAnimation!!.getOffset(material) else null
//    }

    override var light = 0


    override fun changeAnimation(newAnimation: AnimationInstance?) {}

    override fun update(instanceBuffer: SSBOBuffer) {

        super.update(instanceBuffer)
        instanceBuffer.put(light and 0xFFFF).put((light ushr 16) and 0xFFFF).put(0).put(0);
        instanceBuffer.put(tint).put(1.0f)
        instanceBuffer.put(teraTint).put(teraActive);
    }

    override fun update(absoluteTime: Double) {
        super.update(absoluteTime)
    }

    fun setAnimation(animation: Animation?) {
        if (currentAnimation == null) currentAnimation = StaticAnimationInstance(animation)
        (currentAnimation as StaticAnimationInstance).setAnimation(animation)
    }

    override fun getTransforms(): Array<Matrix4f> {
        return matrixTransforms
    }

    private class StaticAnimationInstance(animation: Animation?) : AnimationInstance(animation) {
        fun setAnimation(animation: Animation?) {
            this.animation = animation
        }
    }
}
