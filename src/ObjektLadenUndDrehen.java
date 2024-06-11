import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.glColor3d;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

public class ObjektLadenUndDrehen extends LWJGLBasisFenster {
    Model object = null;
    boolean useKugel = false;

    public ObjektLadenUndDrehen(String title, int width, int height, String fileName, float size, boolean useKugel) {
        super(title, width, height);
        this.useKugel = useKugel;

        JFrame f = new JFrame();
        f.setTitle(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Canvas c = new Canvas();
        f.add(c);
        // Breite des Anfangsfensters anpassen
        f.setBounds(100, 100, 700, 600);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        if (!useKugel) {
            loadObject(fileName);
            object.size = size;
        }
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
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        long start = System.nanoTime();
        while (!Display.isCloseRequested()) {
            double t = (System.nanoTime() - start) / 1e9;
            POGL.clearBackgroundWithColor(0.15f, 0.15f, 0.15f, 1.0f);
            glLoadIdentity();
            glFrustum(-1, 1, -1, 1, 4, 10);
            glTranslated(0, -1, -8);
            glRotatef((float) t * 50.0f, 0.0f, 1.0f, 0.0f);

            if (!useKugel) {
                glScaled(2.0 / object.size, 2.0 / object.size, 2.0 / object.size);
            }
            // Farben von Gelb zu Orange ändern
            double red = 1.0;
            double green = 0.8 + 0.2 * Math.sin(t);
            double blue = 0.0;
            glColor3d(red, green, blue);

            if (useKugel) {
                POGL.renderEgg(12);
            } else {
                POGL.renderObject(object);
            }

            Display.update();
        }
    }

    public static void main(String[] args) {
        // Create the selection window
    	JFrame selectionFrame = new JFrame("Wähle Objekt oder Kugel");
        selectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        selectionFrame.setSize(490, 110);
        selectionFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        selectionFrame.add(panel);
        panel.setLayout(null);

        JButton objButton = new JButton("Lade OBJ Glühwürmchen");
        objButton.setBounds(30, 20, 200, 30);
        panel.add(objButton);

        JButton kugelButton = new JButton("Lade OpenGL Glühwürmchen");
        kugelButton.setBounds(240, 20, 200, 30);
        panel.add(kugelButton);

        objButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionFrame.dispose();
                new ObjektLadenUndDrehen("Objekt drehen", 500, 500, "objects/firefly.obj", 10, false).start();
            }
        });

        kugelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionFrame.dispose();
                new ObjektLadenUndDrehen("Kugel drehen", 500, 500, null, 10, true).start();
            }
        });

        selectionFrame.setVisible(true);
    }
}
