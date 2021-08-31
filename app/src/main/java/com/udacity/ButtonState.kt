package com.udacity


sealed class ButtonState {
    object Indeterminate : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()

}