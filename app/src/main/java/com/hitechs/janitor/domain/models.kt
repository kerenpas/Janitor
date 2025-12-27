package com.hitechs.janitor.domain

data class GarbageBag(val weight: Double)

data class TripsResult(
    val totalTrips: Int,
    val tripGroups: List<List<GarbageBag>>
)
