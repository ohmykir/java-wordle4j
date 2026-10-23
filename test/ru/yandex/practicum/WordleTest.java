package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exception.ClosedInputException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {
    private static final List<String> WORDS = List.of(
            "герой", "гонец", "стена", "слово", "книга", "белка", "земля", "рынок",
            "завод", "право", "песня", "птица", "дождь", "ветер", "комод", "форма");

    private static PrintStream originalOut;
    private static PrintWriter log;
    private static WordleDictionary dictionary;

    private ByteArrayOutputStream captured;
    private WordleGame game;

    @BeforeAll
    static void initShared() {
        originalOut = System.out;
        log = new PrintWriter(originalOut, true);
        dictionary = new WordleDictionary(WORDS, log);
    }

    @BeforeEach
    void setUp() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        game = new WordleGame("герой", dictionary, log);
    }

    @AfterEach
    void restoreOut() {
        System.setOut(originalOut);
    }

    private String output() {
        return captured.toString(StandardCharsets.UTF_8);
    }

    private BufferedReader input(String text) {
        return new BufferedReader(new StringReader(text));
    }

    // ---------- play ----------

    @Test
    void playWinPrintsVictory() throws IOException {
        Wordle.play(game, input("герой\n"));

        assertEquals(GameState.WON, game.getGameState());
        assertTrue(output().contains("Вы угадали слово!"));
    }

    @Test
    void playLossPrintsAnswer() throws IOException {
        Wordle.play(game, input("гонец\nстена\nслово\nкнига\nбелка\nземля\n"));

        assertEquals(GameState.LOST, game.getGameState());
        assertTrue(output().contains("Вы проиграли"));
        assertTrue(output().contains("герой"));
    }

    @Test
    void playShowsResultLineForEachMove() throws IOException {
        Wordle.play(game, input("гонец\nгерой\n"));

        assertTrue(output().contains("+^-^-"));
    }

    @Test
    void playShowsGameErrorAndContinues() throws IOException {
        Wordle.play(game, input("ааааа\nгерой\n"));

        assertTrue(output().contains("Такого слова нет!"));
        assertEquals(GameState.WON, game.getGameState());
    }

    @Test
    void playWithOnlyEnterWinsAndPrintsHintWords() throws IOException {
        Wordle.play(game, input("\n\n\n\n\n"));

        assertEquals(GameState.WON, game.getGameState());
        assertTrue(output().contains("Вы угадали слово!"));
        assertTrue(WORDS.stream().anyMatch(w -> output().contains(w)));
    }

    @Test
    void playThrowsClosedInputExceptionWhenInputEnds() {
        assertThrows(ClosedInputException.class, () -> Wordle.play(game, input("гонец\n")));
        assertEquals(GameState.ONGOING, game.getGameState());
    }

    // ---------- enterGuess ----------

    @Test
    void enterGuessNormalizesInput() throws IOException {
        assertEquals("герой", Wordle.enterGuess(input("  ГЕРОЙ \n")));
    }

    @Test
    void enterGuessReturnsEmptyStringForEmptyLine() throws IOException {
        assertEquals("", Wordle.enterGuess(input("\n")));
        assertEquals("", Wordle.enterGuess(input("   \n")));
    }

    @Test
    void enterGuessThrowsOnClosedInput() {
        assertThrows(ClosedInputException.class, () -> Wordle.enterGuess(input("")));
    }
}