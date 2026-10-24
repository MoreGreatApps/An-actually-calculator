package com.example.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * Robust mathematical expression parser and evaluator for scientific calculations.
 * Supports:
 * - Basic arithmetic (+, -, ×, ÷, %, mod)
 * - Power (^) and roots (√, ∛, nthRoot)
 * - Trigonometric and inverse trig (sin, cos, tan, asin, acos, atan) in DEG or RAD mode
 * - Hyperbolic and inverse hyperbolic (sinh, cosh, tanh, asinh, acosh, atanh)
 * - Logarithmic functions (ln, log, log2)
 * - Exponentials (e^x, 10^x, 2^x)
 * - Combinatorics: Factorial (!), nPr, nCr
 * - Constants: π, e, φ
 * - Implicit multiplication: e.g. 2π, 3(4+5), 5sin(30)
 * - Absolute value, round, floor, ceil, gcd, lcm
 */
class MathEvaluator(private val isDegMode: Boolean = true) {

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    fun evaluate(expression: String): EvalResult {
        val sanitized = sanitize(expression)
        if (sanitized.isEmpty()) {
            return EvalResult.Error("Empty expression")
        }

        return try {
            val parser = Parser(sanitized, isDegMode)
            val result = parser.parse()
            if (result.isNaN()) {
                EvalResult.Error("Undefined")
            } else if (result.isInfinite()) {
                EvalResult.Error(if (result > 0) "Infinity" else "-Infinity")
            } else {
                EvalResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: Exception) {
            EvalResult.Error(e.message ?: "Syntax error")
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
            .replace("φ", "PHI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace(" ", "")
    }

    companion object {
        val PI_VAL = Math.PI
        val E_VAL = Math.E
        val PHI_VAL = 1.61803398874989484820

        fun formatResult(value: Double): String {
            if (value.isNaN()) return "Undefined"
            if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

            // Fix precision artifacts for common values, e.g., 0.30000000000000004 -> 0.3
            val rounded = try {
                val bd = BigDecimal(value, MathContext(12, RoundingMode.HALF_UP))
                // If value is close to an integer within 1e-11
                val nearestInt = round(value)
                if (abs(value - nearestInt) < 1e-11) {
                    nearestInt.toLong().toString()
                } else {
                    val stripped = bd.stripTrailingZeros()
                    val plain = stripped.toPlainString()
                    if (plain.length > 14 || abs(value) >= 1e12 || (abs(value) < 1e-6 && value != 0.0)) {
                        String.format("%.8E", value).replace("E+0", "E+").replace("E-0", "E-")
                    } else {
                        plain
                    }
                }
            } catch (e: Exception) {
                value.toString()
            }
            return rounded
        }

        fun factorial(n: Double): Double {
            if (n < 0) throw ArithmeticException("Domain error: factorial of negative")
            if (n > 170) throw ArithmeticException("Overflow")
            val intN = n.toLong()
            if (abs(n - intN) < 1e-9) {
                if (intN == 0L || intN == 1L) return 1.0
                var res = 1.0
                for (i in 2..intN) {
                    res *= i
                }
                return res
            } else {
                // Gamma function for non-integers: n! = Gamma(n + 1)
                return gammaLanczos(n + 1.0)
            }
        }

        private fun gammaLanczos(z: Double): Double {
            val p = doubleArrayOf(
                676.5203681218851, -1259.1392167224028,
                771.32342877765313, -176.61502916214059,
                12.507343278686905, -0.13857109585620516,
                9.9843695780195716e-6, 1.5056327351493116e-7
            )
            if (z < 0.5) {
                return PI / (sin(PI * z) * gammaLanczos(1.0 - z))
            }
            var t = z - 1.0
            var x = 0.99999999999980993
            for (i in p.indices) {
                x += p[i] / (t + i + 1)
            }
            t += 7.5
            return sqrt(2 * PI) * t.pow(t + 0.5) * exp(-t) * x
        }

        fun nPr(n: Double, r: Double): Double {
            if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid nPr inputs")
            return factorial(n) / factorial(n - r)
        }

        fun nCr(n: Double, r: Double): Double {
            if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid nCr inputs")
            return factorial(n) / (factorial(r) * factorial(n - r))
        }

        fun gcd(a: Long, b: Long): Long {
            var x = abs(a)
            var y = abs(b)
            while (y != 0L) {
                val t = y
                y = x % y
                x = t
            }
            return x
        }

        fun lcm(a: Long, b: Long): Long {
            if (a == 0L || b == 0L) return 0L
            return abs(a * b) / gcd(a, b)
        }
    }

    private class Parser(private val text: String, private val isDeg: Boolean) {
        private var pos = 0
        private var ch: Char = if (text.isNotEmpty()) text[0] else '\u0000'

        private fun nextChar() {
            pos++
            ch = if (pos < text.length) text[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            val x = parseExpression()
            if (pos < text.length) throw ArithmeticException("Unexpected character: '$ch'")
            return x
        }

        // Grammar:
        // Expression = Term { ('+' | '-') Term }
        // Term = FactorWithPercent { ('*' | '/' | '%') FactorWithPercent }
        // FactorWithPercent = Power { '%' }
        // Power = Factor [ '^' Power ]
        // Factor = ('+' | '-') Factor | Primary { '!' }
        // Primary = Number | Constant | Function '(' Expression [ ',' Expression ] ')' | '(' Expression ')'

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Cannot divide by 0")
                        x /= divisor
                    }
                    eat('%') -> {
                        val mod = parseFactor()
                        if (mod == 0.0) throw ArithmeticException("Modulo by 0")
                        x %= mod
                    }
                    // Implicit multiplication if next token could be start of a primary
                    isStartOfPrimary() -> {
                        x *= parseFactor()
                    }
                    else -> return x
                }
            }
        }

        private fun isStartOfPrimary(): Boolean {
            return ch in '0'..'9' || ch == '.' || ch == '(' || ch in 'a'..'z' || ch in 'A'..'Z'
        }

        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x = parsePower()
            while (eat('!')) {
                x = factorial(x)
            }
            return x
        }

