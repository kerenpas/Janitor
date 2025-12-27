package com.hitechs.janitor.presntation

import com.hitechs.janitor.domain.GarbageBag
import com.hitechs.janitor.domain.TripsResult

sealed class InputError {
    object WeightTooLow : InputError()
    object WeightTooHigh : InputError()
    object InvalidFormat : InputError()
}

// Represents the different states of the UI
sealed class JanitorState {
    data class BagsLoaded(
        val bags: List<GarbageBag> = emptyList(),
        val tripsResult: TripsResult? = null,
        val inputError: InputError? = null
    ) : JanitorState()
    data class Error(val message: String) : JanitorState()
}

// Defines user interactions/intents
sealed class JanitorIntent {
    data class AddBag(val weightInput: String) : JanitorIntent()
    object CalculateTrips : JanitorIntent()
    object ClearBags : JanitorIntent()
    object ClearError : JanitorIntent()
}