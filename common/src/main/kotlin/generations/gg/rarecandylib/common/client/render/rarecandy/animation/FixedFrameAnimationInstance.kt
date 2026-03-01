package generations.gg.rarecandylib.common.client.render.rarecandy.animation

import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.animation.AnimationInstance

class FixedFrameAnimationInstance(animation: Animation?, frame: Float) : AnimationInstance(animation) {
    init {
        this.currentTime = frame
    }

    override fun update(secondsPassed: Double) {
//        super.update(secondsPassed);
    }

    override fun updateStart(secondsPassed: Double) {
//        super.updateStart(secondsPassed);
    }

    override fun shouldDestroy(): Boolean {
        return super.shouldDestroy()
    }

    fun setCurrentTime(frame: Float) {
        currentTime = frame
    }
}