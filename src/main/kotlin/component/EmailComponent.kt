package component

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import java.util.regex.Pattern

class EmailComponent : Component() {
    private var email = ""
    private var isValid = true

    @Composable
    override fun createComposable() {
        var isValidEmail by remember { mutableStateOf(true) }
        var emailText by remember { mutableStateOf("") }

        OutlinedTextField(
            value = emailText,
            onValueChange = {
                emailText = it
                onEmailChange(emailText)
                isValidEmail = checkIfValidEmail(it)
                onIsValidEmailChange(isValidEmail)
            },
            label = { Text("Email") },
            isError = !isValidEmail,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                errorBorderColor = Color.Red,
            )
        )
        VerticalSpacer()
    }

    private fun onEmailChange(emailText: String) {
        email = emailText
    }

    private fun onIsValidEmailChange(isValidEmail: Boolean) {
        isValid = isValidEmail
    }

    private fun checkIfValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        val pattern = Pattern.compile(emailRegex)
        return pattern.matcher(email).matches()
    }
}