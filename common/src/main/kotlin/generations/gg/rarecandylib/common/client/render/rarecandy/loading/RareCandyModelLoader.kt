package generations.gg.rarecandylib.common.client.render.rarecandy.loading

import generations.gg.rarecandylib.common.client.render.rarecandy.MinecraftObjectInstance
import generations.gg.rarecandylib.common.client.render.rarecandy.CompiledModel
import gg.generations.rarecandy.pokeutils.MaterialReference
import gg.generations.rarecandy.pokeutils.MeshOptions
import gg.generations.rarecandy.pokeutils.ModelConfig
import gg.generations.rarecandy.pokeutils.ModelNode
import gg.generations.rarecandy.pokeutils.SkeletalTransform
import gg.generations.rarecandy.pokeutils.VariantDetails
import gg.generations.rarecandy.pokeutils.resource.ResourceReader
import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.animation.Skeleton
import gg.generations.rarecandy.renderer.animation.TransformSet
import gg.generations.rarecandy.renderer.components.InstanceDetails
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.loading.AnimResource
import gg.generations.rarecandy.renderer.loading.GfbanmResource
import gg.generations.rarecandy.renderer.loading.ModelLoader
import gg.generations.rarecandy.renderer.loading.SbboOffset
import gg.generations.rarecandy.renderer.loading.SmdResource
import gg.generations.rarecandy.renderer.loading.TrAnimationResource
import gg.generations.rarecandy.renderer.model.Variant
import gg.generations.rarecandy.renderer.model.material.Material
import gg.generations.rarecandy.renderer.rendering.RenderStage
import gg.generations.rarecandy.renderer.storage.DrawBuffer
import gg.generations.rarecandy.renderer.storage.SSBOBuffer
import gg.generations.rarecandy.renderer.textures.TextureArray
import net.minecraft.server.packs.resources.Resource
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.PointerBuffer
import org.lwjgl.assimp.AIBone
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.AIScene
import org.lwjgl.assimp.AIVector3D
import org.lwjgl.assimp.Assimp
import org.lwjgl.opengl.ARBShaderStorageBufferObject.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT
import org.lwjgl.opengl.GL43C
import org.lwjgl.system.MemoryUtil
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.collections.forEach

object RareCandyModelLoader {
    private val temp = Vector3f()
    private val TRANSFORM_ENTRY_BYTES = 32
    private val NORMAL_FACE = intArrayOf(0, 1, 2)
    private val INVERT_FACE = intArrayOf(2, 1, 0)
    private val N = Vector3f()
    private val T = Vector3f()
    private val B = Vector3f()
    private val TEMP = Vector3f()
    fun createObject(
        model: CompiledModel,
        stream: Resource
    ): MultiRenderObject {
        return ModelLoader.createObject(
            { CompiledModel.Model(it) },
            { CompiledModel.Reader(stream) },
            ModelLoader::readImages,
            MaterialReference::process,

            { `object`: MultiRenderObject ->
                model.guiInstance = MinecraftObjectInstance(Matrix4f(), Matrix3f(), 0).also { it.link(`object`) }
                if (`object`.scale == 0f) `object`.scale = 1.0f
            }
        )
    }

