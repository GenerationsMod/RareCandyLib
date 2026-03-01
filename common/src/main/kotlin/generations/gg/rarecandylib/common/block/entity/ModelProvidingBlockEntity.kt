package generations.gg.rarecandylib.common.block.entity

import net.minecraft.world.level.block.state.BlockState

interface ModelProvidingBlockEntity {
    val effectiveState: BlockState

}
