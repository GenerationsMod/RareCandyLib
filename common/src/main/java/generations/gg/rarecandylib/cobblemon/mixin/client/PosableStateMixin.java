package generations.gg.rarecandylib.cobblemon.mixin.client;

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.common.client.render.CobblemonInstanceProvider;
import generations.gg.rarecandylib.common.client.render.rarecandy.CobblemonInstance;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@IfModLoaded("cobblemon")
@Mixin(PosableState.class)
public class PosableStateMixin implements CobblemonInstanceProvider {
    @Unique
    private CobblemonInstance instance;

    public @NotNull CobblemonInstance getInstance() {
        if (instance == null) {
            instance = new CobblemonInstance(new Matrix4f(), new Matrix4f(), null);
        }

        return instance;
    }
}
