package component

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*

class PlaceComponent : Component() {
    @Composable
    override fun createComposable() {
        var text2 by remember { mutableStateOf("") }
        OutlinedTextField(
            label = { Text("Helyszín") },
            value = text2,
            onValueChange = { text2 = it },
        )
        VerticalSpacer()
    }
}