    @Throws(IOException::class)
    fun processModel(
        objects: MultiRenderObject,
        names: ModelLoader.Names,
        asset: ResourceReader,
        animResources: Map<String, AnimResource>,
        config: ModelConfig?
    ) {
        if (config == null) {
            throw RuntimeException("config.json can't be null.")
        } else {
            val scene = ModelLoader.read(asset)
            val rootNode = ModelNode.create(scene.mRootNode())
            val meshes = 0.rangeUntil(scene.mNumMeshes()).map { ix -> AIMesh.create(scene.mMeshes()!!.get(ix)) }.toTypedArray()
            val skeleton = Skeleton(rootNode, meshes, config.excludeMeshNamesFromSkeleton)
            processAnimations(objects, scene, skeleton, animResources, names, config)
            val dimensions = Vector3f()
            var vertexCount = 0
            var indexCount = 0
            val alignment = GL43C.glGetInteger(GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT)
            var maxVertex = 0
//
            for(mesh in meshes) {
                maxVertex = maxVertex.coerceAtLeast(mesh.mNumFaces() * 3)
                vertexCount += mesh.mNumVertices()
                indexCount += mesh.mNumFaces() * 3
            }

            objects.maxVertex = maxVertex
            val indexBytes = indexCount * 4
            val drawBytes = meshes.size * 4
            val vertexBytes = vertexCount * 96
            val materialBytes = objects.materials.size * 208
            val variantBytes = objects.variants.size * 80
            val indexOffset = alignUp(vertexBytes, alignment)
            val drawOffset = indexOffset + alignUp(indexBytes, alignment)
            val materialOffset = drawOffset + alignUp(drawBytes, alignment)
            val variantOffset = materialOffset + alignUp(materialBytes, alignment)
            val totalBytes = variantOffset + variantBytes
            val vertexBuffer = MemoryUtil.memAlloc(vertexBytes)
            val indexBuffer = MemoryUtil.memAlloc(indexBytes)
            val drawBuffer = MemoryUtil.memAlloc(drawBytes)
            val materialBuffer = MemoryUtil.memAlloc(materialBytes)
            val variantBuffer = MemoryUtil.memAlloc(variantBytes)
            objects.vertex = SbboOffset(0, vertexBytes)
            objects.index = SbboOffset(indexOffset, indexBytes)
            objects.meshOffsets = SbboOffset(drawOffset, drawBytes)
            objects.material = SbboOffset(materialOffset, materialBytes)
            objects.variant = SbboOffset(variantOffset, variantBytes)
            val counters = IntArray(2)
            val list = meshes.filter { a -> objects.meshNameToId.containsKey(a.mName().dataString()) }.sortedBy { aiMesh -> objects.meshNameToId[aiMesh.mName().dataString()] }

            for (i in list.indices) {
                val mesh = list[i]
                objects.meshes[i] = processPrimitiveModel(vertexBuffer, indexBuffer, drawBuffer, counters, skeleton, mesh, config.modelOptions ?: emptyMap(), dimensions)
            }

            for (material in objects.materials) {
                material.put(materialBuffer)
            }


            for(variant in objects.variants) {
                variant.put(variantBuffer)
            }

            val buffer = MemoryUtil.memAlloc(totalBytes)
            objects.vertex.put(buffer, vertexBuffer.flip())
            objects.index.put(buffer, indexBuffer.flip())
            objects.meshOffsets.put(buffer, drawBuffer.flip())
            objects.material.put(buffer, materialBuffer.flip())
            objects.variant.put(buffer, variantBuffer.flip())
            val bufferId = GL43C.glGenBuffers()
            GL43C.glBindBuffer(37074, bufferId)
            GL43C.glBufferData(37074, buffer, 35045)
            GL43C.glBindBuffer(37074, 0)
            objects.modelBuffer = bufferId
            objects.drawInfoBuffer = SSBOBuffer(TRANSFORM_ENTRY_BYTES * objects.meshes.size)
            objects.instanceBuffer = SSBOBuffer(InstanceDetails.size)
            MemoryUtil.memFree(buffer)
            MemoryUtil.memFree(vertexBuffer)
            MemoryUtil.memFree(indexBuffer)
            MemoryUtil.memFree(drawBuffer)
            MemoryUtil.memFree(materialBuffer)
            MemoryUtil.memFree(variantBuffer)
            val transform = Matrix4f()
            traverseTree(transform, rootNode, objects)
            Assimp.aiReleaseImport(scene)
        }
    }

