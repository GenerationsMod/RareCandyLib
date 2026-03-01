package generations.gg.rarecandylib.cobblemon.mixin.client;

import com.cobblemon.mod.common.CobblemonClientImplementation;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.cobblemon.client.CobblemonModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("cobblemon")
@Mixin(CobblemonClient.class)
public class CobblemonMixin {
    @Inject(method = "initialize", at = @At("TAIL"))
    public void startModule(CobblemonClientImplementation implementation, CallbackInfo ci) {
        CobblemonModule.INSTANCE.initialize();
    }
}
