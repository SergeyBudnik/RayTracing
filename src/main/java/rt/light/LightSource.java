package rt.light;

import js3d.math.core.Vector;
import js3d.objects.intangible.Ray;
import js3d.objects.support.Color;

public abstract class LightSource {
    public Color color;
    public Color ambientColor;

    public LightSource(Color color) {
        this.color = color;
        this.ambientColor = color;
    }

    public LightSource(Color color, Color ambientColor) {
        this.color = color;
        this.ambientColor = ambientColor;
    }

    public abstract Ray rayToLightSource(Vector position);

    public abstract double getFadingFactor(Vector position);

    public abstract double getDistance2ToLightSource(Vector position);
}
