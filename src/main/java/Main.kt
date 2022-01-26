import com.github.iamniklas.liocore.config.ProgramConfiguration
import kotlin.Throws
import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.github.iamniklas.liocore.led.LEDStripManager
import org.eclipse.paho.client.mqttv3.MqttException
import java.lang.InterruptedException
import kotlin.jvm.JvmStatic
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.io.gpio.PinState
import com.github.iamniklas.liocore.network.mqtt.MQTTListener
import com.github.iamniklas.liocore.network.mqtt.IMqttCallback
import com.github.iamniklas.liocore.network.LEDUpdateModel
import com.github.iamniklas.liocore.procedures.ProcedureFactory

class Main {
    @Throws(Throwable::class)
    protected fun finalize() {
        pinProgramState.low()
        pin.low()
        gpio.shutdown()
    }

    companion object {
        private lateinit var gpio: GpioController
        private lateinit var pinProgramState: GpioPinDigitalOutput
        private lateinit var pin: GpioPinDigitalOutput
        private lateinit var ledMng: LEDStripManager
        @Throws(MqttException::class, InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            ProgramConfiguration.configuration = ProgramConfiguration.readConfigFromFile()

            gpio = GpioFactory.getInstance()!!

            pinProgramState = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Status LED", PinState.HIGH)
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "MyLED", PinState.HIGH)

            // set shutdown state for this pin
            pin.setShutdownOptions(true, PinState.LOW)
            pinProgramState.setShutdownOptions(true, PinState.LOW)

            notifyMsgIncome(pin)
            notifyMsgIncome(pinProgramState)

            Thread.sleep(500)

            pinProgramState.high()

            MQTTListener(object : IMqttCallback {
                override fun onLEDUpdateModelReceive(_updateModel: LEDUpdateModel) {
                    _updateModel.bundle.ledStrip = ledMng
                    _updateModel.bundle.procedureCalls = ledMng
                    val p = ProcedureFactory.getProcedure(_updateModel.procedure, _updateModel.bundle)
                    ledMng.procContainer.removeCurrentProcedure()
                    ledMng.procContainer.queueProcedure(p)
                }

                override fun onLEDUpdateModelReceiveAll(_updateModel: LEDUpdateModel) {
                    _updateModel.bundle.ledStrip = ledMng
                    _updateModel.bundle.procedureCalls = ledMng
                    val p = ProcedureFactory.getProcedure(_updateModel.procedure, _updateModel.bundle)
                    ledMng.procContainer.removeCurrentProcedure()
                    ledMng.procContainer.queueProcedure(p)
                }

                override fun onLEDValueUpdateModelReceive(_valueUpdateModel: LEDUpdateModel) {}
                override fun onLEDValueUpdateModelReceiveAll(_valueUpdateModel: LEDUpdateModel) {}
            }).connect()

            val renderer = SingleLEDRenderer(pin)

            ledMng = LEDStripManager(renderer, ProgramConfiguration.configuration.ledCount)
            while (true) {
                ledMng.update()
            }
        }

        private fun notifyMsgIncome(pin: GpioPinDigitalOutput?) {
            pin!!.high()
            try {
                Thread.sleep(250)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            pin.low()
        }
    }
}