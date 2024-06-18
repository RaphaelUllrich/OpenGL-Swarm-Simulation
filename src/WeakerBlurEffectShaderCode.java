public class WeakerBlurEffectShaderCode {
    public static final String FRAG_SHADER_WEAKER_BLUR_EFFECT = 
        "#version 130\n" +
        "uniform sampler2D tex2;" +
        "uniform vec2 s;" +
        "void main(void) {" +
        "    vec4 color = texture2D(tex2, gl_TexCoord[0].st) * 0.90;" + // Zentraler Pixel stärker gewichtet
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(+s.x, 0)) * 0.019;" + // Benachbarte Pixel geringer gewichtet
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(-s.x, 0)) * 0.019;" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(0, +s.y)) * 0.019;" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(0, -s.y)) * 0.019;" +
        "    gl_FragColor = color * 0.998;" + // Abschwächung der Farbe leicht beibehalten
        "}";
}