        private fun parsePower(): Double {
            var x = parsePrimary()
            if (eat('^')) {
                val p = parseFactor() // right-associative power
                if (x < 0 && p != floor(p)) throw ArithmeticException("Complex result")
                x = x.pow(p)
            }
            return x
        }

        private fun parsePrimary(): Double {
            val startPos = pos
            if (eat('(')) {
                val x = parseExpression()
                if (!eat(')')) throw ArithmeticException("Missing closing parenthesis")
                return x
            }

            // Number
            if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                // Handle scientific notation in literals, e.g. 1.2e5 or 3E-4
                if (ch == 'e' || ch == 'E') {
                    val nextIdx = pos + 1
                    if (nextIdx < text.length && (text[nextIdx] in '0'..'9' || text[nextIdx] == '+' || text[nextIdx] == '-')) {
                        nextChar() // consume E
                        if (ch == '+' || ch == '-') nextChar()
                        while (ch in '0'..'9') nextChar()
                    }
                }
                val numStr = text.substring(startPos, pos)
                return numStr.toDoubleOrNull() ?: throw ArithmeticException("Invalid number: $numStr")
            }

            // Identifiers: Constants or Functions
            if (ch in 'a'..'z' || ch in 'A'..'Z') {
                while (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9') nextChar()
                val id = text.substring(startPos, pos).lowercase()

                // Check constants first
                when (id) {
                    "pi" -> return PI_VAL
                    "e" -> return E_VAL
                    "phi" -> return PHI_VAL
                }

                // Functions with arguments
                if (eat('(')) {
                    val arg1 = parseExpression()
                    var arg2: Double? = null
                    if (eat(',')) {
                        arg2 = parseExpression()
                    }
                    if (!eat(')')) throw ArithmeticException("Missing closing parenthesis for $id")

                    return evaluateFunction(id, arg1, arg2)
                } else {
                    throw ArithmeticException("Expected '(' after function $id")
                }
            }

            throw ArithmeticException("Unexpected token at position $pos")
        }

