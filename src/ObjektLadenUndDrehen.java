import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL13.*;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

public class ObjektLadenUndDrehen extends LWJGLBasisFenster {
    List<Agent> agents;
    Model object = null;
    boolean useKugel = false;
    Random random = new Random();
    Vektor2D target;  // Das aktuelle Ziel
    Vektor2D clickPosition;  // Die Position des aktuellen Klicks
    private boolean shock = false;;

    private int width, height;

    // BlurEffect instance
    private BlurEffect blurEffect;

    private volatile boolean running = true;

    public ObjektLadenUndDrehen(String title, int width, int height, String fileName, float size, boolean useKugel) {
        super(title, width, height);
        this.useKugel = useKugel;
        this.width = width;
        this.height = height;

        // JFrame und Canvas initialisieren
        JFrame f = new JFrame();
        f.setTitle(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        Canvas c = new Canvas();
        f.add(c, BorderLayout.CENTER);
        f.setSize(900, 600);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
                Display.destroy();
            }
        });

        new Thread(() -> {
            try {
                Display.setParent(c);
                Display.setDisplayMode(new DisplayMode(width, height));
                Display.create();
                initGL();
                blurEffect = new BlurEffect(width, height);
                blurEffect.prepareZweiRotierendeFrameBuffer();
                blurEffect.prepareShaderBlurEffect();
                blurEffect.prepareShaderVisualisierung();

                if (!useKugel) {
                    loadObject(fileName);
                    if (object != null) {
                        object.size = size;
                    }
                }

                agents = new ArrayList<>();
                for (int i = 0; i < 30; i++) {
                    Vektor2D position = new Vektor2D(random.nextDouble() * 1 - 0.5, random.nextDouble() * 1 - 0.5);
                    Vektor2D velocity = new Vektor2D(random.nextDouble() * 0.002 - 0.001, random.nextDouble() * 0.002 - 0.001);
                    agents.add(new Agent(i, position, velocity));
                }

                target = null;
                clickPosition = null;

                // Mouse-Listener zu Canvas hinzufügen
                c.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        int mouseX = e.getX();
                        int mouseY = e.getY();
                        // Transformiere Mauskoordinaten in Weltkoordinaten
                        float worldX = (float) (mouseX / (double) c.getWidth() * 4 - 2);
                        float worldY = (float) ((c.getHeight() - mouseY) / (double) c.getHeight() * 4 - 2); 
                        Vektor2D newTarget = new Vektor2D(worldX, -worldY + 1);
                        System.out.println("Mouse Clicked at: " + newTarget.x + ", " + newTarget.y);
                        for (Agent agent : agents) {
                            agent.moveToTarget(newTarget);
                        }

                        target = newTarget;  // Setze das neue Ziel
                        clickPosition = newTarget;  // Aktualisiere die Klickposition
                    }
                });

                renderLoop();
            } catch (LWJGLException e) {
                e.printStackTrace();
            }
        }).start();

        // reset-Button erstellen und hinzufügen
        JButton resetButton = new JButton("Reset Target");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                target = null;
                clickPosition = null;
                for (Agent agent : agents) {
                    agent.resetTarget();
                }
            }
        });
        
        JButton shockButton = new JButton("Shock swarm");
        shockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ex) {
                target = null;
                clickPosition = null;
                for (Agent agent : agents) {
                    agent.shock();
                }
                if (!shock) {
                	shock = true;
                	shockButton.setText("calm dowm");
                }
                else {
                	shock = false;
                	shockButton.setText("Shock swarm");
                }
            }
        });

        // Button zum JFrame hinzufügen
        JPanel panel = new JPanel();
        panel.setBackground(Color.black);
        panel.add(resetButton);
        panel.add(shockButton);
        f.add(panel, BorderLayout.NORTH);
    }

    private void initGL() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
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
        long lastTime = System.nanoTime();
        final double nsPerTick = 1e9 / 60;
        double t = (System.nanoTime() - lastTime) / 1e9;

        while (running && !Display.isCloseRequested()) {
            long now = System.nanoTime();
            double delta = (now - lastTime) / nsPerTick;
            lastTime = now;

            POGL.clearBackgroundWithColor(0.15f, 0.15f, 0.15f, 1.0f);

            glLoadIdentity();
            glFrustum(-1, 1, -1, 1, 4, 10);
            glTranslated(0, -1, -8);

            for (Agent agent : agents) {
                agent.flock(agents);
                agent.update(delta, shock);

                // Apply BlurEffect shader
                if (useKugel) {
                    blurEffect.applyBlurEffect(() -> {
                        glPushMatrix();
                        glTranslated(agent.position.x, agent.position.y, 0);
                        glScaled(0.015, 0.015, 0.015);
                        double red = 1.0;
                        double green = 0.8 + 0.2 * Math.sin(t); // Use current time in seconds
                        double blue = 0.0;
                        glColor3d(red, green, blue);

                        POGL.renderEgg(6);
                        glPopMatrix();
                    });
                } else {
                    blurEffect.applyBlurEffect(() -> {
                        glPushMatrix();
                        glTranslated(agent.position.x, agent.position.y, 0);
                        glRotatef((float) t * 20.0f, 0.0f, 0.0f, 1.0f);
                        glScaled(0.01, 0.01, 0.01);
                        double red = 1.0;
                        double green = 0.8 + 0.2 * Math.sin(t); // Use current time in seconds
                        double blue = 0.0;
                        glColor3d(red, green, blue);
                        if (object != null) {
                            POGL.renderObject(object);
                        }
                        glPopMatrix();
                    }, true);
                }
            }

            // Punkt Zeichnen und target setzen
            if (Mouse.isButtonDown(0)) {
                int mouseX = Mouse.getX();
                int mouseY = Mouse.getY();
                System.out.println("  Mouse X-Pos: " + mouseX);
                System.out.println("  Mouse Y-Pos: " + mouseY);
                // Transformiere Mauskoordinaten in Weltkoordinaten
                float worldX = (float) (mouseX / (double) Display.getWidth() * 2 - 1);
                float worldY = (float) ((Display.getHeight() - mouseY) / (double) Display.getHeight() * 2 - 1); // Anpassung für Y-Koordinate
                Vektor2D newTarget = new Vektor2D(worldX, -worldY);
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
                blurEffect.applyBlurEffect(() -> drawTarget(clickPosition));
            }

            Display.update();
            Display.sync(60); // framerate auf 60 FPS setzen
        }

        Display.destroy();
        System.exit(0);
    }

    private void drawTarget(Vektor2D target) {
        double initialRadius = 0.06;  // Initialer Radius

            glColor4d(1.0, 1.0, 0.0, 1);  // Setze die Farbe mit Alpha-Wert (Gelb)
            glPushMatrix();
            glTranslated(target.x, target.y, 0);
            glScaled(initialRadius, initialRadius, 1.0);  // Skaliere basierend auf dem aktuellen Layer
            drawCircle();  // Zeichne den Kreis
            glPopMatrix();
        
    }

    private void drawCircle() {
        int numSegments = 20;  // Anzahl der Segmente für den Kreis
        glBegin(GL_TRIANGLE_FAN);  // Beginne das Zeichnen eines Triangle Fan
        glVertex2f(0, 0);  // Setze den Mittelpunkt des Kreises

        for (int i = 0; i <= numSegments; i++) {
            double angle = 2 * Math.PI * i / numSegments;  // Berechne den Winkel für das aktuelle Segment
            glVertex2d(Math.cos(angle), Math.sin(angle));  // Setze den Vertex des aktuellen Segments
        }
        glEnd();  // Beende das Zeichnen
    }

    public static void main(String[] args) {
        JFrame selectionFrame = new JFrame("Wähle obj oder OpenGL");
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
                new ObjektLadenUndDrehen("OBJ Glühwürmchen", 890, 525, "objects/fireflyExtended.obj", 10, false).start();
            }
        });

        kugelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionFrame.dispose();
                new ObjektLadenUndDrehen("OpenGL Glühwürmchen", 890, 525, null, 10, true).start();
            }
        });

        selectionFrame.setVisible(true);
    }
}