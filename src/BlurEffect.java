import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;

public class BlurEffect extends LWJGLBasisFenster {
    public int blurTexture1, blurTexture1FB;
    public int blurTexture2, blurTexture2FB;
    public int gaussTexture;
    public int programBlureffect, programBlureffectVisualisation, programWeakerBlureffect;
    private int uniform_fragShaderBlureffect_s, uniform_fragShaderBlureffect_tex2;
    private int uniform_fragShaderBlureffectVisualisation_tex1, uniform_fragShaderWeakerBlureffect_s, uniform_fragShaderWeakerBlureffect_tex2;

    public final int WB, HB;

    private int width, height;

    public BlurEffect(int width, int height) {
        super("", width, height);
        this.width = width;
        this.height = height;
        this.WB = width / 4;
        this.HB = height / 4;
    }

    public void prepareZweiRotierendeFrameBuffer() {
        blurTexture1 = glGenTextures();
        blurTexture1FB = glGenFramebuffers();
        glBindTexture(GL_TEXTURE_2D, blurTexture1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, WB, HB, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture1FB);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture1, 0);

        blurTexture2 = glGenTextures();
        blurTexture2FB = glGenFramebuffers();
        glBindTexture(GL_TEXTURE_2D, blurTexture2);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, WB, HB, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture2, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        glBindTexture(GL_TEXTURE_2D, 0); // steht nur hier, damit die Blöcke austauschbar bleiben
    }

    public void prepareShaderBlurEffect() {
        programBlureffect = glCreateProgram();
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, BlurEffectShaderCode.FRAG_SHADER_BLUR_EFFECT);
        glCompileShader(fragShader);
        System.out.println(glGetShaderInfoLog(fragShader, 1024));
        glAttachShader(programBlureffect, fragShader);
        glLinkProgram(programBlureffect);
        uniform_fragShaderBlureffect_s = glGetUniformLocation(programBlureffect, "s");
        uniform_fragShaderBlureffect_tex2 = glGetUniformLocation(programBlureffect, "tex2");
        glUseProgram(programBlureffect);
        glUniform2f(uniform_fragShaderBlureffect_s, 1.0f / WB, 1.0f / HB);
        glUniform1i(uniform_fragShaderBlureffect_tex2, 0);
        ShaderUtilities.testShaderProgram(programBlureffect);

        // Prepare weaker blur effect shader
        programWeakerBlureffect = glCreateProgram();
        int fragShaderWeaker = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShaderWeaker, WeakerBlurEffectShaderCode.FRAG_SHADER_WEAKER_BLUR_EFFECT);
        glCompileShader(fragShaderWeaker);
        System.out.println(glGetShaderInfoLog(fragShaderWeaker, 1024));
        glAttachShader(programWeakerBlureffect, fragShaderWeaker);
        glLinkProgram(programWeakerBlureffect);
        uniform_fragShaderWeakerBlureffect_s = glGetUniformLocation(programWeakerBlureffect, "s");
        uniform_fragShaderWeakerBlureffect_tex2 = glGetUniformLocation(programWeakerBlureffect, "tex2");
        glUseProgram(programWeakerBlureffect);
        glUniform2f(uniform_fragShaderWeakerBlureffect_s, 1.0f / WB, 1.0f / HB);
        glUniform1i(uniform_fragShaderWeakerBlureffect_tex2, 0);
        ShaderUtilities.testShaderProgram(programWeakerBlureffect);
    }

    public void prepareShaderVisualisierung() {
        programBlureffectVisualisation = glCreateProgram();
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, BlurEffectShaderCode.FRAG_SHADER_BLUR_EFFECT_VISUALISATION);
        glCompileShader(fragShader);
        System.out.println(glGetShaderInfoLog(fragShader, 1024));
        glAttachShader(programBlureffectVisualisation, fragShader);
        glLinkProgram(programBlureffectVisualisation);
        uniform_fragShaderBlureffectVisualisation_tex1 = glGetUniformLocation(programBlureffectVisualisation, "tex1");
        glUseProgram(programBlureffectVisualisation);
        glUniform1i(uniform_fragShaderBlureffectVisualisation_tex1, 0);
        ShaderUtilities.testShaderProgram(programBlureffectVisualisation);
    }

    public void applyBlurEffect(Runnable renderContent) {
        applyBlurEffect(renderContent, false);
    }

    public void applyBlurEffect(Runnable renderContent, boolean weakerBlur) {
        int helpFB = blurTexture1FB;
        int helpT = blurTexture1;
        blurTexture1FB = blurTexture2FB;
        blurTexture1 = blurTexture2;
        blurTexture2FB = helpFB;
        blurTexture2 = helpT;

        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
        glUseProgram(0);
        glViewport(0, 0, WB, HB);
        glLoadIdentity();
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, gaussTexture);
        glColor3f(1, 1, 1);

        renderContent.run();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture1FB);
        glUseProgram(weakerBlur ? programWeakerBlureffect : programBlureffect);
        glViewport(0, 0, WB, HB);
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);
        glLoadIdentity();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, blurTexture2);

        POGL.renderViereckMitTexturbindung();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glUseProgram(programBlureffectVisualisation);
        glViewport(0, 0, width, height);
        glEnable(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, blurTexture1);

        POGL.renderViereckMitTexturbindung();

        glDisable(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    @Override
    public void renderLoop() {
        // No implementation needed for this class
    }
}