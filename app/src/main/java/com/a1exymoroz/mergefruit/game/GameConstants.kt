package com.a1exymoroz.mergefruit.game

/** Ported from the web's src/constants/gameConstants.ts — values kept identical (as dp instead of css px). */
data class FruitType(
    val id: Int,
    val displayName: String,
    val points: Int,
    val radiusDp: Float,
)

val FRUIT_TYPES: List<FruitType> = listOf(
    FruitType(1, "Blueberry", 10, 12f),
    FruitType(2, "Cherry", 20, 16f),
    FruitType(3, "Plum", 50, 20f),
    FruitType(4, "Lemon", 100, 26f),
    FruitType(5, "Kiwi", 200, 32f),
    FruitType(6, "Orange", 500, 40f),
    FruitType(7, "Apple", 1000, 48f),
    FruitType(8, "Peach", 2000, 58f),
    FruitType(9, "Coconut", 5000, 70f),
    FruitType(10, "Melon", 10000, 84f),
    FruitType(11, "Watermelon", 20000, 100f),
)

const val CONTAINER_WIDTH_DP = 300f
const val CONTAINER_HEIGHT_DP = 500f
const val GAME_OVER_LINE_Y_DP = 100f
const val DROP_X_DP = CONTAINER_WIDTH_DP / 2f
const val DROP_Y_DP = 50f
const val CONTAINER_THICKNESS_DP = 20f
const val GAME_OVER_DELAY_MS = 2000L
const val GAME_OVER_GRACE_MS = 500L
