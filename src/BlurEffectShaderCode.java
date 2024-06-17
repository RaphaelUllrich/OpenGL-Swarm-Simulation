public class BlurEffectShaderCode {
	public static final String VERT_SHADER_PASS_THROUGH = 
	        "#version 130\n" +
	        "in vec2 position;" +
	        "void main(void) {" +
	        "    gl_Position = vec4(position, 0.0, 1.0);" +
	        "}";
	
    public static final String FRAG_SHADER_BLUR_EFFECT = 
        "#version 130\n" +
        "uniform sampler2D tex2;" +
        "uniform vec2 s;" +
        "void main(void) {" +
        "    vec4 color = texture2D(tex2, gl_TexCoord[0].st) * 0.72;" + // Zentraler Pixel stärker gewichtet
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(+s.x, 0)) * 0.066;" + // Benachbarte Pixel geringer gewichtet
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(-s.x, 0)) * 0.066;" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(0, +s.y)) * 0.066;" +
        "    color += texture2D(tex2, gl_TexCoord[0].st + vec2(0, -s.y)) * 0.066;" +
        "    gl_FragColor = color * 0.998;" + // Abschwächung der Farbe leicht beibehalten
        "}";

    public static final String FRAG_SHADER_BLUR_EFFECT_VISUALISATION = 
        "#version 130\n" +
        "uniform sampler2D tex1;" +
        "void main(void) {" +
        "    gl_FragColor = texture2D(tex1, gl_TexCoord[0].st) * vec4(1.0, 1.0, 0.0, 1.0) * 5.0;" +
        "}";
}
