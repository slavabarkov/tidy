/**
 * Copyright 2023 Viacheslav Barkov
 * Copyright 2019 Rob Rua
 * The following code is a derivative work of the code from the Easy Bert project,
 * which is licensed MIT.
 */

package com.slavabarkov.tidy.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class FullTokenizer extends Tokenizer {
    private static Map<String, Integer> loadVocabulary(final BufferedReader reader) {
        final Map<String, Integer> vocabulary = new HashMap<>();
        try (BufferedReader readerNew = reader) {
            int index = 0;
            String line;
            while ((line = readerNew.readLine()) != null) {
                vocabulary.put(line.trim(), index++);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return vocabulary;
    }

    private final BasicTokenizer basic;

    private final Map<String, Integer> vocabulary;

    private final WordpieceTokenizer wordpiece;

    public FullTokenizer(final BufferedReader vocabReader, final boolean doLowerCase) {
        vocabulary = loadVocabulary(vocabReader);
        basic = new BasicTokenizer(doLowerCase);
        wordpiece = new WordpieceTokenizer(vocabulary);
    }

    public int[] convert(final String[] tokens) {
        return Arrays.stream(tokens).mapToInt(vocabulary::get).toArray();
    }

    @Override
    public String[] tokenize(final String sequence) {
        return Arrays.stream(wordpiece.tokenize(basic.tokenize(sequence))).flatMap(Stream::of).toArray(String[]::new);
    }

    @Override
    public String[][] tokenize(final String... sequences) {
        return Arrays.stream(basic.tokenize(sequences)).map((final String[] tokens) -> Arrays.stream(wordpiece.tokenize(tokens)).flatMap(Stream::of).toArray(String[]::new)).toArray(String[][]::new);
    }
}
