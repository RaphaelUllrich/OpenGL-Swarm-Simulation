import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;

public class BlurEffect {
    private int width, height;
    private int blurTexture1, blurTexture1FB;
    private int blurTexture2, blurTexture2FB;
    private int programBlurEffect, programBlurEffectVisualization;
    private int uniformBlurEffect_s, uniformBlurEffect_tex2;
    private int uniformBlurEffectVisualization_tex1;

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
            + "    gl_FragColor = texture2D(tex1, gl_TexCoord[0].st);\n"
            + "}";

    public BlurEffect(int width, int height) {
        this.width = width;
        this.height = height;
        prepareFramebufferAndShaders();
    }

    private void prepareFramebufferAndShaders() {
        // Framebuffer setup
        blurTexture1 = glGenTextures();
        blurTexture1FB = glGenFramebuffers();
        glBindTexture(GL_TEXTURE_2D, blurTexture1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture1FB);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture1, 0);

        blurTexture2 = glGenTextures();
        blurTexture2FB = glGenFramebuffers();
        glBindTexture(GL_TEXTURE_2D, blurTexture2);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture2, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Shader setup
        programBlurEffect = glCreateProgram();
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, FRAG_SHADER_BLUR);
        glCompileShader(fragShader);
        glAttachShader(programBlurEffect, fragShader);
        glLinkProgram(programBlurEffect);
        uniformBlurEffect_s = glGetUniformLocation(programBlurEffect, "s");
        uniformBlurEffect_tex2 = glGetUniformLocation(programBlurEffect, "tex2");

        programBlurEffectVisualization = glCreateProgram();
        fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, FRAG_SHADER_VISUALIZE);
        glCompileShader(fragShader);
        glAttachShader(programBlurEffectVisualization, fragShader);
        glLinkProgram(programBlurEffectVisualization);
        uniformBlurEffectVisualization_tex1 = glGetUniformLocation(programBlurEffectVisualization, "tex1");
    }

    public void applyBlurEffect() {
        int helpFB = blurTexture1FB;
        int helpT = blurTexture1;
        blurTexture1FB = blurTexture2FB;
        blurTexture1 = blurTexture2;
        blurTexture2FB = helpFB;
        blurTexture2 = helpT;

        // Apply blur effect
        glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(programBlurEffect);
        glUniform2f(uniformBlurEffect_s, 1.0f / width, 1.0f / height);
        glUniform1i(uniformBlurEffect_tex2, 0);
        glBindTexture(GL_TEXTURE_2D, blurTexture1);
        renderFullscreenQuad();
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    public void renderBlurredResult() {
        // Render the final result to the screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(programBlurEffectVisualization);
        glUniform1i(uniformBlurEffectVisualization_tex1, 0);
        glBindTexture(GL_TEXTURE_2D, blurTexture2);
        renderFullscreenQuad();
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
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
}
