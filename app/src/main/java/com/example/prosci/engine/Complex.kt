package com.example.prosci.engine

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

data class Complex(val re: Double, val im: Double = 0.0) {
    operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
    operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
    operator fun unaryMinus() = Complex(-re, -im)

    operator fun times(other: Complex): Complex {
        return Complex(
            re * other.re - im * other.im,
            re * other.im + im * other.re
        )
    }

    operator fun div(other: Complex): Complex {
        val denom = other.re * other.re + other.im * other.im
        if (denom == 0.0) throw ArithmeticException("Math ERROR")
        return Complex(
            (re * other.re + im * other.im) / denom,
            (im * other.re - re * other.im) / denom
        )
    }

    fun abs(): Double = hypot(re, im)
    fun arg(): Double = atan2(im, re)

    fun format(): String {
        val r = clean(re)
        val i = clean(im)
        return when {
            kotlin.math.abs(im) < 1e-12 -> cleanStr(r)
            kotlin.math.abs(re) < 1e-12 -> "${cleanStr(i)}i"
            i < 0 -> "${cleanStr(r)} − ${cleanStr(-i)}i"
            else -> "${cleanStr(r)} + ${cleanStr(i)}i"
        }
    }

    companion object {
        fun clean(v: Double): Double = if (kotlin.math.abs(v) < 1e-12) 0.0 else v
        fun cleanStr(v: Double): String {
            val rounded = (v * 1e10).toLong() / 1e10
            return if (rounded % 1.0 == 0.0 && kotlin.math.abs(rounded) < 1e14) {
                rounded.toLong().toString()
            } else {
                v.toString()
            }
        }
    }
}
