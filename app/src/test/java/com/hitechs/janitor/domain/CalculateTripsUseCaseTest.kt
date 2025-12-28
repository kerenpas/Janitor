package com.hitechs.janitor.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class CalculateTripsUseCaseTest {

    private lateinit var useCase: CalculateTripsUseCase

    @Before
    fun setup() {
        useCase = CalculateTripsUseCase()
    }

    @Test
    fun `empty list returns zero trips`() {
        // Given
        val bags = emptyList<GarbageBag>()

        // When
        val result = useCase(bags)

        // Then
        assertThat(result.totalTrips).isEqualTo(0)
        assertThat(result.tripGroups).isEmpty()
    }

    @Test
    fun `single light bag returns one trip`() {
        // Given
        val bags = listOf(GarbageBag(1.5))

        // When
        val result = useCase(bags)

        // Then
        assertThat(result.totalTrips).isEqualTo(1)
        assertThat(result.tripGroups).hasSize(1)
        assertThat(result.tripGroups[0].map { it.weight }).containsExactly(1.5)
    }

    @Test
    fun `single heavy bag returns one trip`() {
        // Given
        val bags = listOf(GarbageBag(2.5))

        // When
        val result = useCase(bags)

        // Then
        assertThat(result.totalTrips).isEqualTo(1)
        assertThat(result.tripGroups).hasSize(1)
        assertThat(result.tripGroups[0].map { it.weight }).containsExactly(2.5)
    }

    @Test
    fun `two bags that can be paired together return one trip`() {
        // Given
        val bags = listOf(
            GarbageBag(1.2),
            GarbageBag(1.8)
        )

        // When
        val result = useCase(bags)

        // Then
        assertThat(result.totalTrips).isEqualTo(1)
        assertThat(result.tripGroups).hasSize(1)
        assertThat(result.tripGroups[0]).hasSize(2)
    }

    @Test
    fun `two bags that cannot be paired return two trips`() {
        // Given
        val bags = listOf(
            GarbageBag(1.8),
            GarbageBag(1.8)
        )

        // When
        val result = useCase(bags)

        // Then
        assertThat(result.totalTrips).isEqualTo(2)
        assertThat(result.tripGroups).hasSize(2)
    }

    @Test
    fun `heavy bags get their own trips`() {
        // Given
        val bags = listOf(
            GarbageBag(2.5),
            GarbageBag(2.8),
            GarbageBag(1.5)
        )

        // When
        val result = useCase(bags)

        // Then
        assertThat(result.totalTrips).isEqualTo(3)
        // Heavy bags should be in separate trips
        val heavyBagTrips = result.tripGroups.filter { it.any { bag -> bag.weight > 1.99 } }
        assertThat(heavyBagTrips).hasSize(2)
        heavyBagTrips.forEach { trip ->
            assertThat(trip).hasSize(1)
        }
    }

    @Test
    fun `optimal pairing of light bags`() {
        // Given - bags that should be optimally paired
        val bags = listOf(
            GarbageBag(1.01), // lightest
            GarbageBag(1.5),
            GarbageBag(1.8),
            GarbageBag(1.99)  // heaviest light bag
        )

        // When
        val result = useCase(bags)

        // Then
        // Should pair 1.99 with 1.01 (2.00 total)
        // Should pair 1.8 with 1.5 (3.3 total - wait, this exceeds 3.0!)
        // Actually 1.8 should go alone, 1.5 should go alone or with 1.01
        // Let me reconsider: sorted desc = [1.99, 1.8, 1.5, 1.01]
        // Two pointer: try 1.99 + 1.01 = 3.0 ✓ paired
        // Try 1.8 + 1.5 = 3.3 ✗ can't pair
        // 1.8 goes alone, 1.5 remains and goes alone
        // Total: 3 trips
        assertThat(result.totalTrips).isEqualTo(3)
    }

    @Test
    fun `mixed heavy and light bags`() {
        // Given
        val bags = listOf(
            GarbageBag(2.5),  // heavy
            GarbageBag(1.5),  // light
            GarbageBag(1.5),  // light
            GarbageBag(2.1)   // heavy
        )

        // When
        val result = useCase(bags)

        // Then
        // 2 heavy bags = 2 trips
        // 2 light bags can be paired = 1 trip
        // Total = 3 trips
        assertThat(result.totalTrips).isEqualTo(3)
    }

    @Test
    fun `example from requirements - 1_01 and 1_99`() {
        // Given
        val bags = listOf(
            GarbageBag(1.01),
            GarbageBag(1.99)
        )

        // When
        val result = useCase(bags)

        // Then
        // 1.01 + 1.99 = 3.0 exactly, should be paired
        assertThat(result.totalTrips).isEqualTo(1)
        assertThat(result.tripGroups[0]).hasSize(2)
    }

    @Test
    fun `maximum weight bag`() {
        // Given
        val bags = listOf(
            GarbageBag(3.0),
            GarbageBag(3.0)
        )

        // When
        val result = useCase(bags)

        // Then
        // Both bags are at max weight, should go separately
        assertThat(result.totalTrips).isEqualTo(2)
        result.tripGroups.forEach { trip ->
            assertThat(trip).hasSize(1)
        }
    }

    @Test
    fun `minimum weight bags can be paired`() {
        // Given
        val bags = listOf(
            GarbageBag(1.01),
            GarbageBag(1.01)
        )

        // When
        val result = useCase(bags)

        // Then
        // 1.01 + 1.01 = 2.02, should be paired
        assertThat(result.totalTrips).isEqualTo(1)
        assertThat(result.tripGroups[0]).hasSize(2)
    }

    @Test
    fun `large number of bags`() {
        // Given
        val bags = List(10) { GarbageBag(1.5) }

        // When
        val result = useCase(bags)

        // Then
        // All bags are 1.5kg, can be paired: 1.5 + 1.5 = 3.0
        // 10 bags / 2 = 5 trips
        assertThat(result.totalTrips).isEqualTo(5)
        result.tripGroups.forEach { trip ->
            assertThat(trip).hasSize(2)
        }
    }

    @Test
    fun `all trips respect weight limit`() {
        // Given
        val bags = listOf(
            GarbageBag(1.01),
            GarbageBag(1.5),
            GarbageBag(1.8),
            GarbageBag(1.99),
            GarbageBag(2.5),
            GarbageBag(3.0)
        )

        // When
        val result = useCase(bags)

        // Then
        // Verify no trip exceeds 3.0 kg
        result.tripGroups.forEach { trip ->
            val totalWeight = trip.sumOf { it.weight }
            assertThat(totalWeight).isAtMost(3.0)
        }
    }
}
