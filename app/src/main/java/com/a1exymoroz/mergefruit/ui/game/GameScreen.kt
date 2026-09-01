package com.a1exymoroz.mergefruit.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.a1exymoroz.mergefruit.R
import com.a1exymoroz.mergefruit.data.auth.AuthUiState
import com.a1exymoroz.mergefruit.game.Booster
import com.a1exymoroz.mergefruit.game.CONTAINER_HEIGHT_DP
import com.a1exymoroz.mergefruit.game.CONTAINER_WIDTH_DP
import com.a1exymoroz.mergefruit.game.FRUIT_TYPES
import com.a1exymoroz.mergefruit.game.GAME_OVER_LINE_Y_DP
import com.a1exymoroz.mergefruit.game.GameUiState
import com.a1exymoroz.mergefruit.game.GameViewModel
import com.a1exymoroz.mergefruit.ui.auth.AuthViewModel
import com.a1exymoroz.mergefruit.ui.common.LanguageSwitcher
import com.a1exymoroz.mergefruit.ui.common.ThemeSwitcher
import com.a1exymoroz.mergefruit.ui.fruit.FruitVisual
import com.a1exymoroz.mergefruit.ui.leaderboard.LeaderboardSection
import com.a1exymoroz.mergefruit.ui.leaderboard.LeaderboardViewModel
import com.a1exymoroz.mergefruit.ui.theme.GameThemeOption
import com.a1exymoroz.mergefruit.ui.theme.GameOverLineColor
import com.a1exymoroz.mergefruit.ui.theme.LocalGameTheme
import com.a1exymoroz.mergefruit.ui.theme.TextSecondary
import com.a1exymoroz.mergefruit.ui.theme.gameBackground
import com.a1exymoroz.mergefruit.ui.theme.rememberGameThemeSpec
import kotlin.math.roundToInt

/** Mirrors src/components/containers/MergeFruitGame.tsx. */
@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    authViewModel: AuthViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    onNavigateToLogin: () -> Unit,
    onSetTheme: (GameThemeOption) -> Unit,
) {
    val gameState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val leaderboardState by leaderboardViewModel.state.collectAsStateWithLifecycle()
    val submitState by leaderboardViewModel.submitState.collectAsStateWithLifecycle()

    val themeOption = LocalGameTheme.current
    val themeSpec = rememberGameThemeSpec(themeOption)

    LaunchedEffect(authState.isGuest) {
        if (!authState.isGuest) leaderboardViewModel.refresh()
    }
    LaunchedEffect(gameState.gameOver) {
        if (!gameState.gameOver) leaderboardViewModel.resetSubmitState()
    }

    Box(Modifier.fillMaxSize().gameBackground(themeOption)) {
        if (themeSpec.showSnow) {
            SnowLayer(Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GameHud(
                authState = authState,
                score = gameState.score,
                highScore = leaderboardState.highScore,
                nextFruitId = gameState.nextFruit?.id,
                themeOption = themeOption,
                wearHats = themeSpec.wearHats,
                onLogout = { authViewModel.logout(); onNavigateToLogin() },
                onLoginClick = onNavigateToLogin,
                onSetTheme = onSetTheme,
            )

            Spacer(Modifier.height(10.dp))

            BoosterBar(
                gameState = gameState,
                cardBackground = themeSpec.hudCardBackground,
                wearHats = themeSpec.wearHats,
                onToggleBooster = gameViewModel::toggleBooster,
                onSwap = gameViewModel::swapNextFruit,
                onHold = gameViewModel::holdFruit,
            )

            Spacer(Modifier.height(10.dp))

            GameContainerSection(
                gameState = gameState,
                containerBackground = themeSpec.containerBackground,
                containerBorder = themeSpec.containerBorder,
                wearHats = themeSpec.wearHats,
                showCandyCane = themeSpec.showCandyCane,
                onDrop = gameViewModel::dropFruit,
                onApplyBoosterAt = gameViewModel::applyBoosterAt,
            )

            Spacer(Modifier.height(10.dp))

            FruitProgressBar(
                largestFruitId = gameState.largestFruitId,
                wearHats = themeSpec.wearHats,
                cardBackground = themeSpec.hudCardBackground,
            )

            Spacer(Modifier.height(10.dp))

            Button(onClick = gameViewModel::resetGame) { Text(stringResource(R.string.game_reset)) }

            Spacer(Modifier.height(16.dp))

            InstructionsSection()

            if (!authState.isGuest) {
                Spacer(Modifier.height(16.dp))
                LeaderboardSection(state = leaderboardState, onRetry = leaderboardViewModel::refresh)
            }

            Spacer(Modifier.height(16.dp))
        }

        if (gameState.gameOver) {
            GameOverOverlay(
                score = gameState.score,
                highScore = leaderboardState.highScore,
                isGuest = authState.isGuest,
                displayName = authState.user?.displayName,
                submitState = submitState,
                leaderboardEntries = leaderboardState.entries,
                onSubmitScore = { leaderboardViewModel.submitScore(gameState.score) },
                onPlayAgain = gameViewModel::resetGame,
                onLoginClick = onNavigateToLogin,
            )
        }
    }
}

@Composable
private fun GameHud(
    authState: AuthUiState,
    score: Int,
    highScore: Long,
    nextFruitId: Int?,
    themeOption: GameThemeOption,
    wearHats: Boolean,
    onLogout: () -> Unit,
    onLoginClick: () -> Unit,
    onSetTheme: (GameThemeOption) -> Unit,
) {
    val user = authState.user

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (authState.isGuest) stringResource(R.string.auth_guest_greeting)
                else stringResource(R.string.auth_hi, user?.displayName ?: ""),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Button(onClick = if (authState.isGuest) onLoginClick else onLogout) {
                Text(stringResource(if (authState.isGuest) R.string.auth_log_in else R.string.auth_log_out))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LanguageSwitcher()
            ThemeSwitcher(current = themeOption, onToggle = onSetTheme)
        }

        Text(
            stringResource(R.string.common_app_title_game),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            textAlign = TextAlign.Center,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCard(
                label = stringResource(R.string.game_score_short),
                value = "%,d".format(score),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "🏆",
                value = "%,d".format(highScore),
                modifier = Modifier.weight(1f),
            )
            NextFruitCard(nextFruitId = nextFruitId, wearHats = wearHats)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun NextFruitCard(nextFruitId: Int?, wearHats: Boolean) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.game_next_short), color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        val fruit = nextFruitId?.let { id -> FRUIT_TYPES.firstOrNull { it.id == id } }
        if (fruit != null) {
            FruitVisual(fruit, sizeDp = 34.dp, wearHat = wearHats)
        } else {
            Spacer(Modifier.size(34.dp))
        }
    }
}

