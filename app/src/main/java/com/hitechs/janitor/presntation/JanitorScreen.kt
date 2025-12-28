package com.hitechs.janitor.presntation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitechs.janitor.R
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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Input Section
        item {
            Column {
                Text("Enter Bag Weight (kg) (1.01 up to 3.00):")
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
        }

        // Add Bag Button
        item {
            Button(
                onClick = {
                    viewModel.processIntent(JanitorIntent.AddBag(bagWeight))
                    bagWeight = "" // Always clear input after attempting to add
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Bag")
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = state.bags.isNotEmpty(),
                    onClick = { viewModel.processIntent(JanitorIntent.CalculateTrips) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Calculate Trips")
                }

                Button(
                    enabled = state.bags.isNotEmpty(),
                    onClick = { viewModel.processIntent(JanitorIntent.ClearBags) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }

        // Trips Result Section
        state.tripsResult?.let { result ->
            item {
                Text(
                    text = "Total Trips: ${result.totalTrips}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            itemsIndexed(
                items = result.tripGroups,
                key = { index, _ -> "trip_$index" }
            ) { index, trip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Trip ${index + 1}: ${trip.map { String.format("%.2f", it.weight) }.joinToString(", ")} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Bags List Section
        item {
            Text(
                text = "Current Bags:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(
            items = state.bags,
            key = { bag -> bag.id }
        ) { bag ->
            BagListItem(bag = bag)
        }
    }
}


@Composable
fun BagListItem(
    bag: GarbageBag
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bag weight display
            Text(
                text = "• ${String.format("%.2f", bag.weight)} kg",
                style = MaterialTheme.typography.bodyLarge
            )
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