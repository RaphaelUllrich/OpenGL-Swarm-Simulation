

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_RGB32F;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import java.awt.Canvas;
import java.nio.ByteBuffer;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;



public class BlurEffektGPU extends LWJGLBasisFenster {
	public static String fragShaderBlureffect = ""
		+ "#version 130\n"
		+ ""
		+ "uniform sampler2D tex2;" 
		+ "uniform vec2 s;" 
		+ "" 
		+ "void main (void) {" 
		+ "	  gl_FragColor = (texture2D(tex2, gl_TexCoord[0].st+vec2(+s.x,    0)) +"
		+ "		              texture2D(tex2, gl_TexCoord[0].st+vec2(-s.x,    0)) +"
		+ "		              texture2D(tex2, gl_TexCoord[0].st+vec2(   0, +s.y)) +"
		+ "	               	  texture2D(tex2, gl_TexCoord[0].st+vec2(   0, -s.y)) +" 
		+ "		              texture2D(tex2, gl_TexCoord[0].st)) * 0.2 * 0.998;" 
		+ "}";
	
	public static String fragShaderBlureffectVisualisation = "" 
		+ "#version 130\n"
		+ ""
		+ "uniform sampler2D tex1;" 
		+ ""
		+ "void main (void) {"
		+ "	  gl_FragColor = texture2D(tex1, gl_TexCoord[0].st) * vec4(1.0, 1.0, 0.0, 1.0) * 5.0;" 
		+ "}";

	final int WB = WIDTH/4, HB = HEIGHT/4;
	
	private int blurTexture1, blurTexture1FB;
	private int blurTexture2, blurTexture2FB;
	private int gaussTexture;
	private int programBlureffect, programBlureffectVisualisation;
	private int uniform_fragShaderBlureffect_s, uniform_fragShaderBlureffect_tex2;
	private int uniform_fragShaderBlureffectVisualisation_tex1;
	
	public BlurEffektGPU(String title, int width, int height) {
        super(title, width, height);
		
		JFrame f = new JFrame();
		Canvas c = new Canvas();

		f.getContentPane().add(c);
		f.setBounds(0, 0, WIDTH, HEIGHT);
		f.setTitle(title);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initDisplay(c);
	}
	
	public void prepareZweiRotierendeFrameBuffer() {
		blurTexture1 	= glGenTextures();
		blurTexture1FB 	= glGenFramebuffers();
		glBindTexture(GL_TEXTURE_2D, blurTexture1);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, WB, HB, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glBindFramebuffer(GL_FRAMEBUFFER, blurTexture1FB);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture1, 0);

