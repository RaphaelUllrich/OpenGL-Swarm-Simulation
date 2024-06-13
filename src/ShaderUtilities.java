

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LUMINANCE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glGetProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class ShaderUtilities {
	private static ByteBuffer infoBuffer = BufferUtils.createByteBuffer(1024);
	private static IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
	
	public static String readShadercode(String file) throws Exception {
		FileReader fi;
		BufferedReader in;
		String line, content = "";
		
		try {
			fi = new FileReader(file);
			in = new BufferedReader(fi);
			while ((line = in.readLine()) != null) {
				content += line+"\n"; // newline wichtig f�r directive!
			}
			
			in.close();
			return content;
		}
		catch (Exception e) { throw new Exception("File not found"); }
	}
	
	public static void testShaderProgram(int shader) {
		errorBuffer.rewind();
		glGetProgram(shader, GL_LINK_STATUS, errorBuffer);
		System.out.println(errorBuffer.get(0)==GL_TRUE?"OK":"ERROR");
		int error = errorBuffer.get(0);
		errorBuffer.put(0,1024);
		glGetProgramInfoLog(shader, errorBuffer, infoBuffer);		
		if (error!=GL_TRUE) 
		{
			byte bytes[] = new byte[1024];
			infoBuffer.get(bytes).rewind();
			System.err.println(new String(bytes, 0, errorBuffer.get(0)));
		}
	}
	
	public static int prepareGaussTexture() {
		// Eine Texture mit einer 2D-Gauss-Verteilung wird erzeugt. Der Bereich l�uft von
		// 0 (transparent) bis 1 (voll gef�llt) und wird sp�ter additiv hinzugenommen.
		int textureNumber = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureNumber);
		final int WG = 32;
		final int HG = 32;
		FloatBuffer gaussBuffer = BufferUtils.createFloatBuffer(WG * HG);
		final float SIGMA_SQ = WG / 1, S = 0.4f;
		for (int y=0, Y=-HG/2; y<HG; y++, Y++)
			for (int x=0, X=-WG/2; x<WG; x++, X++)
				gaussBuffer.put(x+y*WG, S * (float)Math.exp(-0.5*(X*X+Y*Y)/SIGMA_SQ));

		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, WG, HG, 0, GL_LUMINANCE, GL_FLOAT, gaussBuffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glBindTexture(GL_TEXTURE_2D, 0); // steht nur hier, damit die Bl�cke austauschbar bleiben
		
		return textureNumber;
	}
}
