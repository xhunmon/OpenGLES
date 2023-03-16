attribute vec4 a_Position;//顶点坐标
attribute vec2 a_TexCoord;//纹理坐标
varying vec2 v_TexCoord;//传递给片元的纹理坐标

uniform float u_Scale;

void main() {
    gl_Position = vec4(a_Position.x * u_Scale, a_Position.y * u_Scale, a_Position.zw);
    v_TexCoord = a_TexCoord;
}