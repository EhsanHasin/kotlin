/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("MathKt")
package kotlin

/**
 * Returns `true` if the specified number is a
 * Not-a-Number (NaN) value, `false` otherwise.
 */
@kotlin.internal.InlineOnly
public actual inline fun Double.isNaN(): Boolean = java.lang.Double.isNaN(this)

/**
 * Returns `true` if the specified number is a
 * Not-a-Number (NaN) value, `false` otherwise.
 */
@kotlin.internal.InlineOnly
public actual inline fun Float.isNaN(): Boolean = java.lang.Float.isNaN(this)

/**
 * Returns `true` if this value is infinitely large in magnitude.
 */
@kotlin.internal.InlineOnly
public actual inline fun Double.isInfinite(): Boolean = java.lang.Double.isInfinite(this)

/**
 * Returns `true` if this value is infinitely large in magnitude.
 */
@kotlin.internal.InlineOnly
public actual inline fun Float.isInfinite(): Boolean = java.lang.Float.isInfinite(this)

/**
 * Returns `true` if the argument is a finite floating-point value; returns `false` otherwise (for `NaN` and infinity arguments).
 */
@kotlin.internal.InlineOnly
public actual inline fun Double.isFinite(): Boolean = !isInfinite() && !isNaN()

/**
 * Returns `true` if the argument is a finite floating-point value; returns `false` otherwise (for `NaN` and infinity arguments).
 */
@kotlin.internal.InlineOnly
public actual inline fun Float.isFinite(): Boolean = !isInfinite() && !isNaN()

/**
 * Returns a bit representation of the specified floating-point value as [Long]
 * according to the IEEE 754 floating-point "double format" bit layout.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Double.toBits(): Long = java.lang.Double.doubleToLongBits(this)

/**
 * Returns a bit representation of the specified floating-point value as [Long]
 * according to the IEEE 754 floating-point "double format" bit layout,
 * preserving `NaN` values exact layout.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Double.toRawBits(): Long = java.lang.Double.doubleToRawLongBits(this)

/**
 * Returns the [Double] value corresponding to a given bit representation.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Double.Companion.fromBits(bits: Long): Double = java.lang.Double.longBitsToDouble(bits)

/**
 * Returns a bit representation of the specified floating-point value as [Int]
 * according to the IEEE 754 floating-point "single format" bit layout.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Float.toBits(): Int = java.lang.Float.floatToIntBits(this)

/**
 * Returns a bit representation of the specified floating-point value as [Int]
 * according to the IEEE 754 floating-point "single format" bit layout,
 * preserving `NaN` values exact layout.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Float.toRawBits(): Int = java.lang.Float.floatToRawIntBits(this)

/**
 * Returns the [Float] value corresponding to a given bit representation.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Float.Companion.fromBits(bits: Int): Float = java.lang.Float.intBitsToFloat(bits)



@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Int.countOneBits(): Int = Integer.bitCount(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Int.countLeadingZeroBits(): Int = Integer.numberOfLeadingZeros(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Int.countTrailingZeroBits(): Int = Integer.numberOfTrailingZeros(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Int.takeHighestOneBit(): Int = Integer.highestOneBit(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Int.takeLowestOneBit(): Int = Integer.lowestOneBit(this)




@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Long.countOneBits(): Int = java.lang.Long.bitCount(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Long.countLeadingZeroBits(): Int = java.lang.Long.numberOfLeadingZeros(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Long.countTrailingZeroBits(): Int = java.lang.Long.numberOfTrailingZeros(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Long.takeHighestOneBit(): Long = java.lang.Long.highestOneBit(this)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Long.takeLowestOneBit(): Long = java.lang.Long.lowestOneBit(this)


@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Byte.countOneBits(): Int = (toInt() and 0xFF).countOneBits()

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Byte.countLeadingZeroBits(): Int = (toInt() and 0xFF).countLeadingZeroBits() - (Int.SIZE_BITS - Byte.SIZE_BITS)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Byte.countTrailingZeroBits(): Int = (toInt() or 0x100).countTrailingZeroBits()

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Byte.takeHighestOneBit(): Byte = (toInt() and 0xFF).takeHighestOneBit().toByte()

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Byte.takeLowestOneBit(): Byte = toInt().takeLowestOneBit().toByte()



@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Short.countOneBits(): Int = (toInt() and 0xFFFF).countOneBits()

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Short.countLeadingZeroBits(): Int = (toInt() and 0xFFFF).countLeadingZeroBits() - (Int.SIZE_BITS - Short.SIZE_BITS)

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Short.countTrailingZeroBits(): Int = (toInt() or 0x10000).countTrailingZeroBits()

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Short.takeHighestOneBit(): Short = (toInt() and 0xFFFF).takeHighestOneBit().toShort()

@SinceKotlin("1.3")
@ExperimentalStdlibApi
@kotlin.internal.InlineOnly
public inline fun Short.takeLowestOneBit(): Short = toInt().takeLowestOneBit().toShort()