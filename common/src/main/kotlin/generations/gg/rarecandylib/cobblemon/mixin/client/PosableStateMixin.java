package generations.gg.rarecandylib.cobblemon.mixin.client;

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import generations.gg.rarecandylib.cobblemon.CobblemonInstanceProvider;
import generations.gg.rarecandylib.cobblemon.client.CobblemonInstance;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PosableState.class)
public class PosableStateMixin implements CobblemonInstanceProvider {
    private CobblemonInstance instance;

    public @NotNull CobblemonInstance getInstance() {
        if (instance == null) {
            instance = new CobblemonInstance(new Matrix4f(), new Matrix4f(), null);
        }

        return instance;
    }
}
