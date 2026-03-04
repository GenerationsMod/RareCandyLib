package generations.gg.rarecandylib.iris.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.common.client.RareCandyLibClient;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("iris")
@Mixin(value = IrisRenderingPipeline.class)
public class IrisRenderingPipelineMixin {
//    @WrapOperation(method = "beginLevelRendering", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;renderAll()V"), remap = false)
//    public void firstPass(CompositeRenderer instance, Operation<Void> original) {
//        RareCandyLibClient.LOGGER.debug("WTF");
//        RareCandyLibClient.INSTANCE.firstRenderPass();
//        original.call(instance);
//    }
}
