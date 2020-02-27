/*
 * This file is part of kython.
 *
 * kython is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kython is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with kython.  If not, see <https://www.gnu.org/licenses/>.
 */

package green.sailor.kython.interpreter.util

import kotlin.math.*

/**
 * Implementation of Complex numbers used by
 * [PyComplex][green.sailor.kython.interpreter.pyobject.numeric.PyComplex].
 */
data class Complex(val real: Double = 0.0, val imaginary: Double = 0.0) : Comparable<Complex> {
    val abs = hypot(real, imaginary)

    operator fun plus(other: Complex) =
        copy(real = real + other.real, imaginary = imaginary + other.imaginary)

    operator fun minus(other: Complex) =
        copy(real = real - other.real, imaginary = imaginary - other.imaginary)

    operator fun times(other: Complex) = copy(
        real = real * other.real - imaginary * other.imaginary,
        imaginary = real * other.imaginary + imaginary * other.real
    )

    operator fun div(other: Complex) = this * other.conjugate() / other.abs.pow(2.0)

    operator fun times(factor: Double) = copy(real = factor * real, imaginary = factor * imaginary)

    operator fun div(factor: Double) = copy(real = real / factor, imaginary = imaginary / factor)

    fun conjugate() = copy(imaginary = -imaginary)

    fun inverse() = copy(real = real / abs, imaginary = -imaginary / abs)

    override fun compareTo(other: Complex): Int = (abs - other.abs).toInt()

    override fun toString(): String = buildString {
        // This currently doesnt account for int/float repr but w/e.
        if (real != 0.0) {
            val sign = if (real.sign < 0) '+' else '-'
            append("$real$sign")
        }
        append("{$imaginary}j")
    }
}

// For later.
fun exponential(exponent: Complex) = Complex(
    real = cos(exponent.imaginary),
    imaginary = sin(exponent.imaginary)
) * exp(exponent.real)

val Double.j: Complex get() = Complex(0.0, this)

fun Double.toComplex() = Complex(this)
fun Long.toComplex() = toDouble().toComplex()

operator fun Double.plus(other: Complex): Complex = Complex(this + other.real, other.imaginary)
