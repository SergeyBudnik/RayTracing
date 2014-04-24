package rt;

import js3d.loaders.ModelLoader;
import js3d.loaders.ObjModelLoader;
import js3d.loaders.WrongModelFileFormatException;

import js3d.objects.complex.Model;
import js3d.objects.core.SceneObject;
import js3d.objects.primitives.Primitive;
import js3d.objects.primitives.Triangle;

import rt.light.LightSource;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.*;

public class Scene {
    public Collection<SceneObject> sceneObjects = new ArrayList<>();
    public Collection<LightSource> lightSources = new ArrayList<>();
    public Camera camera;

    public Scene(Camera camera) {
        this.camera = camera;
    }

    public void addSceneObject(SceneObject sceneObject) {
        sceneObjects.add(sceneObject);
    }

    public void addLightSource(LightSource lightSource) {
        lightSources.add(lightSource);
    }
}
