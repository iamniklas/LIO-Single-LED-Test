import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.github.iamniklas.liocore.led.LEDRenderer
import com.github.iamniklas.liocore.led.colorspace.LIOColor
import java.util.ArrayList

public class SingleLEDRenderer(private val pin: GpioPinDigitalOutput) : LEDRenderer() {
    override fun render(_colorData: ArrayList<LIOColor>?) {
        if (_colorData!![0].r > 128 || _colorData[0].g > 128 || _colorData[0].b > 128) {
            pin.high()
        } else {
            pin.low()
        }
    }
}