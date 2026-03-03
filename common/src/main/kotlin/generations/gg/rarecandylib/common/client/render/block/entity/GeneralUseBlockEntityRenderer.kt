package generations.gg.rarecandylib.common.client.render.block.entity

import com.mojang.blaze3d.vertex.PoseStack
import generations.gg.rarecandylib.common.block.GenericModelBlock
import generations.gg.rarecandylib.common.client.model.InstanceProvider
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.AngleProvider
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.FrameProvider
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.ModelProvider
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.TintProvider
import generations.gg.rarecandylib.common.client.model.ModelContextProviders.VariantProvider
import generations.gg.rarecandylib.common.client.render.rarecandy.*
import generations.gg.rarecandylib.common.client.render.rarecandy.ModelRegistry.prepForBER
import generations.gg.rarecandylib.common.client.render.rarecandy.animation.FixedFrameAnimationInstance
import generations.gg.rarecandylib.common.client.render.rarecandy.ModelRegistry
import generations.gg.rarecandylib.common.client.render.rarecandy.Pipelines.instanceOrNull
import generations.gg.rarecandylib.common.util.set
import gg.generations.rarecandy.renderer.animation.AnimationInstance
import gg.generations.rarecandy.renderer.rendering.ObjectInstance
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3

open class GeneralUseBlockEntityRenderer<T>(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<T> where T : BlockEntity, T: VariantProvider, T: ModelProvider, T: InstanceProvider {


    override fun render(
        blockEntity: T,
        partialTick: Float,
        stack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {

        blockEntity.blockState.block.instanceOrNull<GenericModelBlock<T>>()?.takeIf { it.canRender(blockEntity) } ?: return

        if (blockEntity.instanceArray == null) {
            val amount = instanceAmount()
            blockEntity.instanceArray = arrayOfNulls(amount)

            for (i in 0 until amount) blockEntity.instanceArray!![i] = blockEntity.generateInstance()
        }

        stack.pushPose()
        if(blockEntity is AngleProvider) prepForBER(stack, blockEntity)
        renderModels(stack, bufferSource, blockEntity, packedLight)
        stack.popPose()
    }

    protected open fun renderModels(
        stack: PoseStack,
        buffersource: MultiBufferSource,
        blockEntity: T,
        packedLight: Int
    ) {
        if (blockEntity.isAnimated) renderModelFrameProvider(stack, buffersource, blockEntity, packedLight)
        else renderModelProvider(stack, buffersource, blockEntity, packedLight)
    }

    protected fun renderModelProvider(
        stack: PoseStack,
        buffersource: MultiBufferSource,
        blockEntity: T,
        packedLight: Int
    ) {
        val model = ModelRegistry[blockEntity]

        if (model?.renderObject == null) return

        stack.scale(model.renderObject!!.scale, model.renderObject!!.scale, model.renderObject!!.scale)

        val variant = blockEntity.variant

        blockEntity.instanceArray!!.requireNoNulls().forEach { instance ->
            if (instance.materialId() != variant) {
                instance.setVariant(variant)
            }

            instance.set(stack.last())

            (instance as BlockObjectInstance).light = packedLight
            if (blockEntity is TintProvider) instance.tint = blockEntity.tint
            model.render(instance, buffersource)
        }
    }

    protected open fun instanceAmount(): Int {
        return 1
    }

    protected fun renderModelFrameProvider(
        stack: PoseStack,
        buffersource: MultiBufferSource,
        blockEntity: T,
        packedLight: Int
    ) {
        //TODO: Get this operational
        val model = ModelRegistry[blockEntity]

        if (model?.renderObject == null) return

        stack.scale(model.renderObject!!.scale, model.renderObject!!.scale, model.renderObject!!.scale)

        val primeInstance = blockEntity.instanceArray!![0]!!

        if (model.renderObject!!.isReady) {
            primeInstance.link(model.renderObject)

            val animationInstance = (primeInstance as AnimatedObjectInstance)
            val animation = animationInstance.animationsIfAvailable[blockEntity.animation]

            if (animation != null) {
                if (blockEntity is FrameProvider) {
                    animationInstance.changeAnimation(FixedFrameAnimationInstance(animation, blockEntity.frame))
                } else {
                    animationInstance.changeAnimation(AnimationInstance(animation))
                }
            }
        }

        val offset = blockEntity.blockPos.toVec3d().subtract(Minecraft.getInstance().cameraEntity!!.position())

        stack.translate(offset.x.toFloat(),
            offset.y.toFloat(), offset.z.toFloat())

        primeInstance.set(stack.last())
        
        (primeInstance as BlockLightValueProvider).light = packedLight
        if (blockEntity is TintProvider) (primeInstance as BlockAnimatedObjectInstance).tint = blockEntity.tint

        val instance = primeInstance as AnimatedObjectInstance

        val provider = blockEntity.instanceOrNull<FrameProvider>()

        if(provider != null) instance.currentAnimation?.instanceOrNull<FixedFrameAnimationInstance>()?.takeIf { it.currentTime != provider.frame }?.run { this.currentTime = provider.frame }

        model.render(instance, buffersource)
    }

    protected fun renderResourceLocation(
        source: MultiBufferSource,
        location: ResourceLocation,
        stack: PoseStack,
        objectInstance: ObjectInstance
    ) {
        objectInstance.set(stack.last())

        val model = ModelRegistry[location]
        model?.render(objectInstance, source)
    }

    override fun shouldRenderOffScreen(blockEntity: T): Boolean {
        return true
    }
}

private fun BlockPos.toVec3d(): Vec3 {
    return Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}
