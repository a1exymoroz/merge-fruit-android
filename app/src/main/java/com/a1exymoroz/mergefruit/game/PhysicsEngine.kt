package com.a1exymoroz.mergefruit.game

import org.dyn4j.dynamics.Body
import org.dyn4j.world.World
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import kotlin.math.hypot

/**
 * Wraps a dyn4j [World] to reproduce the web version's Matter.js game loop
 * (see the original src/hooks/useGamePhysics.ts) using dyn4j as the native
 * equivalent physics engine.
 *
 * dyn4j works in abstract "meters"; [PIXELS_PER_METER] is a display-scale
 * choice (not derived from the web version, which used Matter.js's own
 * pixel-based units) picked so fruit radii and the container fall in a
 * range dyn4j's default solver tuning handles well. Gravity/damping below
 * are a first-pass feel match — expect to retune them once the app can
 * actually be run and played.
 */
class PhysicsEngine {

    companion object {
        private const val PIXELS_PER_METER = 40f
        private const val GRAVITY_MPS2 = 25f
        private const val RESTITUTION = 0.4f
        private const val FRICTION = 0.6f
        private const val DENSITY = 1f
        private const val LINEAR_DAMPING = 0.05f
        private const val SETTLED_VELOCITY_DP_PER_S = 60f
    }

    private class LiveFruit(
        val uniqueId: Long,
        val fruitType: FruitType,
        val body: Body,
        val createdAtMs: Long,
    )

    private val world = World<Body>()
    private val liveFruits = LinkedHashMap<Long, LiveFruit>()
    private var nextUniqueId = 1L

    init {
        world.gravity = Vector2(0.0, -GRAVITY_MPS2.toDouble())
        addStaticWalls()
    }

    private fun addStaticWalls() {
        addWall(
            cxDp = CONTAINER_WIDTH_DP / 2f,
            cyDp = CONTAINER_HEIGHT_DP + CONTAINER_THICKNESS_DP / 2f,
            widthDp = CONTAINER_WIDTH_DP,
            heightDp = CONTAINER_THICKNESS_DP,
        )
        addWall(
            cxDp = -CONTAINER_THICKNESS_DP / 2f,
            cyDp = CONTAINER_HEIGHT_DP / 2f,
            widthDp = CONTAINER_THICKNESS_DP,
            heightDp = CONTAINER_HEIGHT_DP,
        )
        addWall(
            cxDp = CONTAINER_WIDTH_DP + CONTAINER_THICKNESS_DP / 2f,
            cyDp = CONTAINER_HEIGHT_DP / 2f,
            widthDp = CONTAINER_THICKNESS_DP,
            heightDp = CONTAINER_HEIGHT_DP,
        )
    }

    private fun addWall(cxDp: Float, cyDp: Float, widthDp: Float, heightDp: Float) {
        val wall = Body()
        wall.addFixture(Geometry.createRectangle(dpToM(widthDp), dpToM(heightDp)))
        wall.setMass(MassType.INFINITE)
        wall.translate(dpXToM(cxDp), dpYFromTopToM(cyDp))
        world.addBody(wall)
    }

    /** Spawns a fruit at the given container-relative position (dp, y-down from the top). */
    fun spawnFruit(fruitType: FruitType, xDp: Float, yDp: Float, vxDpPerS: Float = 0f, vyDpPerS: Float = 0f): Long {
        val id = nextUniqueId++
        val body = Body()
        body.addFixture(Geometry.createCircle(dpToM(fruitType.radiusDp)), DENSITY.toDouble(), FRICTION.toDouble(), RESTITUTION.toDouble())
        body.setMass(MassType.NORMAL)
        body.setLinearDamping(LINEAR_DAMPING.toDouble())
        body.translate(dpXToM(xDp), dpYFromTopToM(yDp))
        if (vxDpPerS != 0f || vyDpPerS != 0f) {
            // vy is flipped: dp-space y-down velocity becomes world-space y-up velocity.
            body.linearVelocity = Vector2(dpToM(vxDpPerS).toDouble(), -dpToM(vyDpPerS).toDouble())
        }
        world.addBody(body)
        liveFruits[id] = LiveFruit(id, fruitType, body, System.currentTimeMillis())
        return id
    }

    fun clear() {
        liveFruits.values.forEach { world.removeBody(it.body) }
        liveFruits.clear()
    }

    /** The live fruit whose circle contains the given container point, if any (nearest centre wins). */
    private fun fruitAt(xDp: Float, yDp: Float): LiveFruit? =
        liveFruits.values
            .mapNotNull { f ->
                val (cx, cy) = worldCenterDp(f.body)
                val d = hypot((cx - xDp).toDouble(), (cy - yDp).toDouble()).toFloat()
                if (d <= f.fruitType.radiusDp) f to d else null
            }
            .minByOrNull { it.second }
            ?.first

    /** Bomb booster: removes the fruit under [xDp],[yDp]. Returns true if one was hit. */
    fun removeFruitAt(xDp: Float, yDp: Float): Boolean {
        val hit = fruitAt(xDp, yDp) ?: return false
        world.removeBody(hit.body)
        liveFruits.remove(hit.uniqueId)
        return true
    }

