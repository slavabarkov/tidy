/**
 * Copyright 2023 Viacheslav Barkov
 * Copyright 2019 Rob Rua
 * The following code is a derivative work of the code from the Easy Bert project,
 * which is licensed MIT.
 */

package com.slavabarkov.tidy.tokenizer;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class Tokenizer {
    protected static Stream<String> whitespaceTokenize(final String sequence) {
        return Arrays.stream(sequence.trim().split("\\s+"));
    }

    public abstract String[] tokenize(String sequence);

    public abstract String[][] tokenize(String... sequences);
}