#version 120

attribute vec2 position;
attribute vec2 texCoords;

varying vec2 v_texCoords;

void main() {
    gl_Position = vec4(position, 0.0, 1.0);
    v_texCoords = texCoords;
}
