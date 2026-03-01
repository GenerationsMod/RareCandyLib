package generations.gg.rarecandylib.cobblemon.mixin.client;

import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.cobblemon.client.RareCandyBone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@IfModLoaded("cobblemon")
@Mixin(PokemonOnShoulderRenderer.class)
public abstract class PokemonOnShoulderRenderMixin {
    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/player/Player;FFFFFFZ)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V"))
    public void renderTranslate(PoseStack instance, double x, double y, double z, @Local(argsOnly = true) boolean pLeftShoulder, @Local(name = "model") PosableModel model) {
        if(model.getRootPart() instanceof RareCandyBone) {
            x += pLeftShoulder ? 0.175 : -0.175;
        }

        instance.translate(x, y, z);
    }
}
