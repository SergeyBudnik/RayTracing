package rt;

import js3d.math.core.Matrix;
import js3d.math.core.Vector;

public class Camera {
    public Vector pos, dir, up, right;
    public double fovX, fovY;
    public double tgFovX, tgFovY;

    public Camera(Vector pos, double ax, double ay, double az, double fovX, double fovY) {
        this.pos = pos;

        dir = new Vector(0, 0, 1);

        dir = dir.mul(Matrix.rotationX(Math.PI * ax / 180));
        dir = dir.mul(Matrix.rotationY(Math.PI * ay / 180));
        dir = dir.mul(Matrix.rotationZ(Math.PI * az / 180));

        up = new Vector(0, -1, 0);

        this.right = dir.cross(up);

        this.fovX = fovX;
        this.fovY = fovY;

        this.tgFovX = Math.tan(Math.PI * fovX / 360);
        this.tgFovY = Math.tan(Math.PI * fovY / 360);
    }
}
