package generations.gg.rarecandylib.iris.mixin.client;

import generations.gg.rarecandylib.iris.client.IrisShader;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExtendedShader.class)
public abstract class ExtendedShaderAccessor implements IrisShader {
    @Shadow
    @Final
    private GlFramebuffer writingToBeforeTranslucent;

    @Shadow
    @Final
    private GlFramebuffer writingToAfterTranslucent;

    @Override
    public GlFramebuffer fbo(@NotNull IrisRenderingPipeline pipeline) {
        return pipeline.isBeforeTranslucent? this.writingToBeforeTranslucent : this.writingToAfterTranslucent;
    }
}

