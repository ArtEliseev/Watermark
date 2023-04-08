package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess


class Watermark(private val input: BufferedImage) {
    val image = createImage(input)
    val color: Color? = if (!withOrWithoutTransparencyColor(image)) null else receiveTransparencyColor()
    val transparency: Boolean = receiveTransparency(image)
    val weight: Int = receiveWeight()
    val position: Position = receivePosition()

    enum class Method {
        SINGLE, GRID
    }

    inner class Position(val method: Method, val x: Int? = null, val y: Int? = null)

    private fun createImage(input: BufferedImage): BufferedImage {
        println("Input the watermark image filename:")
        val watermarkFileName = readln()
        val watermarkFile = File(watermarkFileName)
        if (!watermarkFile.exists()) {
            println("The file $watermarkFileName doesn't exist.").also { exitProcess(0) }
        } else {
            val image: BufferedImage = ImageIO.read(watermarkFile)
            if (image.colorModel.numColorComponents != 3) {
                println("The number of watermark color components isn't 3.").also { exitProcess(0) }
            } else if (!BITS.contains(image.colorModel.pixelSize)) {
                println("The watermark isn't 24 or 32-bit.").also { exitProcess(0) }
            } else if (input.height < image.height || input.width < image.width) {
                println("The watermark's dimensions are larger.").also { exitProcess(0) }
            } else return image
        }
    }

    private fun withOrWithoutTransparencyColor(image: BufferedImage): Boolean {
        if (image.colorModel.pixelSize == BITS[1]) return false
        println("Do you want to set a transparency color?")
        return readln() == "yes"
    }

    private fun receiveTransparencyColor(): Color {
        println("Input a transparency color ([Red] [Green] [Blue]):")
        val input = readln()
        val regex = "(\\d+) (\\d+) (\\d+)".toRegex()
        if (input.matches(regex)) {
            val (red, green, blue) = input.split(" ").map { it.toInt() }
            if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
                println("The transparency color input is invalid.").also { exitProcess(0) }
            } else return Color(red, green, blue)
        } else println("The transparency color input is invalid.").also { exitProcess(0) }
    }

    private fun receiveTransparency(image: BufferedImage): Boolean {
        if (image.colorModel.transparency == Transparency.TRANSLUCENT) {
            println("Do you want to use the watermark's Alpha channel?")
            if (readln().lowercase() == "yes") {
                return true
            }
        }
        return false
    }

    private fun receiveWeight(): Int {
        println("Input the watermark transparency percentage (Integer 0-100):")
        val weight = readln().toIntOrNull()
        if (weight == null) {
            println("The transparency percentage isn't an integer number.").also { exitProcess(0) }
        } else if (weight < 0 || weight > 100) {
            println("The transparency percentage is out of range.").also { exitProcess(0) }
        } else return weight
    }

    private fun receivePosition(): Position {
        val diffX = input.width - image.width
        val diffY = input.height - image.height
        println("Choose the position method (single, grid):")
        when (readln()) {
            "single" -> {
                println(
                    "Input the watermark position ([x 0-$diffX] " +
                            "[y 0-$diffY]):"
                )
                val positions = readln()
                if (!positions.matches("(-?\\d+) (-?\\d+)".toRegex())) {
                    println("The position input is invalid.").also { exitProcess(0) }
                } else {
                    val (x, y) = positions.split(" ").map { it.toInt() }
                    if (x !in 0..diffX || y !in 0..diffY) {
                        println("The position input is out of range.").also { exitProcess(0) }
                    } else return Position(Method.SINGLE, x, y)
                }
            }
            "grid" -> return Position(Method.GRID)
            else -> println("The position method input is invalid.").also { exitProcess(0) }
        }
    }
}