    private fun processAnimations(obj: MultiRenderObject, scene: AIScene, skeleton: Skeleton, animResources: Map<String, AnimResource>, names: ModelLoader.Names, config: ModelConfig) {
        obj.animations = arrayOfNulls(animResources.size)
        obj.hideDuringAnimation = Array(obj.meshes.size) { BooleanArray(obj.animations.size) }
        obj.animationNameToId = mutableMapOf()
        obj.animationNames = arrayOfNulls(obj.animations.size)
        val offSetsToInsert = mutableMapOf<String, Animation.Offset>()
        animResources.onEachIndexed { i, entry ->
            val name = entry.key
            val animResource = entry.value
            var fps = animResource.fps()
            fps = if(config.animationFpsOverride != null && config.animationFpsOverride.containsKey(name)) config.animationFpsOverride[name]!!.toLong() else fps
            var loops = animResource.loops()
            loops = if(config.animationLoopsOverride != null && config.animationLoopsOverride.containsKey(name)) config.animationLoopsOverride[name]!! else loops
            val offsets = animResource.offsets
            offsets.forEach({ (trackName, offset) -> config.getMaterialsForAnimation(trackName).forEach({ offSetsToInsert[it] = offset }) })
            offsets.putAll(offSetsToInsert)
            offSetsToInsert.clear()
            val offsetsArray = arrayOfNulls<Animation.Offset>(names.materials.size)
            offsets.forEach { (s, offset) ->
                val id = names.materials.indexOf(s)
                if (id != -1) {
                    offsetsArray[id] = offset
                }
            }

            val nodes = animResource.nodes
            val ignoreScaling = config.ignoreScaleInAnimation != null && (config.ignoreScaleInAnimation.contains(name) || config.ignoreScaleInAnimation.contains("all"))
            obj.animations[i] = Animation(i, fps.toInt(), loops, skeleton, nodes, offsetsArray, ignoreScaling, config.offsets.getOrDefault(name, SkeletalTransform()).scale(config.scale))
            obj.animationNameToId[name] = i
            obj.animationNames[i] = name
        }

        names.meshes.onEachIndexed { mesh, meshName ->
            val defaultAnimation = config.hideDuringAnimation.getOrDefault(meshName, ModelConfig.HideDuringAnimation.NONE)

            obj.animationNames.forEachIndexed { animation, animationName ->
                obj.hideDuringAnimation[mesh][animation] = defaultAnimation.check(animationName)
            }
        }

    }

    private fun processVariants(obj: MultiRenderObject, config: ModelConfig, names: ModelLoader.Names, aliases: Map<String, List<String>>) {
        val defaultVariant = IntArray(names.meshes.size)
        val variantList = mutableListOf<Variant>()
        config.defaultVariant.forEach { k, v ->
            var mesh = names.meshes.indexOf(k)
            val effect = v.effect()?.let {
                when (it) {
                    "galaxy" -> 1
                    "pastel" -> 2
                    "shadow" -> 3
                    "sketch" -> 4
                    "vintage" -> 5
                    else -> 0
                }
            } ?: 0

            val paradox = v.paradox() == true
            val hide = v.hide() == true
            val variant = addOrGetIndex(variantList, Variant(names.materials.indexOf(v.material()), effect, paradox, hide, (v.transform() ?: TransformSet.DEFAULT).array()))

            if (!aliases.isEmpty() && aliases.containsKey(k)) {
                for (s in aliases.get(k)!!) {
                    mesh = names.meshes.indexOf(s)
                    defaultVariant[mesh] = variant
                }
            } else {
                defaultVariant[mesh] = variant
            }

        }
        config.variants?.also { variants ->
            variants.forEach { (variantKey, variantParent) ->
                    val variantIndex = names.variants.indexOf(variantKey)
                var child = config.variants[variantParent.inherits()]

                var map = variantParent.details

                    while (child != null) {
                        val details = child.details
                        child = if (variantKey == child.inherits) null else variants.get(child.inherits())
                        applyVariantDetails(details, map)
                    }

                    applyVariantDetails(config.defaultVariant, map)
                applyVariant(obj, variantIndex, map, aliases, names, variantList)
            }

        } ?: let {
            for((mesh, element) in defaultVariant.withIndex()) {
                val variant = element
                set(obj, mesh, 0, variant, variantList)
            }
        }

        obj.variants = variantList.toTypedArray()
    }

