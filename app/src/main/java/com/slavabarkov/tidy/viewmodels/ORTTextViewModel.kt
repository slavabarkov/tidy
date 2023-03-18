/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.viewmodels

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.app.Application
import android.util.JsonReader
import androidx.lifecycle.AndroidViewModel
import com.slavabarkov.tidy.R
import com.slavabarkov.tidy.normalizeL2
import com.slavabarkov.tidy.tokenizer.ClipTokenizer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.IntBuffer
import java.util.HashMap

class ORTTextViewModel(application: Application) : AndroidViewModel(application) {
    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val modelID = R.raw.textual_quant
    private val resources = getApplication<Application>().resources
    private val model = resources.openRawResource(modelID).readBytes()
    private val session = ortEnv.createSession(model)

    private val tokenizerVocab: Map<String, Int> = getVocab()
    private val tokenizerMerges: HashMap<Pair<String, String>, Int> = getMerges()
    private val tokenBOS: Int = 49406
    private val tokenEOS: Int = 49407
    private val tokenizer = ClipTokenizer(tokenizerVocab, tokenizerMerges)

    private val queryFilter = Regex("[^A-Za-z0-9 ]")

    fun init() {
    }

    fun getTextEmbedding(text: String): FloatArray {
        // Tokenize
        val textClean = queryFilter.replace(text, "").lowercase()
        var tokens: MutableList<Int> = ArrayList()
        tokens.add(tokenBOS)
        tokens.addAll(tokenizer.encode(textClean))
        tokens.add(tokenEOS)

        var mask: MutableList<Int> = ArrayList()
        for (i in 0 until tokens.size) {
            mask.add(1)
        }
        while (tokens.size < 77) {
            tokens.add(0)
            mask.add(0)
        }
        tokens = tokens.subList(0, 77)
        mask = mask.subList(0, 77)

        // Convert to tensor
        val inputShape = longArrayOf(1, 77)
        val inputIds = IntBuffer.allocate(1 * 77)
        inputIds.rewind()
        for (i in 0 until 77) {
            inputIds.put(tokens[i])
        }
        inputIds.rewind()
        val inputIdsTensor = OnnxTensor.createTensor(ortEnv, inputIds, inputShape)

        val attentionMask = IntBuffer.allocate(1 * 77)
        attentionMask.rewind()
        for (i in 0 until 77) {
            attentionMask.put(mask[i])
        }
        attentionMask.rewind()
        val attentionMaskTensor = OnnxTensor.createTensor(ortEnv, attentionMask, inputShape)

        val inputMap: MutableMap<String, OnnxTensor> = HashMap()
        inputMap["input_ids"] = inputIdsTensor
        inputMap["attention_mask"] = attentionMaskTensor

        val output = session?.run(inputMap)
        output.use {
            @Suppress("UNCHECKED_CAST") var rawOutput =
                ((output?.get(0)?.value) as Array<FloatArray>)[0]
            rawOutput = normalizeL2(rawOutput)
            return rawOutput
        }
    }

    fun getVocab(): Map<String, Int> {
        val vocab = hashMapOf<String, Int>().apply {
            resources.openRawResource(R.raw.vocab).use {
                val vocabReader = JsonReader(InputStreamReader(it, "UTF-8"))
                vocabReader.beginObject()
                while (vocabReader.hasNext()) {
                    val key = vocabReader.nextName().replace("</w>", " ")
                    val value = vocabReader.nextInt()
                    put(key, value)
                }
                vocabReader.close()
            }
        }
        return vocab
    }

    fun getMerges(): HashMap<Pair<String, String>, Int> {
        val merges = hashMapOf<Pair<String, String>, Int>().apply {
            resources.openRawResource(R.raw.merges).use {
                val mergesReader = BufferedReader(InputStreamReader(it))
                mergesReader.useLines { seq ->
                    seq.drop(1).forEachIndexed { i, s ->
                        val list = s.split(" ")
                        val keyTuple = list[0] to list[1].replace("</w>", " ")
                        put(keyTuple, i)
                    }
                }
            }
        }
        return merges
    }
}