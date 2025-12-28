package com.hitechs.janitor.domain

import java.util.UUID

data class GarbageBag(val weight: Double, val id: UUID = UUID.randomUUID())

data class TripsResult(
    val totalTrips: Int,
    val tripGroups: List<List<GarbageBag>>
)
