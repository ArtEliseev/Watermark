package watermark

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess
import java.awt.Color

val BITS = listOf(24, 32)

fun main() {
    val input = getInput()
    val watermark = Watermark(input)
    createOutput(input, watermark)
}

fun getInput(): BufferedImage {
    println("Input the image filename:")
    val fileName = readln()
    val imageFile = File(fileName)
    if (!imageFile.exists()) {
        println("The file $fileName doesn't exist.").also { exitProcess(0) }
    } else {
        val input: BufferedImage = ImageIO.read(imageFile)
        if (input.colorModel.numColorComponents != 3) {
            println("The number of image color components isn't 3.").also { exitProcess(0) }
        } else if (!BITS.contains(input.colorModel.pixelSize)) {
            println("The image isn't 24 or 32-bit.").also { exitProcess(0) }
        } else return input
    }
}

fun createOutput(input: BufferedImage, watermark: Watermark) {
    println("Input the output image filename (jpg or png extension):")
    val outputFileName = readln()
    if (!outputFileName.matches(""".+\.(jp|pn)g""".toRegex())) {
        println("The output file extension isn't \"jpg\" or \"png\".").also { exitProcess(0) }
    } else {
        val outputFile = File(outputFileName)
        val output = BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until input.width) {
            for (y in 0 until input.height) {
                val imageColor = Color(input.getRGB(x, y))
                val watermarkColor = if (watermark.position.method == Watermark.Method.GRID) {
                    Color(
                        watermark.image.getRGB(x % watermark.image.width, y % watermark.image.height),
                        watermark.transparency
                    )
                } else {
                    if (x in (watermark.position.x?.until(watermark.position.x + watermark.image.width)
                            ?: (0 until watermark.image.width)) &&
                        y in (watermark.position.y?.until(watermark.position.y + watermark.image.height)
                            ?: (0 until watermark.image.height))
                    ) {
                        Color(
                            watermark.image.getRGB(x - (watermark.position.x ?: 0), y - (watermark.position.y ?: 0)),
                            watermark.transparency
                        )
                    } else
                        Color(input.getRGB(x, y))
                }
                val watermarkColorMatchesTransparencyColor =
                            watermarkColor.red == watermark.color?.red &&
                            watermarkColor.green == watermark.color.green &&
                            watermarkColor.blue == watermark.color.blue
                val color = if (watermarkColor.alpha == 0 || watermarkColorMatchesTransparencyColor) {
                    Color(imageColor.red, imageColor.green, imageColor.blue)
                } else {
                    Color(
                        (watermark.weight * watermarkColor.red + (100 - watermark.weight) * imageColor.red) / 100,
                        (watermark.weight * watermarkColor.green + (100 - watermark.weight) * imageColor.green) / 100,
                        (watermark.weight * watermarkColor.blue + (100 - watermark.weight) * imageColor.blue) / 100
                    )
                }
                output.setRGB(x, y, color.rgb)
            }
        }
        ImageIO.write(output, outputFileName.split(".").last(), outputFile)
        println("The watermarked image $outputFileName has been created.")
    }
}