package rt;

import js3d.math.core.Vector;

import js3d.objects.core.SceneObject;

import js3d.objects.intangible.Ray;

import js3d.objects.support.Color;
import js3d.objects.support.IntersectionInfo;

import rt.light.LightSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RayTracer {
    public static enum LightingModel {
        PHONG, BLINN_PHONG
    }

    private class RayTracerRunnable implements Runnable {
        private int sw, sh;
        private int startRow;
        private int finishRow;
        private java.awt.Color [][] buf;

        public RayTracerRunnable(int sw, int sh, int startRow, int finishRow, java.awt.Color [][] buf) {
            this.sw = sw;
            this.sh = sh;
            this.startRow = startRow;
            this.finishRow = finishRow;
            this.buf = buf;
        }

        @Override
        public void run() {
            for (int x = 0; x < sw; x++) {
                for (int y = startRow; y < finishRow; y++) {
                    Ray r = getRay(sw, sh, x, y);

                    buf[x][y] = getColor(r, 0, null).awtColor();
                }
            }
        }
    }

    private int traceDepth = 0;

    private Scene scene;

    public RayTracer(Scene scene) {
        this.scene = scene;
    }

    public void trace(int nThreads, Graphics g, int sw, int sh, int traceDepth, File output) {
        this.traceDepth = traceDepth;

        long t1 = System.currentTimeMillis();

        Thread [] threads = new Thread [nThreads];

        java.awt.Color [][] buf = new java.awt.Color[sw][sh];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Thread(new RayTracerRunnable(sw, sh, i * sh / nThreads, (i + 1) * sh / nThreads, buf));
        }

        for (int i = 0; i < nThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < nThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                /* Do nothing */
            }
        }

        for (int i = 0; i < sw; i++) {
            for (int j = 0; j < sh; j++) {
                g.setColor(buf[i][j]);
                g.drawRect(i, j, 1, 1);
            }
        }

        BufferedImage res = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < sw; i++) {
            for (int j = 0; j < sh; j++) {
                res.setRGB(i, j, buf[i][j].getRGB());
            }
        }

        try {
            ImageIO.write(res, "bmp", output);
        } catch (IOException e) {
            System.out.println("Error on writing image into specified file.");
        }

        long t2 = System.currentTimeMillis();

        drawTime(g, t2 - t1, nThreads);
    }

    private Ray getRay(int sw, int sh, int x, int y) {
        double normX = (1.0 * x / sw - 0.5) * sw / sh;
        double normY = 1.0 * y / sh - 0.5;

        Vector v1 = scene.camera.right.mul(normX).mul(scene.camera.tgFovX);
        Vector v2 = scene.camera.up.mul(normY).mul(scene.camera.tgFovY);
        Vector v3 = scene.camera.dir;

        Vector rayDir = v1.add(v2).add(v3).normal();

        return new Ray(scene.camera.pos, rayDir);
    }

    private Color getColor(Ray r, int depth, SceneObject self) {
        Color c = new Color();

        IntersectionInfo closestIO = null;
        SceneObject closestSceneObject = null;

        IntersectionInfo io;

        for (SceneObject so : scene.sceneObjects) {
            if (so == self) {
                continue;
            }

            if ((io = so.intersects(r)) != null) {
                if (closestIO == null || io.getClosestIO().dist < closestIO.getClosestIO().dist) {
                    closestIO = io;
                    closestSceneObject = so;
                }
            }
        }

        if (closestIO != null) {
            addAmbientComponent(c, closestIO);

            for (LightSource ls : scene.lightSources) {
                Ray rToLs = ls.rayToLightSource(closestIO.getClosestIO().pos);
                double distToLs = ls.getDistance2ToLightSource(closestIO.getClosestIO().pos);

                boolean intersects = false;

                for (SceneObject so : scene.sceneObjects) {
                    IntersectionInfo ioWithSO = so.intersects(rToLs);

                    if (ioWithSO != null && ioWithSO.getClosestIO().dist * ioWithSO.getClosestIO().dist < distToLs) {
                        intersects = true;
                        break;
                    }
                }

                if (intersects) {
                    continue;
                }

                double fadeFactor = ls.getFadingFactor(closestIO.getClosestIO().pos);

                addDiffuseComponent(c, rToLs, fadeFactor, ls, closestIO);
                addSpecularComponent(c, r, ls, closestIO);
            }

            if (depth < traceDepth) {
                addReflectionComponent(c, r, closestIO, closestSceneObject, depth + 1);
            }
        }

        return c.clump();
    }

    private void addAmbientComponent(Color c, IntersectionInfo closestIO) {
        c.r += closestIO.getClosestIO().material.ambient.r;
        c.g += closestIO.getClosestIO().material.ambient.g;
        c.b += closestIO.getClosestIO().material.ambient.b;
    }

    private void addDiffuseComponent(Color c, Ray rToLs, double fadingFactor, LightSource ls, IntersectionInfo closestIO) {

        double cos = Math.abs(Vector.cos(closestIO.getClosestIO().normal, rToLs.dir));

        c.r += cos * closestIO.getClosestIO().material.diffuse.r * ls.ambientColor.r * fadingFactor;
        c.g += cos * closestIO.getClosestIO().material.diffuse.g * ls.ambientColor.g * fadingFactor;
        c.b += cos * closestIO.getClosestIO().material.diffuse.b * ls.ambientColor.b * fadingFactor;
    }

    private void addSpecularComponent(Color c, Ray ray, LightSource ls, IntersectionInfo closestIO) {
        Color color = new Color();

        color.add(closestIO.getClosestIO().material.specular);

        Vector n = closestIO.getClosestIO().normal;
        Vector v = new Vector(0, 0, 0).sub(ray.dir);
        Vector l = ls.rayToLightSource(closestIO.getClosestIO().pos).dir;

        double dot;

        if (scene.lightingModel == LightingModel.PHONG) {
            Vector r = n.mul(2 * n.dot(v)).sub(v).normal();

            dot = r.dot(l);
        } else {
            Vector lv = l.add(v);

            Vector h = lv.div(lv.len());

            dot = n.dot(h);
        }

        double cosA = dot <= 0 ? 0 : Math.pow(dot, closestIO.getClosestIO().material.specularPower);

        color.r *= cosA;
        color.g *= cosA;
        color.b *= cosA;

        color.mul(ls.color);

        c.add(color);
    }

    private void addReflectionComponent(Color c, Ray ray, IntersectionInfo closestIO, SceneObject self, int depth) {
        Vector n = closestIO.getClosestIO().normal;
        Vector v = new Vector(0, 0, 0).sub(ray.dir);

        Vector r = n.mul(2 * n.dot(v)).sub(v).normal();

        Color color = getColor(new Ray(closestIO.getClosestIO().pos, r), depth + 1, self);

        color.r *= closestIO.getClosestIO().material.reflectionRatio;
        color.g *= closestIO.getClosestIO().material.reflectionRatio;
        color.b *= closestIO.getClosestIO().material.reflectionRatio;

        c.add(color);
    }

    private void drawTime(Graphics g, double dt, int nThreads) {
        String infoString = "Scene rendered in: " + dt + " ms by " + nThreads + " threads.";

        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(infoString, g);

        g.setColor(java.awt.Color.BLACK);
        g.fillRect(0, 0, (int)rect.getWidth() + 10, (int)rect.getHeight());
        g.setColor(java.awt.Color.WHITE);
        g.drawString(infoString, 5, fm.getAscent());
    }
}
