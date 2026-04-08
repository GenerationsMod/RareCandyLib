package generations.gg.rarecandylib.common.client.render.rarecandy

import com.mojang.blaze3d.systems.RenderSystem
import generations.gg.rarecandylib.common.RareCandyLib.id
import generations.gg.rarecandylib.common.client.TextureLoader
import generations.gg.rarecandylib.common.client.MatrixCache
import generations.gg.rarecandylib.common.client.RareCandyLibClient
import generations.gg.rarecandylib.common.client.TeraProvider
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.TintProvider
import gg.generations.rarecandy.pokeutils.BlendType
import gg.generations.rarecandy.pokeutils.CullType
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.animation.AnimationController
import gg.generations.rarecandy.renderer.animation.Transform
import gg.generations.rarecandy.renderer.loading.ITexture
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry
import gg.generations.rarecandy.renderer.pipeline.Pipeline
import gg.generations.rarecandy.renderer.pipeline.UniformUploadContext
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.sin

data class ShaderSet(val regular: Pipeline, val terastal: Pipeline) {
    fun destroy() {
        regular.destroy()
        terastal.destroy()
    }

    fun getPipeline(isTeraActive: Boolean): Pipeline = if(isTeraActive) terastal else regular

    companion object {
        fun create(manager: ResourceManager, category: String): ShaderSet {
            return ShaderSet(
                Pipelines.createRegular(manager, category),
                Pipelines.createTerastal(manager, category)
            )
        }
    }
}

object Pipelines {
    var alternateShaderProvider: () -> ShaderSet? = { null }
    val ONE = Vector3f(1f, 1f, 1f)
    val ZERO = Vector3f(0f, 0f, 0f)

    var useLegacy = false

    @JvmStatic
    fun toggleRendering() {
        useLegacy = !useLegacy
    }


    private lateinit var default: ShaderSet
    private var firstInit = true;

    fun onInitialize(manager: ResourceManager, category: String = "default") {
        if(!firstInit) default.destroy()

        firstInit = false;

        default = ShaderSet.create(manager, "default")

        PipelineRegistry.setFunction({ _, instance, _ -> return@setFunction (RareCandyLibClient.isIrisRendering.takeIf { it }?.let { alternateShaderProvider.invoke() } ?: default).getPipeline(instance.instanceOrNull<MinecraftObjectInstance>()?.teraActive ?: false) })
    }

    private val TEMP = Vector4f()

    fun createTerastal(manager: ResourceManager, category: String): Pipeline = Pipeline.Builder()
        .supplyBooleanUniform("legacy") { useLegacy }
        .supplyEnumUniform("FogShape", RenderSystem.getShaderFogShape())
        .supplyMat4("viewMatrix") { MatrixCache.viewMatrix }
        .supplyMat4("modelMatrix") { it.instance().modelMatrix() }
        .supplyMat3("normalMatrix") { it.instance().normalMatrix() }
        .supplyMat4("projectionMatrix") { MatrixCache.projectionMatrix }
        .supplyVec2("uvOffset") { it.transform.offset() ?: Transform.DEFAULT_OFFSET }
        .supplyVec2("uvScale") { it.transform.scale() ?: Transform.DEFAULT_SCALE }
        .supplyMat4s("boneTransforms") { ctx ->
            ctx.instance().instanceOrNull<AnimatedObjectInstance>()?.transforms ?: AnimationController.NO_ANIMATION
        }

        .supplyColorArray("ColorModulator") { RenderSystem.getShaderColor() }
        .supplyFloatUniform("FogStart") { RenderSystem.getShaderFogStart() }
        .supplyFloatUniform("FogEnd") { RenderSystem.getShaderFogEnd() }
        .supplyColorArray("FogColor") { RenderSystem.getShaderFogColor() }

