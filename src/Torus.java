

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glVertex3d;

import org.lwjgl.util.vector.Vector3f;

public class Torus {
   double x = 0f, y = 0f, z = 0f;
   double r = .5f, t = 0.125 * 2;
   private double steps, stepSize;
   
   public Torus(double x, double y, double z, double r, double thickness, double steps) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.r = r;
      this.t = thickness;
      this.steps = steps;
      initialize();
   }
   
   public void setSteps(int steps) {
      this.steps = steps;
      initialize();
   }

   private void initialize() {
      stepSize = ((360 / steps) * (Math.PI / 360));
   }
   
   Vector3f a = new Vector3f(), b = new Vector3f(),
         c = new Vector3f(), d = new Vector3f(),
         ab = new Vector3f(), ac = new Vector3f(),
         n = new Vector3f();
   Vector3f al = new Vector3f(), ad = new Vector3f(),
         bd = new Vector3f(), br = new Vector3f(),
         _w = new Vector3f(), _x = new Vector3f(),
         _y = new Vector3f(), _z = new Vector3f();

   public void render() {
      glBegin(GL_QUADS);
      for (double j = 0; j < steps * 2; j++)
         for (double i = -steps; i < steps; i++) {

            a.x = (float) (Math.cos(i * stepSize)
                  * Math.cos(j * stepSize) * t + x + Math.cos(j
                  * stepSize)
                  * r);
            a.y = (float) (Math.sin(i * stepSize) * t + y);
            a.z = (float) (Math.cos(i * stepSize)
                  * Math.sin(j * stepSize) * t + z + Math.sin(j
                  * stepSize)
                  * r);

            al.x = (float) (Math.cos(i * stepSize)
                  * Math.cos((j - 1) * stepSize) * t + x + Math
                  .cos((j - 1) * stepSize) * r);
            al.y = (float) (Math.sin(i * stepSize) * t + y);
            al.z = (float) (Math.cos(i * stepSize)
                  * Math.sin((j - 1) * stepSize) * t + z + Math
                  .sin((j - 1) * stepSize) * r);

            ad.x = (float) (Math.cos((i - 1) * stepSize)
                  * Math.cos((j) * stepSize) * t + x + Math.cos((j)
                  * stepSize)
                  * r);
            ad.y = (float) (Math.sin((i - 1) * stepSize) * t + y);
            ad.z = (float) (Math.cos((i - 1) * stepSize)
                  * Math.sin((j) * stepSize) * t + z + Math.sin((j)
                  * stepSize)
                  * r);

            b.x = (float) (Math.cos(i * stepSize)
                  * Math.cos((j * stepSize) + stepSize) * t + x + Math
                  .cos((j * stepSize) + stepSize) * r);
            b.y = (float) (Math.sin(i * stepSize) * t + y);
            b.z = (float) (Math.cos(i * stepSize)
                  * Math.sin((j * stepSize) + stepSize) * t + z + Math
                  .sin((j * stepSize) + stepSize) * r);

            bd.x = (float) (Math.cos(i * stepSize)
                  * Math.cos((j * stepSize) + stepSize) * t + x + Math
                  .cos((j * stepSize) + stepSize) * r);
            bd.y = (float) (Math.sin(i * stepSize) * t + y);
            bd.z = (float) (Math.cos(i * stepSize)
                  * Math.sin((j * stepSize) + stepSize) * t + z + Math
                  .sin((j * stepSize) + stepSize) * r);

            br.x = (float) (Math.cos(i * stepSize)
                  * Math.cos((j * stepSize) + stepSize) * t + x + Math
                  .cos((j * stepSize) + stepSize) * r);
            br.y = (float) (Math.sin(i * stepSize) * t + y);
            br.z = (float) (Math.cos(i * stepSize)
                  * Math.sin((j * stepSize) + stepSize) * t + z + Math
                  .sin((j * stepSize) + stepSize) * r);

            c.x = (float) (Math.cos((i * stepSize) + stepSize)
                  * Math.cos((j * stepSize) + stepSize) * t + x + Math
                  .cos((j * stepSize) + stepSize) * r);
            c.y = (float) (Math.sin((i * stepSize) + stepSize) * t + y);
            c.z = (float) (Math.cos((i * stepSize) + stepSize)
                  * Math.sin((j * stepSize) + stepSize) * t + z + Math
                  .sin((j * stepSize) + stepSize) * r);

            Vector3f.sub(b, a, ab);
            Vector3f.sub(c, a, ac);
            Vector3f.cross(ab, ac, n).normalise();

            d.x = (float) (Math.cos((i * stepSize) + stepSize)
                  * Math.cos(j * stepSize) * t + x + Math.cos(j
                  * stepSize)
                  * r);
            d.y = (float) (Math.sin((i * stepSize) + stepSize) * t + y);
            d.z = (float) (Math.cos((i * stepSize) + stepSize)
                  * Math.sin(j * stepSize) * t + z + Math.sin(j
                  * stepSize)
                  * r);

            glTexCoord2d(j / (steps * 2), (i + steps) / (steps * 2));
            glNormal3f(n.x, n.y, n.z);
            glVertex3d(a.x, a.y, a.z);

            glTexCoord2d((j + 1) / (steps * 2), (i + steps) / (steps * 2));
            glNormal3f(n.x, n.y, n.z);
            glVertex3d(b.x, b.y, b.z);

            glTexCoord2d((j + 1) / (steps * 2), ((i + 1) + steps) / (steps * 2));
            glNormal3f(n.x, n.y, n.z);
            glVertex3d(c.x, c.y, c.z);

            glTexCoord2d(j / (steps * 2), ((i + 1) + steps) / (steps * 2));
            glNormal3f(n.x, n.y, n.z);
            glVertex3d(d.x, d.y, d.z);
         }
      glEnd();
   }
}
