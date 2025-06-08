package generations.gg.rarecandylib.fabric.client

import generations.gg.rarecandylib.common.client.RareCandyLibClient
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft

class RareCandyLibFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        RareCandyLibClient.onInitialize(Minecraft.getInstance())
    }
}
