package generations.gg.rarecandylib.cobblemon.mixin.client;

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import generations.gg.rarecandylib.common.client.render.MinecraftObjectInstanceProvider;
import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftObjectInstance;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@IfModLoaded("cobblemon")
@Mixin(PosableState.class)
public class PosableStateMixin implements MinecraftObjectInstanceProvider {
    @Unique
    private MinecraftObjectInstance instance;

    public @NotNull MinecraftObjectInstance getInstance() {
        if (instance == null) {
            instance = new MinecraftObjectInstance(new Matrix4f(), new Matrix3f(), null);
        }

        return instance;
    }
}
