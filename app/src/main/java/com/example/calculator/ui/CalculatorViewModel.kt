package com.example.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.engine.*
import com.example.calculator.model.CalcMode
import com.example.calculator.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {

    // --- Active Mode ---
    private val _currentMode = MutableStateFlow(CalcMode.SCIENTIFIC)
    val currentMode: StateFlow<CalcMode> = _currentMode.asStateFlow()

    fun setMode(mode: CalcMode) {
        _currentMode.value = mode
    }

    // --- Scientific Calculator State ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _livePreview = MutableStateFlow<String?>(null)
    val livePreview: StateFlow<String?> = _livePreview.asStateFlow()

    private val _evaluatedResult = MutableStateFlow<String?>(null)
    val evaluatedResult: StateFlow<String?> = _evaluatedResult.asStateFlow()

    private val _isDegMode = MutableStateFlow(true)
    val isDegMode: StateFlow<Boolean> = _isDegMode.asStateFlow()

    private val _isSecondMode = MutableStateFlow(false)
    val isSecondMode: StateFlow<Boolean> = _isSecondMode.asStateFlow()

    private val _isHypMode = MutableStateFlow(false)
    val isHypMode: StateFlow<Boolean> = _isHypMode.asStateFlow()

    private val _memoryValue = MutableStateFlow<Double?>(null)
    val memoryValue: StateFlow<Double?> = _memoryValue.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun toggleDegRad() {
        _isDegMode.value = !_isDegMode.value
        updateLivePreview()
    }

    fun toggleSecondMode() {
        _isSecondMode.value = !_isSecondMode.value
    }

    fun toggleHypMode() {
        _isHypMode.value = !_isHypMode.value
    }

    fun appendInput(token: String) {
        _errorMessage.value = null
        val current = _expression.value

        // If we just evaluated a result and user starts with an operator (+, -, ×, ÷, ^), continue from result
        if (_evaluatedResult.value != null) {
            val lastRes = _evaluatedResult.value!!
            if (token in listOf("+", "−", "×", "÷", "^", "%")) {
                _expression.value = lastRes + token
                _evaluatedResult.value = null
                updateLivePreview()
                return
            } else if (token !in listOf("(", ")")) {
                // If user types a number or function, start fresh
                _expression.value = ""
                _evaluatedResult.value = null
            } else {
                _evaluatedResult.value = null
            }
        }

        _expression.value += token
        updateLivePreview()
    }

    fun appendFunction(funcName: String) {
        _errorMessage.value = null
        val current = _expression.value
        if (_evaluatedResult.value != null) {
            _expression.value = ""
            _evaluatedResult.value = null
        }
        _expression.value += "$funcName("
        updateLivePreview()
    }

    fun backspace() {
        _errorMessage.value = null
        val cur = _expression.value
        if (cur.isEmpty()) return

        // If string ends with function prefix like "sin(", "asin(", "sqrt(", delete the whole token
        val funcTokens = listOf("asin(", "acos(", "atan(", "asinh(", "acosh(", "atanh(", "sinh(", "cosh(", "tanh(", "sin(", "cos(", "tan(", "log10(", "log2(", "sqrt(", "cbrt(", "abs(", "ln(", "log(", "exp(", "root(")
        for (ft in funcTokens) {
            if (cur.endsWith(ft)) {
                _expression.value = cur.removeSuffix(ft)
                updateLivePreview()
                return
            }
        }

        _expression.value = cur.dropLast(1)
        updateLivePreview()
    }

    fun clearAll() {
        _expression.value = ""
        _livePreview.value = null
        _evaluatedResult.value = null
        _errorMessage.value = null
    }

    fun calculate() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        val evaluator = MathEvaluator(isDegMode = _isDegMode.value)
        when (val res = evaluator.evaluate(expr)) {
            is MathEvaluator.EvalResult.Success -> {
                val formatted = res.formatted
                _evaluatedResult.value = formatted
                _livePreview.value = null
                _errorMessage.value = null

                // Add to history
                val item = HistoryItem(
                    expression = expr,
                    result = formatted
                )
                _history.value = listOf(item) + _history.value.take(49)
            }
            is MathEvaluator.EvalResult.Error -> {
                _errorMessage.value = res.message
            }
        }
    }

    private fun updateLivePreview() {
        val expr = _expression.value.trim()
        if (expr.isEmpty() || expr.length < 2) {
            _livePreview.value = null
            return
        }

        // Auto-close open parentheses for live preview
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        val closedExpr = if (openCount > closeCount) {
            expr + ")".repeat(openCount - closeCount)
        } else {
            expr
        }

        val evaluator = MathEvaluator(isDegMode = _isDegMode.value)
        when (val res = evaluator.evaluate(closedExpr)) {
            is MathEvaluator.EvalResult.Success -> {
                // Only show if different from literal input
                if (res.formatted != expr) {
                    _livePreview.value = res.formatted
                } else {
                    _livePreview.value = null
                }
            }
            is MathEvaluator.EvalResult.Error -> {
                _livePreview.value = null
            }
        }
    }

    // Memory operations
    fun memoryClear() {
        _memoryValue.value = null
    }

    fun memoryRecall() {
        val mem = _memoryValue.value ?: return
        val formatted = MathEvaluator.formatResult(mem)
        appendInput(formatted)
    }

    fun memoryStore() {
        val currentVal = getCurrentNumericValue() ?: return
        _memoryValue.value = currentVal
    }

    fun memoryAdd() {
        val currentVal = getCurrentNumericValue() ?: return
        val prev = _memoryValue.value ?: 0.0
        _memoryValue.value = prev + currentVal
    }

    fun memorySubtract() {
        val currentVal = getCurrentNumericValue() ?: return
        val prev = _memoryValue.value ?: 0.0
        _memoryValue.value = prev - currentVal
    }

    private fun getCurrentNumericValue(): Double? {
        val eval = _evaluatedResult.value?.toDoubleOrNull()
        if (eval != null) return eval
        val preview = _livePreview.value?.toDoubleOrNull()
        if (preview != null) return preview
        val current = _expression.value.toDoubleOrNull()
        if (current != null) return current
        val evaluator = MathEvaluator(isDegMode = _isDegMode.value)
        val res = evaluator.evaluate(_expression.value)
        return if (res is MathEvaluator.EvalResult.Success) res.value else null
    }

    fun recallHistory(item: HistoryItem) {
        _expression.value = item.expression
        _evaluatedResult.value = item.result
        _livePreview.value = null
        _errorMessage.value = null
    }

    fun insertHistoryResult(result: String) {
        appendInput(result)
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    // Insert constant into expression and switch to Scientific mode
    fun insertConstant(constant: ConstantItem) {
        _currentMode.value = CalcMode.SCIENTIFIC
        appendInput(constant.symbol)
    }

    // --- Programmer Calculator State ---
    private val _activeRadix = MutableStateFlow(NumberBase.DEC)
    val activeRadix: StateFlow<NumberBase> = _activeRadix.asStateFlow()

    private val _wordSize = MutableStateFlow(WordSize.QWORD)
    val wordSize: StateFlow<WordSize> = _wordSize.asStateFlow()

    private val _programmerValue = MutableStateFlow(0L)
    val programmerValue: StateFlow<Long> = _programmerValue.asStateFlow()

    private val _programmerPendingOp = MutableStateFlow<String?>(null)
    val programmerPendingOp: StateFlow<String?> = _programmerPendingOp.asStateFlow()

    private val _programmerStoredValue = MutableStateFlow<Long?>(null)
    val programmerStoredValue: StateFlow<Long?> = _programmerStoredValue.asStateFlow()

    fun setRadix(base: NumberBase) {
        _activeRadix.value = base
    }

    fun setWordSize(ws: WordSize) {
        _wordSize.value = ws
        _programmerValue.value = _programmerValue.value and ws.mask
        _programmerStoredValue.value = _programmerStoredValue.value?.let { it and ws.mask }
    }

    fun programmerInputDigit(digitChar: Char) {
        val radix = _activeRadix.value.radix
        val digitVal = digitChar.digitToIntOrNull(radix) ?: return
        val current = _programmerValue.value
        val masked = current and _wordSize.value.mask
        val next = (masked * radix + digitVal) and _wordSize.value.mask
        _programmerValue.value = next
    }

    fun programmerBackspace() {
        val radix = _activeRadix.value.radix
        val current = _programmerValue.value and _wordSize.value.mask
        _programmerValue.value = (current / radix) and _wordSize.value.mask
    }

    fun programmerClear() {
        _programmerValue.value = 0L
        _programmerPendingOp.value = null
        _programmerStoredValue.value = null
    }

    fun programmerBitwiseOp(op: String) {
        val current = _programmerValue.value and _wordSize.value.mask
        when (op) {
            "NOT" -> {
                _programmerValue.value = current.inv() and _wordSize.value.mask
            }
            "LSH" -> {
                _programmerValue.value = (current shl 1) and _wordSize.value.mask
            }
            "RSH" -> {
                _programmerValue.value = (current ushr 1) and _wordSize.value.mask
            }
            else -> {
                // Binary ops: AND, OR, XOR, MOD, +, -, *, /
                _programmerStoredValue.value = current
                _programmerPendingOp.value = op
                _programmerValue.value = 0L
            }
        }
    }

    fun programmerCalculate() {
        val op = _programmerPendingOp.value ?: return
        val stored = _programmerStoredValue.value ?: return
        val current = _programmerValue.value and _wordSize.value.mask
        val mask = _wordSize.value.mask

        val result = when (op) {
            "AND" -> (stored and current) and mask
            "OR" -> (stored or current) and mask
            "XOR" -> (stored xor current) and mask
            "NAND" -> (stored and current).inv() and mask
            "NOR" -> (stored or current).inv() and mask
            "MOD" -> if (current != 0L) (stored % current) and mask else stored
            "+" -> (stored + current) and mask
            "-" -> (stored - current) and mask
            "*" -> (stored * current) and mask
            "/" -> if (current != 0L) (stored / current) and mask else stored
            else -> current
        }

        _programmerValue.value = result
        _programmerPendingOp.value = null
        _programmerStoredValue.value = null
    }

    // --- Unit Converter State ---
    private val _converterCategory = MutableStateFlow(UnitCategory.LENGTH)
    val converterCategory: StateFlow<UnitCategory> = _converterCategory.asStateFlow()

    private val _fromUnit = MutableStateFlow(UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH)[0])
    val fromUnit: StateFlow<UnitDefinition> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH)[1])
    val toUnit: StateFlow<UnitDefinition> = _toUnit.asStateFlow()

    private val _converterInput = MutableStateFlow("1")
    val converterInput: StateFlow<String> = _converterInput.asStateFlow()

    private val _converterResult = MutableStateFlow("")
    val converterResult: StateFlow<String> = _converterResult.asStateFlow()

    init {
        updateConverter()
    }

    fun setConverterCategory(cat: UnitCategory) {
        _converterCategory.value = cat
        val units = UnitConverterEngine.getUnitsForCategory(cat)
        _fromUnit.value = units[0]
        _toUnit.value = if (units.size > 1) units[1] else units[0]
        updateConverter()
    }

    fun setFromUnit(unit: UnitDefinition) {
        _fromUnit.value = unit
        updateConverter()
    }

    fun setToUnit(unit: UnitDefinition) {
        _toUnit.value = unit
        updateConverter()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        updateConverter()
    }

    fun setConverterInput(input: String) {
        _converterInput.value = input
        updateConverter()
    }

    private fun updateConverter() {
        val num = _converterInput.value.toDoubleOrNull()
        if (num == null) {
            _converterResult.value = ""
            return
        }
        val converted = UnitConverterEngine.convert(num, _fromUnit.value, _toUnit.value)
        _converterResult.value = MathEvaluator.formatResult(converted)
    }
}
