package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exception.InGameException;
import ru.yandex.practicum.exception.IncorrectInputException;
import ru.yandex.practicum.exception.WordNotInDictionaryException;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WordleGameTest {

    private static final List<String> WORDS = List.of(
            "герой", "гонец", "стена", "слово", "книга", "белка", "земля", "рынок",
            "завод", "право", "песня", "птица", "дождь", "ветер", "комод", "форма",
            "точка", "линия", "мышка", "нитка", "озеро", "огонь", "отзыв", "парта",
            "пенал", "плита", "радио", "ручка", "салат", "сапог", "сосна", "геоид");

    private static PrintWriter log;
    private static WordleDictionary dictionary;

    private WordleGame game;

    @BeforeAll
    static void initShared() {
        log = new PrintWriter(System.out, true);
        dictionary = new WordleDictionary(WORDS, log);
    }

    @BeforeEach
    void newGame() {
        game = new WordleGame("герой", dictionary, log);
    }

    @Test
    void wrongLengthIsRejected() {
        assertThrows(IncorrectInputException.class, () -> game.nextStep("сло"));
        assertThrows(IncorrectInputException.class, () -> game.nextStep("словосло"));
    }

    @Test
    void latinLettersAreRejected() {
        assertThrows(IncorrectInputException.class, () -> game.nextStep("hello"));
    }

    @Test
    void wordNotInDictionaryIsRejected() {
        assertThrows(WordNotInDictionaryException.class, () -> game.nextStep("ааааа"));
    }

    @Test
    void repeatedWordIsRejected() {
        game.nextStep("гонец");
        assertThrows(IncorrectInputException.class, () -> game.nextStep("гонец"));
    }

    @Test
    void rejectedWordsDoNotSpendSteps() {
        for (int i = 0; i < 3; i++) {
            assertThrows(InGameException.class, () -> game.nextStep("hello"));
            assertThrows(InGameException.class, () -> game.nextStep("ааааа"));
            assertThrows(InGameException.class, () -> game.nextStep("сло"));
        }
        assertEquals(GameState.ONGOING, game.getGameState());

        for (String w : List.of("гонец", "стена", "белка", "книга", "слово")) {
            game.nextStep(w);
        }
        assertEquals(GameState.ONGOING, game.getGameState());

        game.nextStep("земля");
        assertEquals(GameState.LOST, game.getGameState());
    }

    @Test
    void emptyInputAsksHint() {
        Step step = game.nextStep("");
        assertNotEquals(GameState.LOST, game.getGameState());
        assertTrue(step.isHint());
        assertNotEquals("", step.output());
    }

    @Test
    void rightWordWinsGame() {
        game.nextStep("герой");
        assertEquals(GameState.WON, game.getGameState());
    }

    @Test
    void allPositionsOpenedByPlayerHintReturnsAnswer() {
        List<String> words = List.of("герой", "герда", "немой");
        WordleDictionary small = new WordleDictionary(words, log);
        WordleGame g = new WordleGame("герой", small, log);

        g.nextStep("герда");
        g.nextStep("немой");
        assertEquals(GameState.ONGOING, g.getGameState());

        Step hint = g.nextStep("");

        assertTrue(hint.isHint());
        assertEquals("герой", hint.guess());
        assertEquals(GameState.WON, g.getGameState());
    }
}
