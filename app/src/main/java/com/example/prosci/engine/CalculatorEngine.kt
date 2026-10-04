package com.example.prosci.engine

import java.util.Locale
import kotlin.math.*

class CalcException(val msg: String, val position: Int = 0) : Exception(msg)

enum class AngleUnit {
    DEG, RAD, GRA
}

enum class NumberFormatType {
    NORM, FIX, SCI
}

data class NumberFormatSetting(val type: NumberFormatType, val digits: Int) {
    override fun toString(): String = when (type) {
        NumberFormatType.NORM -> "NORM"
        NumberFormatType.FIX -> "FIX $digits"
        NumberFormatType.SCI -> "SCI $digits"
    }
}

data class Fraction(val num: Long, val den: Long) {
    fun toDisplayString(): String = if (den == 1L) "$num" else "$num/$den"
}

data class ExactResult(
    val decimalStr: String,
    val fraction: Fraction? = null,
    val exactSymbolic: String? = null
)

typealias Scope = Map<Char, Double>
typealias ExprFn = (Scope) -> Double

class CalculatorEngine(
    var angleUnit: AngleUnit = AngleUnit.DEG,
    var formatSetting: NumberFormatSetting = NumberFormatSetting(NumberFormatType.NORM, 0)
) {
    var ans: Double = 0.0
    val variables = mutableMapOf<Char, Double>(
        'A' to 0.0, 'B' to 0.0, 'C' to 0.0, 'D' to 0.0,
        'E' to 0.0, 'F' to 0.0, 'X' to 0.0, 'Y' to 0.0, 'M' to 0.0
    )

    sealed class Token(val pos: Int) {
        class Num(val value: Double, val raw: String, pos: Int) : Token(pos)
        class Func(val name: String, pos: Int) : Token(pos)
        class Var(val name: Char, pos: Int) : Token(pos)
        class Const(val name: String, val value: Double, pos: Int) : Token(pos)
        class Op(val op: Char, pos: Int) : Token(pos)
    }

    private fun clean(y: Double): Double = if (abs(y) < 1e-14) 0.0 else y

    private fun factorial(x: Double): Double {
        if (x < 0 || x % 1.0 != 0.0 || x > 69.0) throw CalcException("Math ERROR")
        var f = 1.0
        val n = x.toInt()
        for (i in 2..n) f *= i
        return f
    }

    private fun perm(n: Double, r: Double, isComb: Boolean): Double {
        if (n < 0 || r < 0 || r > n || n % 1.0 != 0.0 || r % 1.0 != 0.0) throw CalcException("Math ERROR")
        var p = 1.0
        val ni = n.toInt()
        val ri = r.toInt()
        for (i in 0 until ri) p *= (ni - i)
        if (isComb) {
            for (i in 2..ri) p /= i
        }
        return round(p)
    }

    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val s = input

        while (i < s.length) {
            val c = s[i]
            if (c.isWhitespace()) {
                i++
                continue
            }

            val p = i
            // Number parsing
            if (c.isDigit() || (c == '.' && i + 1 < s.length && s[i + 1].isDigit())) {
                var j = i
                var hasDot = false
                while (j < s.length && (s[j].isDigit() || (!hasDot && s[j] == '.'))) {
                    if (s[j] == '.') hasDot = true
                    j++
                }
                val raw = s.substring(i, j)
                val value = raw.toDoubleOrNull() ?: throw CalcException("Syntax ERROR", p)
                tokens.add(Token.Num(value, raw, p))
                i = j
                continue
            }

            // Operators
            if (c in "+-×÷*/^!%(),") {
                tokens.add(Token.Op(c, p))
                i++
                continue
            }

            // Constants
            if (c == 'π') {
                tokens.add(Token.Const("π", PI, p))
                i++
                continue
            }
            if (c == 'e' && (i + 1 == s.length || (!s[i + 1].isLetterOrDigit() && s[i + 1] != '('))) {
                tokens.add(Token.Const("e", E, p))
                i++
                continue
            }

            // Multi-char tokens / function names
            val remaining = s.substring(i)
            val funcList = listOf(
                "d/dx", "asinh", "acosh", "atanh", "sinh", "cosh", "tanh",
                "asin", "acos", "atan", "sin", "cos", "tan", "log", "ln",
                "sqrt", "cbrt", "abs", "root", "nPr", "nCr", "Ans", "Ran#",
                "∫", "Σ"
            )
            var matchedFunc: String? = null
            for (fn in funcList) {
                if (remaining.startsWith(fn)) {
                    matchedFunc = fn
                    break
                }
            }

            if (matchedFunc != null) {
                if (matchedFunc == "Ans") {
                    tokens.add(Token.Const("Ans", ans, p))
                } else if (matchedFunc == "Ran#") {
                    val rnd = round(Math.random() * 1000.0) / 1000.0
                    tokens.add(Token.Const("Ran#", rnd, p))
                } else {
                    tokens.add(Token.Func(matchedFunc, p))
                }
                i += matchedFunc.length
                continue
            }

            // Single letter variables
            if (c in "ABCDEFXYM") {
                tokens.add(Token.Var(c, p))
                i++
                continue
            }

            throw CalcException("Syntax ERROR", p)
        }
        return tokens
    }

    private fun asinh(x: Double) = ln(x + sqrt(x * x + 1.0))
    private fun acosh(x: Double) = ln(x + sqrt(x * x - 1.0))
    private fun atanh(x: Double) = 0.5 * ln((1.0 + x) / (1.0 - x))

    private fun callFunc(name: String, args: List<ExprFn>, scope: Scope): Double {
        val x = args[0](scope)
        val r = if (args.size > 1) args[1](scope) else 0.0
        val k = when (angleUnit) {
            AngleUnit.DEG -> PI / 180.0
            AngleUnit.GRA -> PI / 200.0
            AngleUnit.RAD -> 1.0
        }

        return when (name) {
            "sin" -> clean(sin(x * k))
            "cos" -> clean(cos(x * k))
            "tan" -> {
                if (abs(cos(x * k)) < 1e-12) throw CalcException("Math ERROR")
                clean(tan(x * k))
            }
            "asin" -> {
                if (abs(x) > 1.0) throw CalcException("Math ERROR")
                asin(x) / k
            }
            "acos" -> {
                if (abs(x) > 1.0) throw CalcException("Math ERROR")
                acos(x) / k
            }
            "atan" -> atan(x) / k
            "sinh" -> sinh(x)
            "cosh" -> cosh(x)
            "tanh" -> tanh(x)
            "asinh" -> asinh(x)
            "acosh" -> {
                if (x < 1.0) throw CalcException("Math ERROR")
                acosh(x)
            }
            "atanh" -> {
                if (abs(x) >= 1.0) throw CalcException("Math ERROR")
                atanh(x)
            }
            "log" -> {
                if (x <= 0) throw CalcException("Math ERROR")
                log10(x)
            }
            "ln" -> {
                if (x <= 0) throw CalcException("Math ERROR")
                ln(x)
            }
            "sqrt" -> {
                if (x < 0) throw CalcException("Math ERROR")
                sqrt(x)
            }
            "cbrt" -> cbrt(x)
            "abs" -> abs(x)
            "root" -> {
                if (x == 0.0 || (r < 0 && x.toInt() % 2 == 0)) throw CalcException("Math ERROR")
                if (r < 0) -(-r).pow(1.0 / x) else r.pow(1.0 / x)
            }
            "∫" -> {
                if (args.size < 3) throw CalcException("Syntax ERROR")
                val l = args[1](scope)
                val u = args[2](scope)
                val n = 2000
                val h = (u - l) / n
                val f: (Double) -> Double = { t -> args[0](scope + ('X' to t)) }
                var s = f(l) + f(u)
                for (j in 1 until n) {
                    s += f(l + j * h) * (if (j % 2 != 0) 4.0 else 2.0)
                }
                s * h / 3.0
            }
            "d/dx" -> {
                if (args.size < 2) throw CalcException("Syntax ERROR")
                val pt = args[1](scope)
                val h = 1e-3
                val f: (Double) -> Double = { t -> args[0](scope + ('X' to t)) }
                (-f(pt + 2 * h) + 8 * f(pt + h) - 8 * f(pt - h) + f(pt - 2 * h)) / (12 * h)
            }
            "Σ" -> {
                if (args.size < 3) throw CalcException("Syntax ERROR")
                val start = args[1](scope).roundToInt()
                val end = args[2](scope).roundToInt()
                var sum = 0.0
                for (kVal in start..end) {
                    sum += args[0](scope + ('X' to kVal.toDouble()))
                }
                sum
            }
            else -> throw CalcException("Math ERROR")
        }
    }

    private inner class Parser(val tokens: List<Token>, val inputLen: Int) {
        var idx = 0

        fun peek(): Token? = if (idx < tokens.size) tokens[idx] else null
        fun eofPos(): Int = peek()?.pos ?: inputLen
        fun isOp(c: Char): Boolean = peek() is Token.Op && (peek() as Token.Op).op == c

        fun isStartOfExpression(t: Token?): Boolean {
            if (t == null) return false
            if (t is Token.Num || t is Token.Var || t is Token.Const) return true
            if (t is Token.Func && t.name != "nPr" && t.name != "nCr") return true
            if (t is Token.Op && t.op == '(') return true
            return false
        }

        fun parseExpr(): ExprFn {
            var left: ExprFn = parseTerm()
            while (isOp('+') || isOp('-')) {
                val op = (tokens[idx++] as Token.Op).op
                val right: ExprFn = parseTerm()
                val prev = left
                left = if (op == '+') {
                    { v: Scope -> prev(v) + right(v) }
                } else {
                    { v: Scope -> prev(v) - right(v) }
                }
            }
            return left
        }

        fun parseTerm(): ExprFn {
            var left: ExprFn = parsePerm()
            while (isOp('×') || isOp('÷') || isOp('*') || isOp('/')) {
                val op = (tokens[idx++] as Token.Op).op
                val right: ExprFn = parsePerm()
                val prev = left
                left = if (op == '×' || op == '*') {
                    { v: Scope -> prev(v) * right(v) }
                } else {
                    { v: Scope ->
                        val d = right(v)
                        if (d == 0.0) throw CalcException("Math ERROR")
                        prev(v) / d
                    }
                }
            }
            return left
        }

        fun parsePerm(): ExprFn {
            var left: ExprFn = parseUnary()
            while (peek() is Token.Func && ((peek() as Token.Func).name == "nPr" || (peek() as Token.Func).name == "nCr")) {
                val fnName = (tokens[idx++] as Token.Func).name
                val right: ExprFn = parseUnary()
                val prev = left
                left = { v: Scope -> perm(prev(v), right(v), fnName == "nCr") }
            }
            return left
        }

        fun parseUnary(): ExprFn {
            if (isOp('-')) {
                idx++
                val next = parseUnary()
                return { v: Scope -> -next(v) }
            }
            return parseImplicitMult()
        }

        fun parseImplicitMult(): ExprFn {
            var left: ExprFn = parsePower()
            while (isStartOfExpression(peek())) {
                val right: ExprFn = parsePower()
                val prev = left
                left = { v: Scope -> prev(v) * right(v) }
            }
            return left
        }

        fun parsePowerUnary(): ExprFn {
            if (isOp('-')) {
                idx++
                val next = parsePowerUnary()
                return { v: Scope -> -next(v) }
            }
            return parsePower()
        }

        fun parsePower(): ExprFn {
            val base: ExprFn = parsePostfix()
            if (isOp('^')) {
                idx++
                val exp = parsePowerUnary()
                return { v: Scope ->
                    val bVal = base(v)
                    val eVal = exp(v)
                    val res = bVal.pow(eVal)
                    if (res.isNaN() || res.isInfinite()) throw CalcException("Math ERROR")
                    res
                }
            }
            return base
        }

        fun parsePostfix(): ExprFn {
            var q: ExprFn = parsePrimary()
            while (true) {
                val prev = q
                if (isOp('!')) {
                    idx++
                    q = { v: Scope -> factorial(prev(v)) }
                } else if (isOp('%')) {
                    idx++
                    q = { v: Scope -> prev(v) / 100.0 }
                } else {
                    return q
                }
            }
        }

        fun parsePrimary(): ExprFn {
            val t = peek() ?: throw CalcException("Syntax ERROR", eofPos())
            idx++

            when (t) {
                is Token.Num -> {
                    val n = t.value
                    return { n }
                }
                is Token.Var -> {
                    val varName = t.name
                    return { scope: Scope ->
                        scope[varName] ?: variables[varName] ?: 0.0
                    }
                }
                is Token.Const -> {
                    val c = t.value
                    return { c }
                }
                is Token.Op -> {
                    if (t.op == '(') {
                        val inner = parseExpr()
                        if (isOp(')')) {
                            idx++
                        } else if (peek() != null) {
                            throw CalcException("Syntax ERROR", eofPos())
                        }
                        return inner
                    }
                    throw CalcException("Syntax ERROR", t.pos)
                }
                is Token.Func -> {
                    if (!isOp('(')) throw CalcException("Syntax ERROR", eofPos())
                    idx++
                    val args = mutableListOf(parseExpr())
                    while (isOp(',')) {
                        idx++
                        args.add(parseExpr())
                    }
                    if (isOp(')')) {
                        idx++
                    } else if (peek() != null) {
                        throw CalcException("Syntax ERROR", eofPos())
                    }
                    val fnName = t.name
                    return { scope: Scope -> callFunc(fnName, args, scope) }
                }
            }
        }
    }

    fun compile(input: String): ExprFn {
        val tokens = tokenize(input)
        val parser = Parser(tokens, input.length)
        val compiled = parser.parseExpr()
        if (parser.idx < tokens.size) {
            throw CalcException("Syntax ERROR", tokens[parser.idx].pos)
        }
        return compiled
    }

    fun evaluate(expr: String): Double {
        val fn = compile(expr)
        val res = fn(emptyMap())
        if (res.isNaN() || res.isInfinite() || abs(res) > 9.999999999e99) {
            throw CalcException("Math ERROR")
        }
        return (res * 1e14).roundToLong() / 1e14
    }

    fun toFraction(x: Double, maxDenominator: Long = 9999L): Fraction? {
        if (x % 1.0 == 0.0) return null
        val sign = if (x < 0) -1L else 1L
        val a = abs(x)
        if (a > 1e10 || a < 1e-9) return null

        var h1 = 1L
        var h0 = 0L
        var k1 = 0L
        var k0 = 1L
        var b = a

        for (n in 0 until 30) {
            val f = floor(b).toLong()
            val h = f * h1 + h0
            val k = f * k1 + k0
            h0 = h1
            h1 = h
            k0 = k1
            k1 = k

            if (k1 > maxDenominator) return null
            if (abs(a - h1.toDouble() / k1.toDouble()) <= 1e-11 * a) {
                return Fraction(sign * h1, k1)
            }
            val r = b - f
            if (r < 1e-12) break
            b = 1.0 / r
        }
        return null
    }

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

    fun toExact(x: Double): String? {
        val a = abs(x)
        val s = if (x < 0) "−" else ""
        if (a == 0.0 || a > 1e6 || a % 1.0 == 0.0) return null

        val sq = a * a
        val f = if (sq % 1.0 == 0.0) Fraction(round(sq).toLong(), 1L) else toFraction(sq, 200L)
        if (f != null && f.num <= 100000L) {
            var n = f.num * f.den
            var o = 1L
            var i = 2L
            while (i * i <= n) {
                while (n % (i * i) == 0L) {
                    n /= (i * i)
                    o *= i
                }
                i++
            }
            val g = gcd(o, f.den)
            val q = f.den / g
            o /= g
            if (n > 1 && abs(a - o.toDouble() * sqrt(n.toDouble()) / q.toDouble()) < 1e-9 * a) {
                val t = (if (o > 1) "$o" else "") + "√$n"
                return s + (if (q > 1) "$t/$q" else t)
            }
        }

        val u = a / PI
        val p = toFraction(u, 999L) ?: if (abs(u - round(u)) < 1e-11) Fraction(round(u).toLong(), 1L) else null
        if (p != null && p.num <= 999L) {
            val t = (if (p.num > 1) "${p.num}" else "") + "π"
            return s + (if (p.den > 1) "$t/${p.den}" else t)
        }

        return null
    }

    fun formatDecimal(v: Double): String {
        return when (formatSetting.type) {
            NumberFormatType.FIX -> String.format(Locale.US, "%.${formatSetting.digits}f", v)
            NumberFormatType.SCI -> String.format(Locale.US, "%.${formatSetting.digits}e", v)
            NumberFormatType.NORM -> {
                if (v == 0.0) return "0"
                val a = abs(v)
                if (a >= 1e10 || a < 1e-2) {
                    String.format(Locale.US, "%.9e", v).replace(Regex("(\\.\\d*?)0+e"), "$1e").replace(".e", "e")
                } else {
                    val s = String.format(Locale.US, "%.10f", v).trimEnd('0').trimEnd('.')
                    s
                }
            }
        }
    }

    fun getResult(v: Double): ExactResult {
        val frac = toFraction(v)
        val sym = toExact(v)
        val dec = formatDecimal(v)
        return ExactResult(decimalStr = dec, fraction = frac, exactSymbolic = sym)
    }

    fun stats(values: List<Double>): Map<String, Double> {
        val n = values.size
        if (n == 0) return emptyMap()
        val sorted = values.sorted()
        val sum = values.sum()
        val sumSq = values.sumOf { it * it }
        val mean = sum / n
        val popVar = (sumSq / n) - (mean * mean)
        val popSd = if (popVar > 0) sqrt(popVar) else 0.0
        val sampleSd = if (n > 1) sqrt((sumSq - n * mean * mean) / (n - 1)) else Double.NaN
        val median = if (n % 2 == 1) sorted[n / 2] else (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        val q1List = sorted.subList(0, n / 2)
        val q3List = sorted.subList(n - (n / 2), n)
        val q1 = if (q1List.isNotEmpty()) {
            if (q1List.size % 2 == 1) q1List[q1List.size / 2] else (q1List[q1List.size / 2 - 1] + q1List[q1List.size / 2]) / 2.0
        } else median
        val q3 = if (q3List.isNotEmpty()) {
            if (q3List.size % 2 == 1) q3List[q3List.size / 2] else (q3List[q3List.size / 2 - 1] + q3List[q3List.size / 2]) / 2.0
        } else median

        return mapOf(
            "n" to n.toDouble(),
            "Σx" to sum,
            "Σx²" to sumSq,
            "x̄" to mean,
            "σx" to popSd,
            "sx" to sampleSd,
            "minX" to sorted.first(),
            "maxX" to sorted.last(),
            "Q1" to q1,
            "Med" to median,
            "Q3" to q3
        )
    }

    fun linearRegression(xList: List<Double>, yList: List<Double>): Triple<Double, Double, Double> {
        val n = xList.size
        val sx = xList.sum()
        val sy = yList.sum()
        val sxy = xList.indices.sumOf { xList[it] * yList[it] }
        val sxx = xList.sumOf { it * it }
        val syy = yList.sumOf { it * it }

        val b = (n * sxy - sx * sy) / (n * sxx - sx * sx)
        val a = (sy - b * sx) / n
        val r = (n * sxy - sx * sy) / sqrt((n * sxx - sx * sx) * (n * syy - sy * sy))
        return Triple(a, b, r)
    }

    fun gaussJordan(matrix: Array<DoubleArray>, n: Int): Double {
        var det = 1.0
        for (k in 0 until n) {
            var pivot = k
            for (r in k + 1 until n) {
                if (abs(matrix[r][k]) > abs(matrix[pivot][k])) pivot = r
            }
            if (abs(matrix[pivot][k]) < 1e-12) return 0.0
            if (pivot != k) {
                val temp = matrix[pivot]
                matrix[pivot] = matrix[k]
                matrix[k] = temp
                det = -det
            }
            val v = matrix[k][k]
            det *= v
            for (j in matrix[k].indices) matrix[k][j] /= v
            for (r in 0 until n) {
                if (r != k) {
                    val f = matrix[r][k]
                    for (j in matrix[k].indices) {
                        matrix[r][j] -= f * matrix[k][j]
                    }
                }
            }
        }
        return det
    }

    fun polynomialRoots(coeffs: List<Double>): List<Complex> {
        val n = coeffs.size - 1
        val c0 = coeffs[0]
        val a = coeffs.map { it / c0 }
        val maxCoeff = a.drop(1).maxOfOrNull { abs(it) } ?: 1.0
        val m = 1.0 + maxCoeff

        val roots = Array(n) { i ->
            Complex(m * cos(2 * PI * i / n + 0.4), m * sin(2 * PI * i / n + 0.4))
        }

        for (t in 0 until 400) {
            for (i in 0 until n) {
                var p = Complex(1.0, 0.0)
                var q = Complex(1.0, 0.0)
                for (k in 1..n) {
                    p = (p * roots[i]) + Complex(a[k], 0.0)
                }
                for (j in 0 until n) {
                    if (j != i) q = q * (roots[i] - roots[j])
                }
                roots[i] = roots[i] - (p / q)
            }
        }
        return roots.toList()
    }
}
