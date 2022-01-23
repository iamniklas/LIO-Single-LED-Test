import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.github.iamniklas.liocore.led.LEDRenderer

public class SingleLEDRenderer(private val pin: GpioPinDigitalOutput) : LEDRenderer(300) {
    override fun render() {
        if (colorData[0].r > 128 || colorData[0].g > 128 || colorData[0].b > 128) {
            pin.high()
        } else {
            pin.low()
        }
    }
}