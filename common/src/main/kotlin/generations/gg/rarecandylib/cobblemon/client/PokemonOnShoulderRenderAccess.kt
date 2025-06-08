package generations.gg.rarecandylib.cobblemon.client

import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer
import net.minecraft.nbt.CompoundTag
import java.util.*

interface PokemonOnShoulderRenderAccess {
    fun invokeExtractUuid(tag: CompoundTag?): UUID?
    fun invokeExtractData(shoulderNbt: CompoundTag?, pokemonUUID: UUID?): PokemonOnShoulderRenderer.ShoulderData?
}
