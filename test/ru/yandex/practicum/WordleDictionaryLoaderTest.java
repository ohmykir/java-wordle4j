package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exception.DictionaryNotFoundException;
import ru.yandex.practicum.exception.EmptyDictionaryException;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WordleDictionaryLoaderTest {
    private static PrintWriter log;

    @TempDir
    Path tempDir;

    @BeforeAll
    static void initLog() {
        log = new PrintWriter(System.out, true);
    }

    private Path writeFile(String content) throws IOException {
        Path file = tempDir.resolve("dict.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void loadsNormalizesFiltersAndDeduplicates() throws IOException {
        Path file = writeFile("Ёжики\nежики\nГЕРОЙ\n  герой  \nкот\nпятибуква\n\nстена\r\n");

        WordleDictionary dictionary = new WordleDictionaryLoader(log).createWordleDictionary(file.toString());

        assertEquals(List.of("ежики", "герой", "стена"), dictionary.getWords());
    }

    @Test
    void wordsOfWrongLengthAreDropped() throws IOException {
        Path file = writeFile("кот\nстена\nпятибуква\n");

        WordleDictionary dictionary = new WordleDictionaryLoader(log).createWordleDictionary(file.toString());

        assertEquals(List.of("стена"), dictionary.getWords());
    }

    @Test
    void readsUtf8Correctly() throws IOException {
        Path file = writeFile("герой\n");

        WordleDictionary dictionary = new WordleDictionaryLoader(log).createWordleDictionary(file.toString());

        assertTrue(dictionary.contains("герой"));
    }

    @Test
    void emptyResultThrowsEmptyDictionaryExceptionWithFileName() throws IOException {
        Path file = writeFile("кот\nдом\n\n");
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

        EmptyDictionaryException ex = assertThrows(EmptyDictionaryException.class,
                () -> loader.createWordleDictionary(file.toString()));

        assertTrue(ex.getMessage().contains(file.toString()));
    }

    @Test
    void emptyFileThrowsEmptyDictionaryException() throws IOException {
        Path file = writeFile("");
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

        assertThrows(EmptyDictionaryException.class, () -> loader.createWordleDictionary(file.toString()));
    }

    @Test
    void missingFileThrowsDictionaryNotFoundWithNameAndCause() {
        String missing = tempDir.resolve("no_such_file.txt").toString();
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

        DictionaryNotFoundException ex = assertThrows(DictionaryNotFoundException.class,
                () -> loader.createWordleDictionary(missing));

        assertTrue(ex.getMessage().contains(missing));
    }
}