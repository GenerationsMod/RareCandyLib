package generations.gg.rarecandylib.common.block

import net.minecraft.world.level.block.entity.BlockEntity

interface GenericModelBlock<T> {
    fun canRender(t: T): Boolean

}
