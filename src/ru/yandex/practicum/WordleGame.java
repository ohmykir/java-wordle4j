package ru.yandex.practicum;

import ru.yandex.practicum.exception.HintException;
import ru.yandex.practicum.exception.IncorrectInputException;
import ru.yandex.practicum.exception.WordNotInDictionaryException;

import java.io.PrintWriter;
import java.util.*;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {
    private final String answer;

    private GameState gameState;

    private int steps;

    private final WordleDictionary dictionary;

    private final Map<String, String> usedWords = new LinkedHashMap<>();

    private final PrintWriter logWriter;

    private final Random random = new Random();

    public WordleGame(String answer, WordleDictionary dictionary, PrintWriter logWriter) {
        this.answer = answer;
        this.gameState = GameState.ONGOING;
        this.dictionary = dictionary;
        this.steps = 0;
        this.logWriter = logWriter;

        logWriter.println("INFO: Игра инициализирована. Ответ: " + answer);
    }

    public GameState getGameState() {
        return gameState;
    }

    public String getAnswer() {
        return answer;
    }

    public Step nextStep(String guess) {
        boolean isHint = guess.isEmpty();

        if (isHint) {
            guess = giveHint();
        } else {
            validateGuess(guess);
        }

        String output = checkGuess(guess, answer);
        usedWords.put(guess, output);

        return makeStep(guess, output, isHint);
    }

    private void validateGuess(String guess) {
        if (guess.length() != WordleRules.WORD_LENGTH) {
            logWriter.printf("WARN: Ввод \"%s\" отклонен, причина: неверная длина.%n", guess);
            throw new IncorrectInputException("Длина слова должна составлять " + WordleRules.WORD_LENGTH + " символов!");
        }

        if (!guess.matches("[а-яА-ЯёЁ ]+")){
            logWriter.printf("WARN: Ввод \"%s\" отклонен, причина: неверная раскладка.%n", guess);
            throw new IncorrectInputException("Неверная раскладка");
        }

        if (usedWords.containsKey(guess)) {
            logWriter.printf("WARN: Ввод \"%s\" отклонен, причина: слово введено ранее.%n", guess);
            throw new IncorrectInputException("Это слово уже было!");
        }

        if (!dictionary.contains(guess)) {
            logWriter.printf("WARN: Ввод \"%s\" отклонен, причина: слово отсутствует в словаре.%n", guess);
            throw new WordNotInDictionaryException("Такого слова нет!");
        }
    }

    static String checkGuess(String guess, String target) {
        if (guess.equals(target)) {
            String exact = String.valueOf(WordleRules.EXACT);
            return exact.repeat(target.length());
        }

        String absent = String.valueOf(WordleRules.ABSENT);
        StringBuilder step = new StringBuilder(absent.repeat(target.length()));
        StringBuilder ans = new StringBuilder(target);
        List<Integer> indexes = new ArrayList<>();
        for (int i = guess.length() - 1; i >= 0; i--) {
            if (target.charAt(i) == guess.charAt(i)) {
                step.setCharAt(i, WordleRules.EXACT);
                indexes.add(i);
            }
        }
        indexes.forEach(ans::deleteCharAt);
        for (int i = 0; i < guess.length(); i++) {
            if (step.charAt(i) != WordleRules.EXACT && ans.lastIndexOf(String.valueOf(guess.charAt(i))) != -1) {
                step.setCharAt(i, WordleRules.PRESENT);
                ans.deleteCharAt(ans.lastIndexOf(String.valueOf(guess.charAt(i))));
            }
        }

        return step.toString();
    }

    private Step makeStep(String guess, String output, boolean isHint) {
        steps++;

        if (guess.equals(answer)) {
            gameState = GameState.WON;
        } else if (steps >= WordleRules.MAX_STEPS) {
            gameState = GameState.LOST;
        }

        logWriter.printf("INFO: Ход номер %d: подсказка? - %s, ввод - %s, вывод - %s. Состояние: %s.%n",
                        steps,
                        isHint? "да" : "нет",
                        guess,
                        output,
                        gameState.toString());
        return new Step(guess, output, isHint);
    }

    private String giveHint() {
        List<String> possibleHints = filterWordsByHistory(dictionary.getWords());
        logWriter.printf("INFO: Подсказка: претендентов после фильтра по истории: %d, ",  possibleHints.size());
        List<Integer> unknownPositions = findUnknownPositions();

        if(unknownPositions.isEmpty()){
            logWriter.println("INFO: Подсказка: все буквы открыты, выдается ответ.");
            return answer;
        }

        int bonusLetterIndex = unknownPositions.get(random.nextInt(unknownPositions.size()));
        possibleHints = filterWordsByLetterAt(bonusLetterIndex, possibleHints);

        if (possibleHints.isEmpty()) {
            String message = String.format("пустой список кандидатов в подсказки. Ответ: %s, история: %s, доп позиция: %s",
                                        answer,
                                        usedWords,
                                        bonusLetterIndex);
            throw new HintException(message);
        }
        logWriter.printf("претендентов после фильтра по бонусной букве: %d%n", possibleHints.size());

        return possibleHints.get(random.nextInt(possibleHints.size()));
    }

    private List<String> filterWordsByHistory(List<String> words) {
        List<String> possibleHints = new ArrayList<>(words);
        for (Map.Entry<String, String> entry : usedWords.entrySet()) {
            possibleHints = possibleHints.stream().filter(s -> checkGuess(entry.getKey(), s).equals(entry.getValue())).toList();
        }

        return possibleHints;
    }

    private List<String> filterWordsByLetterAt(int position, List<String> words) {
        char bonusLetter = answer.charAt(position);
        return words.stream().filter(s -> s.charAt(position) == bonusLetter).toList();
    }

    private List<Integer> findUnknownPositions() {
        String absent = String.valueOf(WordleRules.ABSENT);
        StringBuilder knownExactPositions = new StringBuilder(absent.repeat(WordleRules.WORD_LENGTH));
        for (Map.Entry<String, String> entry : usedWords.entrySet()) {
            for (int i = 0; i < WordleRules.WORD_LENGTH; i++) {
                if (entry.getValue().charAt(i) == WordleRules.EXACT) {
                    knownExactPositions.setCharAt(i, WordleRules.EXACT);
                }
            }
        }

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < knownExactPositions.length(); i++) {
            if (knownExactPositions.charAt(i) != WordleRules.EXACT) {
                indexes.add(i);
            }
        }

        return indexes;
    }
}