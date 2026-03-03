package generations.gg.rarecandylib.fabric.client

import com.mojang.blaze3d.platform.InputConstants
import generations.gg.rarecandylib.common.client.RareCandyLibClient
import generations.gg.rarecandylib.common.client.RareCandyLibClientImplementation
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.KeyMapping
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import org.lwjgl.glfw.GLFW
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class RareCandyLibFabricClient : ClientModInitializer, RareCandyLibClientImplementation{
    override fun onInitializeClient() {
        RareCandyLibClient.onInitialize(this)
        ClientLifecycleEvents.CLIENT_STARTED.register(RareCandyLibClient::setupClient)

    }

    override fun registerResourceReloader(
        identifier: ResourceLocation,
        reloader: PreparableReloadListener,
        dependencies: Collection<ResourceLocation>,
    ) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(GenerationsReloadListener(identifier, reloader, dependencies))
    }

    override fun isModLoaded(modid: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(modid)
    }

    class GenerationsReloadListener(private val identifier: ResourceLocation, private val reloader: PreparableReloadListener, private val dependencies: Collection<ResourceLocation>) : IdentifiableResourceReloadListener {

        override fun reload(synchronizer: PreparableReloadListener.PreparationBarrier, manager: ResourceManager, prepareProfiler: ProfilerFiller, applyProfiler: ProfilerFiller, prepareExecutor: Executor, applyExecutor: Executor): CompletableFuture<Void> = this.reloader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)

        override fun getFabricId(): ResourceLocation = this.identifier

        override fun getName(): String = this.reloader.name

        override fun getFabricDependencies(): MutableCollection<ResourceLocation> = this.dependencies.toMutableList()
    }

}
