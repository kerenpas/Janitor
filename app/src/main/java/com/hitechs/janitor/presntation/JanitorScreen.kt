package com.hitechs.janitor.presntation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitechs.janitor.domain.GarbageBag
import com.hitechs.janitor.domain.TripsResult

@Composable
fun JanitorScreen(viewModel: JanitorViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val currentState = state) {
        is JanitorState.BagsLoaded -> LoadedView(currentState, viewModel)
        is JanitorState.Error -> ErrorView(currentState, viewModel)
    }
}

@Composable
fun LoadedView(
    state: JanitorState.BagsLoaded,
    viewModel: JanitorViewModel
) {
    var bagWeight by remember { mutableStateOf("") }


    Column(modifier = Modifier.padding(16.dp)) {
        // Bag Input Section with Error Handling
        Column {
            Text("Enter Bag Weight (kg) (1.01 up to 3.00):")
            TextField(
                value = bagWeight,
                onValueChange = {
                    bagWeight = it
                    // Clear error when user starts typing
                    if (state.inputError != null) {
                        viewModel.processIntent(JanitorIntent.ClearError)
                    }
                },
                label = { Text("Bag Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.inputError != null,
                supportingText = {
                    when (state.inputError) {
                        is InputError.WeightTooLow -> Text(
                            "Weight must be at least 1.01 kg",
                            color = MaterialTheme.colorScheme.error
                        )
                        is InputError.WeightTooHigh -> Text(
                            "Weight must not exceed 3.0 kg",
                            color = MaterialTheme.colorScheme.error
                        )
                        is InputError.InvalidFormat -> Text(
                            "Invalid number format",
                            color = MaterialTheme.colorScheme.error
                        )
                        null -> {}
                    }
                }
            )

            // Error Dismissal Button
            state.inputError?.let {
                TextButton(
                    onClick = { viewModel.processIntent(JanitorIntent.ClearError) }
                ) {
                    Text("Dismiss Error", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Add Bag Button
        Button(
            onClick = {
                viewModel.processIntent(JanitorIntent.AddBag(bagWeight))
                bagWeight = "" // Always clear input after attempting to add
            }
        ) {
            Text("Add Bag")
        }

        // Bags List
        Text("Current Bags:")
        state.bags.forEach { bag ->
            Text("• ${String.format("%.2f", bag.weight)} kg")
        }

        // Action Buttons
        Row {
            Button(
                enabled = state.bags.isNotEmpty(),
                onClick = { viewModel.processIntent(JanitorIntent.CalculateTrips) }
            ) {
                Text("Calculate Trips")
            }

            Button(
                enabled = state.bags.isNotEmpty(),
                onClick = { viewModel.processIntent(JanitorIntent.ClearBags) }
            ) {
                Text("Clear")
            }
        }

        // Trips Result
        state.tripsResult?.let { result ->
            Text("Total Trips: ${result.totalTrips}")
            result.tripGroups.forEachIndexed { index, trip ->
                Text("Trip ${index + 1}: ${trip.map { String.format("%.2f", it.weight) }.joinToString(", ")} kg")
            }
        }
    }
}



@Composable
fun ErrorView(
    state: JanitorState.Error,
    viewModel: JanitorViewModel
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Error: ${state.message}",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = { viewModel.processIntent(JanitorIntent.ClearBags) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Dismiss")
        }
    }
}

// Mock ViewModel for previews
private class PreviewJanitorViewModel : JanitorViewModel(
    calculateTripsUseCase = com.hitechs.janitor.domain.CalculateTripsUseCase()
)

@Preview(showBackground = true, name = "Empty Bags List")
@Composable
@Suppress("ViewModelConstructorInComposable")
fun LoadedViewEmptyPreview() {
    MaterialTheme {
        LoadedView(
            state = JanitorState.BagsLoaded(
                bags = emptyList(),
                tripsResult = null,
                inputError = null
            ),
            viewModel = PreviewJanitorViewModel()
        )
    }
}

@Preview(showBackground = true, name = "With Bags")
@Composable
@Suppress("ViewModelConstructorInComposable")
fun LoadedViewWithBagsPreview() {
    MaterialTheme {
        LoadedView(
            state = JanitorState.BagsLoaded(
                bags = listOf(
                    GarbageBag(1.5),
                    GarbageBag(2.3),
                    GarbageBag(1.8)
                ),
                tripsResult = null,
                inputError = null
            ),
            viewModel = PreviewJanitorViewModel()
        )
    }
}

@Preview(showBackground = true, name = "With Trips Result")
@Composable
@Suppress("ViewModelConstructorInComposable")
fun LoadedViewWithTripsPreview() {
    MaterialTheme {
        LoadedView(
            state = JanitorState.BagsLoaded(
                bags = listOf(
                    GarbageBag(1.5),
                    GarbageBag(2.3),
                    GarbageBag(1.8)
                ),
                tripsResult = TripsResult(
                    totalTrips = 2,
                    tripGroups = listOf(
                        listOf(GarbageBag(2.3)),
                        listOf(GarbageBag(1.5), GarbageBag(1.8))
                    )
                ),
                inputError = null
            ),
            viewModel = PreviewJanitorViewModel()
        )
    }
}

@Preview(showBackground = true, name = "With Input Error")
@Composable
@Suppress("ViewModelConstructorInComposable")
fun LoadedViewWithErrorPreview() {
    MaterialTheme {
        LoadedView(
            state = JanitorState.BagsLoaded(
                bags = listOf(GarbageBag(1.5)),
                tripsResult = null,
                inputError = InputError.WeightTooHigh
            ),
            viewModel = PreviewJanitorViewModel()
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
@Suppress("ViewModelConstructorInComposable")
fun ErrorViewPreview() {
    MaterialTheme {
        ErrorView(
            state = JanitorState.Error("Something went wrong!"),
            viewModel = PreviewJanitorViewModel()
        )
    }
}