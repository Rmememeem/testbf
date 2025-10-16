package com.example.babyfoot.logic

import kotlin.math.pow

object EloCalculator {

    private const val DEFAULT_K = 32.0

    private fun expectedScore(ratingA: Double, ratingB: Double): Double {
        return 1.0 / (1.0 + 10.0.pow((ratingB - ratingA) / 400.0))
    }

    fun update(
        ratingA: Int,
        ratingB: Int,
        scoreA: Double,
        kFactor: Double = DEFAULT_K
    ): Pair<Int, Int> {
        val expA = expectedScore(ratingA.toDouble(), ratingB.toDouble())
        val expB = 1.0 - expA
        val newA = ratingA + kFactor * (scoreA - expA)
        val newB = ratingB + kFactor * ((1.0 - scoreA) - expB)
        return newA.roundToInt() to newB.roundToInt()
    }

    fun teamAdjustments(
        winners: Pair<Int, Int>,
        losers: Pair<Int, Int>,
        kFactor: Double = DEFAULT_K
    ): Pair<Double, Double> {
        val winnersAvg = winners.average()
        val losersAvg = losers.average()
        val expWinners = expectedScore(winnersAvg, losersAvg)
        val expLosers = 1.0 - expWinners
        val newW = winnersAvg + kFactor * (1.0 - expWinners)
        val newL = losersAvg + kFactor * (0.0 - expLosers)
        val deltaW = newW - winnersAvg
        val deltaL = newL - losersAvg
        return deltaW to deltaL
    }

    private fun Pair<Int, Int>.average(): Double = (first + second) / 2.0
}
