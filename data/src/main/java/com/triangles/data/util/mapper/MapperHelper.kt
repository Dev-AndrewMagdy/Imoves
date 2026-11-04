package com.triangles.data.util.mapper

fun interface Mapper<in I, out O> {
    fun map(input: I): O
}

fun <I, O> Mapper<I, O>.mapAll(inputs: Iterable<I>): List<O> = inputs.map(::map)
