package component.arduino

import com.fazecast.jSerialComm.SerialPort
import component.exception.DeviceNotFoundException

object SerialPortHandlerFactory {
    private const val AUTHENTICATION_REQUEST = "AUTHENTICATION_REQUEST_$KEY"
    private const val AUTHENTICATED = "AUTHENTICATED_$KEY"

    fun tryFindSerialPort(): SerialPortHandler {
        val ports = SerialPort.getCommPorts().map(SerialPort::getSystemPortName)
        if (ports.isNotEmpty()) {
            ports.forEach {
                val serialPortHandler = trySerialPort(it)
                if (serialPortHandler != null) {
                    return serialPortHandler
                }
            }
        }
        throw DeviceNotFoundException()
    }

    private fun trySerialPort(portName: String): SerialPortHandler? {
        val serialPortHandler: SerialPortHandler
        var isSuccessful = false
        try {
            serialPortHandler = SerialPortHandler(portName)
            serialPortHandler.readMax {
                serialPortHandler.write(AUTHENTICATION_REQUEST)
                isSuccessful = (it == AUTHENTICATED)
                isSuccessful
            }
        } catch (exception: Exception) {
            return null
        }

        if (isSuccessful) {
            return serialPortHandler
        }

        return null
    }
}