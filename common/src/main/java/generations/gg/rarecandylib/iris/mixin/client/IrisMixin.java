package generations.gg.rarecandylib.iris.mixin.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.iris.client.ShaderPackInfo;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("iris")
@Mixin(Iris.class)
public abstract class IrisMixin {

    @Shadow
    private static String currentPackName;

    @Shadow
    private static boolean fallback;

    @Inject(method = "loadShaderpack", at = @At(value = "RETURN"), remap = false)
    private static void vanillaStart(CallbackInfo ci) {
        System.out.println("Testing");
        ShaderPackInfo.INSTANCE.shadersChange(currentPackName, fallback);
    }
}
