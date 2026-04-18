package generations.gg.rarecandylib.iris.client

import generations.gg.rarecandylib.common.client.RareCandyLibClient

object IrisModule {
//    var shaderImpl: ShaderSet? = null
//    private val loggedEvents = mutableSetOf<String>()
//    private var requestedPackName: String? = null
//
//    val shader: ShaderSet?
//        get() = computerShaderSet()

//    private fun computerShaderSet(): ShaderSet? {
//        if (!initialized) {
//            Minecraft.getInstance().resourceManager?.also {
//                shaderImpl = ShaderSet.create(Minecraft.getInstance().resourceManager, "iris/$packName")
//                initialized = true
//                RareCandyLibClient.LOGGER.info(
//                    "Iris shader init: requestedPack={}, mappedPack={}, shaderLoaded={}",
//                    requestedPackName,
//                    packName,
//                    shaderImpl != null
//                )
//            }
//        }

//        return shaderImpl
//    }

//    private var initialized = false
//    private var packName: String? = null

    fun initialize() {
//        RareCandyLibClient.isIrisRenderingImpl = { IrisApi.getInstance().isShaderPackInUse }
//        Pipelines.alternateShaderProvider = { shader }
        RareCandyLibClient.LOGGER.info("RareCandyLib Iris Module Enabled.")
    }

//    @JvmStatic
//    fun loadShader(currentPackName: String) {
//        shader?.destroy()
//
//        requestedPackName = currentPackName
//        loggedEvents.clear()
//        initialized = true
//        packName = null
//        if (currentPackName.contains("SuperDuper", true)) {
//            packName = "superduper"
//
//            initialized = false
//        }
//
//        RareCandyLibClient.LOGGER.info(
//            "Iris shader pack load: requestedPack={}, mappedPack={}, supported={}",
//            requestedPackName,
//            packName,
//            packName != null
//        )

//        Minecraft.getInstance().resourceManager?.also {
//            shaderImpl = ShaderSet.create(Minecraft.getInstance().resourceManager, "iris/$packName")
//            initialized = true
//            RareCandyLibClient.LOGGER.info(
//                "Iris shader pack ready: requestedPack={}, mappedPack={}, shaderLoaded={}",
//                requestedPackName,
//                packName,
//                shaderImpl != null
//            )
//        }
//    }

//    @JvmStatic
//    fun firstPass(pipeline: IrisRenderingPipeline) {
//        runPass("first", pipeline, RareCandyLibClient::firstRenderPass)
//    }

//    @JvmStatic
//    fun secondPass(pipeline: IrisRenderingPipeline) {
//        runPass("second", pipeline, RareCandyLibClient::secondRenderPass)
//    }

//    private fun runPass(passName: String, pipeline: IrisRenderingPipeline, renderPass: () -> Unit) {
//        val currentShader = shader ?: run {
//            logOnce("missing-shader:$passName:${requestedPackName}:${packName}") {
//                RareCandyLibClient.LOGGER.warn(
//                    "Iris pass skipped: pass={}, requestedPack={}, mappedPack={}, reason=shader-null",
//                    passName,
//                    requestedPackName,
//                    packName
//                )
//            }
//            return
//        }

//        val frameBuffer = IrisFbo.getFbo(pipeline)
//        if (frameBuffer == null) {
//            logOnce("missing-fbo:$passName:${shaderKey(pipeline)}:${requestedPackName}:${packName}") {
//                RareCandyLibClient.LOGGER.warn(
//                    "Iris pass skipped: pass={}, requestedPack={}, mappedPack={}, beforeTranslucent={}, key={}, reason=fbo-null, shaderHash={}",
//                    passName,
//                    requestedPackName,
//                    packName,
//                    pipeline.isBeforeTranslucent,
//                    shaderKey(pipeline),
//                    currentShader.hashCode()
//                )
//            }
//            return
//        }
//
//        frameBuffer.bind()
//        logFramebuffer(passName, pipeline, frameBuffer, currentShader)
//        renderPass()
//    }
//
//    private fun logFramebuffer(passName: String, pipeline: IrisRenderingPipeline, frameBuffer: net.irisshaders.iris.gl.framebuffer.GlFramebuffer, shaderSet: ShaderSet) {
//        val key = shaderKey(pipeline)
//        val renderTargets = (pipeline as IrisRenderingPipelineExt).getRenderTargets()
//        val drawBinding = GL40.glGetInteger(GL40.GL_DRAW_FRAMEBUFFER_BINDING)
//        val readBinding = GL40.glGetInteger(GL40.GL_READ_FRAMEBUFFER_BINDING)
//        val drawStatus = GL40.glCheckFramebufferStatus(GL40.GL_DRAW_FRAMEBUFFER)
//        val depthAttachmentType = GL40.glGetFramebufferAttachmentParameteri(
//            GL40.GL_DRAW_FRAMEBUFFER,
//            GL40.GL_DEPTH_ATTACHMENT,
//            GL40.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE
//        )
//        val depthAttachmentObject = GL40.glGetFramebufferAttachmentParameteri(
//            GL40.GL_DRAW_FRAMEBUFFER,
//            GL40.GL_DEPTH_ATTACHMENT,
//            GL40.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME
//        )
//        val colorAttachments = (0..3).joinToString(", ") { index ->
//            "c$index=${frameBuffer.getColorAttachment(index)}"
//        }
//
//        logOnce("fbo-debug:$passName:$key:${frameBuffer.getId()}:$drawBinding:$readBinding") {
//            RareCandyLibClient.LOGGER.info(
//                "Iris FBO debug: pass={}, requestedPack={}, mappedPack={}, beforeTranslucent={}, key={}, fboId={}, drawBinding={}, readBinding={}, fboStatus={}, drawStatus={}, hasDepth={}, depthAttachmentType={}, depthAttachmentObject={}, currentDepthTex={}, noTranslucentsDepthTex={}, depthTestEnabled={}, shaderHash={}, {}",
//                passName,
//                requestedPackName,
//                packName,
//                pipeline.isBeforeTranslucent,
//                key,
//                frameBuffer.getId(),
//                drawBinding,
//                readBinding,
//                frameBuffer.getStatus(),
//                drawStatus,
//                frameBuffer.hasDepthAttachment(),
//                attachmentTypeName(depthAttachmentType),
//                depthAttachmentObject,
//                renderTargets.depthTexture,
//                renderTargets.depthTextureNoTranslucents.textureId,
//                GL40.glIsEnabled(GL40.GL_DEPTH_TEST),
//                shaderSet.hashCode(),
//                colorAttachments
//            )
//        }
//    }
//
//    private fun shaderKey(pipeline: IrisRenderingPipeline): String =
//        if (pipeline.isBeforeTranslucent) "ENTITIES_SOLID" else "ENTITIES_TRANSLUCENT"
//
//    private fun attachmentTypeName(type: Int): String = when (type) {
//        GL40.GL_NONE -> "GL_NONE"
//        GL40.GL_TEXTURE -> "GL_TEXTURE"
//        GL40.GL_RENDERBUFFER -> "GL_RENDERBUFFER"
//        else -> "UNKNOWN"
//    }
//
//    private inline fun logOnce(key: String, block: () -> Unit) {
//        if (loggedEvents.add(key)) {
//            block()
//        }
//    }
}
