import rt.RayTracer;
import rt.Scene;
import yml.YmlParser;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RayTracingStarter {
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
