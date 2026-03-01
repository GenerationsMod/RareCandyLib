package generations.gg.rarecandylib.common.client.render.rarecandy

object MinecraftClientGameProvider {
    private val START_TIME = System.currentTimeMillis().toDouble()

    val timePassed: Double
        get() = (System.currentTimeMillis() - START_TIME) / 1000f
}