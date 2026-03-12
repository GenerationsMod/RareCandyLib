package generations.gg.rarecandylib.common.block

import net.minecraft.world.level.block.state.BlockState

interface GenericRotatableModelBlock {
    val shouldRotateSpecial: Boolean
    val width: Int
    val length: Int

    fun getWidthValue(state: BlockState): Int
    fun getLengthValue(state: BlockState): Int
}
