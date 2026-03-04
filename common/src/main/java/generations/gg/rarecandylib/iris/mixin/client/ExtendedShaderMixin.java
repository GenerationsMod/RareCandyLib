package generations.gg.rarecandylib.iris.mixin.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.iris.client.ExtendedShaderAccess;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@IfModLoaded("iris")
@Mixin(ExtendedShader.class)
public class ExtendedShaderMixin implements ExtendedShaderAccess {
    @Shadow
    @Final
    private GlFramebuffer writingToBeforeTranslucent;

    @Shadow
    @Final
    private GlFramebuffer writingToAfterTranslucent;

    @Override
    public @NotNull GlFramebuffer getFrameBuffer(boolean beforeTranslucent) {
        return beforeTranslucent ? writingToBeforeTranslucent : writingToAfterTranslucent;
    }
}
