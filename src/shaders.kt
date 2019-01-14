

const val fragmentShader = """
precision highp float;

uniform vec4 uColor;

varying vec2 sPos;

void main() {
    gl_FragColor = uColor;
}
"""

const val vertexShader = """
attribute vec2 aVal;

varying vec2 sPos;

void main() {
    vec2 vPos = aVal;
    gl_Position = vec4(vPos, 0.0, 1.0);
    sPos = vPos;
}
"""

const val vertexShaderMat = """
attribute vec2 aVal;

varying vec2 sPos;

uniform vec2 uResolution;
uniform mat4 uPerspective;
uniform mat4 uModel;

void main() {
    vec2 zeroToOne = aVal / uResolution;
    vec2 zeroToTwo = zeroToOne * 2.0;
    vec2 clipSpace = zeroToTwo - 1.0;
    vec4 final = vec4(clipSpace * vec2(1, -1), 0.0, 1.0);
    gl_Position = final;
    sPos = vec2(final.x, final.y);
}
"""