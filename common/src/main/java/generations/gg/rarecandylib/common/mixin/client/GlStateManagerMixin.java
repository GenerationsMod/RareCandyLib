package generations.gg.rarecandylib.common.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import generations.gg.rarecandylib.common.client.render.RenderStateRecord;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {

    @Inject(method = "_enableBlend", at = @At("RETURN"))
    private static void recordBlendEnable(CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setBlendEnabled(true);
    }

    @Inject(method = "_disableBlend", at = @At("RETURN"))
    private static void recordBlendDisable(CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setBlendEnabled(false);
    }

    @Inject(method = "_blendFuncSeparate", at = @At("RETURN"))
    private static void recordBlendFunSeperate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setSrcRgb(srcRgb);
        RenderStateRecord.setDstRgb(dstRgb);
        RenderStateRecord.setSrcAlpha(srcAlpha);
        RenderStateRecord.setDstAlpha(dstAlpha);
    }

    @Inject(method = "_blendFunc", at = @At("RETURN"))
    private static void recordBlendFunSeperate(int srcRgb, int dstRgb, CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setSrcRgb(srcRgb);
        RenderStateRecord.setDstRgb(dstRgb);
    }

    @Inject(method = "_enableDepthTest", at = @At("RETURN"))
    private static void recordDepthTestEnable(CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setDepthTestEnabled(true);
    }

    @Inject(method = "_disableDepthTest", at = @At("RETURN"))
    private static void recordDepthTestDisable(CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setDepthTestEnabled(false);
    }

    @Inject(method = "_enableCull", at = @At("RETURN"))
    private static void recordNullEnable(CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setCullEnabled(true);
    }

    @Inject(method = "_disableCull", at = @At("RETURN"))
    private static void recordNullDisable(CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setCullEnabled(false);
    }

    @Inject(method = "_depthMask", at = @At("RETURN"))
    private static void recordDepthMask(boolean depthMask, CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setDepthMask(depthMask);
    }

    @Inject(method = "_depthFunc", at = @At("RETURN"))
    private static void recordDepthFunc(int depthFunc, CallbackInfo ci) {
        if(RenderStateRecord.isActive()) return;
        RenderStateRecord.setDepthFunc(depthFunc);
    }
}
