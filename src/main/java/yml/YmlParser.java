package yml;

import js3d.loaders.ObjModelLoader;
import js3d.loaders.WrongModelFileFormatException;
import js3d.math.core.Matrix;
import js3d.math.core.Vector;
import js3d.objects.complex.*;
import js3d.objects.core.ClosedObject;
import js3d.objects.core.SceneObject;
import js3d.objects.primitives.Sphere;
import js3d.objects.support.Color;
import js3d.objects.support.Material;

import org.yaml.snakeyaml.Yaml;

import rt.Camera;
import rt.RayTracer;
import rt.Scene;
import rt.light.DirectionalLightSource;
import rt.light.LightSource;
import rt.light.PointLightSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.*;

public class YmlParser {
    public Scene parse(InputStream is) {
        Camera camera = null;
        RayTracer.LightingModel lightingModel = RayTracer.LightingModel.PHONG;
        Map<Integer, Material> materialsInfo = new HashMap<>();
        List<LightSource> lightSources = new ArrayList<>();
        List<SceneObject> sceneObjects = new ArrayList<>();

        ArrayList os = (ArrayList)new Yaml().load(is);

        for (Object o : os) {
            HashMap oh = (HashMap)o;

            if (oh.containsKey("options")) {
                lightingModel = ((HashMap)oh.get("options")).get("shading_type").equals("phong") ?
                        RayTracer.LightingModel.PHONG : RayTracer.LightingModel.BLINN_PHONG;
            } if (oh.containsKey("camera")) {
                HashMap cameraInfoMap = (HashMap)oh.get("camera");

                HashMap positionInfoMap = (HashMap)cameraInfoMap.get("position");

                HashMap orientationInfoMap = (HashMap)cameraInfoMap.get("orientation");

                double h = -extractNumber(orientationInfoMap, "h");
                double p = extractNumber(orientationInfoMap, "p");
                double r = extractNumber(orientationInfoMap, "r");

                double fovX = extractNumber(cameraInfoMap, "fov_x");
                double fovY = extractNumber(cameraInfoMap, "fov_y");

                camera = new Camera(extractVector(positionInfoMap), r, h + 90, p, fovX, fovY);
            } else if (oh.containsKey("material")) {
                int materialId = System.identityHashCode(oh.keySet().iterator().next());

                HashMap materialInfoMap = (HashMap)oh.get("material");

                Color ambientColor = extractColor((HashMap)materialInfoMap.get("ambient"));
                Color diffuseColor = extractColor((HashMap)materialInfoMap.get("diffuse"));
                Color specularColor = extractColor((HashMap)materialInfoMap.get("specular"));

                double specularPower = extractNumber(materialInfoMap, "specular_power");
                double reflectionRatio = 0;

                if (materialInfoMap.containsKey("reflection_ratio")) {
                    reflectionRatio = extractNumber(materialInfoMap, "reflection_ratio");
                }

                materialsInfo.put(materialId, new Material(ambientColor, diffuseColor, specularColor, specularPower, reflectionRatio));
            } else if (oh.containsKey("directional_light")) {
                HashMap directionalLightInfo = (HashMap)oh.get("directional_light");

                Vector direction = extractVector((HashMap) directionalLightInfo.get("direction")).normal();

                Color color = extractColor((HashMap)directionalLightInfo.get("color"));

                Color ambientColor = null;

                if (directionalLightInfo.containsKey("ambient_color")) {
                    ambientColor = extractColor((HashMap)directionalLightInfo.get("ambient_color"));
                }

                direction = direction.mul(-1);

                if (ambientColor != null) {
                    lightSources.add(new DirectionalLightSource(color, ambientColor, direction));
                } else {
                    lightSources.add(new DirectionalLightSource(color, direction));
                }
            } else if (oh.containsKey("point_light")) {
                HashMap pointLightInfo = (HashMap)oh.get("point_light");

                Vector position = extractVector((HashMap) pointLightInfo.get("position"));

                Color color = extractColor((HashMap)pointLightInfo.get("color"));

                Color ambientColor = null;

                if (pointLightInfo.containsKey("ambient_color")) {
                    ambientColor = extractColor((HashMap)pointLightInfo.get("ambient_color"));
                }

                double distance = extractNumber(pointLightInfo, "distance");
                double fadeExponent = extractNumber(pointLightInfo, "fade_exponent");

                if (ambientColor != null) {
                    lightSources.add(new PointLightSource(position, color, ambientColor, distance, fadeExponent));
                } else {
                    lightSources.add(new PointLightSource(position, color, distance, fadeExponent));
                }
            } else if (oh.containsKey("node")) {
                parseScene((ArrayList) oh.get("node"), materialsInfo, sceneObjects);
            }
        }

        Scene scene = new Scene(camera, lightingModel);

        for (LightSource ls : lightSources) {
            scene.addLightSource(ls);
        }

        for (SceneObject so : sceneObjects) {
            scene.addSceneObject(so);
        }

        return scene;
    }

    private void parseScene(ArrayList scene, Map<Integer, Material> materialsInfo, List<SceneObject> sceneObjects) {
        for (Object o : scene) {
            SceneObject so = getNode((ArrayList) ((HashMap) o).get("node"), materialsInfo, Material.BLACK_MIRROR);

            if (so != null) {
                sceneObjects.add(so);
            }
        }
    }

