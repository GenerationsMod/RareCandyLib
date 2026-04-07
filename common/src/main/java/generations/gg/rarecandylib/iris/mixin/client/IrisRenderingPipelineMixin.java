package generations.gg.rarecandylib.iris.mixin.client;

import generations.gg.rarecandylib.common.client.RareCandyLibClient;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisRenderingPipeline.class)
public class IrisRenderingPipelineMixin {
    @Inject(
            method = "finalizeLevelRendering",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/FinalPassRenderer;renderFinalPass()V",
                    shift = At.Shift.AFTER
            )
    )
    public void renderStuff(CallbackInfo ci) {
        RareCandyLibClient.INSTANCE.firstRenderPass();
        RareCandyLibClient.INSTANCE.secondRenderPass();
    }
}