    private fun traverseTree(transform: Matrix4f, node: ModelNode, objs: MultiRenderObject) {
        applyTransforms(transform, node)
        objs.rootTransformation = objs.rootTransformation.add(transform, Matrix4f())

        for(child in node.children) {
            traverseTree(transform, child, objs)
        }

    }

    private fun applyVariantDetails(applied: Map<String, VariantDetails>, appliee: MutableMap<String, VariantDetails>) {
        applied.forEach { (k, v) -> appliee.compute(k) { s, variantDetails -> variantDetails?.fillIn(v) ?: v }
        }

    }

    private fun applyVariant(obj: MultiRenderObject, variantKey: Int, variantMap: Map<String, VariantDetails>, aliases: Map<String, List<String>>, names: ModelLoader.Names, variants: MutableList<Variant>) {
        variantMap.forEach {(k, v) ->
            var mesh = names.meshes.indexOf(k)
            val mat = names.materials.indexOf(v.material())
            val hide = v.hide() == true
            val paradox = v.paradox() == true
            val transform = v.transform() ?: TransformSet.DEFAULT
            val effect = v.effect()?.let {
                when (it) {
                    "galaxy" -> 1
                    "pastel" -> 2
                    "shadow" -> 3
                    "sketch" -> 4
                    "vintage" -> 5
                    else -> 0
                }
            } ?: 0

            val variant = addOrGetIndex(variants, Variant(mat, effect, paradox, hide, transform.array()))
            if (!aliases.isEmpty() && aliases.containsKey(k)) {
                for (s in aliases[k]!!) {
                    mesh = names.meshes.indexOf(s)
                    set(obj, mesh, variantKey, variant, variants)
                }
            } else {
                set(obj, mesh, variantKey, variant, variants)
            }

        }
    }

    private fun set(obj: MultiRenderObject, mesh: Int, variantKey: Int, variant: Int, variants: MutableList<Variant>) {
        val material = obj.materials[(variants[variant]).material()]
        val stage = RenderStage.from(material)
        obj.drawBuffer.computeIfAbsent(stage, { DrawBuffer(16 * obj.meshes.size) })
        obj.stageRelationships[mesh][variantKey] = stage
        obj.variantRelationships[mesh][variantKey] = variant
    }

    private fun addOrGetIndex(variants: MutableList<Variant>, variant: Variant): Int {
        var index = variants.indexOf(variant)
        if (index == -1) {
            index = variants.size
            variants.add(variant)
        }

        return index
    }

    private fun applyTransforms(transform: Matrix4f, node: ModelNode) {
        transform.set(node.transform)
    }

    private fun <T> PointerBuffer.forEach(function: (Int) -> T, block: T.() -> Unit) {
        for (i in 0 until capacity()) function(i).apply(block)
    }

