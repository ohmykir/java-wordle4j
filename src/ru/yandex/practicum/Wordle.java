package ru.yandex.practicum;

import ru.yandex.practicum.exception.ApplicationException;
import ru.yandex.practicum.exception.ClosedInputException;
import ru.yandex.practicum.exception.InGameException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {

    private static final String DICTIONARY_FILENAME = "words_ru.txt";
    private static final String LOG_FILENAME = "log_file.txt";

    public static void main(String[] args) {
        try (PrintWriter logWriter = createLogFile();
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            try {
                logWriter.println("INFO: Wordle запущена");

                WordleDictionaryLoader wordleDictionaryLoader = createDictionaryLoader(logWriter);
                WordleDictionary wordleDictionary = createDictionary(wordleDictionaryLoader, DICTIONARY_FILENAME);
                WordleGame game = initGame(wordleDictionary, logWriter);

                play(game, reader);

                logWriter.println("INFO: Wordle завершена штатно");
            } catch (ClosedInputException e) {
                logWriter.println("INFO: Ввод закрыт, игра завершена досрочно");
                System.out.println("Ввод закрыт. Игра завершена.");
            } catch (ApplicationException e) {
                logWriter.printf("ERROR: ошибка программы: %s%n", e.getMessage());
                System.out.printf("Не удалось запустить или продолжить игру. Подробности в %s%n", LOG_FILENAME);
            } catch (IOException e) {
                logWriter.printf("ERROR: ошибка ввода-вывода %s%n",e.getMessage());
                System.out.printf("Не удалось запустить или продолжить игру. Подробности в %s%n", LOG_FILENAME);
            } catch (Exception e) {
                logWriter.println("ERROR: Неожиданная ошибка");
                e.printStackTrace(logWriter);
                System.out.printf("Непредвиденная ошибка. Подробности в %s%n", LOG_FILENAME);
            }
        } catch (IOException e) {
            System.out.printf("Не удалось создать файл лога: %s", e.getMessage());
        }
    }

    private static PrintWriter createLogFile() throws IOException {
        File file = new File(LOG_FILENAME);
        return new PrintWriter(file, StandardCharsets.UTF_8);
    }

    private static WordleDictionaryLoader createDictionaryLoader(PrintWriter logWriter) {
        return new WordleDictionaryLoader(logWriter);
    }

    private static WordleDictionary createDictionary(WordleDictionaryLoader wordleDictionaryLoader, String fileName) throws IOException {
        return wordleDictionaryLoader.createWordleDictionary(fileName);
    }

    private static WordleGame initGame(WordleDictionary wordleDictionary, PrintWriter logWriter) {
        String answer = pickAnswer(wordleDictionary);
        return new WordleGame(answer, wordleDictionary, logWriter);
    }

    private  static String pickAnswer(WordleDictionary wordleDictionary) {
        int dictionarySize = wordleDictionary.size();
        int answerNumber = (int) (Math.random() * dictionarySize);

        return wordleDictionary.getWordByNumber(answerNumber);
    }

    static void play(WordleGame game, BufferedReader reader) throws IOException {
        String guess;

        while (game.getGameState() == GameState.ONGOING) {
            guess = enterGuess(reader);

            try {
                Step step = game.nextStep(guess);

                if (step.isHint()) {
                    System.out.println(step.guess());
                }
                System.out.println(step.output());
            } catch (InGameException e) {
                System.out.println(e.getMessage());
            }
        }

        if (game.getGameState() == GameState.WON) {
            System.out.println("Вы угадали слово!");
        } else {
            System.out.printf("Вы проиграли! Правильный ответ: %s. %n", game.getAnswer());
        }
    }

    static String enterGuess(BufferedReader reader) throws IOException {
        String input = reader.readLine();
        if (input == null) {
            throw new ClosedInputException("ввод закрыт");
        }
        input = WordleDictionary.normalizeWord(input);

        return input;
    }
}