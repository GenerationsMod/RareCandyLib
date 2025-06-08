package generations.gg.rarecandylib.common.client.model;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.model.GLModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Supplier;

public class GenerationsModelLoader extends ModelLoader {
    public GenerationsModelLoader(int numThreads) {
        super(numThreads);
    }
    public MultiRenderObject<MeshObject> compiledModelMethod(CompiledModel model, InputStream stream, Supplier<MeshObject> supplier, String name, boolean requiresVariantTexture) {
        return createObject(
                () -> new PixelAsset(stream, name),
                (gltfModel, animResources, textures, config, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    try {
//                        if(GenerationsCore.CONFIG.client.useVanilla) {
//                            VanillaModelLoader.processModel(object, gltfModel, animResources, textures, config, glCalls, supplier);
//                        } else {
                            processModel(object, gltfModel, animResources, textures, config, glCalls, supplier, GLModel::new);
//                        }
                    } catch (Exception e) {
                        System.out.println("Oh no! Model : " + name + " didn't properly load!");
                        e.printStackTrace();
                    }
                    return glCalls;
                },
                object -> {
                    if(object.scale == 0f) object.scale = 1.0f;

//                    if(GenerationsCore.CONFIG.client.logModelLoading) GenerationsCore.LOGGER.info("Done Loading: " + name);
                }
        );
    }
}