    private SceneObject getNode(ArrayList nodeList, Map<Integer, Material> materialsInfo, Material mat) {
        Material m = mat;

        double tx = 0, ty = 0, tz = 0;
        double sx = 1, sy = 1, sz = 1;
        double rx = 0, ry = 0, rz = 0;

        SceneObject so = null;

        for (Object o : nodeList) {
            if (o instanceof String) {
                if (o.equals("material")) {
                    int materialKey = System.identityHashCode(o);

                    for (int mk : materialsInfo.keySet()) {
                        if (mk == materialKey) {
                            m = materialsInfo.get(mk);
                        }
                    }
                }
            } else if (o instanceof HashMap) {
                HashMap ho = (HashMap)o;

                if (ho.containsKey("lcs")) {
                    HashMap lcs = (HashMap)ho.get("lcs");

                    if (lcs.containsKey("x")) {
                        tz = extractNumber(lcs, "x");
                    }

                    if (lcs.containsKey("y")) {
                        tx = extractNumber(lcs, "y");
                    }

                    if (lcs.containsKey("z")) {
                        ty = extractNumber(lcs, "z");
                    }

                    if (lcs.containsKey("sx")) {
                        sx = extractNumber(lcs, "sx");
                    }

                    if (lcs.containsKey("sy")) {
                        sy = extractNumber(lcs, "sy");
                    }

                    if (lcs.containsKey("sz")) {
                        sz = extractNumber(lcs, "sz");
                    }

                    if (lcs.containsKey("p")) {
                        ry = Math.PI * extractNumber(lcs, "p") / 180;
                    }

                    if (lcs.containsKey("r")) {
                        rx = Math.PI * extractNumber(lcs, "r") / 180;
                    }

                    if (lcs.containsKey("h")) {
                        ry = Math.PI * (extractNumber(lcs, "h") + 90) / 180;
                    }
                } else if (ho.containsKey("cylinder")) {
                    HashMap cMap = (HashMap)ho.get("cylinder");

                    double r = extractNumber(cMap, "radius");
                    double h = extractNumber(cMap, "height");

                    so = new Cylinder(h, r, m);
                } else if (ho.containsKey("cube")) {
                    HashMap cMap = (HashMap)ho.get("cube");

                    double w = extractNumber(cMap, "w");

                    so = new Cube(w, m);
                }  else if (ho.containsKey("sphere")) {
                    HashMap sMap = (HashMap)ho.get("sphere");

                    double r = extractNumber(sMap, "radius");

                    so = new Sphere(m, r);
                } else if (ho.containsKey("obj_model")) {
                    HashMap oMap = (HashMap)ho.get("obj_model");

                    String model = (String)oMap.get("file_name");

                    try {
                        so = new ObjModelLoader().load(new File(model), m);
                    } catch (FileNotFoundException | WrongModelFileFormatException e) {
                        /* Do nothing */
                    }
                } else if (ho.containsKey("node")) {
                    so = getNode((ArrayList) ho.get("node"), materialsInfo, m);
                } else if (ho.containsKey("csg_difference")) {
                    HashMap csgDifferenceMap = (HashMap)ho.get("csg_difference");

                    ClosedObject left = (ClosedObject)getNode((ArrayList)csgDifferenceMap.get("left_node"), materialsInfo, m);
                    ClosedObject right = (ClosedObject)getNode((ArrayList)csgDifferenceMap.get("right_node"), materialsInfo, m);

                    so = new DifferenceOfObjects(left, right);
                } else if (ho.containsKey("csg_union")) {
                    HashMap csgDifferenceMap = (HashMap)ho.get("csg_union");

                    ClosedObject left = (ClosedObject)getNode((ArrayList)csgDifferenceMap.get("left_node"), materialsInfo, m);
                    ClosedObject right = (ClosedObject)getNode((ArrayList)csgDifferenceMap.get("right_node"), materialsInfo, m);

                    so = new UnionOfObjects(left, right);
                } else if (ho.containsKey("csg_intersection")) {
                    HashMap csgDifferenceMap = (HashMap)ho.get("csg_intersection");

                    ClosedObject left = (ClosedObject)getNode((ArrayList)csgDifferenceMap.get("left_node"), materialsInfo, m);
                    ClosedObject right = (ClosedObject)getNode((ArrayList)csgDifferenceMap.get("right_node"), materialsInfo, m);

                    so = new IntersectionOfObjects(left, right);
                }
            }
        }

        if (so != null) {
            so.rotateX(rx);
            so.rotateY(ry);
            so.rotateZ(rz);

            so.scale(sx, sy, sz);
            so.translate(tx, ty, tz);
        }

        return so;
    }

    private Vector extractVector(HashMap map) {
        return new Vector(extractNumber(map, "y"), extractNumber(map, "z"), extractNumber(map, "x"));
    }

    private Color extractColor(HashMap map) {
        return new Color(extractNumber(map, "r"), extractNumber(map, "g"), extractNumber(map, "b"));
    }

    private double extractNumber(HashMap map, String key) {
        Object res = map.get(key);

        if (res instanceof Integer) {
            return (Integer)map.get(key);
        } else {
            return (Double)map.get(key);
        }
    }
}
