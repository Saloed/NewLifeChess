package util

operator fun LongArray.get(index: Byte) = Operation.get(this, index)
operator fun LongArray.set(index: Byte, value: Long) = Operation.set(this, index, value)

operator fun <T> Array<T>.get(index: Byte) = Operation.get(this, index)
operator fun <T> Array<T>.set(index: Byte, value: T) = Operation.set(this, index, value)

infix fun Byte.and(other: Byte) = Operation.and(this, other)
infix fun Byte.or(other: Byte) = Operation.or(this, other)
infix fun Byte.xor(other: Byte) = Operation.xor(this, other)
fun Byte.inv() = Operation.not(this)

infix fun Byte.shl(shift: Byte) = Operation.shl(this, shift)
infix fun Byte.shr(shift: Byte) = Operation.shr(this, shift)
infix fun Byte.ushr(shift: Byte) = Operation.ushr(this, shift)


fun cshl(value: Long, shift: Byte) = Operation.rotateLeft(value, shift)

const val ZERO: Byte = 0
val F0: Byte = 0b01110000.toByte() or ((1 shl 7).toByte())

fun main(arg: Array<String>) {
    val t1: Byte = 63
    val t2: Byte = 64

    println(t2 shr 1)
}