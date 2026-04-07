package generations.gg.rarecandylib.common.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import generations.gg.rarecandylib.common.client.MatrixCache;
import generations.gg.rarecandylib.common.client.RareCandyLibClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"))
    private void pokecraft$firstPass(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        MatrixCache.setProjectionMatrix(RenderSystem.getProjectionMatrix());
        MatrixCache.setViewMatrix(RenderSystem.getModelViewMatrix());
        if(!RareCandyLibClient.isIrisRendering()) RareCandyLibClient.INSTANCE.firstRenderPass();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCloudsType()Lnet/minecraft/client/CloudStatus;"))
    private void pokecraft$secondPass(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if(!RareCandyLibClient.isIrisRendering()) RareCandyLibClient.INSTANCE.secondRenderPass();
    }


}