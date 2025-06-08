package generations.gg.rarecandylib.cobblemon;

import dev.architectury.platform.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CobblemonMixinPlugin implements IMixinConfigPlugin {
    private List<String> lists = List.of("ModelAssetVariationMixin",
            "PokemonClientDelegateMixin",
            "PokemonItemRendererMixin",
            "PokemonOnShoulderRendererCompanionAccessorMixin",
            "PokemonOnShoulderRenderMixin",
            "PosableStateMixin");

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        System.out.println("Blep: " + targetClassName + " / " + mixinClassName);

        return lists.stream().noneMatch(mixinClassName::endsWith) || Platform.isModLoaded("cobblemon");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null; // Must match mixin JSON declaration style
    }

    @Override
    public void preApply(String targetClassName, ClassNode classNode, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode classNode, String mixinClassName, IMixinInfo mixinInfo) {
    }
}