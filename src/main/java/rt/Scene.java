package rt;

import js3d.objects.core.SceneObject;

import rt.light.LightSource;

import java.util.*;

public class Scene {
    public static enum LightingModel {
        PHONG, BLINN_PHONG
    }

    public Collection<SceneObject> sceneObjects = new ArrayList<>();
    public Collection<LightSource> lightSources = new ArrayList<>();
    public Camera camera;
    public RayTracer.LightingModel lightingModel;

    public Scene(Camera camera, RayTracer.LightingModel lightingModel) {
        this.camera = camera;
        this.lightingModel = lightingModel;
    }

    public void addSceneObject(SceneObject sceneObject) {
        sceneObjects.add(sceneObject);
    }

    public void addLightSource(LightSource lightSource) {
        lightSources.add(lightSource);
    }
}
