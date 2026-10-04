package com.example.prosci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.prosci.ui.CalcMode
import com.example.prosci.ui.CalculatorViewModel
import com.example.prosci.ui.components.*
import com.example.prosci.ui.theme.LocalProSciColors
import com.example.prosci.ui.theme.ProSciTheme
import com.example.prosci.ui.theme.ProSciThemes

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsState()
            val currentTheme = ProSciThemes.getThemeById(state.themeId)

            ProSciTheme(colorScheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = currentTheme.background
                ) {
                    Scaffold(
                        containerColor = currentTheme.background,
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        CalculatorScreen(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalProSciColors.current

    // Outer screen: full-screen, centered, non-scrolling, adapts 100% to selected theme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.background,
                        colors.background.copy(alpha = 0.95f)
                    )
                )
            )
            .testTag("calculator_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Calculator Body: Centered, fixed frame that fits the screen without shifting,
        // dynamically themed with cardBackground and borderLight
        Box(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.cardBackground,
                            colors.cardBackground.copy(alpha = 0.96f)
                        )
                    )
                )
                .border(
                    2.dp,
                    colors.borderLight,
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("calculator_body")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Fixed Top Header Bar: Does not scroll, uses theme colors
                CalculatorToolbar(
                    state = state,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Fixed LCD Display: 152dp comfortable height, stable, does not grow/shrink with menus
                LcdScreen(
                    state = state,
                    onOpenHistory = { viewModel.openHistoryDialog() },
                    onCursorMoved = { pos -> viewModel.setCursorPosition(pos) },
                    onSelectMenuIndex = { idx -> viewModel.selectWizardMenuIndex(idx) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Keypad / Menu Area: Occupies remaining vertical space (weight 1f).
                // Well-proportioned keys, smooth touch scroll for small screens, zero horizontal overflow.
                Keypad(
                    state = state,
                    onKeyPress = { main, shift, alpha, action ->
                        viewModel.onKeyPressed(main, shift, alpha, action)
                    },
                    onModeClick = { viewModel.openModeDialog() },
                    onAngleClick = { viewModel.cycleAngleUnit() },
                    onFormatClick = { viewModel.cycleNumberFormat() },
                    onMathIoClick = { viewModel.toggleMathIo() },
                    onSdClick = { viewModel.toggleStandardDecimal() },
                    onCompClick = { viewModel.setMode(CalcMode.COMP) },
                    onStatClick = { viewModel.setMode(CalcMode.STAT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        // Dialog Overlays (Unchanged 100%)
        if (state.showModeDialog) {
            ModeDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeModeDialog() }
            )
        }

        if (state.showConstantsDialog) {
            ConstantsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeConstantsDialog() }
            )
        }

        if (state.showConversionDialog) {
            ConversionDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeConversionDialog() }
            )
        }

        if (state.showHistoryDialog) {
            HistoryDialog(
                history = state.history,
                viewModel = viewModel,
                onDismiss = { viewModel.closeHistoryDialog() }
            )
        }

        if (state.showHelpDialog) {
            HelpDialog(
                onDismiss = { viewModel.closeHelpDialog() }
            )
        }
    }
}
