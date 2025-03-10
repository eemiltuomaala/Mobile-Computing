package com.example.composetutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _lightLevel = MutableStateFlow(0f)
    val lightLevel: StateFlow<Float> get() = _lightLevel

    fun updateLightLevel(lux: Float) {
        viewModelScope.launch {
            _lightLevel.value = lux
        }
    }
}