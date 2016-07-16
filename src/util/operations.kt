package util

operator fun LongArray.get(index: Byte) = Operation.get(this, index)
operator fun LongArray.set(index: Byte, value: Long) = Operation.set(this, index, value)

infix fun Byte.and(other: Byte) = Operation.and(this, other)
infix fun Byte.or(other: Byte) = Operation.or(this, other)
infix fun Byte.xor(other: Byte) = Operation.xor(this, other)

//fun main(arg: Array<String>) {
////    val b: Byte = 5
////    val g: Byte = 9
////
////    val e = b and g
////
////    val c = LongArray(64) { i -> i.toLong() }
////    c[b] = 999
////    val a = c[b]
////    println(a)
//
//    val t1: Byte = 63
//    val t2: Byte = 64
//    println(t1 and t2)
//    println(t1 or t2)
//    println(t1 xor t2)
//
//}