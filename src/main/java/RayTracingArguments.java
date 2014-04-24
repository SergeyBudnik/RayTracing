import java.io.File;

public class RayTracingArguments {
    public static class InvalidRayTracingArgumentsException extends Exception {}

    public File scene = null;
    public int resolutionX = -1;
    public int resolutionY = -1;
    public File output = null;
    public int traceDepth = -1;

    public RayTracingArguments(String [] args) throws InvalidRayTracingArgumentsException {
        try {
            for (String arg : args) {
                if (arg.startsWith("--scene=")) {
                    scene = new File(arg.substring("--scene=".length()));

                    if (!scene.exists() || !scene.isFile()) {
                        throw new InvalidRayTracingArgumentsException();
                    }
                } else if (arg.startsWith("--resolution_x=")) {
                    resolutionX = Integer.parseInt(arg.substring("--resolution_x=".length()));

                    if (resolutionX <= 0) {
                        throw new InvalidRayTracingArgumentsException();
                    }
                } else if (arg.startsWith("--resolution_y=")) {
                    resolutionY = Integer.parseInt(arg.substring("--resolution_y=".length()));

                    if (resolutionY <= 0) {
                        throw new InvalidRayTracingArgumentsException();
                    }
                } else if (arg.startsWith("--output=")) {
                    output = new File(arg.substring("--output=".length()));
                } else if (arg.startsWith("--trace_depth=")) {
                    traceDepth = Integer.parseInt(arg.substring("--trace_depth=".length()));

                    if (traceDepth < 0) {
                        throw new InvalidRayTracingArgumentsException();
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new InvalidRayTracingArgumentsException();
        }

        if (scene == null || resolutionX == -1 || resolutionY == -1 || output == null || traceDepth == -1) {
            throw new InvalidRayTracingArgumentsException();
        }
    }
}
