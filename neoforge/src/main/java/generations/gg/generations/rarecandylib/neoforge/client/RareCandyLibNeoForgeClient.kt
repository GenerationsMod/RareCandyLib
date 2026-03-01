package generations.gg.generations.rarecandylib.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants
import generations.gg.rarecandylib.common.RareCandyLib
import generations.gg.rarecandylib.common.client.RareCandyLibClient
import generations.gg.rarecandylib.common.client.RareCandyLibClient.onInitialize
import generations.gg.rarecandylib.common.client.RareCandyLibClientImplementation
import generations.gg.rarecandylib.common.client.model.Keybinds
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.common.NeoForge
import org.lwjgl.glfw.GLFW
import java.util.ArrayList

/**
 * This class is used to initialize the Forge client side of the mod.
 * @see FMLClientSetupEvent
 *
 * @see GenerationsCoreClien
 * t
 *Quigg, WaterPicker
 */
@Mod(value = RareCandyLib.MOD_ID, dist = [Dist.CLIENT])
        class GenerationsCoreClientForge(eventBus: IEventBus): RareCandyLibClientImplementation {
    private var reloadableResources: MutableList<PreparableReloadListener> = ArrayList()

    /**
     * Initializes the client side of the Forge mod.
     * @param eventBus The event bus to register the client side of the mod to.
     */
    init {

//        GenerationsCoreClient.onInitialize(this) //COmment this out when doing datagen. Datagen mod doens't like it for some reason.

        onInitialize(this)

        eventBus.addListener { event: RegisterKeyMappingsEvent ->
                RareCandyLibClient.TOGGLE_CONDITIONS_KEY = KeyMapping(
                        "Toggle Battle Conditions",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        "Generations"
                )
            // TODO: replace name/category with lang i'm lazy
            event.register(RareCandyLibClient.TOGGLE_CONDITIONS_KEY)
        }


        eventBus.addListener<RegisterClientReloadListenersEvent> {
            for(loader in reloadableResources) {
                System.out.println("[GenerationsTextureLoaderLoader] Actually ${loader.name}")
                it.registerReloadListener(loader)
            }
        }

        with(NeoForge.EVENT_BUS) {
            addListener<InputEvent.Key> { Keybinds.pressDown(it.key, it.scanCode, it.action, it.modifiers) }
            addListener<ClientTickEvent.Post> {
                RareCandyLibClient.onTick()
            }
        }


        eventBus.addListener { event: FMLClientSetupEvent ->
                forgeClientSetup(
                        event
                )
        }
    }

    override fun registerResourceReloader(
            identifier: ResourceLocation,
            reloader: PreparableReloadListener,
            dependencies: Collection<ResourceLocation>
    ) {
        System.out.println("[GenerationsTextureLoaderLoader] Loading ${reloader.name}")
        reloadableResources.add(reloader)
    }

    override fun isModLoaded(modid: String): Boolean {
        return ModList.get().isLoaded(modid)
    }

    companion object {
        private fun forgeClientSetup(event: FMLClientSetupEvent) {
            RareCandyLibClient.setupClient(Minecraft.getInstance())
        }
    }
}
