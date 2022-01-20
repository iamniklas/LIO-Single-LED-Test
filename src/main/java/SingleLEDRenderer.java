import com.github.iamniklas.liocore.led.LEDRenderer;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class SingleLEDRenderer extends LEDRenderer {

    private final GpioPinDigitalOutput pin;

    public SingleLEDRenderer(GpioPinDigitalOutput _pin) {
        super(300);
        pin = _pin;
    }

    @Override
    public void render() {
        if(colorData[0].r > 128 || colorData[0].g > 128 || colorData[0].b > 128) {
            pin.high();
        }
        else {
            pin.low();
        }
    }
}