		blurTexture2 	= glGenTextures();
		blurTexture2FB 	= glGenFramebuffers();
		glBindTexture(GL_TEXTURE_2D, blurTexture2);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, WB, HB, 0, GL_BGRA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture2, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		glBindTexture(GL_TEXTURE_2D, 0); // steht nur hier, damit die Bl�cke austauschbar bleiben
	}
	
	public void prepareShaderBlurEffect() {
		programBlureffect = glCreateProgram();

		int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragShader, fragShaderBlureffect);
		glCompileShader(fragShader);
		System.out.println(glGetShaderInfoLog(fragShader, 1024));
		glAttachShader(programBlureffect, fragShader);

		glLinkProgram(programBlureffect);
		uniform_fragShaderBlureffect_s 		= glGetUniformLocation(programBlureffect, "s");
		uniform_fragShaderBlureffect_tex2 	= glGetUniformLocation(programBlureffect, "tex2");
		glUseProgram(programBlureffect);
		
		glUniform2f(uniform_fragShaderBlureffect_s, 1.0f / WB, 1.0f / HB);
		glUniform1i(uniform_fragShaderBlureffect_tex2, 0);
		
		ShaderUtilities.testShaderProgram(programBlureffect);
	}
	
	public void prepareShaderVisualisierung() {
		programBlureffectVisualisation = glCreateProgram();

		int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragShader, fragShaderBlureffectVisualisation);
		glCompileShader(fragShader);
		System.out.println(glGetShaderInfoLog(fragShader, 1024));
		glAttachShader(programBlureffectVisualisation, fragShader);

		glLinkProgram(programBlureffectVisualisation);
		uniform_fragShaderBlureffectVisualisation_tex1 = glGetUniformLocation(programBlureffectVisualisation, "tex1");
		glUseProgram(programBlureffectVisualisation);
		
		glUniform1i(uniform_fragShaderBlureffectVisualisation_tex1, 0);
		
		ShaderUtilities.testShaderProgram(programBlureffectVisualisation);
	}
	
	@Override
	public void renderLoop() {
		prepareZweiRotierendeFrameBuffer();
		gaussTexture = ShaderUtilities.prepareGaussTexture();
		
		prepareShaderBlurEffect();
		prepareShaderVisualisierung();

		while (!Display.isCloseRequested()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int helpFB 		= blurTexture1FB;
			int helpT  		= blurTexture1;
			blurTexture1FB 	= blurTexture2FB;
			blurTexture1 	= blurTexture2;
			blurTexture2FB 	= helpFB;
			blurTexture2 	= helpT;

			// **************************************
			// Wenn die Maustaste gedr�ckt wurde, wollen wir die Gauss-Verteilung, die wir
			// in der gaussTexture erzeugt haben, verwenden, um diese in den Blureffekt
			// einflie�en zu lassen. Schwarze Werte werden dabei durch die additive
			// Verkn�pfung vernachl�ssigt und lassen sich daher als transparent interpretieren.
			if (Mouse.isButtonDown(0)) {
				glBindFramebuffer(GL_FRAMEBUFFER, blurTexture2FB);
				glUseProgram(0);

				glViewport(0, 0, WB, HB);
				glLoadIdentity();
				glTranslated(Mouse.getX() * 2f / WIDTH - 1, Mouse.getY() * 2f / HEIGHT - 1, 0);
				glScaled(0.1, 0.1, 0.1); 

				glEnable(GL_BLEND);
				glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR);
				glEnable(GL_TEXTURE_2D);
				glBindTexture(GL_TEXTURE_2D, gaussTexture);
				glColor3f(1, 1, 1);
				
				POGL.renderViereckMitTexturbindung();
				
				glDisable(GL_TEXTURE_2D);
				glDisable(GL_BLEND);
			}
			// **************************************

			// **************************************
			// Der Shader blurEffectShader erzeugt aus der zweiten
			// Texture (waterTexture_2) den BlurEffekt und speichert diesen in FrameBuffer
			// (waterTexture_1_FBO). 
			glBindFramebuffer(GL_FRAMEBUFFER, blurTexture1FB);
			glUseProgram(programBlureffect);
			glViewport(0, 0, WB, HB);

			glClearColor(0, 0, 0, 1);
			glClear(GL_COLOR_BUFFER_BIT);
			
			glLoadIdentity();

			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, blurTexture2);

			POGL.renderViereckMitTexturbindung();

			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, 0);
			// **************************************

			// **************************************
			// Der Blureffekt wird jetzt von Frame zu
			// Frame berechnet und das im nicht-sichtbaren
			// Bereich. Um das Ganze jetzt anschaulich zu
			// machen verwenden wir den FrameBuffer, der den
			// Ausgabebereich definiert, in unserem Fall das
			// erzeugte Canvasobjekt im JFrame.
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glUseProgram(programBlureffectVisualisation);
			glViewport(0, 0, WIDTH, HEIGHT);

			glEnable(GL_TEXTURE_2D);
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, blurTexture1);

			POGL.renderViereckMitTexturbindung();
			
			glDisable(GL_TEXTURE_2D);

			// Gebundene Texturen in der umgekehrten Reihenfolge
			// frei geben.
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, 0);

			glUseProgram(0);
			// **************************************

			Display.update();
		}
		Display.destroy();
	}
	
	public static void main(String[] args) throws LWJGLException {
		new BlurEffektGPU("vividus Verlag. Dino-Buch. Kapitel 2: BlurEffektGPU.java", 800, 450).start();
	}
}