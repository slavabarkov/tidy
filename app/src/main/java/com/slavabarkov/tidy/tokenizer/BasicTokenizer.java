/**
 * Copyright 2023 Viacheslav Barkov
 * Copyright 2019 Rob Rua
 * The following code is a derivative work of the code from the Easy Bert project,
 * which is licensed MIT.
 */

package com.slavabarkov.tidy.tokenizer;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableSet;

public class BasicTokenizer extends Tokenizer {
    private static final Set<Integer> CONTROL_CATEGORIES = ImmutableSet.of((int)Character.CONTROL,
        (int)Character.FORMAT,
        (int)Character.PRIVATE_USE,
        (int)Character.SURROGATE,
        (int)Character.UNASSIGNED);

    private static final Set<Integer> PUNCTUATION_CATEGORIES = ImmutableSet.of((int)Character.CONNECTOR_PUNCTUATION,
        (int)Character.DASH_PUNCTUATION,
        (int)Character.END_PUNCTUATION,
        (int)Character.FINAL_QUOTE_PUNCTUATION,
        (int)Character.INITIAL_QUOTE_PUNCTUATION,
        (int)Character.OTHER_PUNCTUATION,
        (int)Character.START_PUNCTUATION);

    private static final Set<Integer> SAFE_CONTROL_CHARACTERS = ImmutableSet.of((int)'\t', (int)'\n', (int)'\r');

    private static final Set<Integer> STRIP_CHARACTERS = ImmutableSet.of(0, 0xFFFD);

    private static final Set<Integer> WHITESPACE_CHARACTERS = ImmutableSet.of((int)' ', (int)'\t', (int)'\n', (int)'\r');

    private static String cleanText(final String sequence) {
        final StringBuilder builder = new StringBuilder();
        sequence.codePoints().filter((final int codePoint) -> !STRIP_CHARACTERS.contains(codePoint) && !isControl(codePoint))
            .map((final int codePoint) -> isWhitespace(codePoint) ? ' ' : codePoint)
            .forEachOrdered((final int codePoint) -> builder.append(Character.toChars(codePoint)));
        return builder.toString();
    }

    private static boolean isChineseCharacter(final int codePoint) {
        return codePoint >= 0x4E00 && codePoint <= 0x9FFF ||
            codePoint >= 0x3400 && codePoint <= 0x4DBF ||
            codePoint >= 0x20000 && codePoint <= 0x2A6DF ||
            codePoint >= 0x2A700 && codePoint <= 0x2B73F ||
            codePoint >= 0x2B740 && codePoint <= 0x2B81F ||
            codePoint >= 0x2B820 && codePoint <= 0x2CEAF ||
            codePoint >= 0xF900 && codePoint <= 0xFAFF ||
            codePoint >= 0x2F800 && codePoint <= 0x2FA1F;
    }

    private static boolean isControl(final int codePoint) {
        return !SAFE_CONTROL_CHARACTERS.contains(codePoint) && CONTROL_CATEGORIES.contains(Character.getType(codePoint));
    }

    private static boolean isPunctuation(final int codePoint) {
        return codePoint >= 33 && codePoint <= 47 ||
            codePoint >= 58 && codePoint <= 64 ||
            codePoint >= 91 && codePoint <= 96 ||
            codePoint >= 123 && codePoint <= 126 ||
            PUNCTUATION_CATEGORIES.contains(Character.getType(codePoint));
    }

    private static boolean isWhitespace(final int codePoint) {
        return WHITESPACE_CHARACTERS.contains(codePoint) || Character.SPACE_SEPARATOR == Character.getType(codePoint);
    }

    private static Stream<String> splitOnPunctuation(final String token) {
        final Stream.Builder<String> stream = Stream.builder();

        final StringBuilder builder = new StringBuilder();
        token.codePoints().forEachOrdered((final int codePoint) -> {
            if(isPunctuation(codePoint)) {
                stream.accept(builder.toString());
                builder.setLength(0);
                stream.accept(String.valueOf(Character.toChars(codePoint)));
            } else {
                builder.append(Character.toChars(codePoint));
            }
        });
        if(builder.length() > 0) {
            stream.accept(builder.toString());
        }

        return stream.build();
    }

    private static String stripAccents(final String token) {
        final StringBuilder builder = new StringBuilder();
        Normalizer.normalize(token, Normalizer.Form.NFD).codePoints()
            .filter((final int codePoint) -> Character.NON_SPACING_MARK != Character.getType(codePoint))
            .forEachOrdered((final int codePoint) -> builder.append(Character.toChars(codePoint)));
        return builder.toString();
    }

    private static String tokenizeChineseCharacters(final String sequence) {
        final StringBuilder builder = new StringBuilder();
        sequence.codePoints().forEachOrdered((final int codePoint) -> {
            if(isChineseCharacter(codePoint)) {
                builder.append(' ');
                builder.append(Character.toChars(codePoint));
                builder.append(' ');
            } else {
                builder.append(Character.toChars(codePoint));
            }
        });
        return builder.toString();
    }

    private final boolean doLowerCase;

    public BasicTokenizer(final boolean doLowerCase) {
        this.doLowerCase = doLowerCase;
    }

    private String stripAndSplit(String token) {
        if(doLowerCase) {
            token = stripAccents(token.toLowerCase());
        }
        return String.join(" ", splitOnPunctuation(token).toArray(String[]::new));
    }

    @Override
    public String[][] tokenize(final String... sequences) {
        return Arrays.stream(sequences)
            .map(BasicTokenizer::cleanText)
            .map(BasicTokenizer::tokenizeChineseCharacters)
            .map((final String sequence) -> whitespaceTokenize(sequence).toArray(String[]::new))
            .map((final String[] tokens) -> Arrays.stream(tokens)
                .map(this::stripAndSplit)
                .flatMap(BasicTokenizer::whitespaceTokenize)
                .toArray(String[]::new))
            .toArray(String[][]::new);
    }

    @Override
    public String[] tokenize(final String sequence) {
        return whitespaceTokenize(tokenizeChineseCharacters(cleanText(sequence)))
            .map(this::stripAndSplit)
            .flatMap(BasicTokenizer::whitespaceTokenize)
            .toArray(String[]::new);
    }
}
