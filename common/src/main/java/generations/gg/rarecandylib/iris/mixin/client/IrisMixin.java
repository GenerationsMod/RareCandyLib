package generations.gg.rarecandylib.iris.mixin.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.iris.client.IrisModule;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Iris.class)
@IfModLoaded("iris")
public class IrisMixin {

    @Inject(method = "onEarlyInitialize", at = @At("RETURN"))
    public void onEarlyInitialize(CallbackInfo ci) {
        IrisModule.INSTANCE.initialize();
    }
}
