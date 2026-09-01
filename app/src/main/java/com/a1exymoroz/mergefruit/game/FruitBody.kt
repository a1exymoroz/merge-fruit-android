package com.a1exymoroz.mergefruit.game

/** A fruit's on-screen render state for one frame, in dp within the game container. */
data class FruitRenderState(
    val uniqueId: Long,
    val fruitType: FruitType,
    val xDp: Float,
    val yDp: Float,
    val angleRad: Float,
)

/** Result of stepping the physics world forward by one frame. */
data class PhysicsStepResult(
    val fruits: List<FruitRenderState>,
    /** Points earned this frame from merges (0 if none). */
    val scoreDelta: Int,
    /** True if at least one settled fruit currently sits above the game-over line, past its grace period. */
    val fruitAboveLine: Boolean,
)
