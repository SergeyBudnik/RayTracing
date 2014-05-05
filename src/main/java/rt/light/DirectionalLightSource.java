package rt.light;

import js3d.math.core.Vector;
import js3d.objects.intangible.Ray;
import js3d.objects.support.Color;

public class DirectionalLightSource extends LightSource {
    private Vector antiDir;

    public DirectionalLightSource(Color color, Vector dir) {
        super(color);

        this.antiDir = new Vector(-dir.x, -dir.y, -dir.z);
    }

    public DirectionalLightSource(Color color, Color ambientColor, Vector dir) {
        super(color, ambientColor);

        this.antiDir = new Vector(-dir.x, -dir.y, -dir.z);
    }

    @Override
    public Ray rayToLightSource(Vector position) {
        return new Ray(position, antiDir);
    }

    @Override
    public double getFadingFactor(Vector position) {
        return 1.0;
    }

    @Override
    public double getDistance2ToLightSource(Vector position) {
        return Double.POSITIVE_INFINITY;
    }
}
