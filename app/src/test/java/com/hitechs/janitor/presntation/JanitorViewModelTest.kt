package com.hitechs.janitor.presntation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hitechs.janitor.domain.CalculateTripsUseCase
import com.hitechs.janitor.domain.GarbageBag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JanitorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: JanitorViewModel
    private lateinit var calculateTripsUseCase: CalculateTripsUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        calculateTripsUseCase = CalculateTripsUseCase()
        viewModel = JanitorViewModel(calculateTripsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is BagsLoaded with empty list`() = runTest {
        // When
        viewModel.state.test {
            val state = awaitItem()

            // Then
            assertThat(state).isInstanceOf(JanitorState.BagsLoaded::class.java)
            val bagsLoadedState = state as JanitorState.BagsLoaded
            assertThat(bagsLoadedState.bags).isEmpty()
            assertThat(bagsLoadedState.tripsResult).isNull()
            assertThat(bagsLoadedState.inputError).isNull()
        }
    }

    @Test
    fun `add valid bag updates state`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("1.5"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).hasSize(1)
            assertThat(state.bags[0].weight).isEqualTo(1.5)
            assertThat(state.inputError).isNull()
        }
    }

    @Test
    fun `add bag with weight too low shows error`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("0.5"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).isEmpty()
            assertThat(state.inputError).isInstanceOf(InputError.WeightTooLow::class.java)
        }
    }

    @Test
    fun `add bag with weight too high shows error`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("3.5"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).isEmpty()
            assertThat(state.inputError).isInstanceOf(InputError.WeightTooHigh::class.java)
        }
    }

    @Test
    fun `add bag with invalid format shows error`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("invalid"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).isEmpty()
            assertThat(state.inputError).isInstanceOf(InputError.InvalidFormat::class.java)
        }
    }

    @Test
    fun `add bag with empty string shows error`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag(""))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).isEmpty()
            assertThat(state.inputError).isInstanceOf(InputError.InvalidFormat::class.java)
        }
    }

    @Test
    fun `add multiple valid bags`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("1.5"))
        viewModel.processIntent(JanitorIntent.AddBag("2.3"))
        viewModel.processIntent(JanitorIntent.AddBag("1.8"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).hasSize(3)
            assertThat(state.bags.map { it.weight }).containsExactly(1.5, 2.3, 1.8)
        }
    }

    @Test
    fun `clear error removes input error`() = runTest {
        // Given - add invalid bag to create error
        viewModel.processIntent(JanitorIntent.AddBag("5.0"))

        // When
        viewModel.processIntent(JanitorIntent.ClearError)

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.inputError).isNull()
        }
    }

    @Test
    fun `clear bags resets state`() = runTest {
        // Given - add some bags
        viewModel.processIntent(JanitorIntent.AddBag("1.5"))
        viewModel.processIntent(JanitorIntent.AddBag("2.3"))

        // When
        viewModel.processIntent(JanitorIntent.ClearBags)

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).isEmpty()
            assertThat(state.tripsResult).isNull()
            assertThat(state.inputError).isNull()
        }
    }

    @Test
    fun `calculate trips updates state with result`() = runTest {
        // Given
        viewModel.processIntent(JanitorIntent.AddBag("1.5"))
        viewModel.processIntent(JanitorIntent.AddBag("2.3"))

        // When
        viewModel.processIntent(JanitorIntent.CalculateTrips)

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.tripsResult).isNotNull()
            assertThat(state.tripsResult?.totalTrips).isGreaterThan(0)
        }
    }

    @Test
    fun `calculate trips with empty bags list`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.CalculateTrips)

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.tripsResult).isNotNull()
            assertThat(state.tripsResult?.totalTrips).isEqualTo(0)
        }
    }

    @Test
    fun `add bag at minimum weight 1_01`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("1.01"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).hasSize(1)
            assertThat(state.bags[0].weight).isEqualTo(1.01)
            assertThat(state.inputError).isNull()
        }
    }

    @Test
    fun `add bag at maximum weight 3_0`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("3.0"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).hasSize(1)
            assertThat(state.bags[0].weight).isEqualTo(3.0)
            assertThat(state.inputError).isNull()
        }
    }

    @Test
    fun `add bag at exactly 1_00 shows error`() = runTest {
        // When
        viewModel.processIntent(JanitorIntent.AddBag("1.00"))

        // Then
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).isEmpty()
            assertThat(state.inputError).isInstanceOf(InputError.WeightTooLow::class.java)
        }
    }

    @Test
    fun `adding bag after error clears previous bags`() = runTest {
        // Given - add a valid bag first
        viewModel.processIntent(JanitorIntent.AddBag("1.5"))

        // When - try to add invalid bag
        viewModel.processIntent(JanitorIntent.AddBag("5.0"))

        // Then - previous bags should remain, only error shown
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).hasSize(1) // Previous bag remains
            assertThat(state.inputError).isInstanceOf(InputError.WeightTooHigh::class.java)
        }
    }

    @Test
    fun `calculate trips then add more bags clears trip result`() = runTest {
        // Given
        viewModel.processIntent(JanitorIntent.AddBag("1.5"))
        viewModel.processIntent(JanitorIntent.CalculateTrips)

        // When - add another bag
        viewModel.processIntent(JanitorIntent.AddBag("2.0"))

        // Then - trips result should be cleared
        viewModel.state.test {
            val state = awaitItem() as JanitorState.BagsLoaded
            assertThat(state.bags).hasSize(2)
            assertThat(state.tripsResult).isNull()
        }
    }
}
