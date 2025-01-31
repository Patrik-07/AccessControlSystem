import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import component.EmailComponent
import component.HorizontalSpacer
import component.PlaceComponent
import component.VerticalSpacer
import component.arduino.AcsDevice
import component.arduino.SerialPortHandlerFactory
import component.cardregister.CardRegister
import component.cardregister.CardState
import component.exception.DeviceNotFoundException
import kotlinx.coroutines.*
import java.io.File
import configuration.Configuration
import configuration.WorkingHoursConfiguration
import java.io.FileWriter
import java.time.LocalDate

val emailComponent = EmailComponent()
val placeComponent = PlaceComponent()

sealed class State {
    object Main : State()
    object WorkerList: State()
    object CardRead : State()
}

@Composable
fun MainComponent(
    onWorkerListState: () -> Unit,
    onCardRearState: () -> Unit
) {
    emailComponent.createComposable()
    placeComponent.createComposable()

    Button(onClick = { } ) {
        Text("Havi report letöltés")
    }
    VerticalSpacer(45.dp)

    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
        onClick = {
            onWorkerListState()
        }
    ) {
        Text("Alkalmazottak")
    }
    VerticalSpacer(45.dp)

    Button(
        modifier = Modifier
            .width(280.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
        onClick = {
            AcsDevice.setToReadMode()
            onCardRearState()
        }
    ) {
        Text("Belépő kártya regisztálás")
    }
}

@Composable
fun WorkerListComponent(onStateChange: () -> Unit) {
    var tags by remember { mutableStateOf(Configuration.tags.toMap()) }

    Surface {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keys = tags.keys.toList()
            if (keys.isEmpty()) {
                item {
                    Text("Nincs regisztrálva alkalmazott")
                }
            } else {
                items(keys) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.width(200.dp),
                            text = tags[it]!!
                        )
                        HorizontalSpacer()
                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                            onClick = {
                                Configuration.tags.remove(it)
                                tags = Configuration.tags.toMap()
                            }
                        ) {
                            Text("Törlés")
                        }
                    }
                }
            }
            item {
                VerticalSpacer()
                Button(
                    onClick = {
                        onStateChange()
                    }
                ) {
                    Text("Vissza")
                }
            }
        }
    }
}

@Composable
fun CardReadComponent(onStateChange: () -> Unit) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardRegister(CardState.Reading) {
                onStateChange()
            }
        }
    }
}

@Composable
@Preview
fun App() {
    var state by remember { mutableStateOf<State>(State.Main) }

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                State.Main -> MainComponent(
                    { state = State.WorkerList },
                    { state = State.CardRead }
                )
                State.WorkerList -> WorkerListComponent {
                    state = State.Main
                }
                State.CardRead -> CardReadComponent {
                    AcsDevice.setToIdleMode()
                    state = State.Main
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title)
        VerticalSpacer(20.dp)
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.Red)
    }
}

sealed class AppState {
    object LoadingPort : AppState()
    object LoadingConfiguration : AppState()
    object Success : AppState()
    data class Error(val message: String) : AppState()
}

@OptIn(DelicateCoroutinesApi::class)
fun main() = application {
    val appState = mutableStateOf<AppState>(AppState.LoadingPort)

    Window(
        title = "Beléptető - Admin",
        onCloseRequest = {
            val configDir = createRoamingAppSubdirectory()
            val configFile = File(configDir, "config.json")
            val json = jacksonObjectMapper().writeValueAsString(Configuration)
            FileWriter(configFile).use { it.write("") }
            FileWriter(configFile, true).use { it.write(json) }
            exitApplication()
        }
    ) {
        MaterialTheme(
            typography = Typography(
                defaultFontFamily = FontFamily(
                    Font(
                        File("src/main/resources/font/Poppins-Medium.ttf")
                    )
                ),
            )
        ) {
            when (val state = appState.value) {
                is AppState.LoadingPort -> LoadingScreen("Beléptető eszköz keresése...")
                is AppState.LoadingConfiguration -> {
                    LoadingScreen("Adatok betöltése...")
                    try {
                        val configDir = createRoamingAppSubdirectory()

                        val configFile = File(configDir, "config.json")
                        if (!configFile.exists()) {
                            configFile.createNewFile()
                        } else {
                            val config = jacksonObjectMapper().readValue(configFile, Configuration::class.java)
                            Configuration.email = config.email
                            Configuration.location = config.location
                            Configuration.tags = config.tags
                        }

                        val workingHoursConfigFile = File(configDir, "workingHoursConfig.json")
                        if (!workingHoursConfigFile.exists()) {
                            workingHoursConfigFile.createNewFile()
                            WorkingHoursConfiguration.currentDate = LocalDate.now()
                        } else {
                            val workingHoursConfig = jacksonObjectMapper().readValue(
                                workingHoursConfigFile,
                                WorkingHoursConfiguration::class.java
                            )
                            WorkingHoursConfiguration.daysPerEmployee = workingHoursConfig.daysPerEmployee
                            WorkingHoursConfiguration.currentDate = workingHoursConfig.currentDate
                        }
                    } catch (ex: Exception) {
                        appState.value = AppState.Error("Nem sikerült beolvasni a kártya adatokat")
                    }
                    appState.value = AppState.Success
                }
                is AppState.Success -> App()
                is AppState.Error -> ErrorScreen(state.message)
            }
        }
    }

    GlobalScope.launch {
        try {
            AcsDevice.setSerialPortHandler(
                SerialPortHandlerFactory.tryFindSerialPort()
            )
            appState.value = AppState.LoadingConfiguration
            AcsDevice.setToIdleMode()
            AcsDevice.read()
        } catch (deviceNotFoundException: DeviceNotFoundException) {
            appState.value = AppState.Error(deviceNotFoundException.message ?: "Ismeretlen hiba")
        }
    }
}

fun createRoamingAppSubdirectory(): File {
    val appDataDir = File(
        System.getenv("APPDATA"),
        "ACS_Config"
    )
    if (!appDataDir.exists()) {
        appDataDir.mkdirs()
    }
    return appDataDir
}
