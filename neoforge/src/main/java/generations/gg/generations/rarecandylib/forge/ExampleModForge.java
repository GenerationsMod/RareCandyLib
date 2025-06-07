package generations.gg.generations.rarecandylib.forge;

import generations.gg.generations.rarecandylib.ExampleMod;
import net.minecraftforge.fml.common.Mod;

/**
 * Main class for the mod on the Forge platform.
 */
@Mod(ExampleMod.MOD_ID)
public class ExampleModForge {
    public ExampleModForge() {
        ExampleMod.init();
    }
}
