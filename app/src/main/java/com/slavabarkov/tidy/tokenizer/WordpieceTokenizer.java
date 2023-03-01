/**
 * Copyright 2023 Viacheslav Barkov
 * Copyright 2019 Rob Rua
 * The following code is a derivative work of the code from the Easy Bert project,
 * which is licensed MIT.
 */

package com.slavabarkov.tidy.tokenizer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class WordpieceTokenizer extends Tokenizer {
    private static final int DEFAULT_MAX_CHARACTERS_PER_WORD = 200;
    private static final String DEFAULT_UNKNOWN_TOKEN = "[unk]";

    private final int maxCharactersPerWord;
    private final String unknownToken;
    private final Map<String, Integer> vocabulary;

    public WordpieceTokenizer(final Map<String, Integer> vocabulary) {
        this.vocabulary = vocabulary;
        unknownToken = DEFAULT_UNKNOWN_TOKEN;
        maxCharactersPerWord = DEFAULT_MAX_CHARACTERS_PER_WORD;
    }

    private Stream<String> splitToken(final String token) {
        final char[] characters = token.toCharArray();
        if (characters.length > maxCharactersPerWord) {
            return Stream.of(unknownToken);
        }

        final Stream.Builder<String> subtokens = Stream.builder();
        int start = 0;
        int end;
        while (start < characters.length) {
            end = characters.length;
            boolean found = false;
            while (start < end) {
                final String substring = (start > 0 ? "##" : "") + String.valueOf(characters, start, end - start);
                if (vocabulary.containsKey(substring)) {
                    subtokens.accept(substring);
                    found = true;
                    break;
                }
                end--;
            }
            if (!found) {
                subtokens.accept(unknownToken);
                break;
            }
            start = end;
        }
        return subtokens.build();
    }

    @Override
    public String[] tokenize(final String sequence) {
        return whitespaceTokenize(sequence).flatMap(this::splitToken).toArray(String[]::new);
    }

    @Override
    public String[][] tokenize(final String... sequences) {
        return Arrays.stream(sequences).map((final String sequence) -> whitespaceTokenize(sequence).toArray(String[]::new)).map((final String[] tokens) -> Arrays.stream(tokens).flatMap(this::splitToken).toArray(String[]::new)).toArray(String[][]::new);
    }
}
