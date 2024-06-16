import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtils {

    public static int loadShaders(String vertexPath, String fragmentPath) {
        int vertexShader = createShader(vertexPath, GL_VERTEX_SHADER);
        int fragmentShader = createShader(fragmentPath, GL_FRAGMENT_SHADER);

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        IntBuffer linked = BufferUtils.createIntBuffer(1);
        glGetProgram(program, GL_LINK_STATUS, linked);
        if (linked.get(0) == GL_FALSE) {
            System.err.println("Shader program linking failed.");
            //System.err.println(glGetProgramInfoLog(program));
            return -1;
        }

        glValidateProgram(program);
        return program;
    }

    private static int createShader(String filePath, int shaderType) {
        StringBuilder shaderSource = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int shader = glCreateShader(shaderType);
        glShaderSource(shader, shaderSource);
        glCompileShader(shader);

        IntBuffer compiled = BufferUtils.createIntBuffer(1);
        glGetShader(shader, GL_COMPILE_STATUS, compiled);
        if (compiled.get(0) == GL_FALSE) {
            System.err.println("Shader compilation failed for " + filePath);
            //System.err.println(glGetShaderInfoLog(shader));
            return -1;
        }

        return shader;
    }
}