    private fun processPrimitiveModel(vertexBuffer: ByteBuffer, indexBuffer: ByteBuffer, drawBuffer: ByteBuffer, counters: IntArray, skeleton: Skeleton, mesh: AIMesh, options: Map<String, MeshOptions>, dimensions: Vector3f): Int {
        val name = mesh.mName().dataString()
        val faceArray = if (options.containsKey(name) && options[name]?.invert() == true) INVERT_FACE else NORMAL_FACE
        val amount = mesh.mNumVertices()
        val aiFaces = mesh.mFaces()
        val indexOffset = counters[0]
        val vertexOffset = counters[1]
        val numFaces = mesh.mNumFaces()
        val drawRecord = numFaces * 3
        drawBuffer.putInt(indexOffset)
        counters[0] += drawRecord

        for (j in 0 until numFaces) {
            val aiFace = aiFaces.get(j).mIndices()
            indexBuffer.putInt(vertexOffset + aiFace.get(faceArray[0])).putInt(vertexOffset + aiFace.get(faceArray[1]))
                .putInt(vertexOffset + aiFace.get(faceArray[2]))
        }

        counters[1] += amount
        val aiVert = mesh.mVertices()
        val aiUV = mesh.mTextureCoords(0) ?: throw RuntimeException("Error UV coordinates not found!")

        val aiNormals = mesh.mNormals() ?: throw RuntimeException("Error Normals not found!")
        val aiTangents = mesh.mTangents() ?: throw RuntimeException("Error Tangent not found!")
        val aiBitangents = mesh.mBitangents() ?: throw RuntimeException("Error Bitangent not found!")
        val ids = IntArray(amount * 4)
        val weights = FloatArray(amount * 4)
        mesh.mBones()?.forEach(AIBone::create) {
            val weight = mWeights()
            val index = skeleton.getId(mName().dataString())

            for (weightId in 0 until weight.capacity()) {
                val aiWeight = weight.get(weightId)
                val vertexId = aiWeight.mVertexId()
                if (aiWeight.mWeight() > 0.0F) {
                    addBoneData(ids, weights, vertexId, index, aiWeight.mWeight())
                }
            }
        }

        val isEmpty = ids.all { it == 0 }

        for (i in 0 until amount) {
            val position = aiVert.get(i)
            val uv = aiUV.get(i)
            val normal = aiNormals.get(i)
            val tangent = aiTangents.get(i)
            val bitangent = aiBitangents.get(i)

            vertexBuffer.putFloat(position.x())
            vertexBuffer.putFloat(position.y())
            vertexBuffer.putFloat(position.z())
            vertexBuffer.putFloat(0.0F)
            vertexBuffer.putFloat(uv.x())
            vertexBuffer.putFloat(1.0F - uv.y() % 1.0F)
            vertexBuffer.putFloat(0.0F)
            vertexBuffer.putFloat(0.0F)
            vertexBuffer.putFloat(normal.x())
            vertexBuffer.putFloat(normal.y())
            vertexBuffer.putFloat(normal.z())
            vertexBuffer.putFloat(0.0F)
            computeTangent(vertexBuffer, tangent, bitangent, normal)
            if (isEmpty) {
                vertexBuffer.putInt(1)
                vertexBuffer.putInt(0)
                vertexBuffer.putInt(0)
                vertexBuffer.putInt(0)
                vertexBuffer.putFloat(1.0F)
                vertexBuffer.putFloat(0.0F)
                vertexBuffer.putFloat(0.0F)
                vertexBuffer.putFloat(0.0F)
            } else {
                vertexBuffer.putInt(ids[i * 4])
                vertexBuffer.putInt(ids[i * 4 + 1])
                vertexBuffer.putInt(ids[i * 4 + 2])
                vertexBuffer.putInt(ids[i * 4 + 3])
                vertexBuffer.putFloat(weights[i * 4])
                vertexBuffer.putFloat(weights[i * 4 + 1])
                vertexBuffer.putFloat(weights[i * 4 + 2])
                vertexBuffer.putFloat(weights[i * 4 + 3])
            }

            dimensions.max(temp.set(position.x(), position.y(), position.z()))
        }

        return drawRecord
    }
//
    private fun computeTangent(vertexBuffer: ByteBuffer, tangent: AIVector3D, bitangent: AIVector3D, normal: AIVector3D) {
        N.set(normal.x(), normal.y(), normal.z())
    T.set(tangent.x(), tangent.y(), tangent.z())
    B.set(bitangent.x(), bitangent.y(), bitangent.z())
    N.mul(T.dot(N), TEMP)
    T.sub(TEMP).normalize()
    N.cross(T, TEMP)
    val handedness = if(TEMP.dot(B) < 0.0F) -1.0f else 1.0f
    N.cross(T, B)
    B.mul(handedness)
    vertexBuffer.putFloat(T.x())
    vertexBuffer.putFloat(T.y())
    vertexBuffer.putFloat(T.z()).putFloat(handedness)
}

