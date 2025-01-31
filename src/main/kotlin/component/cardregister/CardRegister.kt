package component.cardregister

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import component.VerticalSpacer
import component.arduino.AcsDevice
import configuration.Configuration
import kotlinx.coroutines.delay

sealed class CardState {
    object Reading : CardState()
    data class Registered(val tag: String, val name: String) : CardState()
    data class UnRegistered(val tag: String) : CardState()
    data class Finished(val title: String) : CardState()
}

@Composable
fun CardRegister(
    initialCardState: CardState,
    onBackToMain: () -> Unit
) {
    var cardState by remember { mutableStateOf(initialCardState) }
    var skipReadCount = 3

    AcsDevice.setOnRead {
        if (skipReadCount != 0) {
            Thread.sleep(1000)
            skipReadCount--
        } else {
            val tagString = it.split(" ")
            if (tagString.size >= 2) {
                val tag = tagString[1]
                val tags = Configuration.tags
                cardState = if (tags.containsKey(tag)) {
                    CardState.Registered(tag, tags[tag]!!)
                } else {
                    CardState.UnRegistered(tag)
                }
            }
        }
    }

    when (val state = cardState) {
        CardState.Reading -> CardReading(onBackToMain)
        is CardState.Registered -> CardRegistered(
            state,
            {
                cardState = CardState.Finished("Alkalmazott: \"${state.name}\" törölve lett")
            },
            {
                onBackToMain()
            },
        )
        is CardState.UnRegistered -> CardUnRegistered(
            state.tag,
            {
                cardState = CardState.Finished("Az alkalmazott regisztrálva lett")
            },
            {
                onBackToMain()
            }
        )
        is CardState.Finished -> CardFinished(
            state.title,
            3
        ) {
            onBackToMain()
        }
    }
}

@Composable
fun CardReading(
    onBackToMain: () -> Unit
) {
    Text("Olvasd le a belépő kártyát!")
    VerticalSpacer()
    Image(
        painter = painterResource("img/card.png"),
        contentDescription = "card",
        modifier = Modifier
            .size(120.dp)
    )

    Button(
        onClick = {
            AcsDevice.clearOnRead()
            AcsDevice.setToIdleMode()
            onBackToMain()
        }
    ) {
        Text("Vissza")
    }
}

@Composable
fun CardRegistered(
    registeredState: CardState.Registered,
    onDelete: () -> Unit,
    onBackToMain: () -> Unit
) {
    Text("Alkalmazott: \"${registeredState.name}\" már regisztrálva van.")
    VerticalSpacer(25.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
            onClick = {
                Configuration.tags.remove(registeredState.tag)
                AcsDevice.clearOnRead()
                AcsDevice.setToIdleMode()
                onDelete()
            }
        ) {
            Text("Alkalmazott törlése")
        }
        Button(
            onClick = {
                AcsDevice.clearOnRead()
                AcsDevice.setToIdleMode()
                onBackToMain()
            }
        ) {
            Text("Mégse")
        }
    }
}

@Composable
fun CardUnRegistered(
    tag: String,
    onSave: () -> Unit,
    onBackToMain: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Text("A kártya nincs hozzárendelve egyetlen alkalmazothoz sem.")
    VerticalSpacer(45.dp)

    Text("Alkalmazott regisztrálása:")
    VerticalSpacer()
    OutlinedTextField(
        label = { Text("Alkalmazott neve") },
        value = name,
        onValueChange = { name = it },
    )
    VerticalSpacer()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
            onClick = {
                Configuration.tags[tag] = name
                AcsDevice.clearOnRead()
                AcsDevice.setToIdleMode()
                onSave()
            }
        ) {
            Text("Alkalmazott mentése")
        }
        Button(
            onClick = {
                AcsDevice.clearOnRead()
                AcsDevice.setToIdleMode()
                onBackToMain()
            }
        ) {
            Text("Mégse")
        }
    }
}

@Composable
fun CardFinished(
    title: String,
    initialCount: Int,
    onBackToMain: () -> Unit,
) {
    var timer by remember { mutableStateOf(initialCount) }

    Text("$title ($timer)")
    VerticalSpacer(25.dp)
    Image(
        painter = painterResource("img/checkmark.png"),
        contentDescription = "checkmark",
        modifier = Modifier
            .size(120.dp)
    )

    LaunchedEffect(key1 = timer) {
        if (timer > 0) {
            delay(1000)
            timer--
            if (timer == 0) {
                onBackToMain()
            }
        }
    }
}
