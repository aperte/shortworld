if (typeof kotlin === 'undefined') {
  throw new Error("Error loading module 'shortworld'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'shortworld'.");
}
var shortworld = function (_, Kotlin) {
  'use strict';
  var println = Kotlin.kotlin.io.println_s8jyv4$;
  var Unit = Kotlin.kotlin.Unit;
  var ensureNotNull = Kotlin.ensureNotNull;
  var toList = Kotlin.kotlin.collections.toList_7wnvza$;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var L0 = Kotlin.Long.ZERO;
  var equals = Kotlin.equals;
  var sum = Kotlin.kotlin.collections.sum_dmxgdv$;
  var numberToInt = Kotlin.numberToInt;
  var throwCCE = Kotlin.throwCCE;
  var listOf = Kotlin.kotlin.collections.listOf_mh5how$;
  var IntRange = Kotlin.kotlin.ranges.IntRange;
  var math = Kotlin.kotlin.math;
  var plus = Kotlin.kotlin.collections.plus_mydzjv$;
  var vertexShader;
  var fragmentShader;
  function main$lambda$lambda(closure$manager) {
    return function (it) {
      closure$manager.driver();
      return Unit;
    };
  }
  function main$lambda(it) {
    var manager = new Manager();
    println('Initialized!');
    return window.requestAnimationFrame(main$lambda$lambda(manager));
  }
  function main(args) {
    ensureNotNull(document.body).onload = main$lambda;
  }
  var ArrayList_init = Kotlin.kotlin.collections.ArrayList_init_ww73n8$;
  function RingBuffer2(capacity, init) {
    var list = ArrayList_init(capacity);
    for (var index = 0; index < capacity; index++) {
      list.add_11rb$(init(index));
    }
    this.inner = list;
    this.index = 0;
    this.roundTrip = capacity;
  }
  RingBuffer2.prototype.add_11rb$ = function (value) {
    if (this.index >= this.roundTrip) {
      this.index = 0;
    }
    this.inner.set_wxm5ur$(this.index, value);
    this.index = this.index + 1 | 0;
  };
  RingBuffer2.prototype.values = function () {
    return toList(this.inner);
  };
  RingBuffer2.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'RingBuffer2',
    interfaces: []
  };
  function Manager() {
    var tmp$, tmp$_0;
    this.canvas = Kotlin.isType(tmp$ = document.getElementById('glCanvas'), HTMLCanvasElement) ? tmp$ : throwCCE();
    this.gl = Kotlin.isType(tmp$_0 = this.canvas.getContext('webgl'), WebGLRenderingContext) ? tmp$_0 : throwCCE();
    this.program = null;
    this.program = createProgram(this.gl, fragmentShader, vertexShader);
    this.ticks = L0;
    this.last_second = L0;
    this.circle = new Circle(this.gl, this.program, 1.0, 36);
    this.elapsed = 0.0;
    this.seconds = 0;
    this.elapsed_buffer = new RingBuffer2(5, Manager$elapsed_buffer$lambda);
    this.last = Date.now();
  }
  function Manager$driver$lambda(this$Manager) {
    return function (it) {
      this$Manager.driver();
      return Unit;
    };
  }
  var Math_0 = Math;
  Manager.prototype.driver = function () {
    this.elapsed += Date.now() - this.last;
    var start = Date.now();
    if (equals(this.ticks.modulo(Kotlin.Long.fromInt(1000)), L0)) {
      var tmp$ = this.ticks;
      var a = this.seconds;
      var tpsec = tmp$.div(Kotlin.Long.fromInt(Math_0.max(a, 1)));
      println('tick=' + this.ticks.toString() + ', ticks_per_second=' + tpsec.toString());
      var averaged = sum(this.elapsed_buffer.values()).div(Kotlin.Long.fromInt(5));
      println('average_ticks_per_last_5_seconds=' + averaged.toString());
    }
    this.logic();
    this.render();
    this.ticks = this.ticks.inc();
    this.elapsed += Date.now() - start;
    if (numberToInt(this.elapsed) > 1000) {
      this.elapsed_buffer.add_11rb$(this.ticks.subtract(this.last_second));
      this.seconds = this.seconds + 1 | 0;
      this.elapsed = 0.0;
      this.last_second = this.ticks;
    }
    this.last = Date.now();
    window.requestAnimationFrame(Manager$driver$lambda(this));
  };
  Manager.prototype.render = function () {
    this.gl.viewport(0, 0, this.canvas.width, this.canvas.height);
    this.gl.clearColor(0.0, 0.0, 0.0, 1.0);
    this.gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT);
    this.gl.useProgram(this.program);
    var uColor = this.gl.getUniformLocation(this.program, 'uColor');
    this.gl.uniform4fv(uColor, [0.7, 0.7, 0.2, 1.0]);
    this.circle.render();
    renderSquare(this.gl, this.program);
  };
  Manager.prototype.logic = function () {
  };
  function Manager$elapsed_buffer$lambda(it) {
    return L0;
  }
  Manager.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Manager',
    interfaces: []
  };
  function renderSquare(gl, program) {
    var square_sides = new Float32Array([0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0]);
    var vbuf = gl.createBuffer();
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vbuf);
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, square_sides, WebGLRenderingContext.STATIC_DRAW);
    var aPos = gl.getAttribLocation(program, 'aPos');
    gl.enableVertexAttribArray(aPos);
    gl.vertexAttribPointer(aPos, 2, WebGLRenderingContext.FLOAT, false, 0, 0);
    gl.drawArrays(WebGLRenderingContext.TRIANGLES, 0, square_sides.length / 2 | 0);
  }
  function Vertex(x, y) {
    this.x = x;
    this.y = y;
  }
  Vertex.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Vertex',
    interfaces: []
  };
  Vertex.prototype.component1 = function () {
    return this.x;
  };
  Vertex.prototype.component2 = function () {
    return this.y;
  };
  Vertex.prototype.copy_dleff0$ = function (x, y) {
    return new Vertex(x === void 0 ? this.x : x, y === void 0 ? this.y : y);
  };
  Vertex.prototype.toString = function () {
    return 'Vertex(x=' + Kotlin.toString(this.x) + (', y=' + Kotlin.toString(this.y)) + ')';
  };
  Vertex.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.x) | 0;
    result = result * 31 + Kotlin.hashCode(this.y) | 0;
    return result;
  };
  Vertex.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.x, other.x) && Kotlin.equals(this.y, other.y)))));
  };
  function asArray($receiver) {
    var length = $receiver.size * 2 | 0;
    var vertices = new Float32Array(length);
    var index = {v: 0};
    var tmp$;
    tmp$ = $receiver.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      var tmp$_0, tmp$_1;
      vertices[tmp$_0 = index.v, index.v = tmp$_0 + 1 | 0, tmp$_0] = element.x;
      vertices[tmp$_1 = index.v, index.v = tmp$_1 + 1 | 0, tmp$_1] = element.y;
    }
    return vertices;
  }
  var ArrayList_init_0 = Kotlin.kotlin.collections.ArrayList_init_287e2$;
  var collectionSizeOrDefault = Kotlin.kotlin.collections.collectionSizeOrDefault_ba2ldo$;
  function Circle(gl, program, radius, segments) {
    this.gl = gl;
    this.program = program;
    this.bufname = null;
    this.vertices = 0;
    var segmentsActual = {v: segments};
    while (360 % segmentsActual.v !== 0) {
      segmentsActual.v = segmentsActual.v + 1 | 0;
    }
    var startX = 0.0 * radius;
    var startY = 0.0 * radius;
    var tmp$ = listOf(new Vertex(startX, startY));
    var $receiver = new IntRange(0, 361);
    var destination = ArrayList_init_0();
    var tmp$_0;
    tmp$_0 = $receiver.iterator();
    while (tmp$_0.hasNext()) {
      var element = tmp$_0.next();
      if (element % (360 / segmentsActual.v | 0) === 0)
        destination.add_11rb$(element);
    }
    var destination_0 = ArrayList_init(collectionSizeOrDefault(destination, 10));
    var tmp$_1;
    tmp$_1 = destination.iterator();
    while (tmp$_1.hasNext()) {
      var item = tmp$_1.next();
      var rad = item * (math.PI / 180);
      destination_0.add_11rb$(new Vertex(Math_0.cos(rad) * radius, Math_0.sin(rad) * radius));
    }
    var array = plus(tmp$, destination_0);
    this.bufname = ensureNotNull(this.gl.createBuffer());
    this.gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, this.bufname);
    this.gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, asArray(array), WebGLRenderingContext.STATIC_DRAW);
    this.vertices = array.size;
  }
  Circle.prototype.render = function () {
    this.gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, this.bufname);
    var aPos = this.gl.getAttribLocation(this.program, 'aPos');
    this.gl.enableVertexAttribArray(aPos);
    this.gl.vertexAttribPointer(aPos, 2, WebGLRenderingContext.FLOAT, false, 0, 0);
    this.gl.drawArrays(WebGLRenderingContext.TRIANGLE_FAN, 0, this.vertices);
  };
  Circle.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Circle',
    interfaces: []
  };
  function renderCircle(gl, program, radius, segments) {
    if (radius === void 0)
      radius = 1.0;
    if (segments === void 0)
      segments = 36;
    var segmentsActual = {v: segments};
    while (360 % segmentsActual.v !== 0) {
      segmentsActual.v = segmentsActual.v + 1 | 0;
    }
    var startX = 0.0 * radius;
    var startY = 0.0 * radius;
    var tmp$ = listOf(new Vertex(startX, startY));
    var $receiver = new IntRange(0, 361);
    var destination = ArrayList_init_0();
    var tmp$_0;
    tmp$_0 = $receiver.iterator();
    while (tmp$_0.hasNext()) {
      var element = tmp$_0.next();
      if (element % (360 / segmentsActual.v | 0) === 0)
        destination.add_11rb$(element);
    }
    var destination_0 = ArrayList_init(collectionSizeOrDefault(destination, 10));
    var tmp$_1;
    tmp$_1 = destination.iterator();
    while (tmp$_1.hasNext()) {
      var item = tmp$_1.next();
      var rad = item * (math.PI / 180);
      destination_0.add_11rb$(new Vertex(Math_0.cos(rad) * radius, Math_0.sin(rad) * radius));
    }
    var vertices = plus(tmp$, destination_0);
    var buf = gl.createBuffer();
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buf);
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, asArray(vertices), WebGLRenderingContext.STATIC_DRAW);
    gl.useProgram(program);
    var uColor = gl.getUniformLocation(program, 'uColor');
    gl.uniform4fv(uColor, [0.7, 0.7, 0.2, 1.0]);
    var aPos = gl.getAttribLocation(program, 'aPos');
    gl.enableVertexAttribArray(aPos);
    gl.vertexAttribPointer(aPos, 2, WebGLRenderingContext.FLOAT, false, 0, 0);
    gl.drawArrays(WebGLRenderingContext.TRIANGLE_FAN, 0, vertices.size);
  }
  function compileShader(gl, code, type) {
    var shader = gl.createShader(type);
    gl.shaderSource(shader, code);
    gl.compileShader(shader);
    return ensureNotNull(shader);
  }
  function createProgram(gl, frag_code, vertex_code) {
    var prog = gl.createProgram();
    var frag_shader = compileShader(gl, frag_code, WebGLRenderingContext.FRAGMENT_SHADER);
    gl.attachShader(prog, frag_shader);
    var vshader = compileShader(gl, vertex_code, WebGLRenderingContext.VERTEX_SHADER);
    gl.attachShader(prog, vshader);
    gl.linkProgram(prog);
    return ensureNotNull(prog);
  }
  Object.defineProperty(_, 'vertexShader', {
    get: function () {
      return vertexShader;
    }
  });
  Object.defineProperty(_, 'fragmentShader', {
    get: function () {
      return fragmentShader;
    }
  });
  _.main_kand9s$ = main;
  _.RingBuffer2 = RingBuffer2;
  _.Manager = Manager;
  _.renderSquare_mtoon9$ = renderSquare;
  _.Vertex = Vertex;
  _.asArray_5w3ql4$ = asArray;
  _.Circle = Circle;
  _.renderCircle_ott0zc$ = renderCircle;
  _.compileShader_gc78yd$ = compileShader;
  _.createProgram_h0kzx1$ = createProgram;
  vertexShader = '\nattribute vec2 aPos;\n\nvarying vec2 vertPos;\n\nvoid main() {\n    vec2 vPos = aPos;\n    gl_Position = vec4(vPos, 0.0, 1.0);\n    vertPos = vPos;\n}\n';
  fragmentShader = '\nprecision highp float;\n\nuniform vec4 uColor;\n\nvarying vec2 vertPos;\n\nvoid main() {\n    vec4 col = uColor + vertPos.y + vertPos.x;\n    gl_FragColor = col;\n}\n';
  main([]);
  Kotlin.defineModule('shortworld', _);
  return _;
}(typeof shortworld === 'undefined' ? {} : shortworld, kotlin);

//# sourceMappingURL=shortworld.js.map
