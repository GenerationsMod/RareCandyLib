package generations.gg.rarecandylib.iris.client

import generations.gg.rarecandylib.common.client.RareCandyLibClient
import net.irisshaders.iris.api.v0.IrisApi

object IrisModule {
    fun initialize() {
        RareCandyLibClient.isIrisRenderingImpl = { IrisApi.getInstance().isShaderPackInUse }
        RareCandyLibClient.LOGGER.info("RareCandyLib Iris Module Enabled.")
    }
}