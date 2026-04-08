
package generations.gg.rarecandylib.iris.mixin.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.common.client.RareCandyLibClient;
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines;
import generations.gg.rarecandylib.iris.client.IrisFbo;
import generations.gg.rarecandylib.iris.client.IrisModule;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(IrisRenderingPipeline.class)
@IfModLoaded("iris")
public abstract class IrisRenderingPipelineMixin {

    @Inject(method = "beginTranslucents", at = @At("HEAD"))
    private void rarecandylib$beforeDeferred(CallbackInfo ci) {
        IrisModule.firstPass((IrisRenderingPipeline) (Object) this);
    }

    @Inject(method = "beginTranslucents", at = @At("TAIL"))
    private void rarecandylib$afterDeferred(CallbackInfo ci) {
        IrisModule.secondPass((IrisRenderingPipeline) (Object) this);
    }
}