package component.arduino

import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.*

class SerialPortHandler(portName: String) {
    private var serialPort: SerialPort = SerialPort.getCommPort(portName)
    init {
        serialPort.openPort()
    }

    fun readMax(
        timeout: Int = 30,
        sleep: Long = 1000L,
        onReceiveMessage: (String) -> Boolean
    ) {
        if (!serialPort.isOpen) {
            return
        }

        val inputStream = serialPort.inputStream
        val buffer = ByteArray(1024)
        var bytesRead: Int

        runBlocking {
            repeat(timeout) {
                if (serialPort.bytesAvailable() > 0) {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val message = String(buffer, 0, bytesRead).trim()
                        if (onReceiveMessage(message)) {
                            return@runBlocking
                        }
                    }
                }
                delay(sleep)
            }
        }
    }

    fun readWhile(
        sleep: Long = 1000L,
        onReceiveMessage: (String) -> Boolean
    ) {
        if (!serialPort.isOpen) {
            return
        }

        val inputStream = serialPort.inputStream
        val buffer = ByteArray(1024)
        var bytesRead: Int

        runBlocking {
            while(true) {
                if (serialPort.bytesAvailable() > 0) {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val messages = String(buffer, 0, bytesRead)
                            .trim()
                            .split("\n")

                        if (messages.isNotEmpty()) {
                            val message = messages.last()
                            if (onReceiveMessage(message)) {
                                return@runBlocking
                            }
                        }
                    }
                }
                delay(sleep)
            }
        }
    }

    fun write(message: String) {
        if (!serialPort.isOpen) {
            return
        }

        val outputStream = serialPort.outputStream
        outputStream.write((message).toByteArray())
        outputStream.flush()
    }
}