package generations.gg.rarecandylib.iris.client.shaderpacks

import com.mojang.blaze3d.systems.RenderSystem
import generations.gg.rarecandylib.common.client.GenerationsTextureLoader
import generations.gg.rarecandylib.common.client.MatrixCache
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.TintProvider
import generations.gg.rarecandylib.common.client.render.rarecandy.BlockLightValueProvider
import generations.gg.rarecandylib.common.client.render.rarecandy.CobblemonInstance
import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftClientGameProvider
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.ONE
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.ZERO
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.getTextureOrOther
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.instanceOrNull
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.isStatueMaterial
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.pingpong
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.shader
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyBooleanUniform
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyColorArray
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyEnumUniform
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyFloatUniform
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyInt
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyMat3
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyMat4
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyMat4s
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyTexture
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyVec2
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.supplyVec3
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.transform
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.useLegacy
import generations.gg.rarecandylib.common.client.render.rarecandy.StatueInstance
import gg.generations.rarecandy.pokeutils.BlendType
import gg.generations.rarecandy.pokeutils.CullType
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.animation.AnimationController
import gg.generations.rarecandy.renderer.animation.Transform
import gg.generations.rarecandy.renderer.loading.ITexture
import gg.generations.rarecandy.renderer.pipeline.Pipeline
import gg.generations.rarecandy.renderer.pipeline.UniformUploadContext
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import net.minecraft.client.Minecraft
import net.minecraft.server.packs.resources.ResourceManager
import kotlin.takeIf

object Complementary {
    object Reimagined {
        val shader: (ResourceManager) -> Pair<Pipeline, Pipeline> = { createRegular(it).let { pipeline -> pipeline to pipeline } }

        fun createRegular(manager: ResourceManager): Pipeline = Pipeline.Builder()
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

            .supplyTexture("diffuse", 0) {
                it.instance()
                    .instanceOrNull<StatueInstance>()?.material?.let { GenerationsTextureLoader.getTextureOrNull(it) }
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

            .supplyVec3("tint") { it.instance().instanceOrNull<CobblemonInstance>()?.tint?.takeIf { it != ZERO } ?: ONE }

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
            .shader(manager, "shaders/iris/complementary_reimagined/regular.vs.glsl", "shaders/iris/complementary_reimagined/regular.fs.glsl")
            .build()

    }

}