        private fun evaluateFunction(func: String, a: Double, b: Double?): Double {
            return when (func) {
                // Trigonometry
                "sin" -> {
                    val rad = if (isDeg) Math.toRadians(a) else a
                    // Handle exact multiples like sin(180 deg) = 0
                    if (isDeg && (a % 180.0 == 0.0)) 0.0 else sin(rad)
                }
                "cos" -> {
                    val rad = if (isDeg) Math.toRadians(a) else a
                    if (isDeg && ((a - 90.0) % 180.0 == 0.0)) 0.0 else cos(rad)
                }
                "tan" -> {
                    if (isDeg && ((a - 90.0) % 180.0 == 0.0)) throw ArithmeticException("Undefined tan")
                    val rad = if (isDeg) Math.toRadians(a) else a
                    tan(rad)
                }
                // Inverse Trig
                "asin", "arcsin" -> {
                    if (a < -1.0 || a > 1.0) throw ArithmeticException("Domain error: asin out of [-1, 1]")
                    val rad = asin(a)
                    if (isDeg) Math.toDegrees(rad) else rad
                }
                "acos", "arccos" -> {
                    if (a < -1.0 || a > 1.0) throw ArithmeticException("Domain error: acos out of [-1, 1]")
                    val rad = acos(a)
                    if (isDeg) Math.toDegrees(rad) else rad
                }
                "atan", "arctan" -> {
                    val rad = atan(a)
                    if (isDeg) Math.toDegrees(rad) else rad
                }
                // Hyperbolic
                "sinh" -> sinh(a)
                "cosh" -> cosh(a)
                "tanh" -> tanh(a)
                "asinh" -> ln(a + sqrt(a * a + 1.0))
                "acosh" -> {
                    if (a < 1.0) throw ArithmeticException("Domain error: acosh for x < 1")
                    ln(a + sqrt(a * a - 1.0))
                }
                "atanh" -> {
                    if (a <= -1.0 || a >= 1.0) throw ArithmeticException("Domain error: atanh out of (-1, 1)")
                    0.5 * ln((1.0 + a) / (1.0 - a))
                }
                // Logarithms
                "ln" -> {
                    if (a <= 0.0) throw ArithmeticException("Domain error: ln for x <= 0")
                    ln(a)
                }
                "log", "log10" -> {
                    if (a <= 0.0) throw ArithmeticException("Domain error: log for x <= 0")
                    log10(a)
                }
                "log2" -> {
                    if (a <= 0.0) throw ArithmeticException("Domain error: log2 for x <= 0")
                    ln(a) / ln(2.0)
                }
                // Roots and Powers
                "sqrt" -> {
                    if (a < 0.0) throw ArithmeticException("Domain error: sqrt of negative")
                    sqrt(a)
                }
                "cbrt" -> {
                    if (a < 0.0) -(-a).pow(1.0 / 3.0) else a.pow(1.0 / 3.0)
                }
                "root", "nthroot" -> {
                    val rootDegree = b ?: 2.0
                    if (rootDegree == 0.0) throw ArithmeticException("Zero root")
                    if (a < 0.0 && rootDegree.toLong() % 2L == 0L) throw ArithmeticException("Even root of negative")
                    if (a < 0.0) -(-a).pow(1.0 / rootDegree) else a.pow(1.0 / rootDegree)
                }
                "exp" -> exp(a)
                "abs" -> abs(a)
                "floor" -> floor(a)
                "ceil" -> ceil(a)
                "round" -> round(a)
                "rand", "random" -> Math.random() * a
                // Combinatorics
                "npr" -> {
                    val r = b ?: throw ArithmeticException("nPr requires 2 arguments: nPr(n, r)")
                    nPr(a, r)
                }
                "ncr" -> {
                    val r = b ?: throw ArithmeticException("nCr requires 2 arguments: nCr(n, r)")
                    nCr(a, r)
                }
                "gcd" -> {
                    val r = b ?: throw ArithmeticException("gcd requires 2 arguments: gcd(a, b)")
                    gcd(a.toLong(), r.toLong()).toDouble()
                }
                "lcm" -> {
                    val r = b ?: throw ArithmeticException("lcm requires 2 arguments: lcm(a, b)")
                    lcm(a.toLong(), r.toLong()).toDouble()
                }
                else -> throw ArithmeticException("Unknown function '$func'")
            }
        }
    }
}
