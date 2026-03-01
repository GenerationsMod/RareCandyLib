package generations.gg.rarecandylib.common.client.model

import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f

class ModelContextProviders {
    interface AngleProvider {
        val angle: Float
    }

    interface VariantProvider : ModelProvider {
        val variant: String?
    }

    interface ModelProvider {
        val model: ResourceLocation?

        val isAnimated: Boolean
            get() = false

        val animation: String
            get() = ""
    }

    interface FrameProvider : ModelProvider {
        val frame: Float
    }

    interface TintProvider {
        var tint: Vector3f?
    }
}