    /** Upgrade booster: replaces the fruit under [xDp],[yDp] with the next tier. Returns true if applied. */
    fun upgradeFruitAt(xDp: Float, yDp: Float): Boolean {
        val hit = fruitAt(xDp, yDp) ?: return false
        val nextType = FRUIT_TYPES.firstOrNull { it.id == hit.fruitType.id + 1 } ?: return false
        val (cx, cy) = worldCenterDp(hit.body)
        world.removeBody(hit.body)
        liveFruits.remove(hit.uniqueId)
        spawnFruit(nextType, cx, cy)
        return true
    }

    /**
     * Advances the simulation by [dtSeconds] and resolves same-type merges.
     *
     * The web version guards merges with a `mergeQueue` + 50ms delayed
     * release because Matter.js's async collision events can otherwise
     * double-fire for the same pair within one tick. This is a synchronous
     * per-frame scan over a fixed snapshot instead, so a fruit consumed
     * earlier in the scan is simply absent from the map for the rest of the
     * scan — no equivalent queue/delay is needed.
     */
    fun step(dtSeconds: Float): PhysicsStepResult {
        world.update(dtSeconds.toDouble())

        var scoreDelta = 0
        val snapshot = liveFruits.values.toList()
        val consumed = HashSet<Long>()

        for (i in snapshot.indices) {
            val a = snapshot[i]
            if (a.uniqueId in consumed) continue
            for (j in i + 1 until snapshot.size) {
                val b = snapshot[j]
                if (b.uniqueId in consumed) continue
                if (a.fruitType.id != b.fruitType.id) continue
                if (a.fruitType.id >= FRUIT_TYPES.size) continue // Watermelon: no further merges

                val (ax, ay) = worldCenterDp(a.body)
                val (bx, by) = worldCenterDp(b.body)
                val dist = hypot((ax - bx).toDouble(), (ay - by).toDouble()).toFloat()
                if (dist >= a.fruitType.radiusDp + b.fruitType.radiusDp) continue

                // Merge a + b into the next fruit type.
                val nextType = FRUIT_TYPES.firstOrNull { it.id == a.fruitType.id + 1 } ?: continue

                val avA = velocityDpPerS(a.body)
                val avB = velocityDpPerS(b.body)
                val avgVx = (avA.first + avB.first) / 2f
                val avgVy = (avA.second + avB.second) / 2f
                val mergeX = (ax + bx) / 2f
                val mergeY = (ay + by) / 2f

                world.removeBody(a.body)
                world.removeBody(b.body)
                liveFruits.remove(a.uniqueId)
                liveFruits.remove(b.uniqueId)
                consumed.add(a.uniqueId)
                consumed.add(b.uniqueId)

                spawnFruit(nextType, mergeX, mergeY, avgVx, avgVy)
                scoreDelta += nextType.points
                break
            }
        }

        val nowMs = System.currentTimeMillis()
        var fruitAboveLine = false
        val renderStates = ArrayList<FruitRenderState>(liveFruits.size)
        for (fruit in liveFruits.values) {
            val (x, y) = worldCenterDp(fruit.body)
            renderStates.add(FruitRenderState(fruit.uniqueId, fruit.fruitType, x, y, angleRad(fruit.body)))

            val (_, vy) = velocityDpPerS(fruit.body)
            val top = y - fruit.fruitType.radiusDp
            val isSettled = kotlin.math.abs(vy) < SETTLED_VELOCITY_DP_PER_S
            val isPastGrace = nowMs - fruit.createdAtMs > GAME_OVER_GRACE_MS
            if (top < GAME_OVER_LINE_Y_DP && isSettled && isPastGrace) {
                fruitAboveLine = true
            }
        }

        return PhysicsStepResult(renderStates, scoreDelta, fruitAboveLine)
    }

    // --- unit conversions -------------------------------------------------
    // dyn4j is y-up; the game container is expressed y-down from its top,
    // matching the web layout (see gameConstants.ts). dpXToM/dpYFromTopToM
    // convert a container position to world meters; worldCenterDp inverts it.

    private fun dpToM(valueDp: Float): Double = (valueDp / PIXELS_PER_METER).toDouble()
    private fun dpXToM(xDp: Float): Double = dpToM(xDp)
    private fun dpYFromTopToM(yDp: Float): Double = dpToM(CONTAINER_HEIGHT_DP - yDp)

    private fun worldCenterDp(body: Body): Pair<Float, Float> {
        val c = body.worldCenter
        val xDp = (c.x * PIXELS_PER_METER).toFloat()
        val yDp = CONTAINER_HEIGHT_DP - (c.y * PIXELS_PER_METER).toFloat()
        return xDp to yDp
    }

    private fun velocityDpPerS(body: Body): Pair<Float, Float> {
        val v = body.linearVelocity
        val vxDp = (v.x * PIXELS_PER_METER).toFloat()
        val vyDp = -(v.y * PIXELS_PER_METER).toFloat() // flip back to y-down
        return vxDp to vyDp
    }

    private fun angleRad(body: Body): Float = body.transform.rotationAngle.toFloat()
}
