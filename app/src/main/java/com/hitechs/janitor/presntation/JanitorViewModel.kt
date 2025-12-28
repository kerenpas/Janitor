package com.hitechs.janitor.presntation

import androidx.lifecycle.ViewModel
import com.hitechs.janitor.domain.CalculateTripsUseCase
import com.hitechs.janitor.domain.GarbageBag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
open class JanitorViewModel @Inject constructor(
    private val calculateTripsUseCase: CalculateTripsUseCase
) : ViewModel() {


    private val _state = MutableStateFlow<JanitorState>(JanitorState.BagsLoaded())
    val state: StateFlow<JanitorState> = _state.asStateFlow()


    fun processIntent(intent: JanitorIntent) {
       when (intent) {
            is JanitorIntent.AddBag -> validateAndAddBag(intent.weightInput)
            is JanitorIntent.CalculateTrips -> calculateTrips()
            is JanitorIntent.ClearBags -> clearBags()
            is JanitorIntent.ClearError -> clearError()
        }
    }

    private fun validateAndAddBag(weightInput: String) {
        // Comprehensive input validation
        val weight = weightInput.trim().replace(',', '.').toDoubleOrNull()

        when {
            // Check if input can be parsed to a number
            weight == null -> {
                updateStateWithError(InputError.InvalidFormat)
                return
            }

            // Check lower bound
            weight <= 1.0 -> {
                updateStateWithError(InputError.WeightTooLow)
                return
            }

            // Check upper bound
            weight > 3.0 -> {
                updateStateWithError(InputError.WeightTooHigh)
                return
            }

            // Valid input
            else -> {
                val currentState = _state.value
                if (currentState is JanitorState.BagsLoaded) {
                    val updatedBags = currentState.bags + GarbageBag(weight = weight)
                    _state.value = currentState.copy(
                        bags = updatedBags,
                        inputError = null,
                        tripsResult = null // Clear trips when adding new bag
                    )
                } else {
                    _state.value = JanitorState.BagsLoaded(
                        bags = listOf(GarbageBag(weight = weight))
                    )
                }
            }
        }
    }

    private fun updateStateWithError(error: InputError) {
        val currentState = _state.value
        if (currentState is JanitorState.BagsLoaded) {
            _state.value = currentState.copy(inputError = error)
        } else {
            _state.value = JanitorState.BagsLoaded(inputError = error)
        }
    }

    private fun calculateTrips() {
        val currentState = _state.value
        if (currentState is JanitorState.BagsLoaded) {
            try {
                val tripsResult = calculateTripsUseCase(currentState.bags)
                _state.value = currentState.copy(tripsResult = tripsResult)
            } catch (e: Exception) {
                _state.value = JanitorState.Error("Failed to calculate trips: ${e.message}")
            }
        }
    }

    private fun clearBags() {
        _state.value = JanitorState.BagsLoaded()
    }

    private fun clearError() {
        val currentState = _state.value
        if (currentState is JanitorState.BagsLoaded) {
            _state.value = currentState.copy(inputError = null)
        }
    }


}