        .supplyTexture("diffuse", 0) {
            it.instance()
                .instanceOrNull<StatueInstance>()?.material?.let { TextureLoader.getTextureOrNull(it) }
                ?: it.getTextureOrOther({ it.material.images().diffuse }) { ITextureLoader.instance().nuetralFallback }
        }
        .supplyTexture(
            "mask",
            1
        ) { it.getTextureOrOther({ it.material.images().mask }) { ITextureLoader.instance().darkFallback } }
        .supplyTexture(
            "layer",
            2
        ) { it.getTextureOrOther({ it.material.images().layer }) { ITextureLoader.instance().darkFallback } }
        .supplyTexture("paradoxMask", 5) { ITextureLoader.instance().getTexture("paradox_mask") }

        .supplyInt("colorMethod") { it.material.colorMethod }
        .supplyInt("effect") { it.material.effect }
        .supplyVec3("tint") { it.instance().instanceOrNull<MinecraftObjectInstance>()?.tint?.takeIf { it != ZERO } ?: ONE }

        .supplyInt("frame") { pingpong(MinecraftClientGameProvider.timePassed).toInt() }

        .supplyVec3("baseColor1") { ctx ->
            ctx.instance().instanceOrNull<TintProvider>()?.tint
                ?: ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor1 ?: ONE
        }
        .supplyVec3("baseColor2") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor2 ?: ONE }
        .supplyVec3("baseColor3") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor3 ?: ONE }
        .supplyVec3("baseColor4") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor4 ?: ONE }
        .supplyVec3("baseColor5") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor5 ?: ONE }
        .supplyVec3("emiColor1") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor1 ?: ONE }
        .supplyVec3("emiColor2") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor2 ?: ONE }
        .supplyVec3("emiColor3") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor3 ?: ONE }
        .supplyVec3("emiColor4") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor4 ?: ONE }
        .supplyVec3("emiColor5") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor5 ?: ONE }
        .supplyFloatUniform("emiIntensity1") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity1 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity2") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity2 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity3") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity3 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity4") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity4 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity5") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity5 ?: 1.0f
        }
        .supplyVec3("Light0_Direction") { RenderSystem.shaderLightDirections[0] }
        .supplyVec3("Light1_Direction") { RenderSystem.shaderLightDirections[1] }
        .supplyVec3("teraTint") {
            it.instance().instanceOrNull<MinecraftObjectInstance>()?.teraTint?.takeIf { it != ZERO } ?: ONE
        }

        .prePostDraw({ material ->
            if (material.cullType() != CullType.None) {
                RenderSystem.enableCull()
            } else {
                RenderSystem.disableCull()
            }

            if (material.blendType() == BlendType.Regular) {
                RenderSystem.enableBlend()
                RenderSystem.defaultBlendFunc()
            }
        }, {/* material ->
                if (material.blendType() == BlendType.Regular) {
                    RenderSystem.disableBlend()
                }
            */
        })
        .shader(manager, "shaders/$category/terastal.vs.glsl", "shaders/$category/terastal.fs.glsl")
        .build()


    fun createRegular(manager: ResourceManager, category: String): Pipeline = Pipeline.Builder()
        .supplyBooleanUniform("legacy") { useLegacy }
        .supplyEnumUniform("FogShape", RenderSystem.getShaderFogShape())
        .supplyMat4("viewMatrix") { MatrixCache.viewMatrix }
        .supplyMat4("modelMatrix") { it.instance().modelMatrix() }
        .supplyMat3("normalMatrix") { it.instance().normalMatrix() }
        .supplyMat4("projectionMatrix") { MatrixCache.projectionMatrix }
        .supplyVec2("uvOffset") { it.transform.offset() ?: Transform.DEFAULT_OFFSET }
        .supplyVec2("uvScale") { it.transform.scale() ?: Transform.DEFAULT_SCALE }
        .supplyMat4s("boneTransforms") { ctx ->
            ctx.instance().instanceOrNull<AnimatedObjectInstance>()?.transforms ?: AnimationController.NO_ANIMATION
        }

        .supplyColorArray("ColorModulator") { RenderSystem.getShaderColor() }
        .supplyFloatUniform("FogStart") { RenderSystem.getShaderFogStart() }
        .supplyFloatUniform("FogEnd") { RenderSystem.getShaderFogEnd() }
        .supplyColorArray("FogColor") { RenderSystem.getShaderFogColor() }

        .supplyTexture("diffuse", 0) {
            it.instance()
                .instanceOrNull<StatueInstance>()?.material?.let { TextureLoader.getTextureOrNull(it) }
                ?: it.getTextureOrOther({ it.material.images().diffuse }) { ITextureLoader.instance().nuetralFallback }
        }
        .supplyTexture(
            "mask",
            1
        ) { it.getTextureOrOther({ it.material.images().mask }) { ITextureLoader.instance().darkFallback } }
        .supplyTexture(
            "layer",
            2
        ) { it.getTextureOrOther({ it.material.images().layer }) { ITextureLoader.instance().darkFallback } }
        .supplyTexture("lightmap", 3) { Minecraft.getInstance().gameRenderer.lightTexture() as ITexture }
        .supplyTexture(
            "emission",
            4
        ) { it.getTextureOrOther({ it.material.images().emission }) { ITextureLoader.instance().darkFallback } }
        .supplyTexture("paradoxMask", 5) { ITextureLoader.instance().getTexture("paradox_mask") }

        .supplyInt("colorMethod") { it.material.colorMethod }
        .supplyInt("effect") { it.material.effect }

        .supplyUniform("light") { ctx: UniformUploadContext ->
            val light = (ctx.instance() as BlockLightValueProvider).light
            ctx.uniform().upload2i(light and 0xFFFF, light shr 16 and 0xFFFF)
        }

        .supplyVec3("tint") { it.instance().instanceOrNull<MinecraftObjectInstance>()?.tint?.takeIf { it != ZERO } ?: ONE }

        .supplyInt("frame") { pingpong(MinecraftClientGameProvider.timePassed).toInt() }

        .supplyVec3("baseColor1") { ctx ->
            ctx.instance().instanceOrNull<TintProvider>()?.tint
                ?: ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor1 ?: ONE
        }
        .supplyVec3("baseColor2") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor2 ?: ONE }
        .supplyVec3("baseColor3") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor3 ?: ONE }
        .supplyVec3("baseColor4") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor4 ?: ONE }
        .supplyVec3("baseColor5") { ctx -> ctx.takeIf { !it.isStatueMaterial }?.material?.values()?.baseColor5 ?: ONE }
        .supplyVec3("emiColor1") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor1 ?: ONE }
        .supplyVec3("emiColor2") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor2 ?: ONE }
        .supplyVec3("emiColor3") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor3 ?: ONE }
        .supplyVec3("emiColor4") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor4 ?: ONE }
        .supplyVec3("emiColor5") { it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiColor5 ?: ONE }
        .supplyFloatUniform("emiIntensity1") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity1 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity2") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity2 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity3") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity3 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity4") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity4 ?: 0.0f
        }
        .supplyFloatUniform("emiIntensity5") {
            it.takeIf { !it.isStatueMaterial }?.material?.values()?.emiIntensity5 ?: 1.0f
        }
        .supplyBooleanUniform("useLight") { it.material.values().useLight }

        .supplyVec3("Light0_Direction") { RenderSystem.shaderLightDirections[0] }
        .supplyVec3("Light1_Direction") { RenderSystem.shaderLightDirections[1] }

        .prePostDraw({ material ->
            if (material.cullType() != CullType.None) {
                RenderSystem.enableCull()
            } else {
                RenderSystem.disableCull()
            }

            if (material.blendType() == BlendType.Regular) {
                RenderSystem.enableBlend()
                RenderSystem.defaultBlendFunc()
            }
        }, {/* material ->
                if (material.blendType() == BlendType.Regular) {
                    RenderSystem.disableBlend()
                }
            */
        })
        .shader(manager, "shaders/$category/regular.vs.glsl", "shaders/$category/regular.fs.glsl")
        .build()


    fun Pipeline.Builder.shader(manager: ResourceManager, vertex: String, fragment: String): Pipeline.Builder =
        shader(read(manager, vertex.id()), read(manager, fragment.id()))


    fun pingpong(time: Double): Double = (sin(time * Math.PI * 2) * 7 + 7).toInt().toDouble()

    fun Pipeline.Builder.supplyColorArray(
        name: String,
        function: (UniformUploadContext) -> FloatArray
    ): Pipeline.Builder = this.supplyUniform(name) {
        val color = function.invoke(it)
        it.uniform().upload4f(color[0], color[1], color[2], color[3])
    }

    fun Pipeline.Builder.supplyMat4s(
        name: String,
        function: (UniformUploadContext) -> Array<Matrix4f>
    ): Pipeline.Builder = this.supplyUniform(name) { it.uniform().uploadMat4fs(function.invoke(it)) }

    fun Pipeline.Builder.supplyMat4(
        name: String,
        function: (UniformUploadContext) -> Matrix4f
    ): Pipeline.Builder = this.supplyUniform(name) { it.uniform().uploadMat4f(function.invoke(it)) }

    fun Pipeline.Builder.supplyMat3(
        name: String,
        function: (UniformUploadContext) -> Matrix3f
    ): Pipeline.Builder = this.supplyUniform(name) { it.uniform().uploadMat3f(function.invoke(it)) }

    fun Pipeline.Builder.supplyVec2(
        name: String,
        function: (UniformUploadContext) -> Vector2f
    ): Pipeline.Builder = this.supplyUniform(name) { it.uniform().uploadVec2f(function.invoke(it)) }

    fun Pipeline.Builder.supplyVec3(
        name: String,
        function: (UniformUploadContext) -> Vector3f
    ): Pipeline.Builder = this.supplyUniform(name) { it.uniform().uploadVec3f(function.invoke(it)) }

    fun Pipeline.Builder.supplyFloatUniform(
        name: String,
        function: (UniformUploadContext) -> Float
    ): Pipeline.Builder = this.supplyUniform(name) { it.uniform().uploadFloat(function.invoke(it)) }

    fun Pipeline.Builder.supplyEnumUniform(name: String, value: Enum<*>): Pipeline.Builder =
        this.supplyInt(name) { value.ordinal }

    fun Pipeline.Builder.supplyInt(name: String, function: (UniformUploadContext) -> Int): Pipeline.Builder =
        this.also { this.supplyUniform(name) { it.uniform().uploadInt(function.invoke(it)) } }

    public inline fun <reified T> Any?.instanceOrNull(): T? = this as? T

    val UniformUploadContext.isStatueMaterial: Boolean
        get() = statueMaterial != null

    private val UniformUploadContext.statueMaterial: String?
        get() = this.instance().instanceOrNull<StatueInstance>()?.material?.takeIf { TextureLoader.has(it) }

    val UniformUploadContext.transform: Transform
        get() = this.instance().instanceOrNull<AnimatedObjectInstance>()?.getTransform(this.material.materialName)
            ?.takeIf { !it.isUnit } ?: this.`object`().getTransform(this.instance().variant())


    fun UniformUploadContext.getTextureOrOther(
        function: (UniformUploadContext) -> String?,
        supplier: () -> ITexture
    ): ITexture = TextureLoader.getTexture(function.invoke(this))
        ?.takeUnless { texture -> texture === TextureLoader.MissingTextureProxy } ?: supplier.invoke()

    fun Pipeline.Builder.supplyTexture(
        name: String,
        slot: Int,
        function: (UniformUploadContext) -> ITexture
    ): Pipeline.Builder = this.supplyUniform(name) {
        function.invoke(it).bind(slot)
        it.uniform().uploadInt(slot)
    }

    fun Pipeline.Builder.supplyBooleanUniform(
        name: String,
        function: (UniformUploadContext) -> Boolean
    ): Pipeline.Builder {
        return this.supplyUniform(name) { it.uniform().uploadBoolean(function.invoke(it)) }
    }

    fun read(manager: ResourceManager, name: ResourceLocation): String {
        try {
            manager.getResource(name).orElseThrow().open().use { `is` ->
                return String(`is`.readAllBytes())
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to read shader from resource location in shader: $name", e)
        }
    }
}