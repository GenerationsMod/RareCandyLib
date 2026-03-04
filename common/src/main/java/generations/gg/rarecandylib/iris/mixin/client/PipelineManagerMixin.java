package generations.gg.rarecandylib.iris.mixin.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.iris.client.ShaderPackInfo;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;

@IfModLoaded("iris")
@Mixin(PipelineManager.class)
public class PipelineManagerMixin {
    @Inject(method = "preparePipeline", at = @At("RETURN"))
    public void refreshShaders(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
//        System.out.println("Blot Blot");
//        ShaderPackInfo.INSTANCE.apply();
    }
}
