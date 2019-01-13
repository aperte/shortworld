import org.khronos.webgl.*
import org.khronos.webgl.WebGLRenderingContext as GL
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date.Companion.now
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

fun main(args: Array<String>) {
    document.body!!.onload = {
        val manager = Manager()
        println("Initialized!")
        window.requestAnimationFrame { manager.driver() }
    }
}

class RingBuffer2<T>(capacity: Int, init: (Int) -> T) {
    val inner = MutableList(capacity, init)
    var index = 0
    val roundTrip = capacity

    fun add(value: T) {
        if (index >= roundTrip) {
            index = 0
        }
        inner[index] = value
        index++
    }

    fun values(): List<T> {
        return inner.toList()
    }
}

class Manager {
    val canvas = document.getElementById("glCanvas") as HTMLCanvasElement
    val gl = canvas.getContext("webgl") as GL
    var program: WebGLProgram

    init {
        program = createProgram(gl, fragmentShader, vertexShader)
    }

    var ticks = 0L
    var last_second = 0L
    val circle = Circle(gl, program, 1.0f, 36)

    var elapsed: Double = 0.0
    var seconds = 0
    var elapsed_buffer = RingBuffer2(5, { 0L })
    var last = now()

    fun driver() {
        elapsed += now() - last // count time outside our control as well
        val start = now()
        if ((ticks % 1000) == 0L) { // every 1000 ticks
            val tpsec = ticks / max(seconds, 1)
            println("tick=$ticks, ticks_per_second=$tpsec")
            val averaged = elapsed_buffer.values().sum().div(5)
            println("average_ticks_per_last_5_seconds=$averaged")
        }

        logic()
        render()

        ticks++
        elapsed += now() - start
        if (elapsed.toInt() > 1000) {
            elapsed_buffer.add(ticks - last_second)
            seconds++
            elapsed = 0.0
            last_second = ticks
        }
        last = now()
        window.requestAnimationFrame { driver() }
    }

    fun render() {
        gl.viewport(0, 0, canvas.width, canvas.height)
        gl.clearColor(0f, 0f, 0f, 1f)
        gl.clear(GL.COLOR_BUFFER_BIT)

        gl.useProgram(program)
        val uColor = gl.getUniformLocation(program, "uColor")

        gl.uniform4fv(uColor, arrayOf(0.7f, 0.7f, 0.2f, 1f))

        //renderCircle(gl, program)
        circle.render()
        renderSquare(gl, program)
    }

    fun logic() {
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

    val aPos = gl.getAttribLocation(program, "aPos")
    gl.enableVertexAttribArray(aPos)
    gl.vertexAttribPointer(aPos, 2, GL.FLOAT, false, 0, 0)
    gl.drawArrays(GL.TRIANGLES, 0, square_sides.length / 2)
}

data class Vertex(val x: Float, val y: Float)

fun List<Vertex>.asArray(): Float32Array {
    val length = this.size * 2
    val vertices = Float32Array(length)
    var index = 0
    this.forEach {
        vertices[index++] = it.x
        vertices[index++] = it.y
    }
    return vertices
}

class Circle(val gl: GL, val program: WebGLProgram, radius: Float, segments: Int) {
    val bufname: WebGLBuffer
    val vertices: Int

    init {
        var segmentsActual = segments
        while (360 % segmentsActual != 0) {
            segmentsActual += 1
        }
        val startX = 0.0f * radius
        val startY = 0.0f * radius
        val array = listOf(Vertex(startX, startY)) + // first prepend the initial vertex
                (0..361)
                        .filter { it % (360 / segmentsActual) == 0 }
                        .map {
                            val rad: Float = it.toFloat() * (PI.toFloat() / 180)
                            Vertex(cos(rad) * radius, sin(rad) * radius)
                        }
        bufname = gl.createBuffer()!!
        gl.bindBuffer(GL.ARRAY_BUFFER, bufname)
        gl.bufferData(GL.ARRAY_BUFFER, array.asArray(), GL.STATIC_DRAW)
        vertices = array.size
    }

    fun render() {
        gl.bindBuffer(GL.ARRAY_BUFFER, bufname)

        val aPos = gl.getAttribLocation(program, "aPos")
        gl.enableVertexAttribArray(aPos)
        gl.vertexAttribPointer(aPos, 2, GL.FLOAT, false, 0, 0)
        gl.drawArrays(GL.TRIANGLE_FAN, 0, vertices)
    }
}

fun renderCircle(gl: GL, program: WebGLProgram, radius: Float = 1.0f, segments: Int = 36) {
    var segmentsActual = segments
    while (360 % segmentsActual != 0) {
        segmentsActual += 1
    }
    val startX = 0.0f * radius
    val startY = 0.0f * radius
    val vertices = listOf(Vertex(startX, startY)) + // first prepend the initial vertex
            (0..361)
            .filter { it % (360/segmentsActual) == 0 }
            .map {
                val rad: Float = it.toFloat() * (PI.toFloat() / 180)
                Vertex(cos(rad) * radius, sin(rad) * radius)
            }
    val buf = gl.createBuffer()
    gl.bindBuffer(GL.ARRAY_BUFFER, buf)
    gl.bufferData(GL.ARRAY_BUFFER, vertices.asArray(), GL.STATIC_DRAW)

    gl.useProgram(program)
    val uColor = gl.getUniformLocation(program, "uColor")

    gl.uniform4fv(uColor, arrayOf(0.7f, 0.7f, 0.2f, 1f))

    val aPos = gl.getAttribLocation(program, "aPos")
    gl.enableVertexAttribArray(aPos)
    gl.vertexAttribPointer(aPos, 2, GL.FLOAT, false, 0, 0)
    gl.drawArrays(GL.TRIANGLE_FAN, 0, vertices.size)
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
