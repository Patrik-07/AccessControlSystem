package component

import androidx.compose.runtime.Composable

abstract class Component {
    @Composable
    abstract fun createComposable()
}