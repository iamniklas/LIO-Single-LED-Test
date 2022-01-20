import com.github.iamniklas.liocore.network.*;
import com.github.iamniklas.liocore.network.mqtt.*;
import com.pi4j.io.gpio.*;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    private static GpioController gpio;
    private static GpioPinDigitalOutput pinProgramState;
    private static GpioPinDigitalOutput pin;

    public static void main(String[] args) throws MqttException, InterruptedException {
        gpio = GpioFactory.getInstance();
        // create gpio controller

        // provision gpio pin #wPi00/BCM17 as an output pin and turn on
        pinProgramState = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Status LED", PinState.HIGH);
        // provision gpio pin #wPi02/BCM27 as an output pin and turn on
        pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "MyLED", PinState.HIGH);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);
        pinProgramState.setShutdownOptions(true, PinState.LOW);

        notifyMsgIncome(pin);
        notifyMsgIncome(pinProgramState);

        Thread.sleep(500);
        pinProgramState.high();

        new MQTTListener(new IMqttCallback() {
            @Override
            public void onLEDUpdateModelReceive(LEDUpdateModel _updateModel) {
                notifyMsgIncome(pin);
            }

            @Override
            public void onLEDUpdateModelReceiveAll(LEDUpdateModel _updateModel) {
                notifyMsgIncome(pin);
            }

            @Override
            public void onLEDValueUpdateModelReceive(LEDValueUpdateModel _valueUpdateModel) {

            }

            @Override
            public void onLEDValueUpdateModelReceiveAll(LEDValueUpdateModel _valueUpdateModel) {

            }
        }).connect();

        //System.out.println("Exiting ControlGpioExample");
    }

    static void notifyMsgIncome(GpioPinDigitalOutput pin) {
        pin.high();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pin.low();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        pinProgramState.low();
        pin.low();
        gpio.shutdown();
    }
}