@Composable
private fun GameContainerSection(
    gameState: GameUiState,
    containerBackground: Color,
    containerBorder: Color,
    wearHats: Boolean,
    showCandyCane: Boolean,
    onDrop: (Float) -> Unit,
    onApplyBoosterAt: (Float, Float) -> Unit,
) {
    var dropX by remember { mutableFloatStateOf(CONTAINER_WIDTH_DP / 2f) }
    var isDragging by remember { mutableStateOf(false) }
    val nextFruit = gameState.nextFruit

    val config = LocalConfiguration.current
    val density = LocalDensity.current
    // The 3D box adds depth beyond the front face: dx to the right, dy on top.
    val depthXFrac = 0.16f
    val depthYFrac = depthXFrac * 0.5f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showCandyCane) {
            CandyCane(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(58.dp)
                    .padding(bottom = 4.dp),
            )
        }

        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            val fitWidth = maxWidth.value / (CONTAINER_WIDTH_DP * (1f + depthXFrac))
            val fitHeight = (config.screenHeightDp * 0.52f) /
                (CONTAINER_HEIGHT_DP + CONTAINER_WIDTH_DP * depthXFrac * depthYFrac)
            val scale = minOf(fitWidth, fitHeight).coerceAtLeast(0.4f)

            val frontWDp = CONTAINER_WIDTH_DP * scale
            val frontHDp = CONTAINER_HEIGHT_DP * scale
            val depthXDp = frontWDp * depthXFrac
            val depthYDp = frontWDp * depthYFrac
            val lineY = GAME_OVER_LINE_Y_DP * scale

            val box = with(density) {
                GlassBox(frontWDp.dp.toPx(), frontHDp.dp.toPx(), depthXDp.dp.toPx(), depthYDp.dp.toPx())
            }

            Box(Modifier.size((frontWDp + depthXDp).dp, (frontHDp + depthYDp).dp)) {
                Canvas(Modifier.matchParentSize()) { drawGlassBoxBack(box, containerBackground) }

              Box(
                modifier = Modifier
                    .offsetDp(0f, depthYDp)
                    .size(frontWDp.dp, frontHDp.dp)
                    .clip(RoundedCornerShape(6.dp)),
              ) {
                gameState.fruits.forEach { fruit ->
                    val diameterDp = fruit.fruitType.radiusDp * 2 * scale
                    FruitVisual(
                        fruitType = fruit.fruitType,
                        sizeDp = diameterDp.dp,
                        wearHat = wearHats,
                        modifier = Modifier
                            .offsetDp((fruit.xDp - fruit.fruitType.radiusDp) * scale, (fruit.yDp - fruit.fruitType.radiusDp) * scale)
                            .rotate(Math.toDegrees(fruit.angleRad.toDouble()).toFloat()),
                    )
                }

                Box(
                    modifier = Modifier
                        .offsetDp(0f, lineY)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(GameOverLineColor.copy(alpha = 0.6f)),
                )
                Text(
                    stringResource(R.string.game_over_line_label),
                    color = GameOverLineColor,
                    fontSize = 10.sp,
                    modifier = Modifier.offsetDp(4f, lineY + 2f),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineY.dp)
                        .pointerInput(nextFruit?.id, scale) {
                            if (nextFruit == null) return@pointerInput
                            val minX = nextFruit.radiusDp
                            val maxX = CONTAINER_WIDTH_DP - nextFruit.radiusDp
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                isDragging = true
                                dropX = (down.position.x.toDp().value / scale).coerceIn(minX, maxX)
                                down.consume()

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    dropX = (change.position.x.toDp().value / scale).coerceIn(minX, maxX)
                                    change.consume()
                                }
                                isDragging = false
                                onDrop(dropX)
                            }
                        },
                ) {
                    nextFruit?.let { fruit ->
                        val diameter = fruit.radiusDp * 2 * scale
                        FruitVisual(
                            fruitType = fruit,
                            sizeDp = diameter.dp,
                            wearHat = wearHats,
                            modifier = Modifier
                                .offsetDp((dropX - fruit.radiusDp) * scale, (lineY - diameter) / 2f)
                                .alpha(0.6f),
                        )
                    }
                    Text(
                        stringResource(if (isDragging) R.string.game_release_to_drop else R.string.game_move_to_position),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
                    )
                }

                gameState.armedBooster?.let { armed ->
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(armed, scale) {
                                detectTapGestures { off ->
                                    onApplyBoosterAt(off.x.toDp().value / scale, off.y.toDp().value / scale)
                                }
                            },
                    ) {
                        Text(
                            stringResource(
                                if (armed == Booster.BOMB) R.string.booster_hint_bomb
                                else R.string.booster_hint_upgrade,
                            ),
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xAA000000))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
              }

                // Near glass edges + reflections, over the fruits.
                Canvas(Modifier.matchParentSize()) { drawGlassBoxFront(box, containerBorder) }
            }
        }
    }
}

