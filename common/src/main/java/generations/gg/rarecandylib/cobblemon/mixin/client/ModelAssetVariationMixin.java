package generations.gg.rarecandylib.cobblemon.mixin.client;

import com.cobblemon.mod.common.client.render.ModelAssetVariation;
import com.google.gson.annotations.SerializedName;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.cobblemon.client.IVariant;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@IfModLoaded("cobblemon")
@Mixin(ModelAssetVariation.class)
public class ModelAssetVariationMixin implements IVariant {
    @Unique
    @SerializedName("variant")
    @Nullable
    private String variant = null; // Now explicitly nullable

    @Nullable
    public String getVariant() {
        return variant;
    }

    public void setVariant(@Nullable String variant) {
        this.variant = variant;
    }
}
