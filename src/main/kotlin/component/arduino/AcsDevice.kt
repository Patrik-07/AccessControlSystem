package component.arduino

object AcsDevice {
    private const val IDLE_REQUEST = "IDLE_REQUEST_$KEY"
    private const val TAG_READ_REQUEST = "TAG_READ_REQUEST_$KEY"

    private lateinit var serialPortHandler: SerialPortHandler
    private var mode: Mode = Mode.IDLE

    private var onRead: (String) -> Unit = {}

    fun setOnRead(onRead: (String) -> Unit) {
        this.onRead = onRead
    }

    fun clearOnRead() {
        this.onRead = { }
    }

    fun setSerialPortHandler(serialPortHandler: SerialPortHandler) {
        this.serialPortHandler = serialPortHandler
    }

    fun read() {
        serialPortHandler.readWhile(50) {
            when (mode) {
                Mode.IDLE -> {
                    serialPortHandler.write(IDLE_REQUEST)
                }
                Mode.TAG_READ -> {
                    serialPortHandler.write(TAG_READ_REQUEST)
                }
                else -> {}
            }
            if (mode != Mode.AUTHENTICATION && mode != Mode.IDLE) {
                onRead(it)
            }
            println(it)
            false
        }
    }

    fun setToReadMode() {
        mode = Mode.TAG_READ
    }

    fun setToIdleMode() {
        mode = Mode.IDLE
    }
}

// package component.arduino
//
// object AcsDevice {
//     private const val IDLE_REQUEST = "IDLE_REQUEST_$KEY"
//     private const val TAG_READ_REQUEST = "TAG_READ_REQUEST_$KEY"
//
//     private const val IDLE_MODE = "IDLE_$KEY"
//     private const val TAG_READ_MODE = "IDLE_$KEY"
//
//     private lateinit var serialPortHandler: SerialPortHandler
//     private var mode: Mode = Mode.IDLE
//     private var changeMode = false
//
//     private var onRead: (String) -> Unit = {}
//
//     fun setOnRead(onRead: (String) -> Unit) {
//         this.onRead = onRead
//     }
//
//     fun clearOnRead() {
//         this.onRead = { }
//     }
//
//     fun setSerialPortHandler(serialPortHandler: SerialPortHandler) {
//         this.serialPortHandler = serialPortHandler
//     }
//
//     fun read() {
//         serialPortHandler.readWhile(50) {
//             if (changeMode) {
//                 var modeString = ""
//                 when (mode) {
//                     Mode.IDLE -> {
//                         serialPortHandler.write(IDLE_REQUEST)
//                         modeString = IDLE_MODE
//                     }
//                     Mode.TAG_READ -> {
//                         serialPortHandler.write(TAG_READ_REQUEST)
//                         modeString = TAG_READ_MODE
//                     }
//                     else -> {}
//                 }
//                 if (it.contains(modeString)) {
//                     changeMode = false
//                 }
//             }
//             if (mode != Mode.AUTHENTICATION && mode != Mode.IDLE) {
//                 onRead(it)
//             }
//             println(it)
//             false
//         }
//     }
//
//     fun setToReadMode() {
//         write(Mode.TAG_READ)
//     }
//
//     fun setToIdleMode() {
//         write(Mode.IDLE)
//     }
//
//     private fun write(mode: Mode) {
//         changeMode = true
//         this.mode = mode
//     }
// }