private fun Modifier.offsetDp(xDp: Float, yDp: Float): Modifier = this.offset {
    IntOffset((xDp * density).roundToInt(), (yDp * density).roundToInt())
}

@Composable
private fun BoosterBar(
    gameState: GameUiState,
    cardBackground: Color,
    wearHats: Boolean,
    onToggleBooster: (Booster) -> Unit,
    onSwap: () -> Unit,
    onHold: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoosterButton(
            label = stringResource(R.string.booster_bomb),
            count = gameState.bombs,
            armed = gameState.armedBooster == Booster.BOMB,
            enabled = !gameState.gameOver && gameState.bombs > 0,
            onClick = { onToggleBooster(Booster.BOMB) },
        ) { Text("💣", fontSize = 20.sp) }

        BoosterButton(
            label = stringResource(R.string.booster_upgrade),
            count = gameState.upgrades,
            armed = gameState.armedBooster == Booster.UPGRADE,
            enabled = !gameState.gameOver && gameState.upgrades > 0,
            onClick = { onToggleBooster(Booster.UPGRADE) },
        ) { Text("⬆️", fontSize = 20.sp) }

        BoosterButton(
            label = stringResource(R.string.booster_swap),
            count = gameState.swaps,
            armed = false,
            enabled = !gameState.gameOver && gameState.swaps > 0,
            onClick = onSwap,
        ) { Text("🔄", fontSize = 20.sp) }

        BoosterButton(
            label = stringResource(R.string.booster_hold),
            count = gameState.holds,
            armed = false,
            enabled = !gameState.gameOver && gameState.holds > 0,
            onClick = onHold,
        ) {
            val held = gameState.heldFruit
            if (held != null) FruitVisual(held, sizeDp = 22.dp, wearHat = wearHats)
            else Text("📦", fontSize = 20.sp)
        }
    }
}

@Composable
private fun BoosterButton(
    label: String,
    count: Int,
    armed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (armed) Color.White.copy(alpha = 0.35f) else Color.Transparent)
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
        Text(label, color = Color.White, fontSize = 10.sp)
        Text("×$count", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun FruitProgressBar(largestFruitId: Int, wearHats: Boolean, cardBackground: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(cardBackground)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FRUIT_TYPES.forEach { fruit ->
            val reached = fruit.id <= largestFruitId
            FruitVisual(
                fruitType = fruit,
                sizeDp = 22.dp,
                wearHat = wearHats,
                modifier = Modifier.alpha(if (reached) 1f else 0.3f),
            )
        }
    }
}

@Composable
private fun InstructionsSection() {
    Column(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.instructions_title), color = Color.White, fontWeight = FontWeight.Bold)
        listOf(
            R.string.instructions_touch,
            R.string.instructions_merge,
            R.string.instructions_chain,
            R.string.instructions_physics,
            R.string.instructions_game_over_rule,
            R.string.instructions_goal,
        ).forEach { resId ->
            Text("• ${stringResource(resId)}", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
