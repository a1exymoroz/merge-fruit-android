package com.a1exymoroz.mergefruit.game

import android.view.Choreographer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

/** Boosters that need a target tap inside the jar before they take effect. */
enum class Booster { BOMB, UPGRADE }

const val BOOSTER_START_COUNT = 3

data class GameUiState(
    val fruits: List<FruitRenderState> = emptyList(),
    val score: Int = 0,
    val nextFruit: FruitType? = null,
    val gameOver: Boolean = false,
    /** Highest fruit id created so far this run — drives the progress strip. */
    val largestFruitId: Int = 1,
    val bombs: Int = BOOSTER_START_COUNT,
    val upgrades: Int = BOOSTER_START_COUNT,
    val swaps: Int = BOOSTER_START_COUNT,
    val holds: Int = BOOSTER_START_COUNT,
    /** Fruit stashed with the hold booster, swapped back in on the next use. */
    val heldFruit: FruitType? = null,
    /** Non-null while the player is choosing a fruit to bomb / upgrade. */
    val armedBooster: Booster? = null,
)

/**
 * Owns the physics-driven game loop, mirroring the web version's
 * MergeFruitGame.tsx + useGamePhysics.ts: a per-frame step, next-fruit
 * generation, drop handling, and the 2s "fruit stuck above the line"
 * game-over rule.
 */
class GameViewModel : ViewModel() {

    private val physicsEngine = PhysicsEngine()

    private val _uiState = MutableStateFlow(GameUiState(nextFruit = generateNextFruit()))
    val uiState: StateFlow<GameUiState> = _uiState

    private var gameOverTimerStartMs: Long? = null
    private var lastFrameTimeNanos: Long = 0L
    private var running = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return

            val dtSeconds = if (lastFrameTimeNanos == 0L) {
                1f / 60f
            } else {
                ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0f, 1f / 30f)
            }
            lastFrameTimeNanos = frameTimeNanos

            stepPhysics(dtSeconds)

            if (running) {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    init {
        start()
    }

    fun start() {
        if (running) return
        running = true
        lastFrameTimeNanos = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun stepPhysics(dtSeconds: Float) {
        val current = _uiState.value
        val result = physicsEngine.step(dtSeconds)
        val newScore = current.score + result.scoreDelta

        val nowMs = System.currentTimeMillis()
        var gameOver = current.gameOver
        if (result.fruitAboveLine) {
            val startedAt = gameOverTimerStartMs
            if (startedAt == null) {
                gameOverTimerStartMs = nowMs
            } else if (nowMs - startedAt > GAME_OVER_DELAY_MS) {
                gameOver = true
            }
        } else {
            gameOverTimerStartMs = null
        }

        val largestFruitId = maxOf(
            current.largestFruitId,
            result.fruits.maxOfOrNull { it.fruitType.id } ?: 1,
        )

        _uiState.value = current.copy(
            fruits = result.fruits,
            score = newScore,
            gameOver = gameOver,
            largestFruitId = largestFruitId,
            armedBooster = if (gameOver) null else current.armedBooster,
        )

        if (gameOver) {
            stop()
        }
    }

    /** Drops the current next-fruit at [xDp] (clamped to stay inside the container) and queues a new one. */
    fun dropFruit(xDp: Float) {
        val current = _uiState.value
        if (current.gameOver || current.armedBooster != null) return
        val fruit = current.nextFruit ?: return

        val clampedX = xDp.coerceIn(fruit.radiusDp, CONTAINER_WIDTH_DP - fruit.radiusDp)
        physicsEngine.spawnFruit(fruit, clampedX, DROP_Y_DP)
        _uiState.value = current.copy(nextFruit = generateNextFruit())
    }

    /** Arms a target-picking booster, or disarms it if the same one was already armed. */
    fun toggleBooster(booster: Booster) {
        val current = _uiState.value
        if (current.gameOver) return
        val count = if (booster == Booster.BOMB) current.bombs else current.upgrades
        if (count <= 0) return
        _uiState.value = current.copy(armedBooster = if (current.armedBooster == booster) null else booster)
    }

    fun cancelBooster() {
        _uiState.value = _uiState.value.copy(armedBooster = null)
    }

    /** Applies the armed booster to the fruit at the tapped container point; consumes a charge only on a hit. */
    fun applyBoosterAt(xDp: Float, yDp: Float) {
        val current = _uiState.value
        val booster = current.armedBooster ?: return
        val hit = when (booster) {
            Booster.BOMB -> physicsEngine.removeFruitAt(xDp, yDp)
            Booster.UPGRADE -> physicsEngine.upgradeFruitAt(xDp, yDp)
        }
        _uiState.value = current.copy(
            armedBooster = null,
            bombs = if (hit && booster == Booster.BOMB) current.bombs - 1 else current.bombs,
            upgrades = if (hit && booster == Booster.UPGRADE) current.upgrades - 1 else current.upgrades,
        )
    }

    /** Swap booster: replaces the queued next fruit with a fresh one. */
    fun swapNextFruit() {
        val current = _uiState.value
        if (current.gameOver || current.swaps <= 0) return
        _uiState.value = current.copy(
            nextFruit = generateNextFruit(),
            swaps = current.swaps - 1,
        )
    }

    /** Hold booster: stashes the queued fruit, or swaps the stashed one back in. */
    fun holdFruit() {
        val current = _uiState.value
        val next = current.nextFruit ?: return
        if (current.gameOver || current.holds <= 0) return
        _uiState.value = current.copy(
            heldFruit = next,
            nextFruit = current.heldFruit ?: generateNextFruit(),
            holds = current.holds - 1,
        )
    }

    fun resetGame() {
        physicsEngine.clear()
        gameOverTimerStartMs = null
        _uiState.value = GameUiState(nextFruit = generateNextFruit())
        start()
    }

    override fun onCleared() {
        stop()
    }

    companion object {
        /**
         * Ported from fruitUtils.ts generateNextFruit(): 80% of the time pick
         * from the first 3 fruit types, 20% of the time from the first 4
         * (adds Lemon) — only the smallest fruits are droppable at first.
         */
        fun generateNextFruit(): FruitType {
            val pool = FRUIT_TYPES.subList(0, if (Random.nextFloat() < 0.8f) 3 else 4)
            return pool[Random.nextInt(pool.size)]
        }
    }
}
