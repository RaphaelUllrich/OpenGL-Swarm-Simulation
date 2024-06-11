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

public class ObjektLadenUndDrehen extends LWJGLBasisFenster {
    List<Agent> agents;
    Model object = null;
    boolean useKugel = false;
    Random random = new Random();

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
            Vektor2D position = new Vektor2D(random.nextDouble() * 6 - 3, random.nextDouble() * 6 - 3);
            Vektor2D velocity = new Vektor2D(random.nextDouble() * 0.2 - 0.1, random.nextDouble() * 0.2 - 0.1);
            agents.add(new Agent(i, position, velocity));
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

            for (Agent agent : agents) {
                agent.flock(agents);
                agent.update();

                glPushMatrix();
                glTranslated(agent.position.x, agent.position.y, 0);
                glRotatef((float) Math.toDegrees(Math.atan2(agent.velocity.y, agent.velocity.x)), 0, 0, 1);
                glScaled(0.1, 0.1, 0.1);
                if (useKugel) {
                    POGL.renderEgg(8);
                } else if (object != null) {
                    POGL.renderObject(object);
                }
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
