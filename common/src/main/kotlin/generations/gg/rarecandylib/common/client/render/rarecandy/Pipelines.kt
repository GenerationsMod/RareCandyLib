package generations.gg.rarecandylib.common.client.render.rarecandy

import com.mojang.blaze3d.systems.RenderSystem
import generations.gg.rarecandylib.common.RareCandyLib.id
import generations.gg.rarecandylib.common.client.MatrixCache
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.pipeline.Pipeline
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.pipeline.util.Scope
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext
import gg.generations.rarecandy.renderer.textures.ITexture
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.sin

object Pipelines {

    var useLegacy = false

    @JvmStatic
    fun toggleRendering() {
        useLegacy = !useLegacy
    }


    public var pipeline: TraditionalPipeline? = null
    private var firstInit = true;

    fun onInitialize(manager: ResourceManager, category: String = "default") {
        if (!firstInit) pipeline?.destroy()

        firstInit = false;

        pipeline = createRegular(manager, category)
    }

    private val TEMP = Vector4f()

    fun createRegular(manager: ResourceManager, category: String): TraditionalPipeline =
        compile(manager, "shaders", "generic")
//            .autoBool(Scope.GLOBAL, "legacy") { useLegacy }
            .autoEnum(Scope.GLOBAL, "FogShape", RenderSystem.getShaderFogShape())
            .autoFloat(Scope.GLOBAL, "FogStart") { RenderSystem.getShaderFogStart() }
            .autoFloat(Scope.GLOBAL, "FogEnd") { RenderSystem.getShaderFogEnd() }
            .autoFloat4(Scope.GLOBAL, "FogColor") { RenderSystem.getShaderFogColor() }
            .autoMat4(Scope.GLOBAL, "projectionMatrix") { MatrixCache.projectionMatrix }
            .autoMat4(Scope.GLOBAL, "viewMatrix") { MatrixCache.viewMatrix }
            .addSSBORange(
                Scope.MODEL,
                "VertexBuffer",
                0,
                { ctx -> ctx.`object`.modelBuffer },
                { ctx -> ctx.`object`.vertex })
            .addSSBORange(
                Scope.MODEL,
                "IndexBuffer",
                1,
                { ctx -> ctx.`object`.modelBuffer },
                { ctx -> ctx.`object`.index })
            .addSSBORange(
                Scope.MODEL,
                "MeshOffsetBuffer",
                2,
                { ctx -> ctx.`object`.modelBuffer },
                { ctx -> ctx.`object`.meshOffsets })
            .addSSBORange(
                Scope.MODEL,
                "VariantBuffer",
                3,
                { ctx -> ctx.`object`.modelBuffer },
                { ctx -> ctx.`object`.variant })
            .addSSBORange(
                Scope.MODEL,
                "MaterialBuffer",
                4,
                { ctx -> ctx.`object`.modelBuffer },
                { ctx -> ctx.`object`.material })
            .addSSBO(Scope.MODEL, "InstanceBuffer", 5, { ctx -> ctx.`object`.instanceBuffer.bufferId })
            .addSSBO(Scope.MODEL, "DrawInfoBuffer", 6, { ctx -> ctx.`object`.drawInfoBuffer.bufferId })
            .autoSampler2DArray(Scope.MODEL, "images", 0, { ctx -> ctx.`object`().images })
            .autoFloat4(Scope.GLOBAL, "ColorModulator") { RenderSystem.getShaderColor() }
            .autoSampler2D(
                Scope.GLOBAL,
                "lightmap",
                2
            ) { Minecraft.getInstance().gameRenderer.lightTexture().lightTexture.id }
            .autoSampler2D(Scope.GLOBAL, "paradoxMask", 3) {
                ITextureLoader.instance().getTexture("paradox_mask").id()
            }
            .autoInt(Scope.GLOBAL, "frame") { pingpong(MinecraftClientGameProvider.timePassed).toInt() }
            .autoVec3(Scope.GLOBAL, "Light0_Direction") { RenderSystem.shaderLightDirections[0] }
            .autoVec3(Scope.GLOBAL, "Light1_Direction") { RenderSystem.shaderLightDirections[1] }
            .build()


    fun pingpong(time: Double): Double = (sin(time * Math.PI * 2) * 7 + 7).toInt().toDouble()

    private val TEMP_VEC4 = Vector4f()

    fun TraditionalPipeline.Builder.autoFloat4(
        scope: Scope,
        name: String,
        function: (UniformUploadContext) -> FloatArray
    ): TraditionalPipeline.Builder =
        this.autoVec4(scope, name) { function.invoke(it).let { TEMP_VEC4.set(it[0], it[1], it[2], it[3]) } }


    fun TraditionalPipeline.Builder.autoEnum(
        scope: Scope,
        name: String,
        value: Enum<*>
    ): TraditionalPipeline.Builder =
        this.autoInt(scope, name) { value.ordinal }

    public inline fun <reified T> Any?.instanceOrNull(): T? = this as? T

    fun compile(manager: ResourceManager, root: String, type: String): TraditionalPipeline.Builder {
        val libs = mutableMapOf<String, String>()

        manager.listResources("$root/libs") { it.path.endsWith(".lib.glsl") }
            .forEach { (location, _) ->
                libs[location.path.substringAfterLast('/').removeSuffix(".lib.glsl")] = manager.readString(location)
            }

        val vertex = manager.readString("$root/$type.vs.glsl".id()).applyLibraries(libs)
        val fragment = manager.readString("$root/$type.fs.glsl".id()).applyLibraries(libs)

        return TraditionalPipeline.builder(vertex, fragment)
    }

    private fun String.applyLibraries(libs: Map<String, String>): String {
        var source = this

        libs.forEach { (name, snippet) ->
            source = source.replace("#lib:$name", snippet)
        }

        return source
    }

    fun ResourceManager.readString(name: ResourceLocation): String {
        try {
            this.getResource(name).orElseThrow().open().use { `is` ->
                return String(`is`.readAllBytes())
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to read shader from resource location in shader: $name", e)
        }
    }
}