    fun addBoneData(ids: IntArray, weights: FloatArray, vertexId: Int, boneId: Int, weight: Float) {
        val length = vertexId * 4

        for(i in 0 until 4) {
            val blep = length + i
            if (weights[blep] == 0.0F) {
                ids[blep] = boneId
                weights[blep] = weight
                return
            }
        }

    }

    @Throws(Exception::class)
    fun createObject(objBuilder: (ModelLoader.Names) -> MultiRenderObject, reader: () -> ResourceReader, imageConsumer: (ResourceReader, List<String>, Int) -> TextureArray, materialProcess: (MaterialReference, List<String>) -> Material, onFinish: (MultiRenderObject) -> Unit): MultiRenderObject{
        val asset = reader.invoke()
        val config = ModelConfig.read(asset)
        val aliases = config.aliases ?: emptyMap()
        val names = ModelLoader.Names()
        config.defaultVariant.forEach { (s, variantDetails) ->
            if (aliases.isNotEmpty() && aliases.containsKey(s)) {
                val meshesToRenderFirst = config.meshesToRenderFirst != null && config.meshesToRenderFirst.contains(s)

                for (s2 in aliases.get(s)!!) {
                    checkIfAlreadyIn(names.meshes(), s2, meshesToRenderFirst)
                }
            } else {
                checkIfAlreadyIn(
                    names.meshes(),
                    s,
                    config.meshesToRenderFirst != null && config.meshesToRenderFirst.contains(s)
                )
            }

            checkIfAlreadyIn(names.materials(), variantDetails.material())
        }

        config.variants.forEach { (s, variantParent) ->
            checkIfAlreadyIn(names.variants(), s)

            variantParent.details?.forEach { (s1, variantDetails) ->
                if (!aliases.isEmpty() && aliases.containsKey(s1)) {
                    for (s2 in aliases.get(s1)!!) {
                        checkIfAlreadyIn(names.meshes(), s2)
                    }
                } else {
                    checkIfAlreadyIn(names.meshes(), s1)
                }

                if (variantDetails.material() != null) {
                    checkIfAlreadyIn(names.materials(), variantDetails.material())
                }
            }
        }

        config.materials.forEach { (s, referencex) ->
            if (names.materials.contains(s)) {
                referencex.complete(config.materials)
                val images = referencex.images
                checkIfAlreadyIn(names.images, images.diffuse)
                checkIfAlreadyIn(names.images, images.layer)
                checkIfAlreadyIn(names.images, images.emission)
                checkIfAlreadyIn(names.images, images.mask)
            }

        }
        val obj = objBuilder.invoke(names)
        obj.images = imageConsumer.invoke(asset, names.images, config.resolution ?: 1024)

        config.materials.forEach { (name, reference) ->
            val id = obj.materialNameToId[name] ?: -1
            if (id != -1) {
                val material = materialProcess.invoke(reference, names.images)
                obj.materials[id] = material
            }
        }

        processVariants(obj, config, names, aliases)
        obj.scale = config.scale
        val aninResouces = hashMapOf<String, AnimResource>()
        SmdResource.read(asset, aninResouces)
        GfbanmResource.read(asset, aninResouces)
        TrAnimationResource.read(asset, aninResouces)
        processModel(obj, names, asset, aninResouces, config)
        obj.updateDimensions()
        onFinish.invoke(obj)

        return obj
    }

    private fun checkIfAlreadyIn(list: MutableList<String>, entry: String?) {
        if (entry != null) {
            checkIfAlreadyIn(list, entry, false)
        }
    }

    private fun checkIfAlreadyIn(list: MutableList<String>, entry: String, addFirst: Boolean) {
        if (!list.contains(entry)) {
            if (addFirst) {
                list.addFirst(entry)
            } else {
                list.add(entry)
            }
        }
    }

    fun alignUp(value: Int, alignment: Int): Int {
        return (value + alignment - 1) / alignment * alignment
    }

}