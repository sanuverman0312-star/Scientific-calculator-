package com.example.prosci.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prosci.data.CodataConstant
import com.example.prosci.data.ConstantsData
import com.example.prosci.data.HistoryItem
import com.example.prosci.data.UnitConversion
import com.example.prosci.engine.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class CalcMode(val title: String) {
    COMP("COMP"),
    CMPLX("CMPLX"),
    STAT("STAT"),
    BASE_N("BASE-N"),
    EQN("EQN"),
    MATRIX("MATRIX"),
    TABLE("TABLE"),
    VECTOR("VECTOR")
}

data class WizardState(
    val stage: String, // "menu", "prompt", "result", "calc"
    val title: String,
    val menuOptions: List<String> = emptyList(),
    val menuIndex: Int = 0,
    val prompts: List<String> = emptyList(),
    val promptIndex: Int = 0,
    val buffer: String = "",
    val collectedValues: List<String> = emptyList(),
    val resultLines: List<String> = emptyList(),
    val scrollIndex: Int = 0,
    val subType: String = "",
    val baseLabel: String = "DEC",
    val baseRadix: Int = 10,
    val baseResult: String = ""
)

data class CalculatorUiState(
    val expression: String = "",
    val cursorPos: Int = 0,
    val resultText: String = "",
    val exactResult: ExactResult? = null,
    val isShowingDecimal: Boolean = false,
    val errorMessage: String? = null,
    val isShift: Boolean = false,
    val isAlpha: Boolean = false,
    val isHyp: Boolean = false,
    val pendingAction: String = "", // "sto", "rcl"
    val memoryHasValue: Boolean = false,
    val currentMode: CalcMode = CalcMode.COMP,
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val formatSetting: NumberFormatSetting = NumberFormatSetting(NumberFormatType.NORM, 0),
    val isMathIo: Boolean = true,
    val themeId: String = "classic",
    val history: List<HistoryItem> = emptyList(),
    val historyIndex: Int = 0,
    val isDone: Boolean = false,
    val wizardState: WizardState? = null,
    val showModeDialog: Boolean = false,
    val showConstantsDialog: Boolean = false,
    val showConversionDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val showHelpDialog: Boolean = false,
    val copiedToast: Boolean = false
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = CalculatorEngine()
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val numberFormats = listOf(
        NumberFormatSetting(NumberFormatType.NORM, 0),
        NumberFormatSetting(NumberFormatType.FIX, 2),
        NumberFormatSetting(NumberFormatType.FIX, 4),
        NumberFormatSetting(NumberFormatType.SCI, 3),
        NumberFormatSetting(NumberFormatType.SCI, 6)
    )
    private var formatIndex = 0

    private val themes = listOf("classic", "midnight", "light", "neon", "rose", "contrast")

    fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    /* ================= KEYPAD ACTIONS ================= */
    fun onKeyPressed(main: String, shift: String = "", alpha: String = "", action: String = "") {
        vibrate()
        val state = _uiState.value

        var effectiveAction = action
        var effectiveText = main

        if (state.isAlpha && alpha.isNotEmpty()) {
            effectiveText = alpha
            effectiveAction = if (alpha.startsWith("@")) alpha else ""
        } else if (state.isShift && shift.isNotEmpty()) {
            effectiveText = shift
            effectiveAction = if (shift.startsWith("@")) shift else ""
        }

        // Reset Shift / Alpha after key consumption unless it's the shift/alpha key itself
        if (effectiveAction != "@SH" && effectiveAction != "@AL") {
            _uiState.update { it.copy(isShift = false, isAlpha = false) }
        }

        if (effectiveAction.startsWith("@")) {
            handleAction(effectiveAction)
        } else {
            handleType(effectiveText)
        }
    }

    fun setCursorPosition(position: Int) {
        vibrate()
        _uiState.update { curr ->
            val safePos = position.coerceIn(0, curr.expression.length)
            curr.copy(cursorPos = safePos)
        }
    }

    private fun handleType(text: String) {
        val state = _uiState.value
        val wiz = state.wizardState

        // Handle Wizard input
        if (state.currentMode != CalcMode.COMP && state.currentMode != CalcMode.CMPLX && wiz != null) {
            handleWizardType(text, wiz)
            return
        }

        // Handle STO / RCL pending
        if (state.pendingAction.isNotEmpty() && text.length == 1 && text[0] in "ABCDEFXYM") {
            val varChar = text[0]
            if (state.pendingAction == "sto") {
                engine.variables[varChar] = engine.ans
            } else if (state.pendingAction == "rcl") {
                val value = engine.variables[varChar] ?: 0.0
                insertText(engine.formatDecimal(value))
            }
            _uiState.update {
                it.copy(
                    pendingAction = "",
                    memoryHasValue = (engine.variables['M'] ?: 0.0) != 0.0
                )
            }
            return
        }

        var t = text
        if (state.isHyp) {
            if (t.startsWith("sin(") || t.startsWith("cos(") || t.startsWith("tan(")) {
                t = t.replace("(", "h(")
            } else if (t.startsWith("asin(") || t.startsWith("acos(") || t.startsWith("atan(")) {
                t = t.replace("(", "h(")
            }
            _uiState.update { it.copy(isHyp = false) }
        }

        if (state.isDone) {
            if (Regex("^([+\\-×÷*/^!%]|nPr|nCr)").containsMatchIn(t)) {
                _uiState.update { it.copy(expression = "Ans", cursorPos = 3, isDone = false, errorMessage = null) }
            } else {
                _uiState.update { it.copy(expression = "", cursorPos = 0, isDone = false, errorMessage = null) }
            }
        }

        insertText(t)
    }

    private fun insertText(t: String) {
        _uiState.update { curr ->
            val expr = curr.expression
            val pos = curr.cursorPos.coerceIn(0, expr.length)
            val newExpr = expr.substring(0, pos) + t + expr.substring(pos)
            curr.copy(
                expression = newExpr,
                cursorPos = pos + t.length,
                errorMessage = null
            )
        }
    }

    private fun handleAction(action: String) {
        val state = _uiState.value
        val wiz = state.wizardState

        if (action != "@SH" && action != "@AL" && state.currentMode != CalcMode.COMP && state.currentMode != CalcMode.CMPLX && wiz != null) {
            handleWizardAction(action, wiz)
            return
        }

        when (action) {
            "@SH" -> _uiState.update { it.copy(isShift = !it.isShift, isAlpha = false) }
            "@AL" -> _uiState.update { it.copy(isAlpha = !it.isAlpha, isShift = false) }
            "@HYP" -> _uiState.update { it.copy(isHyp = !it.isHyp) }
            "@L" -> {
                _uiState.update { curr ->
                    val pos = curr.cursorPos
                    val newPos = if (pos > 0) pos - 1 else 0
                    curr.copy(cursorPos = newPos)
                }
            }
            "@R" -> {
                _uiState.update { curr ->
                    val pos = curr.cursorPos
                    val newPos = if (pos < curr.expression.length) pos + 1 else curr.expression.length
                    curr.copy(cursorPos = newPos)
                }
            }
            "@U" -> {
                val hist = state.history
                if (hist.isNotEmpty() && state.historyIndex > 0) {
                    val newIdx = state.historyIndex - 1
                    val item = hist[newIdx]
                    _uiState.update {
                        it.copy(
                            historyIndex = newIdx,
                            expression = item.expression,
                            cursorPos = item.expression.length,
                            resultText = item.formattedResult,
                            errorMessage = null,
                            isDone = false
                        )
                    }
                }
            }
            "@D" -> {
                val hist = state.history
                if (hist.isNotEmpty() && state.historyIndex < hist.size - 1) {
                    val newIdx = state.historyIndex + 1
                    val item = hist[newIdx]
                    _uiState.update {
                        it.copy(
                            historyIndex = newIdx,
                            expression = item.expression,
                            cursorPos = item.expression.length,
                            resultText = item.formattedResult,
                            errorMessage = null,
                            isDone = false
                        )
                    }
                }
            }
            "@DEL" -> {
                _uiState.update { curr ->
                    if (curr.isDone) {
                        curr.copy(isDone = false, resultText = "")
                    } else {
                        val expr = curr.expression
                        val pos = curr.cursorPos
                        if (pos > 0) {
                            val newExpr = expr.substring(0, pos - 1) + expr.substring(pos)
                            curr.copy(expression = newExpr, cursorPos = pos - 1, errorMessage = null)
                        } else curr
                    }
                }
            }
            "@AC" -> {
                _uiState.update {
                    it.copy(
                        expression = "",
                        cursorPos = 0,
                        resultText = "",
                        exactResult = null,
                        errorMessage = null,
                        isDone = false,
                        pendingAction = "",
                        isHyp = false
                    )
                }
            }
            "@EQ" -> evaluateCurrentExpression()
            "@STO" -> {
                if (state.expression.isNotEmpty() && !state.isDone) {
                    evaluateCurrentExpression()
                }
                _uiState.update { it.copy(pendingAction = "sto", isAlpha = true) }
            }
            "@RCL" -> {
                _uiState.update { it.copy(pendingAction = "rcl", isAlpha = true) }
            }
            "@M+" -> {
                if (state.expression.isNotEmpty() && !state.isDone) {
                    evaluateCurrentExpression()
                }
                val currentM = engine.variables['M'] ?: 0.0
                engine.variables['M'] = currentM + engine.ans
                _uiState.update { it.copy(memoryHasValue = (engine.variables['M'] ?: 0.0) != 0.0) }
            }
            "@M-" -> {
                if (state.expression.isNotEmpty() && !state.isDone) {
                    evaluateCurrentExpression()
                }
                val currentM = engine.variables['M'] ?: 0.0
                engine.variables['M'] = currentM - engine.ans
                _uiState.update { it.copy(memoryHasValue = (engine.variables['M'] ?: 0.0) != 0.0) }
            }
        }
    }

    private fun evaluateCurrentExpression() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) return

        try {
            val v = engine.evaluate(expr)
            engine.ans = v
            val exact = engine.getResult(v)

            val displayResult = if (state.isMathIo && !state.isShowingDecimal) {
                exact.fraction?.toDisplayString() ?: exact.exactSymbolic ?: exact.decimalStr
            } else {
                exact.decimalStr
            }

            val newHistory = state.history + HistoryItem(expr, v, displayResult)
            _uiState.update {
                it.copy(
                    resultText = displayResult,
                    exactResult = exact,
                    errorMessage = null,
                    isDone = true,
                    history = newHistory,
                    historyIndex = newHistory.size
                )
            }
        } catch (e: CalcException) {
            _uiState.update {
                it.copy(
                    errorMessage = e.msg,
                    cursorPos = e.position.coerceIn(0, expr.length),
                    resultText = ""
                )
            }
        } catch (_: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Syntax ERROR",
                    cursorPos = expr.length,
                    resultText = ""
                )
            }
        }
    }

    fun toggleStandardDecimal() {
        vibrate()
        val state = _uiState.value
        val exact = state.exactResult ?: return
        val newShowingDecimal = !state.isShowingDecimal

        val display = if (newShowingDecimal) {
            exact.decimalStr
        } else {
            exact.fraction?.toDisplayString() ?: exact.exactSymbolic ?: exact.decimalStr
        }

        _uiState.update {
            it.copy(
                isShowingDecimal = newShowingDecimal,
                resultText = display
            )
        }
    }

    fun cycleAngleUnit() {
        vibrate()
        val nextUnit = when (_uiState.value.angleUnit) {
            AngleUnit.DEG -> AngleUnit.RAD
            AngleUnit.RAD -> AngleUnit.GRA
            AngleUnit.GRA -> AngleUnit.DEG
        }
        engine.angleUnit = nextUnit
        _uiState.update { it.copy(angleUnit = nextUnit) }
        if (_uiState.value.expression.isNotEmpty()) {
            evaluateCurrentExpression()
        }
    }

    fun cycleNumberFormat() {
        vibrate()
        formatIndex = (formatIndex + 1) % numberFormats.size
        val setting = numberFormats[formatIndex]
        engine.formatSetting = setting
        _uiState.update { it.copy(formatSetting = setting) }
        if (_uiState.value.exactResult != null) {
            val v = engine.ans
            val exact = engine.getResult(v)
            _uiState.update {
                it.copy(
                    exactResult = exact,
                    resultText = if (it.isShowingDecimal) exact.decimalStr else (exact.fraction?.toDisplayString() ?: exact.exactSymbolic ?: exact.decimalStr)
                )
            }
        }
    }

    fun toggleMathIo() {
        vibrate()
        _uiState.update { it.copy(isMathIo = !it.isMathIo) }
    }

    fun cycleTheme() {
        vibrate()
        val currentIdx = themes.indexOf(_uiState.value.themeId)
        val nextTheme = themes[(currentIdx + 1) % themes.size]
        _uiState.update { it.copy(themeId = nextTheme) }
    }

    fun setTheme(themeId: String) {
        vibrate()
        _uiState.update { it.copy(themeId = themeId) }
    }

    fun copyResult() {
        vibrate()
        val textToCopy = _uiState.value.resultText.ifEmpty { _uiState.value.expression }
        if (textToCopy.isEmpty()) return

        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("ProSci Result", textToCopy)
        clipboard?.setPrimaryClip(clip)

        _uiState.update { it.copy(copiedToast = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            _uiState.update { it.copy(copiedToast = false) }
        }
    }

    fun clearHistory() {
        vibrate()
        _uiState.update { it.copy(history = emptyList(), historyIndex = 0) }
    }

    fun loadHistoryItem(item: HistoryItem) {
        vibrate()
        _uiState.update {
            it.copy(
                expression = item.expression,
                cursorPos = item.expression.length,
                resultText = item.formattedResult,
                errorMessage = null,
                isDone = false,
                showHistoryDialog = false
            )
        }
    }

    /* ================= MODE SELECTION & WIZARD ================= */
    fun setMode(mode: CalcMode) {
        vibrate()
        _uiState.update {
            it.copy(
                currentMode = mode,
                showModeDialog = false,
                expression = "",
                cursorPos = 0,
                resultText = "",
                errorMessage = null,
                wizardState = if (mode != CalcMode.COMP && mode != CalcMode.CMPLX) createWizardForMode(mode) else null
            )
        }
    }

    private fun createWizardForMode(mode: CalcMode): WizardState {
        return when (mode) {
            CalcMode.STAT -> WizardState(
                stage = "menu",
                title = "STAT",
                menuOptions = listOf("1-VAR", "A+BX", "_+CX²", "ln X", "e^X", "A·B^X", "A·X^B", "1/X")
            )
            CalcMode.BASE_N -> WizardState(
                stage = "calc",
                title = "BASE-N",
                baseLabel = "DEC",
                baseRadix = 10,
                buffer = "",
                baseResult = "DEC 0\nHEX 0\nBIN 0\nOCT 0"
            )
            CalcMode.EQN -> WizardState(
                stage = "menu",
                title = "EQN",
                menuOptions = listOf("2 unknowns", "3 unknowns", "Quadratic", "Cubic")
            )
            CalcMode.MATRIX -> WizardState(
                stage = "menu",
                title = "MATRIX",
                menuOptions = listOf("A+B", "A−B", "A×B", "Det A", "A⁻¹", "Aᵀ")
            )
            CalcMode.TABLE -> WizardState(
                stage = "prompt",
                title = "TABLE",
                prompts = listOf("f(X)", "Start", "End", "Step"),
                promptIndex = 0,
                buffer = ""
            )
            CalcMode.VECTOR -> WizardState(
                stage = "menu",
                title = "VECTOR",
                menuOptions = listOf("A+B", "A−B", "A·B", "A×B", "|A|", "Angle")
            )
            else -> WizardState(stage = "menu", title = mode.title)
        }
    }

    private fun handleWizardType(text: String, wiz: WizardState) {
        when (wiz.stage) {
            "menu" -> {
                // Number 1-8 selects menu item directly
                val num = text.toIntOrNull()
                if (num != null && num in 1..wiz.menuOptions.size) {
                    pickWizardMenu(wiz, num - 1)
                }
            }
            "prompt" -> {
                val newBuf = wiz.buffer + text
                _uiState.update { it.copy(wizardState = wiz.copy(buffer = newBuf)) }
            }
            "calc" -> {
                // Base-N typing
                val digits = "0123456789ABCDEF".substring(0, wiz.baseRadix)
                val up = text.uppercase()
                if (up in digits || up in "+-*/()×÷") {
                    val newBuf = wiz.buffer + (if (up == "×") "*" else if (up == "÷") "/" else up)
                    updateBaseN(wiz.copy(buffer = newBuf))
                }
            }
        }
    }

    private fun handleWizardAction(action: String, wiz: WizardState) {
        when (action) {
            "@EQ" -> {
                when (wiz.stage) {
                    "menu" -> pickWizardMenu(wiz, wiz.menuIndex)
                    "prompt" -> {
                        val collected = wiz.collectedValues + wiz.buffer
                        val nextIdx = wiz.promptIndex + 1
                        if (nextIdx >= wiz.prompts.size) {
                            computeWizard(wiz.copy(collectedValues = collected, buffer = ""))
                        } else {
                            _uiState.update {
                                it.copy(
                                    wizardState = wiz.copy(
                                        collectedValues = collected,
                                        promptIndex = nextIdx,
                                        buffer = ""
                                    )
                                )
                            }
                        }
                    }
                    "result" -> {
                        // Restart wizard
                        _uiState.update { it.copy(wizardState = createWizardForMode(_uiState.value.currentMode)) }
                    }
                    "calc" -> updateBaseN(wiz)
                }
            }
            "@DEL" -> {
                if (wiz.stage == "prompt" || wiz.stage == "calc") {
                    val newBuf = if (wiz.buffer.isNotEmpty()) wiz.buffer.dropLast(1) else ""
                    if (wiz.stage == "calc") {
                        updateBaseN(wiz.copy(buffer = newBuf))
                    } else {
                        _uiState.update { it.copy(wizardState = wiz.copy(buffer = newBuf)) }
                    }
                }
            }
            "@AC" -> {
                _uiState.update { it.copy(wizardState = createWizardForMode(_uiState.value.currentMode)) }
            }
            "@U" -> {
                if (wiz.stage == "menu") {
                    val newIdx = (wiz.menuIndex - 1).coerceAtLeast(0)
                    _uiState.update { it.copy(wizardState = wiz.copy(menuIndex = newIdx)) }
                } else if (wiz.stage == "result") {
                    val newScroll = (wiz.scrollIndex - 1).coerceAtLeast(0)
                    _uiState.update { it.copy(wizardState = wiz.copy(scrollIndex = newScroll)) }
                }
            }
            "@D" -> {
                if (wiz.stage == "menu") {
                    val newIdx = (wiz.menuIndex + 1).coerceAtMost(wiz.menuOptions.size - 1)
                    _uiState.update { it.copy(wizardState = wiz.copy(menuIndex = newIdx)) }
                } else if (wiz.stage == "result") {
                    val newScroll = wiz.scrollIndex + 1
                    _uiState.update { it.copy(wizardState = wiz.copy(scrollIndex = newScroll)) }
                }
            }
        }
    }

    fun selectWizardMenuIndex(index: Int) {
        vibrate()
        val wiz = _uiState.value.wizardState ?: return
        if (wiz.stage == "menu") {
            val safeIdx = index.coerceIn(0, wiz.menuOptions.size - 1)
            _uiState.update { it.copy(wizardState = wiz.copy(menuIndex = safeIdx)) }
        }
    }

    private fun pickWizardMenu(wiz: WizardState, index: Int) {
        val mode = _uiState.value.currentMode
        when (mode) {
            CalcMode.STAT -> {
                val type = wiz.menuOptions[index]
                val prompts = if (type == "1-VAR") listOf("X (comma separated)") else listOf("X (comma separated)", "Y (comma separated)")
                _uiState.update {
                    it.copy(
                        wizardState = wiz.copy(
                            stage = "prompt",
                            subType = type,
                            prompts = prompts,
                            promptIndex = 0,
                            buffer = "",
                            collectedValues = emptyList()
                        )
                    )
                }
            }
            CalcMode.EQN -> {
                val type = wiz.menuOptions[index]
                val prompts = when (type) {
                    "2 unknowns" -> listOf("a1", "b1", "c1", "a2", "b2", "c2")
                    "3 unknowns" -> listOf("a1", "b1", "c1", "d1", "a2", "b2", "c2", "d2", "a3", "b3", "c3", "d3")
                    "Quadratic" -> listOf("a", "b", "c")
                    else -> listOf("a", "b", "c", "d")
                }
                _uiState.update {
                    it.copy(
                        wizardState = wiz.copy(
                            stage = "prompt",
                            subType = type,
                            prompts = prompts,
                            promptIndex = 0,
                            buffer = "",
                            collectedValues = emptyList()
                        )
                    )
                }
            }
            CalcMode.MATRIX -> {
                val op = wiz.menuOptions[index]
                val prompts = when (op) {
                    "Det A", "A⁻¹", "Aᵀ" -> listOf("MatA rows", "MatA cols", "MatA data (row-major, comma)")
                    "A+B", "A−B" -> listOf("MatA rows", "MatA cols", "MatA data", "MatB rows", "MatB cols", "MatB data")
                    else -> listOf("MatA rows", "MatA cols", "MatA data", "MatB cols", "MatB data (rows = A cols)")
                }
                _uiState.update {
                    it.copy(
                        wizardState = wiz.copy(
                            stage = "prompt",
                            subType = op,
                            prompts = prompts,
                            promptIndex = 0,
                            buffer = "",
                            collectedValues = emptyList()
                        )
                    )
                }
            }
            CalcMode.VECTOR -> {
                val op = wiz.menuOptions[index]
                val prompts = if (op == "|A|") listOf("VctA (x, y [, z])") else listOf("VctA (x, y [, z])", "VctB (x, y [, z])")
                _uiState.update {
                    it.copy(
                        wizardState = wiz.copy(
                            stage = "prompt",
                            subType = op,
                            prompts = prompts,
                            promptIndex = 0,
                            buffer = "",
                            collectedValues = emptyList()
                        )
                    )
                }
            }
            else -> {}
        }
    }

    private fun parseNumberList(s: String): List<Double> {
        return s.split(Regex("[,\\s;]+")).filter { it.isNotBlank() }.mapNotNull { it.toDoubleOrNull() }
    }

    private fun computeWizard(wiz: WizardState) {
        val lines = mutableListOf<String>()
        val vals = wiz.collectedValues

        try {
            when (_uiState.value.currentMode) {
                CalcMode.STAT -> {
                    val xList = parseNumberList(vals.getOrNull(0) ?: "")
                    if (xList.isEmpty()) {
                        lines.add("ERROR: invalid X list")
                    } else {
                        val statMap = engine.stats(xList)
                        statMap.forEach { (k, v) -> lines.add("$k = ${engine.formatDecimal(v)}") }

                        if (wiz.subType != "1-VAR" && vals.size > 1) {
                            val yList = parseNumberList(vals[1])
                            if (yList.size == xList.size) {
                                lines.add("─── Y ───")
                                val yStats = engine.stats(yList)
                                yStats.forEach { (k, v) -> lines.add("${k.replace('x', 'y')} = ${engine.formatDecimal(v)}") }
                                if (wiz.subType == "A+BX") {
                                    val (a, b, r) = engine.linearRegression(xList, yList)
                                    lines.add("A = ${engine.formatDecimal(a)}")
                                    lines.add("B = ${engine.formatDecimal(b)}")
                                    lines.add("r = ${engine.formatDecimal(r)}")
                                }
                            } else {
                                lines.add("ERROR: X and Y size mismatch")
                            }
                        }
                    }
                }
                CalcMode.EQN -> {
                    val nums = vals.mapNotNull { it.toDoubleOrNull() }
                    when (wiz.subType) {
                        "2 unknowns" -> {
                            if (nums.size >= 6) {
                                val a1 = nums[0]
                                val b1 = nums[1]
                                val c1 = nums[2]
                                val a2 = nums[3]
                                val b2 = nums[4]
                                val c2 = nums[5]
                                val det = a1 * b2 - a2 * b1
                                if (abs(det) < 1e-12) {
                                    lines.add("No unique solution")
                                } else {
                                    val x = (c1 * b2 - c2 * b1) / det
                                    val y = (a1 * c2 - a2 * c1) / det
                                    lines.add("x = ${engine.formatDecimal(x)}")
                                    lines.add("y = ${engine.formatDecimal(y)}")
                                }
                            } else lines.add("ERROR: bad input")
                        }
                        "3 unknowns" -> {
                            if (nums.size >= 12) {
                                val mat = arrayOf(
                                    doubleArrayOf(nums[0], nums[1], nums[2], nums[3]),
                                    doubleArrayOf(nums[4], nums[5], nums[6], nums[7]),
                                    doubleArrayOf(nums[8], nums[9], nums[10], nums[11])
                                )
                                val det = engine.gaussJordan(mat, 3)
                                if (abs(det) < 1e-12) {
                                    lines.add("No unique solution")
                                } else {
                                    lines.add("x = ${engine.formatDecimal(mat[0][3])}")
                                    lines.add("y = ${engine.formatDecimal(mat[1][3])}")
                                    lines.add("z = ${engine.formatDecimal(mat[2][3])}")
                                }
                            } else lines.add("ERROR: bad input")
                        }
                        "Quadratic", "Cubic" -> {
                            val roots = engine.polynomialRoots(nums)
                            roots.forEachIndexed { i, root ->
                                lines.add("x${i + 1} = ${root.format()}")
                            }
                        }
                    }
                }
                CalcMode.MATRIX -> {
                    val op = wiz.subType
                    fun parseMat(rStr: String, cStr: String, dataStr: String): Array<DoubleArray> {
                        val r = rStr.toInt()
                        val c = cStr.toInt()
                        val data = parseNumberList(dataStr)
                        if (data.size != r * c) throw IllegalArgumentException("Size mismatch")
                        return Array(r) { i -> DoubleArray(c) { j -> data[i * c + j] } }
                    }

                    if (op in listOf("Det A", "A⁻¹", "Aᵀ")) {
                        val a = parseMat(vals[0], vals[1], vals[2])
                        when (op) {
                            "Det A" -> {
                                val copy = Array(a.size) { a[it].clone() }
                                val det = engine.gaussJordan(copy, a.size)
                                lines.add("Det A = ${engine.formatDecimal(det)}")
                            }
                            "Aᵀ" -> {
                                for (j in 0 until a[0].size) {
                                    lines.add(a.indices.joinToString("   ") { i -> engine.formatDecimal(a[i][j]) })
                                }
                            }
                            "A⁻¹" -> {
                                val n = a.size
                                val augmented = Array(n) { i -> DoubleArray(2 * n) { j -> if (j < n) a[i][j] else if (j - n == i) 1.0 else 0.0 } }
                                val det = engine.gaussJordan(augmented, n)
                                if (abs(det) < 1e-12) {
                                    lines.add("Singular matrix")
                                } else {
                                    for (i in 0 until n) {
                                        lines.add((n until 2 * n).joinToString("   ") { j -> engine.formatDecimal(augmented[i][j]) })
                                    }
                                }
                            }
                        }
                    } else if (op == "A+B" || op == "A−B") {
                        val a = parseMat(vals[0], vals[1], vals[2])
                        val b = parseMat(vals[3], vals[4], vals[5])
                        for (i in a.indices) {
                            lines.add(a[0].indices.joinToString("   ") { j ->
                                val res = if (op == "A+B") a[i][j] + b[i][j] else a[i][j] - b[i][j]
                                engine.formatDecimal(res)
                            })
                        }
                    } else if (op == "A×B") {
                        val a = parseMat(vals[0], vals[1], vals[2])
                        val bc = vals[3].toInt()
                        val bd = parseNumberList(vals[4])
                        val br = a[0].size
                        val b = Array(br) { i -> DoubleArray(bc) { j -> bd[i * bc + j] } }
                        for (i in a.indices) {
                            lines.add((0 until bc).joinToString("   ") { j ->
                                var sum = 0.0
                                for (k in 0 until br) sum += a[i][k] * b[k][j]
                                engine.formatDecimal(sum)
                            })
                        }
                    }
                }
                CalcMode.VECTOR -> {
                    val op = wiz.subType
                    val a = parseNumberList(vals[0])
                    val normA = sqrt(a.sumOf { it * it })
                    if (op == "|A|") {
                        lines.add("|A| = ${engine.formatDecimal(normA)}")
                    } else {
                        val b = parseNumberList(vals[1])
                        val normB = sqrt(b.sumOf { it * it })
                        val dot = a.indices.sumOf { a[it] * b[it] }

                        when (op) {
                            "A+B" -> lines.add(a.indices.joinToString("   ") { engine.formatDecimal(a[it] + b[it]) })
                            "A−B" -> lines.add(a.indices.joinToString("   ") { engine.formatDecimal(a[it] - b[it]) })
                            "A·B" -> lines.add("A·B = ${engine.formatDecimal(dot)}")
                            "Angle" -> {
                                val cosTheta = (dot / (normA * normB)).coerceIn(-1.0, 1.0)
                                val deg = acos(cosTheta) * 180.0 / Math.PI
                                lines.add("Angle = ${engine.formatDecimal(deg)}°")
                            }
                            "A×B" -> {
                                if (a.size == 3 && b.size == 3) {
                                    val cx = a[1] * b[2] - a[2] * b[1]
                                    val cy = a[2] * b[0] - a[0] * b[2]
                                    val cz = a[0] * b[1] - a[1] * b[0]
                                    lines.add("${engine.formatDecimal(cx)}   ${engine.formatDecimal(cy)}   ${engine.formatDecimal(cz)}")
                                } else lines.add("ERROR: 3D vector required")
                            }
                        }
                    }
                }
                CalcMode.TABLE -> {
                    val expr = vals[0]
                    val start = vals[1].toDouble()
                    val end = vals[2].toDouble()
                    val step = vals[3].toDouble()

                    if (step <= 0 || end < start) throw CalcException("Math ERROR")
                    val fn = engine.compile(expr)
                    lines.add("X\t\tf(X)")
                    var curr = start
                    var count = 0
                    while (curr <= end + 1e-9 && count < 50) {
                        val y = try {
                            engine.formatDecimal(fn(mapOf('X' to curr)))
                        } catch (_: Exception) {
                            "ERROR"
                        }
                        lines.add("${engine.formatDecimal(curr)}\t\t$y")
                        curr += step
                        count++
                    }
                }
                else -> {}
            }
        } catch (_: Exception) {
            lines.add("Math ERROR")
        }

        _uiState.update {
            it.copy(
                wizardState = wiz.copy(
                    stage = "result",
                    resultLines = lines,
                    scrollIndex = 0
                )
            )
        }
    }

    private fun updateBaseN(wiz: WizardState) {
        val s = wiz.buffer.trim()
        if (s.isEmpty()) {
            _uiState.update {
                it.copy(
                    wizardState = wiz.copy(
                        baseResult = "DEC 0\nHEX 0\nBIN 0\nOCT 0"
                    )
                )
            }
            return
        }

        val res = try {
            val v = s.toLong(wiz.baseRadix)
            "DEC $v\nHEX ${java.lang.Long.toHexString(v).uppercase()}\nBIN ${java.lang.Long.toBinaryString(v)}\nOCT ${java.lang.Long.toOctalString(v)}"
        } catch (_: Exception) {
            "Math ERROR"
        }

        _uiState.update {
            it.copy(
                wizardState = wiz.copy(baseResult = res)
            )
        }
    }

    fun switchBaseNRadix(radix: Int, label: String) {
        vibrate()
        val wiz = _uiState.value.wizardState ?: return
        val updated = wiz.copy(baseRadix = radix, baseLabel = label)
        updateBaseN(updated)
    }

    /* ================= DIALOG OPEN / CLOSE ================= */
    fun openModeDialog() = _uiState.update { it.copy(showModeDialog = true) }
    fun closeModeDialog() = _uiState.update { it.copy(showModeDialog = false) }

    fun openConstantsDialog() = _uiState.update { it.copy(showConstantsDialog = true) }
    fun closeConstantsDialog() = _uiState.update { it.copy(showConstantsDialog = false) }

    fun openConversionDialog() = _uiState.update { it.copy(showConversionDialog = true) }
    fun closeConversionDialog() = _uiState.update { it.copy(showConversionDialog = false) }

    fun openHistoryDialog() = _uiState.update { it.copy(showHistoryDialog = true) }
    fun closeHistoryDialog() = _uiState.update { it.copy(showHistoryDialog = false) }

    fun openHelpDialog() = _uiState.update { it.copy(showHelpDialog = true) }
    fun closeHelpDialog() = _uiState.update { it.copy(showHelpDialog = false) }

    fun insertConstant(c: CodataConstant) {
        vibrate()
        closeConstantsDialog()
        val valueStr = if (abs(c.value) < 1e-3 || abs(c.value) >= 1e4) {
            String.format(java.util.Locale.US, "%.5e", c.value).replace("e+", "×10^(").replace("e-", "×10^(-") + ")"
        } else {
            engine.formatDecimal(c.value)
        }
        insertText(valueStr)
    }

    fun insertConversionResult(result: String) {
        vibrate()
        closeConversionDialog()
        insertText(result)
    }
}
