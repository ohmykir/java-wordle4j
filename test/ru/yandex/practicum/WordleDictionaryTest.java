package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WordleDictionaryTest {
    private static PrintWriter log;
    private WordleDictionary dictionary;

    @BeforeAll
    static void initLog() {
        log = new PrintWriter(System.out, true);
    }

    private WordleDictionary create() {
        return new WordleDictionary(List.of("герой", "гонец", "стена"), log);
    }

    @Test
    void normalizeLowercasesAndTrims() {
        assertEquals("герой", WordleDictionary.normalizeWord("  ГЕРОЙ "));
    }

    @Test
    void normalizeReplacesYo() {
        assertEquals("ежик", WordleDictionary.normalizeWord("ЁЖИК"));
        assertEquals("ежик", WordleDictionary.normalizeWord("ёжик"));
    }

    @Test
    void normalizeEmptyAndBlank() {
        assertEquals("", WordleDictionary.normalizeWord(""));
        assertEquals("", WordleDictionary.normalizeWord("   "));
    }

    @Test
    void containsFindsExistingWord() {
        assertTrue(create().contains("герой"));
    }

    @Test
    void containsDoesNotFindMissingOrNotNormalizedWord() {
        dictionary = create();
        assertFalse(dictionary.contains("книга"));
        assertFalse(dictionary.contains("ГЕРОЙ"));
        assertFalse(dictionary.contains("  герой  "));
    }

    @Test
    void sizeAndGetWordByNumber() {
        dictionary = create();
        assertEquals(3, dictionary.size());
        assertEquals("герой", dictionary.getWordByNumber(0));
        assertEquals("стена", dictionary.getWordByNumber(2));
    }

    @Test
    void getWordByNumberOutOfBounds() {
        dictionary = create();
        assertThrows(IndexOutOfBoundsException.class, () -> dictionary.getWordByNumber(3));
        assertThrows(IndexOutOfBoundsException.class, () -> dictionary.getWordByNumber(-1));
    }

    @Test
    void getWordsIsUnmodifiable() {
        dictionary = create();
        List<String> words = dictionary.getWords();
        assertThrows(UnsupportedOperationException.class, () -> words.add("книга"));
    }
}
