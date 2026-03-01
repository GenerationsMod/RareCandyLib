package generations.gg.rarecandylib.common

import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger

object RareCandyLib {
    /** The mod id for  examplemod.  */
    const val MOD_ID: String = "rarecandylib"

    /** The logger for examplemod.  */
    val LOGGER: Logger = LogUtils.getLogger()

    /**
     * Initializes the mod.
     */
    fun init() {
    }

    fun String.id(): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, this)
}
