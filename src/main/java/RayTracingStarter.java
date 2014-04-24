import js3d.loaders.ObjModelLoader;
import js3d.loaders.WrongModelFileFormatException;
import js3d.math.core.Vector;

import js3d.objects.complex.*;
import js3d.objects.core.ClosedObject;
import js3d.objects.core.SceneObject;
import js3d.objects.primitives.*;

import js3d.objects.support.Material;
import js3d.objects.support.Color;

import rt.Camera;
import rt.RayTracer;
import rt.Scene;
import rt.light.DirectionalLightSource;
import rt.light.PointLightSource;
import yml.YmlParser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RayTracingStarter {
    public static Scene getScene() throws FileNotFoundException {
        Camera c = new Camera(new Vector(5, 1, 7), 0, -135, 0, 90, 90);

        Scene s = new Scene(c);

        //s.addLightSource(new PointLightSource(new Vector(6, 3, 0), new Color(0, 0.5, 0), 100, 0.2));
        //s.addLightSource(new PointLightSource(new Vector(0, 2, 3), new Color(0, 0, 0.5), 100, 0.2));

        s.addLightSource(new PointLightSource(new Vector(3, 0.5, 3.5), new Color(0.8, 0.8, 0.8), 100, 0.3));

        s.addLightSource(new DirectionalLightSource(new Color(1.0, 1.0, 1.0), new Vector(0, -1, 0)));
        /*
        int w = 10, n1 = -5, n2 = 5;
        int cw = w / (n2 - n1);

        for (int i = n1; i <= n2; i++) {
            for (int j = n1; j <= n2; j++) {
                Material m = (i + j + (n2 - n1)) % 2 == 0 ? Material.GRAY_MIRROR : Material.BLACK_MIRROR;

                s.addSceneObject(new Triangle(
                        new Vector(cw * i,       0, cw * j      ),
                        new Vector(cw * (i + 1), 0, cw * j      ),
                        new Vector(cw * (i + 1), 0, cw * (j + 1)), m));

                s.addSceneObject(new Triangle(
                        new Vector(cw * i, 0, cw * j),
                        new Vector(cw * i, 0, cw * (j + 1)),
                        new Vector(cw * (i + 1), 0, cw * (j + 1)), m));
            }
        }
        */
/*
        ClosedObject o1 = getIcosahedron();
        ClosedObject o2 = new Sphere(Material.BLACK_MIRROR, 1);

        o2.translate(0, 1, 0);

        s.addSceneObject(new UnionOfSceneObjects(o1, o2));
*/

        /*s.addSceneObject(getO1());*/
        /*
        SceneObject o3 = getO3();

        o3.rotateX(Math.PI / 4);
        o3.rotateZ(Math.PI / 3);

        s.addSceneObject(o3);
        */

        SceneObject o1 = getIcosahedron();

        o1.scale(1, 1, 1);
        o1.translate(2, 0, 0);

        s.addSceneObject(o1);

        SceneObject o2 = getIcosahedron();

        o2.scale(2, 2, 2);
        o2.translate(-4, 0, 0);

        s.addSceneObject(o2);

        return s;
    }

    private static ClosedObject getIcosahedron() {
        try {
            return new ObjModelLoader().load(new File("icosahedron.obj"));
        } catch (FileNotFoundException | WrongModelFileFormatException e) {
            /* Do nothing */
        }

        return new Sphere(Material.BLACK_MIRROR, 1);
    }

    private static ClosedObject getO1() {
        ClosedObject c1 = new Cylinder(3, 0.5, Material.GOLD);
        ClosedObject c2 = new Cylinder(3, 0.5, Material.GOLD);
        ClosedObject c3 = new Cylinder(3, 0.5, Material.GOLD);

        c2.rotateX(Math.PI / 2);
        c3.rotateZ(Math.PI / 2);

        return new UnionOfObjects(c1, new UnionOfObjects(c2, c3));
    }

    private static ClosedObject getO2() {
        ClosedObject c = new Cube(2, Material.COPPER);
        ClosedObject s = new Sphere(Material.BLUE_MIRROR, 1.3);

        return new IntersectionOfObjects(c, s);
    }

    private static ClosedObject getO3() {
        return new DifferenceOfObjects(getO2(), getO1());
    }

    public static void main(String [] args) {
        try {
            final RayTracingArguments rayTracingArguments = new RayTracingArguments(args);

            Scene scene = new YmlParser().parse(new FileInputStream(rayTracingArguments.scene));

            final RayTracer rayTracer = new RayTracer(scene);

            final JFrame frame = new JFrame();

            frame.setSize(rayTracingArguments.resolutionX, rayTracingArguments.resolutionY);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setTitle("Sergey Budnik, 43601/2 | Ray tracing");
            frame.setLocationRelativeTo(null);
            frame.getContentPane().add(new JPanel() {
                @Override
                public void paint(Graphics g) {
                    rayTracer.trace(2, g, rayTracingArguments.resolutionX, rayTracingArguments.resolutionY,
                            rayTracingArguments.traceDepth, rayTracingArguments.output);
                }
            });

            frame.setVisible(true);
        } catch (RayTracingArguments.InvalidRayTracingArgumentsException e) {
            System.out.println("Invalid ray tracing arguments.");
        } catch (FileNotFoundException e) {
            System.out.println("File specified in arguments was not found.");
        }
    }
}
