import extra.math.*
import org.khronos.webgl.*
import org.khronos.webgl.WebGLRenderingContext as GL
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLOListElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
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

class PropertyTracker(val id: String, val updater: () -> String, owner: HTMLUListElement) {
    val element = document.createElement("li") as HTMLLIElement

    init {
        owner.appendChild(element)
    }

    fun update() {
        element.innerHTML = "$id=${updater()}"
    }
}

class Manager {
    val canvas = document.getElementById("glCanvas") as HTMLCanvasElement
    val gl = canvas.getContext("webgl") as GL
    val propertyList = document.getElementById("entries") as HTMLUListElement
    val properties: MutableList<PropertyTracker> = mutableListOf()
    var program: WebGLProgram
    val elements: MutableList<Circle> = mutableListOf()

    init {
        program = createProgram(gl, fragmentShader, vertexShaderMat)
        canvas.addEventListener("click", ::click_handler)
        canvas.addEventListener("keydown", ::keydown_handler)
        canvas.setAttribute("tabindex", "0")
        canvas.focus()
    }

    fun eventHandler(event: Event) {
        println("EVENT: ${event.type}")
    }

    fun click_handler(event: Event) {
        val mevent = event as MouseEvent
        val offsetLeft = canvas.offsetLeft
        val offsetTop = canvas.offsetTop
        val clientX = mevent.clientX - offsetLeft
        val clientY = mevent.clientY - offsetTop
        println("clientX=${clientX}")
        println("clientY=${clientY}")

        val new_circle = Circle(gl, program, 50f, 36, offsetX = clientX.toFloat(), offsetY = clientY.toFloat())
        elements.add(new_circle)
    }

    fun keydown_handler(event: Event) {
        val kevent = event as KeyboardEvent
        when (kevent.keyCode) {
            87 -> viewportY += 50
            83 -> viewportY -= 50
            65 -> viewportX -= 50
            68 -> viewportX += 50
            else -> {
                println("KEYDOWN=${kevent.keyCode} (${kevent.key})")
            }
        }
    }

    var ticks = 0L
    var last_second = 0L

    var elapsed: Double = 0.0
    var seconds = 0
    var elapsed_buffer = RingBuffer2(5, { 0L })
    var last = now()

    var viewportX = 0
    var viewportY = 0

    fun driver() {
        elapsed += now() - last // count time outside our control as well
        val start = now()

        for (tracker in properties) {
            tracker.update()
        }

        logic()
        render()

        ticks++
        elapsed += now() - start
        if (elapsed.toInt() > 1000) {
            elapsed_buffer.add(ticks - last_second)
            seconds++ // note: this will skip seconds that have elapsed when browser tab was inactive, we also do not keep remainders making it skip seconds eventually
            elapsed = 0.0
            last_second = ticks
        }
        last = now()
        window.requestAnimationFrame { driver() }
    }

    val circle = Circle(gl, program, 100f, 36)
    var width = canvas.clientWidth.toFloat()
    var height = canvas.clientHeight.toFloat()
    val model = Mat4.identity()
    val perspective = Mat4.identity()

    fun sortho(width: Float, height: Float, depth: Float): Mat4 {
        return Mat4(
                Float4(x = 2f / width),
                Float4(y = -2f / height),
                Float4(z = 2 / depth),
                Float4(x = -1f, y = 1f, z = 0f, w = 1f)
        )
    }

    fun resize() {
        if (height.toInt() != canvas.clientHeight) {
            height = canvas.clientHeight.toFloat()
            canvas.height = height.toInt()
        }
        if (width.toInt() != canvas.clientWidth) {
            width = canvas.clientWidth.toFloat()
            canvas.width = width.toInt()
        }
        gl.viewport(viewportX, viewportY, width.toInt(), height.toInt())
    }

    init {
        listOf(
                PropertyTracker("ticks", { "$ticks" }, propertyList),
                PropertyTracker("avg_ticks(5sec)", { elapsed_buffer.values().sum().div(5).toString() }, propertyList),
                PropertyTracker("viewport(X,Y)", { "($viewportX,$viewportY)" }, propertyList),
                PropertyTracker("canvas.(cW,cH)", {
                    "(${canvas.clientWidth.toString()},${canvas.clientHeight.toString()})"
                }, propertyList),
                PropertyTracker("canvas.(W,H)", {
                    "(${canvas.width.toString()},${canvas.height.toString()})"
                }, propertyList),
                PropertyTracker("(W,H)", { "($width,$height)"}, propertyList)
        ).forEach { properties.add(it) }
        canvas.height = height.toInt()
        canvas.width = width.toInt()
        println("dpr=${window.devicePixelRatio.toFloat()}")
    }

    val uPerspective = gl.getUniformLocation(program, "uPerspective")
    val uModel = gl.getUniformLocation(program, "uModel")
    val uResolution = gl.getUniformLocation(program, "uResolution")
    val uColor = gl.getUniformLocation(program, "uColor")

    fun render() {
        resize()
        //val perspective = ortho(0f, width, height, 0f, -1f, 1f)
        gl.clearColor(0f, 0f, 0f, 1f)
        gl.clear(GL.COLOR_BUFFER_BIT)

        gl.useProgram(program)

        gl.uniform4fv(uColor, arrayOf(0.7f, 0.7f, 0.2f, 1f))

        gl.uniform2fv(uResolution, arrayOf(width, height))
        gl.uniform4fv(uColor, arrayOf(0.7f, 0.7f, 0.2f, 1f))
        gl.uniformMatrix4fv(uPerspective, false, perspective.toFloatArray().toTypedArray())
        gl.uniformMatrix4fv(uModel, false, model.toFloatArray().toTypedArray())

        circle.render()
        for (ele in elements) {
            ele.render()
        }
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

    val aVal = gl.getAttribLocation(program, "aVal")
    gl.enableVertexAttribArray(aVal)
    gl.vertexAttribPointer(aVal, 2, GL.FLOAT, false, 0, 0)
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

class Circle(val gl: GL, val program: WebGLProgram, radius: Float, segments: Int, offsetX: Float = 0f, offsetY: Float = 0f) {
    val bufname: WebGLBuffer
    val vertices: Int

    init {
        var segmentsActual = segments
        while (360 % segmentsActual != 0) {
            segmentsActual += 1
        }
        val startX = offsetX + 0.0f
        val startY = offsetY + 0.0f
        val array = listOf(Vertex(startX, startY)) + // first prepend the initial vertex
                (0..361)
                        .filter { it % (360 / segmentsActual) == 0 }
                        .map {
                            val rad: Float = it.toFloat() * (PI.toFloat() / 180)
                            Vertex(offsetX + (cos(rad) * radius), offsetY + (sin(rad) * radius))
                        }
        bufname = gl.createBuffer()!!
        gl.bindBuffer(GL.ARRAY_BUFFER, bufname)
        gl.bufferData(GL.ARRAY_BUFFER, array.asArray(), GL.STATIC_DRAW)
        vertices = array.size
    }

    fun render() {
        gl.bindBuffer(GL.ARRAY_BUFFER, bufname)

        val aVal = gl.getAttribLocation(program, "aVal")
        gl.enableVertexAttribArray(aVal)
        gl.vertexAttribPointer(aVal, 2, GL.FLOAT, false, 0, 0)
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

    val aVal = gl.getAttribLocation(program, "aVal")
    gl.enableVertexAttribArray(aVal)
    gl.vertexAttribPointer(aVal, 2, GL.FLOAT, false, 0, 0)
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
