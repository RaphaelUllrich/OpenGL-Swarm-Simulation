public class WeakerBlurEffectShaderCode {
    public static final String VERT_SHADER_ROTATION = 
        "#version 130\n" +
        "uniform float time;\n" +
        "in vec3 position;\n" +
        "in vec3 normal;\n" +
        "in vec2 texcoord;\n" +
        "out vec2 fragTexcoord;\n" +
        "out vec3 fragNormal;\n" +
        "void main() {\n" +
        "    float angle = time * 0.5;\n" +  // Rotation um 0.5 radian pro Sekunde
        "    mat4 rotationMatrix = mat4(\n" +
        "        cos(angle), -sin(angle), 0.0, 0.0,\n" +
        "        sin(angle),  cos(angle), 0.0, 0.0,\n" +
        "        0.0,         0.0,        1.0, 0.0,\n" +
        "        0.0,         0.0,        0.0, 1.0\n" +
        "    );\n" +
        "    vec4 rotatedPosition = rotationMatrix * vec4(position, 1.0);\n" +
        "    gl_Position = gl_ModelViewProjectionMatrix * rotatedPosition;\n" +
        "    fragTexcoord = texcoord;\n" +
        "    fragNormal = normal;\n" +
        "}";

    public static final String FRAG_SHADER_WEAKER_BLUR_EFFECT = 
        "#version 130\n" +
        "uniform sampler2D tex2;\n" +
        "uniform vec2 s;\n" +
        "void main(void) {\n" +
        "    vec4 color = texture2D(tex2, gl_TexCoord[0].st) * 0.95;\n" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(+s.x, 0)) * 0.01;\n" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(-s.x, 0)) * 0.01;\n" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(0, +s.y)) * 0.01;\n" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(0, -s.y)) * 0.01;\n" +
        "    gl_FragColor = color * 0.998;\n" +
        "}";
}
