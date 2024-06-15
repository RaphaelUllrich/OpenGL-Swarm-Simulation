import static org.lwjgl.opengl.GL11.*;
import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Mouse;

public class ObjektLadenUndDrehen extends LWJGLBasisFenster {
    List<Agent> agents;
    Model object = null;
    boolean useKugel = false;
    Random random = new Random();
    Vektor2D target;  // Das aktuelle Ziel
    Vektor2D clickPosition;  // Die Position des aktuellen Klicks

    public ObjektLadenUndDrehen(String title, int width, int height, String fileName, float size, boolean useKugel) {
        super(title, width, height);
        this.useKugel = useKugel;

        JFrame f = new JFrame();
        f.setTitle(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Canvas c = new Canvas();
        f.add(c);
        f.setBounds(100, 100, 700, 600);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        if (!useKugel) {
            loadObject(fileName);
            if (object != null) {
                object.size = size;
            }
        }

        agents = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Vektor2D position = new Vektor2D(random.nextDouble() * 2 - 1, random.nextDouble() * 2);
            Vektor2D velocity = new Vektor2D(random.nextDouble() * 0.002 - 0.001, random.nextDouble() * 0.002 - 0.001);
            agents.add(new Agent(i, position, velocity));
        }

        target = null;
        clickPosition = null;

        initDisplay(c);

        c.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                // Transformiere Mauskoordinaten in Weltkoordinaten
                float worldX = (float) (mouseX / (double) c.getWidth() * 4 - 2);
                float worldY = (float) ((c.getHeight() - mouseY) / (double) c.getHeight() * 4 - 2); // Anpassung für Y-Koordinate
                Vektor2D newTarget = new Vektor2D(worldX, -worldY + 1);
                System.out.println("Mouse Clicked at: " + newTarget.x + ", " + newTarget.y);
                for (Agent agent : agents) {
                    agent.moveToTarget(newTarget);
                }

                target = newTarget;  // Setze das neue Ziel
                clickPosition = newTarget;  // Aktualisiere die Klickposition
            }
        });
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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        long start = System.nanoTime();
        while (!Display.isCloseRequested()) {
            double t = (System.nanoTime() - start) / 1e9;
            POGL.clearBackgroundWithColor(0.15f, 0.15f, 0.15f, 1.0f);
            glLoadIdentity();
            glFrustum(-1, 1, -1, 1, 4, 10);
            glTranslated(0, -1, -8);

            for (Agent agent : agents) {
                agent.flock(agents);
                agent.update(t);

                glPushMatrix();
                glTranslated(agent.position.x, agent.position.y, 0);
                glRotatef((float) Math.toDegrees(Math.atan2(agent.velocity.y, agent.velocity.x)), 0, 0, 1);
                glScaled(0.03, 0.03, 0.03); // Make fireflies smaller
                // Farben von Gelb zu Orange ändern
                double red = 1.0;
                double green = 0.8 + 0.2 * Math.sin(t);
                double blue = 0.0;
                glColor3d(red, green, blue);
                if (useKugel) {
                    POGL.renderEgg(6);
                } else if (object != null) {
                    POGL.renderObject(object);
                }
                glPopMatrix();
            }

            // Punkt Zeichnen und target setzen
            if (Mouse.isButtonDown(0)) {
                int mouseX = Mouse.getX();
                int mouseY = Mouse.getY();
                System.out.println("  Mouse X-Pos: " + mouseX);
                System.out.println("  Mouse Y-Pos: " + mouseY);
                // Transformiere Mauskoordinaten in Weltkoordinaten
                float worldX = (float) (mouseX / (double) Display.getWidth() * 4 - 2);
                float worldY = (float) ((Display.getHeight() - mouseY) / (double) Display.getHeight() * 4 - 2); // Anpassung für Y-Koordinate
                Vektor2D newTarget = new Vektor2D(worldX, -worldY + 1);
                System.out.println("World X-Pos: " + worldX);
                System.out.println("World Y-Pos: " + worldY);

                for (Agent agent : agents) {
                    agent.moveToTarget(newTarget);
                }

                target = newTarget;  // Setze das neue Ziel
                clickPosition = newTarget;  // Aktualisiere die Klickposition
            }

            // Rendern der Klickpositionen
            if (clickPosition != null) {
                drawTarget(clickPosition);
            }

            Display.update();
        }

        glDisable(GL_BLEND);
    }

    private void drawTarget(Vektor2D target) {
        // Zeichne gelben Punkt mit Glow-Effekt
        int layers = 20;
        double initialRadius = 0.01;
        for (int i = layers; i > 0; i--) {
            double alpha = 1.0 / i;
            glColor4d(1.0, 1.0, 0.0, alpha);
            glPushMatrix();
            glTranslated(target.x, target.y, 0);
            glScaled(initialRadius * i, initialRadius * i, 1.0);
            drawCircle();
            glPopMatrix();
        }
    }

    private void drawCircle() {
        int numSegments = 100;
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(0, 0);
        for (int i = 0; i <= numSegments; i++) {
            double angle = 2 * Math.PI * i / numSegments;
            glVertex2d(Math.cos(angle), Math.sin(angle));
        }
        glEnd();
    }

    public static void main(String[] args) {
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
