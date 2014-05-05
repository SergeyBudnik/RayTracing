package rt.light;

import js3d.math.core.Vector;
import js3d.objects.intangible.Ray;
import js3d.objects.support.Color;

public class PointLightSource extends LightSource {
    public Vector pos;
    public double distance;
    public double fadeExponent;

    public PointLightSource(Vector pos, Color color, double distance, double fadeExponent) {
        super(color);

        this.pos = pos;
        this.distance = distance;
        this.fadeExponent = fadeExponent;
    }

    public PointLightSource(Vector pos, Color color, Color ambientColor, double distance, double fadeExponent) {
        super(color, ambientColor);

        this.pos = pos;
        this.distance = distance;
        this.fadeExponent = fadeExponent;
    }

    @Override
    public Ray rayToLightSource(Vector position) {
        Vector dir = pos.sub(position).normal();

        return new Ray(position, dir);
    }

    @Override
    public double getFadingFactor(Vector position) {
        double len2 = getDistance2ToLightSource(position);

        if (len2 >= distance * distance) {
            return 0;
        } else {
            return 1 / (fadeExponent * Math.pow(len2, 0.1));
        }
    }

    @Override
    public double getDistance2ToLightSource(Vector position) {
        return pos.sub(position).len2();
    }
}
