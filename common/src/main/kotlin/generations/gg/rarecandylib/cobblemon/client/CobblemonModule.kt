package generations.gg.rarecandylib.cobblemon.client

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import generations.gg.rarecandylib.common.client.RareCandyLibClient
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.resources.ResourceLocation
import java.io.File

object CobblemonModule {
    fun initialize() {
        RareCandyLibClientMolangFunctions.addAnimationFunctions()

        VaryingModelRepository.registerFactory(".pk", { resourceLocation, resource ->
            ResourceLocation.fromNamespaceAndPath(resourceLocation.namespace, File(resourceLocation.path).getName()) to
                    (ModelPart(
                        emptyList(),
                        mapOf("root" to RareCandyBone(resourceLocation,))
                    )) as Bone

        })

        RareCandyLibClient.LOGGER.info("RareCandyLib Cobblemon Module Enabled.")
    }
}