fun main(args: Array<String>) {
    val msg = "Hello, world! We digress."

    println(msg)

    println(msg.remake())

    val rotted = msg.rot13()
    println(rotted)
    println(rotted.derot())
}

fun String.remake(): String {
    val str = this.map { it.toByte().toChar() }
    return str.joinToString("")
}

fun String.rot13(): String {
    return this.map {
        val byte: Byte = (it.toByte() + 13.toByte()).toByte()
        byte.toChar()
    }.joinToString ( "" )
}

fun String.derot(): String {
    return this.map {
        val byte: Byte = (it.toByte() - 13.toByte()).toByte()
        byte.toChar()
    }.joinToString("")
}