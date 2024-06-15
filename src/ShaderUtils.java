import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

public class ShaderUtils {
    public static int loadShader(String vertexPath, String fragmentPath) {
        StringBuilder vertexCode = new StringBuilder();
        StringBuilder fragmentCode = new StringBuilder();

        try {
            BufferedReader vertexReader = new BufferedReader(new FileReader(vertexPath));
            BufferedReader fragmentReader = new BufferedReader(new FileReader(fragmentPath));
            String line;

            while ((line = vertexReader.readLine()) != null) {
                vertexCode.append(line).append("\n");
            }
            vertexReader.close();

            while ((line = fragmentReader.readLine()) != null) {
                fragmentCode.append(line).append("\n");
            }
            fragmentReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexCode.toString());
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentCode.toString());
        glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        checkCompileErrors(shaderProgram, "PROGRAM");

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return shaderProgram;
    }

    private static void checkCompileErrors(int shader, String type) {
        IntBuffer success = BufferUtils.createIntBuffer(1);
        if (type.equals("PROGRAM")) {
            glGetProgram(shader, GL_LINK_STATUS, success);
            if (success.get(0) == GL_FALSE) {
                System.err.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n" + glGetProgramInfoLog(shader, 1024));
            }
        } else {
            glGetShader(shader, GL_COMPILE_STATUS, success);
            if (success.get(0) == GL_FALSE) {
                System.err.println("ERROR::SHADER::" + type + "::COMPILATION_FAILED\n" + glGetShaderInfoLog(shader, 1024));
            }
        }
    }
}
