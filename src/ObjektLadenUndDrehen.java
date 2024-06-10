import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_POINT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.glColor3d;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;

import java.awt.Canvas;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

public class ObjektLadenUndDrehen extends LWJGLBasisFenster {
   Model object = null;

   public ObjektLadenUndDrehen(String title, int width, int height, String fileName, float size) {
      super(title, width, height);
      JFrame f = new JFrame();
      f.setTitle(title);
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Canvas c = new Canvas();
      f.add(c);
      f.setBounds(100, 100, 600, 600);
      f.setLocationRelativeTo(null);
      f.setVisible(true);

      loadObject(fileName);
      object.size = size;
      initDisplay(c);
   }

   public boolean loadObject(String fileName) {
      try {
         object = POGL.loadModel(new File(fileName));
         return true;
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return false;
   }

   @Override
   public void renderLoop() {
      //glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
      //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
      glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

      long start = System.nanoTime();
      while (!Display.isCloseRequested()) {
         double t = (System.nanoTime()-start)/1e9;
         POGL.clearBackgroundWithColor(0.15f, 0.15f, 0.15f, 1.0f);
         glLoadIdentity();
         glFrustum(-1, 1, -1, 1, 4, 10);
         glTranslated(0, -1, -8);
         glRotatef((float)t*50.0f, 0.0f, 1.0f, 0.0f);
         //glPointSize(5);
         glScaled(2.0/object.size, 2.0/object.size, 2./object.size);
         glColor3d(0.35 + 0.30*Math.sin(t), 0.93 + 0.24*Math.sin(t), 0.73 + 0.22*Math.sin(t));
         POGL.renderObject(object);
         Display.update();
      }
   }

   public static void main(String[] args) throws LWJGLException {
      new ObjektLadenUndDrehen("Obejekt drehen", 500, 500,
            "objects/firefly.obj", 10).start();
   }
}
