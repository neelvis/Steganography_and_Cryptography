fun main() {
    // write your code here
    val ch = List(4) { readLine()!![0].toChar() }

    ch.forEach { println(it - 1) }
}