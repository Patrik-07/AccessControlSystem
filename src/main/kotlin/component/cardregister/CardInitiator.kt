package component.cardregister

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import component.VerticalSpacer

@Composable
fun CardInitiator(onInitiation: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Text("Add meg a belépő kártyához tartozó nevet!")
    VerticalSpacer(10.dp)
    OutlinedTextField(
        label = { Text("Név") },
        value = name,
        onValueChange = { name = it },
    )
    VerticalSpacer(20.dp)
    Button(
        onClick = { onInitiation(name) },
        enabled = name.isNotEmpty()
    ) {
        Text("OK")
    }
}
