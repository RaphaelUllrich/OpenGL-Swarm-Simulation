#version 120

varying vec2 v_texCoords;
uniform sampler2D scene;
uniform vec2 resolution;
uniform float radius;

void main() {
    vec2 tex_offset = 1.0 / resolution; // Gets size of single texel
    vec4 result = vec4(0.0);

    // Sample from 9 surrounding texels
    for (int x = -4; x <= 4; ++x) {
        for (int y = -4; y <= 4; ++y) {
            vec2 offset = vec2(float(x), float(y)) * tex_offset * radius;
            result += texture2D(scene, v_texCoords + offset);
        }
    }

    result /= 81.0; // Average the samples
    gl_FragColor = result;
}
