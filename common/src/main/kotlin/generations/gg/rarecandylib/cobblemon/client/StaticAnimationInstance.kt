package generations.gg.rarecandylib.cobblemon.client

import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.animation.AnimationInstance

class StaticAnimationInstance(animation: Animation) : AnimationInstance(animation) {
    override fun getAnimation(): Animation {
        return animation
    }

    fun setAnimation(animation: Animation) {
        this.animation = animation
    }
}