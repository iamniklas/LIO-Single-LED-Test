import com.github.iamniklas.liocore.led.LEDStripManager;
import com.github.iamniklas.liocore.network.*;
import com.github.iamniklas.liocore.network.mqtt.*;
import com.github.iamniklas.liocore.procedures.Procedure;
import com.github.iamniklas.liocore.procedures.ProcedureFactory;
import com.pi4j.io.gpio.*;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    private static GpioController gpio;
    private static GpioPinDigitalOutput pinProgramState;
    private static GpioPinDigitalOutput pin;

    private static LEDStripManager ledMng;

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
                _updateModel.bundle.ledStrip = ledMng;
                _updateModel.bundle.procedureCalls = ledMng;
                Procedure p = ProcedureFactory.getProcedure(_updateModel.procedure, _updateModel.bundle);
                ledMng.procContainer.removeCurrentProcedure();
                ledMng.procContainer.queueProcedure(p);
            }

            @Override
            public void onLEDUpdateModelReceiveAll(LEDUpdateModel _updateModel) {
                _updateModel.bundle.ledStrip = ledMng;
                _updateModel.bundle.procedureCalls = ledMng;
                Procedure p = ProcedureFactory.getProcedure(_updateModel.procedure, _updateModel.bundle);
                ledMng.procContainer.removeCurrentProcedure();
                ledMng.procContainer.queueProcedure(p);
            }

            @Override
            public void onLEDValueUpdateModelReceive(LEDValueUpdateModel _valueUpdateModel) {

            }

            @Override
            public void onLEDValueUpdateModelReceiveAll(LEDValueUpdateModel _valueUpdateModel) {

            }
        }).connect();

        SingleLEDRenderer renderer = new SingleLEDRenderer(pin);
        ledMng = new LEDStripManager(renderer, false);

        while (true) {
            ledMng.update();
        }
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
