package generations.gg.rarecandylib.iris.mixin.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.irisshaders.iris.pipeline.VanillaRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("iris")
@Mixin(VanillaRenderingPipeline.class)
public class VanillaRenderingPipelineMixin {
    public void vanillaStart() {

    }
}
