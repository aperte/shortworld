import org.khronos.webgl.Float32Array
import org.khronos.webgl.WebGLProgram
import org.khronos.webgl.WebGLShader
import org.khronos.webgl.set
import org.khronos.webgl.WebGLRenderingContext as GL
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

fun main(args: Array<String>) {
    document.body!!.onload = {
        val manager = Manager()
        println("Initialized!")
        window.requestAnimationFrame { manager.render() }
    }
}

class Manager {
    val canvas = document.getElementById("glCanvas") as HTMLCanvasElement
    val gl = canvas.getContext("webgl") as GL
    var program: WebGLProgram

    init {
        program = createProgram(gl, fragmentShader, vertexShader)
    }

    fun render() {
        gl.viewport(0, 0, canvas.width, canvas.height)
        gl.clearColor(0f, 0f, 0f, 1f)
        gl.clear(GL.COLOR_BUFFER_BIT)

        renderCircle(gl, program)
        renderSquare(gl, program)
    }
}

fun renderSquare(gl: GL, program: WebGLProgram) {
    val square_sides = Float32Array(
            arrayOf( 0f, 1f, 1f, 1f, 1f, 0f,
                    0f, 1f, 1f, 0f, 0f, 0f )
    )
    val vbuf = gl.createBuffer()
    gl.bindBuffer(GL.ARRAY_BUFFER, vbuf)
    gl.bufferData(GL.ARRAY_BUFFER, square_sides, GL.STATIC_DRAW)

    gl.useProgram(program)
    val uColor = gl.getUniformLocation(program, "uColor")

    gl.uniform4fv(uColor, arrayOf(1.0f, 1.0f, 0.2f, 1f))

    val aPos = gl.getAttribLocation(program, "aPos")
    gl.enableVertexAttribArray(aPos)
    gl.vertexAttribPointer(aPos, 2, GL.FLOAT, false, 0, 0)
    gl.drawArrays(GL.TRIANGLES, 0, square_sides.length / 2)
}

fun renderCircle(gl: GL, program: WebGLProgram) {
    val segments = 12
    val scale = 0.5f
    // +2 = start and end
    val cnt = (segments + 2) * 2
    val array = Float32Array(cnt)
    var index = 0
    array[index++] = -1.0f * scale
    array[index++] = -1.0f * scale
    (0..361)
            .filter { it % (360/segments) == 0 }
            .map { it.toFloat() }
            .forEach {
                val rad: Float = it * (PI.toFloat() / 180)
                array[index++] = cos(rad) * scale
                array[index++] = sin(rad) * scale
            }
    val vbuf = gl.createBuffer()
    gl.bindBuffer(GL.ARRAY_BUFFER, vbuf)
    gl.bufferData(GL.ARRAY_BUFFER, array.also(::println), GL.STATIC_DRAW)

    gl.useProgram(program)
    val uColor = gl.getUniformLocation(program, "uColor")

    gl.uniform4fv(uColor, arrayOf(0.7f, 0.7f, 0.2f, 1f))

    val aPos = gl.getAttribLocation(program, "aPos")
    gl.enableVertexAttribArray(aPos)
    gl.vertexAttribPointer(aPos, 2, GL.FLOAT, false, 0, 0)
    gl.drawArrays(GL.TRIANGLE_FAN, 0, array.length / 2)
}

fun compileShader(gl: GL, code: String, type: Int): WebGLShader {
    val shader = gl.createShader(type)
    gl.shaderSource(shader, code)
    gl.compileShader(shader)

    return shader!!
}

fun createProgram(gl: GL, frag_code: String, vertex_code: String): WebGLProgram {
    val prog = gl.createProgram()
    val frag_shader = compileShader(gl, frag_code, GL.FRAGMENT_SHADER)
    gl.attachShader(prog, frag_shader)

    val vshader = compileShader(gl, vertex_code, GL.VERTEX_SHADER)
    gl.attachShader(prog, vshader)

    gl.linkProgram(prog)

    return prog!!
}

private val fragmentShader = """
precision highp float;

uniform vec4 uColor;

varying vec2 vertPos;

void main() {
    vec4 col = uColor + vertPos.y;
    gl_FragColor = col;
}
"""

private val vertexShader = """
attribute vec2 aPos;

varying vec2 vertPos;

void main() {
    vec2 vPos = aPos;
    gl_Position = vec4(vPos, 0.0, 1.0);
    vertPos = vPos;
}
"""
