
const val vertexShader = """
attribute vec2 aPos;

varying vec2 vertPos;

void main() {
    vec2 vPos = aPos;
    gl_Position = vec4(vPos, 0.0, 1.0);
    vertPos = vPos;
}
"""

const val fragmentShader = """
precision highp float;

uniform vec4 uColor;

varying vec2 vertPos;

void main() {
    vec4 col = uColor + vertPos.y + vertPos.x;
    gl_FragColor = col;
}
"""