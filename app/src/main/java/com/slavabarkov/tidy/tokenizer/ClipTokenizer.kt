/**
 * Copyright 2023 Viacheslav Barkov
 *
 * The following code is a derivative work of the code from the TensorFlow Lite Transformers
 * with Android project, which is licensed Apache 2.0
 */

package com.slavabarkov.tidy.tokenizer

class ClipTokenizer(
    private val encoder: Map<String, Int>,
    private val bpeRanks: Map<Pair<String, String>, Int>,
) {
    private val encodeRegex =
        Regex("""<\|startoftext\|>|<\|endoftext\|>|'s|'t|'re|'ve|'m|'ll|'d|[\p{L}]+|[\p{N}]|[^\s\p{L}\p{N}]+""")

    fun encode(text: String): MutableList<Int> {
        val tokens = encodeRegex.findAll(text).map { result ->
            result.value.codePoints().boxed().map { byteEncoder[it]!! }.toArray().joinToString("")
        }
        return tokens.map { bpe(it) }.flatten().map { encoder[it]!! }.toMutableList()
    }

    private fun bpe(token: String): List<String> {
        if (token.length <= 1) return listOf("$token ")

        val wordWithBreak = token.map { it.toString() }.toMutableList()
        wordWithBreak[wordWithBreak.size - 1] = "${wordWithBreak[wordWithBreak.size - 1]} "
        var word = wordWithBreak.toList()
        var pairs = getPairs(word)

        while (true) {
            if (!pairs.any { bpeRanks.containsKey(it) }) break
            val (first, second) = pairs.minBy { bpeRanks.getOrDefault(it, Int.MAX_VALUE) }

            var i = 0
            val newWord = mutableListOf<String>()
            while (i < word.size) {
                val j = word.withIndex().indexOfFirst { it.index >= i && it.value == first }
                if (j != -1) {
                    newWord.addAll(word.subList(i, j))
                    i = j
                } else {
                    newWord.addAll(word.subList(i, word.size))
                    break
                }

                if (word[i] == first && i < word.size - 1 && word[i + 1] == second) {
                    newWord.add(first + second)
                    i += 2
                } else {
                    newWord.add(word[i])
                    i += 1
                }
            }

            word = newWord
            if (word.size == 1) {
                break
            } else {
                pairs = getPairs(word)
            }
        }
        return word
    }

    private fun getPairs(word: List<String>): Set<Pair<String, String>> {
        return mutableSetOf<Pair<String, String>>().apply {
            for (i in 0 until word.size - 1) {
                add(word[i] to word[i + 1])
            }
        }
    }
}