import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.lwjgl.opengl.Display;

public class ObjektLadenUndDrehen extends LWJGLBasisFenster {
    private static final String FRAG_SHADER_BLUR = ""
            + "#version 130\n"
            + "uniform sampler2D tex2;\n"
            + "uniform vec2 s;\n"
            + "void main (void) {\n"
            + "    gl_FragColor = (texture2D(tex2, gl_TexCoord[0].st + vec2(+s.x, 0)) +\n"
            + "                   texture2D(tex2, gl_TexCoord[0].st + vec2(-s.x, 0)) +\n"
            + "                   texture2D(tex2, gl_TexCoord[0].st + vec2(0, +s.y)) +\n"
            + "                   texture2D(tex2, gl_TexCoord[0].st + vec2(0, -s.y)) +\n"
            + "                   texture2D(tex2, gl_TexCoord[0].st)) * 0.2 * 0.998;\n"
            + "}";
    
    private static final String FRAG_SHADER_VISUALIZE = ""
            + "#version 130\n"
            + "uniform sampler2D tex1;\n"
            + "void main (void) {\n"
            + "    gl_FragColor = texture2D(tex1, gl_TexCoord[0].st) * vec4(1.0, 1.0, 0.0, 1.0) * 5.0;\n"
            + "}";
    
    private int width, height;
    private int blurTexture1, blurTexture1FB;
    private int blurTexture2, blurTexture2FB;
    private int gaussTexture;
    private int programBlurEffect, programBlurEffectVisualization;
    private int uniformBlurEffect_s, uniformBlurEffect_tex2;
    private int uniformBlurEffectVisualization_tex1;

    List<Agent> agents;
    Model object = null;
    boolean useKugel = false;
    Random random = new Random();

    public ObjektLadenUndDrehen(String title, int width, int height, String fileName, float size, boolean useKugel) {
        super(title, width, height);
        this.width = width;
        this.height = height;
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
            Vektor2D position = new Vektor2D(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
            Vektor2D velocity = new Vektor2D(random.nextDouble() * 0.002 - 0.001, random.nextDouble() * 0.002 - 0.001);
            agents.add(new Agent(i, position, velocity));
        }

        initDisplay(c);
        prepareFramebuffersAndTextures();
        prepareShaders();
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

    private void prepareFramebuffersAndTextures() {
        blurTexture1 = glGenTextures();
        blurTexture1FB = glGenFramebuffers();
        glBindTexture(GL_TEXTURE_2D, blurTexture1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width / 4, height / 4, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture1FB);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture1, 0);

        blurTexture2 = glGenTextures();
        blurTexture2FB = glGenFramebuffers();
        glBindTexture(GL_TEXTURE_2D, blurTexture2);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width / 4, height / 4, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture2, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void prepareShaders() {
        programBlurEffect = glCreateProgram();
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, FRAG_SHADER_BLUR);
        glCompileShader(fragShader);
        glAttachShader(programBlurEffect, fragShader);
        glLinkProgram(programBlurEffect);
        uniformBlurEffect_s = glGetUniformLocation(programBlurEffect, "s");
        uniformBlurEffect_tex2 = glGetUniformLocation(programBlurEffect, "tex2");
        glUseProgram(programBlurEffect);
        glUniform2f(uniformBlurEffect_s, 1.0f / (width / 4), 1.0f / (height / 4));
        glUniform1i(uniformBlurEffect_tex2, 0);

        programBlurEffectVisualization = glCreateProgram();
        fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, FRAG_SHADER_VISUALIZE);
        glCompileShader(fragShader);
        glAttachShader(programBlurEffectVisualization, fragShader);
        glLinkProgram(programBlurEffectVisualization);
        uniformBlurEffectVisualization_tex1 = glGetUniformLocation(programBlurEffectVisualization, "tex1");
        glUseProgram(programBlurEffectVisualization);
        glUniform1i(uniformBlurEffectVisualization_tex1, 0);
    }

    @Override
    public void renderLoop() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        long start = System.nanoTime();
        while (!Display.isCloseRequested()) {
            double t = (System.nanoTime() - start) / 1e9;

            // Render scene to texture
            glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
            glViewport(0, 0, width / 4, height / 4);
            glClearColor(0, 0, 0, 1);
            glClear(GL_COLOR_BUFFER_BIT);
            renderScene(t);

            // Apply blur effect
            glBindFramebuffer(GL_FRAMEBUFFER, blurTexture1FB);
            glUseProgram(programBlurEffect);
            glClearColor(0, 0, 0, 1);
            glClear(GL_COLOR_BUFFER_BIT);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, blurTexture2);
            renderFullscreenQuad();

            // Visualize the blur effect
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, width, height);
            glUseProgram(programBlurEffectVisualization);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, blurTexture1);
            renderFullscreenQuad();

            Display.update();
        }
    }

    private void renderScene(double t) {
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
    }

    private void renderFullscreenQuad() {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(-1, -1);
        glTexCoord2f(1, 0);
        glVertex2f(1, -1);
        glTexCoord2f(1, 1);
        glVertex2f(1, 1);
        glTexCoord2f(0, 1);
        glVertex2f(-1, 1);
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
