/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.viewmodels

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.slavabarkov.tidy.R
import com.slavabarkov.tidy.normalizeL2
import com.slavabarkov.tidy.tokenizer.FullTokenizer
import java.nio.IntBuffer
import java.util.HashMap

class ORTTextViewModel(application: Application) : AndroidViewModel(application) {
    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val modelID = R.raw.textual_quant
    private val resources = getApplication<Application>().resources
    private val model = resources.openRawResource(modelID).readBytes()
    private val session = ortEnv.createSession(model)
    private val vocabID = R.raw.vocab
    private val vocabReader = resources.openRawResource(vocabID).bufferedReader()
    private val tokenizer = FullTokenizer(vocabReader, false)

    fun getTextEmbedding(text: String): FloatArray {
        // Tokenize
        val tokensBasic: MutableList<String> = ArrayList()
        tokensBasic.add("<|startoftext|>")
        tokensBasic.addAll(tokenizer.tokenize(text).asList())
        tokensBasic.removeAll(listOf("[unk]"))
        tokensBasic.add("<|endoftext|>")

        Log.d("ORTTextViewModel", "Tokens, basic: " + tokensBasic.joinToString(" "))
        val tokensFull = tokenizer.convert(tokensBasic.toTypedArray()).toMutableList()
        while (tokensFull.size < 77) {
            tokensFull.add(0)
        }
        Log.d("ORTTextViewModel", "Tokens, full: " + tokensFull.joinToString(" "))

        // Convert to tensor
        val inputShape = longArrayOf(1, 77)

        val inputIds = IntBuffer.allocate(1 * 77)
        inputIds.rewind()
        for (i in 0 until 77) {
            inputIds.put(tokensFull[i])
        }
        inputIds.rewind()
        val inputIdsTensor = OnnxTensor.createTensor(ortEnv, inputIds, inputShape)

        val attentionMask = IntBuffer.allocate(1 * 77)
        attentionMask.rewind()
        for (i in 0 until 77) {
            attentionMask.put(1)
        }
        attentionMask.rewind()
        val attentionMaskTensor = OnnxTensor.createTensor(ortEnv, attentionMask, inputShape)

        val inputMap: MutableMap<String, OnnxTensor> = HashMap()
        inputMap["input_ids"] = inputIdsTensor
        inputMap["attention_mask"] = attentionMaskTensor

        val output = session?.run(inputMap)
        output.use {
            @Suppress("UNCHECKED_CAST")
            var rawOutput = ((output?.get(0)?.value) as Array<FloatArray>)[0]
            Log.d("MainActivity", "Output: " + rawOutput.joinToString(" "))

            rawOutput = normalizeL2(rawOutput)
            return rawOutput
        }


    }


}