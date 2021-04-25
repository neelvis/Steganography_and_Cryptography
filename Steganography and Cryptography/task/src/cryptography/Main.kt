package cryptography

import java.awt.Color
import java.io.File
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.IOException

const val stringEOS = "000000000000000000000011"

fun main() {
    do {
        when (val choice = menu()) {
            "exit" -> { println("Bye!"); break }
            "hide" -> hide()
            "show" -> show()
            else -> println("Wrong task: $choice")
        }
    } while (true)
}

fun menu(): String {
    println("Task (hide, show, exit): ")
    return readLine()!!
}

fun Byte.to8bits(radix: Int): String {
    val s = this.toString(radix)
    var append = ""
    for(i in 1..8 - s.length)
        append += "0"
    return append+s
}

fun setBlueBit(color: Color, bit: Int): Int {
    // apply bit mask to a color to set least significant bit
    return if (color.blue % 2 == 0) {
        Color(color.red, color.green, color.blue.or(bit)).rgb
    } else {
        Color(color.red, color.green, color.blue.and(254 + bit)).rgb
    }
}

fun encrypt(message: ByteArray, password: ByteArray): String {
    var messageBits = ""
    message.forEach { messageBits += it.to8bits(2) }
    var passwordBits = ""
    password.forEach { passwordBits += it.to8bits(2) }
    var encryptedBits = ""
    for (i in messageBits.indices) {
        val index = i % passwordBits.length
        encryptedBits += messageBits[i].toInt().xor(passwordBits[index].toInt())
    }
    return encryptedBits
}

fun hide() {
    println("Input image file:")
    val iImageName = readLine()!!
    println("Output image file:")
    val oImageName = readLine()!!
    println("Input image: $iImageName")
    println("Output image: $oImageName")

    println("Message to hide:")
    val message = readLine()!!
    println("Password:")
    val password = readLine()!!
    val encryptedBits = encrypt(message.toByteArray(), password.toByteArray()) + stringEOS
    lateinit var imageRender: BufferedImage
    lateinit var oImageFile: File

    try {
        imageRender = ImageIO.read(File(iImageName))
    }
    catch(e: IOException) {
        println("Can't read input file!")
        return
    }
    val iWidth = imageRender.width
    val iHeight = imageRender.height

    if (iWidth * iHeight < encryptedBits.length) {
        println("The input image is not large enough to hold this message.")
        return
    }

    for (y in 0 until iHeight) {
        for (x in 0 until iWidth) {
            val color = Color(imageRender.getRGB(x, y))
            val pixelNumber = x + y * iHeight
            if (pixelNumber < encryptedBits.length) {
                val bit = encryptedBits[pixelNumber].toInt() - 48 // Char 0 = 48, Char 1 = 49
                imageRender.setRGB(x, y, setBlueBit(color, bit))

            } else {
                imageRender.setRGB(x, y, color.rgb)
            }
        }
    }

    try {
        oImageFile = File(oImageName)
        ImageIO.write(imageRender, "png", oImageFile)
    }
    catch(e: IOException) {
        println("Can't write to output file!")
        return
    }
    println("Message saved in $oImageName image.")
}

fun decrypt(encrypted: String, password: String): String {
    var messageBits = ""
    for (i in encrypted.indices) {
        val mBit = encrypted[i].toInt() - 48
        val pBit = password[i % password.length].toInt() - 48
        messageBits += mBit.xor(pBit).toString()
    }
    return messageBits
}

fun show() {
    println("Input image file:")
    val iImageName = readLine()!!
    println("Password:")
    var password = ""
    readLine()!!.toByteArray().forEach { password += it.to8bits(2) }
    lateinit var imageRender: BufferedImage
    try {
        imageRender = ImageIO.read(File(iImageName))
    }
    catch(e: IOException) {
        println("Can't read input file!")
        return
    }
    val iWidth = imageRender.width
    val iHeight = imageRender.height

    var encryptedMessageBits = ""

    outLoop@ for (y in 0 until iHeight) {
        for (x in 0 until iWidth) {
            val c = Color(imageRender.getRGB(x, y)).blue
            val mBit = c.and(1)
            encryptedMessageBits += mBit
            val s = encryptedMessageBits.length
            if (s >= 32 && encryptedMessageBits.drop(s - 24) == stringEOS)
                    break@outLoop
        }
    }

    val messageBits = decrypt(encryptedMessageBits.dropLast(24), password)
    val messageBytes = messageBits.chunked(8)
    val arr = ByteArray(messageBytes.size)
    for (i in messageBytes.indices) {
        arr[i] = messageBytes[i].toByte(2)
    }
    val message = arr.toString(Charsets.UTF_8)
    println("Message:\n$message")
}