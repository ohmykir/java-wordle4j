package ru.yandex.practicum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WordleGameCheckGuessTest {
    @Test
    void exampleFromSpecification() {
        assertEquals("+^-^-", WordleGame.checkGuess("гонец", "герой"));
    }

    @Test
    void fullMatch() {
        assertEquals("+++++", WordleGame.checkGuess("герой", "герой"));
    }

    @Test
    void noCommonLetters() {
        assertEquals("-----", WordleGame.checkGuess("абвгд", "жзийк"));
    }

    @Test
    void allLettersPresentButWrongPlaces() {
        assertEquals("^^^^^", WordleGame.checkGuess("абвгд", "бадвг"));
    }

    @Test
    void repeatedLetterInGuessOnlyOneInAnswer() {
        assertEquals("---+-", WordleGame.checkGuess("ооооо", "герой"));
    }

    @Test
    void repeatedLetterInGuessOneOfThemExact() {
        assertEquals("-+^--", WordleGame.checkGuess("ааббб", "баввв"));
    }

    @Test
    void repeatedLetterInAnswerOneInGuessWrongPlace() {
        assertEquals("^--^-", WordleGame.checkGuess("бдеаж", "аабвг"));
    }

    @Test
    void argumentOrderMatters() {
        assertEquals("+^-^-", WordleGame.checkGuess("гонец", "геоид"));
        assertEquals("+^^--", WordleGame.checkGuess("геоид", "гонец"));
    }
}
