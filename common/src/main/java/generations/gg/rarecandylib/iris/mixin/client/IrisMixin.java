package generations.gg.rarecandylib.iris.mixin.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.iris.client.IrisModule;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Iris.class)
@IfModLoaded("iris")
public class IrisMixin {

    @Shadow
    private static String currentPackName;

    @Inject(method = "onEarlyInitialize", at = @At("RETURN"))
    public void onEarlyInitialize(CallbackInfo ci) {
        IrisModule.INSTANCE.initialize();
    }

    @Inject(method = "loadShaderpack", at = @At("RETURN"))
    private static void shaderPackLoaded(CallbackInfo ci) {
        IrisModule.loadShader(currentPackName);
    }
}
