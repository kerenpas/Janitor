package com.hitechs.janitor.domain

import javax.inject.Inject

class CalculateTripsUseCase @Inject constructor() {

    operator fun invoke(bags: List<GarbageBag>): TripsResult {
        val trips = mutableListOf<List<GarbageBag>>()

        // Separate heavy and light bags
        val heavyBags = bags.filter { it.weight > 1.99 }
        val lightBags = bags.filter { it.weight <= 1.99 }

        // Handle heavy bags - each gets its own trip
        trips.addAll(heavyBags.map { listOf(it) })

        // Optimize pairing of light bags
        val sortedLightBags = lightBags.sortedByDescending { it.weight }.toMutableList()

        // Two-pointer approach
        var left = 0
        var right = sortedLightBags.size - 1

        while (left <= right) {
            // If only one bag left, add it as a single trip
            if (left == right) {
                trips.add(listOf(sortedLightBags[left]))
                break
            }

            // Try to pair the lightest and heaviest bags
            val currentTrip = mutableListOf<GarbageBag>()

            if (sortedLightBags[left].weight + sortedLightBags[right].weight <= 3.0) {
                // If they can be paired, add both to the trip
                currentTrip.add(sortedLightBags[left])
                currentTrip.add(sortedLightBags[right])
                trips.add(currentTrip)

                // Move pointers
                left++
                right--
            } else {
                // If they can't be paired, add the heavier bag (right) as a single trip
                trips.add(listOf(sortedLightBags[right]))
                right--
            }
        }

        return TripsResult(trips.size, trips)
    }

}