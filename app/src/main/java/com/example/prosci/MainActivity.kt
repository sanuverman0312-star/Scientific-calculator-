package com.example.prosci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Toolbar
            CalculatorToolbar(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Calculator Body / Casing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBackground)
                    .border(1.5.dp, colors.borderLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("calculator_body")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Calculator Brand Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ProSci",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.functionKeyText,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "fx-991ES · NATURAL-V.P.A.M.",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.functionKeyText.copy(alpha = 0.75f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // LCD Screen
                    LcdScreen(state = state)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Keypad
                    Keypad(
                        onKeyPress = { main, shift, alpha, action ->
                            viewModel.onKeyPressed(main, shift, alpha, action)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer note
            Text(
                text = "ProSci Natural-V.P.A.M. · Tap S⇔D to swap exact & decimal",
                fontSize = 10.sp,
                color = colors.functionKeyText.copy(alpha = 0.5f),
                fontFamily = FontFamily.SansSerif
            )
        }

        // Dialog Overlays
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
