#version 120

varying vec2 vTexCoord;
uniform sampler2D uTexture;
uniform float uBlurSize;

void main() {
    vec4 sum = vec4(0.0);

    sum += texture2D(uTexture, vec2(vTexCoord.x - 4.0*uBlurSize, vTexCoord.y)) * 0.05;
    sum += texture2D(uTexture, vec2(vTexCoord.x - 3.0*uBlurSize, vTexCoord.y)) * 0.09;
    sum += texture2D(uTexture, vec2(vTexCoord.x - 2.0*uBlurSize, vTexCoord.y)) * 0.12;
    sum += texture2D(uTexture, vec2(vTexCoord.x - 1.0*uBlurSize, vTexCoord.y)) * 0.15;
    sum += texture2D(uTexture, vec2(vTexCoord.x, vTexCoord.y)) * 0.16;
    sum += texture2D(uTexture, vec2(vTexCoord.x + 1.0*uBlurSize, vTexCoord.y)) * 0.15;
    sum += texture2D(uTexture, vec2(vTexCoord.x + 2.0*uBlurSize, vTexCoord.y)) * 0.12;
    sum += texture2D(uTexture, vec2(vTexCoord.x + 3.0*uBlurSize, vTexCoord.y)) * 0.09;
    sum += texture2D(uTexture, vec2(vTexCoord.x + 4.0*uBlurSize, vTexCoord.y)) * 0.05;

    gl_FragColor = sum;
}
