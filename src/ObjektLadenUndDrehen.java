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
    List<Vektor2D> clickPositions;  // Liste für die Klickpositionen

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
        for (int i = 0; i < 20; i++) {
            Vektor2D position = new Vektor2D(random.nextDouble() * 2 - 1, random.nextDouble() * 2);
            Vektor2D velocity = new Vektor2D(random.nextDouble() * 0.002 - 0.001, random.nextDouble() * 0.002 - 0.001);
            agents.add(new Agent(i, position, velocity));
        }

        clickPositions = new ArrayList<>();  // Initialisiere die Liste der Klickpositionen
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
                    POGL.renderEgg(8);
                } else if (object != null) {
                    POGL.renderObject(object);
                }
                glPopMatrix();
            }

            //Punkt Zeichnen und target setzen
            if (Mouse.isButtonDown(0)) {
            	//System.out.println("Cool");
                int mouseX = Mouse.getX();
                int mouseY = Mouse.getY();
                System.out.println("  Mouse X-Pos: " + mouseX);
                System.out.println("  Mouse Y-Pos: " + mouseY);
                // Transformiere Mauskoordinaten in Weltkoordinaten
                float worldX = (float) (mouseX / (double) Display.getWidth() * 4 - 2);
                float worldY = (float) ((Display.getHeight() - mouseY) / (double) Display.getHeight() * 4 - 2); // Anpassung für Y-Koordinate
                Vektor2D target = new Vektor2D(worldX, -worldY+1);
                System.out.println("World X-Pos: " + worldX);
                System.out.println("World Y-Pos: " + worldY);

                for (Agent agent : agents) {
                    agent.moveToTarget(target);
                }

                clickPositions.add(target);  // Speichere die Klickposition
            }

            // Rendern der Klickpositionen
            glColor3d(1.0, 0.0, 0.0);  // Rot für die Klickpositionen
            for (Vektor2D clickPosition : clickPositions) {
                glPushMatrix();
                glTranslated(clickPosition.x, clickPosition.y, 0);
                glScaled(0.05, 0.05, 0.05);  // Skaliere den Punkt
                POGL.renderEgg(8);  // Verwende das Ei als Punkt
                glPopMatrix();
            }

            Display.update();
        }
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
