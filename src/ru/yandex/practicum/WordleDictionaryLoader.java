package ru.yandex.practicum;

import ru.yandex.practicum.exception.DictionaryNotFoundException;
import ru.yandex.practicum.exception.EmptyDictionaryException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {

    private final PrintWriter logWriter;

    public WordleDictionaryLoader(PrintWriter logWriter) {
        this.logWriter = logWriter;
        logWriter.println("INFO: Создан загрузчик словаря.");
    }

    public WordleDictionary createWordleDictionary(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            List<String> words = reader.lines().toList();
            int rawSize = words.size();

            words = filterWords(words);

            if (words.isEmpty()) {
                throw new EmptyDictionaryException(String.format("пустой словарь %s, слов отсеяно: %d", fileName, rawSize));
            }

            logWriter.printf("INFO: Словарь успешно загружен. Количество слов: %d. Слов отсеяно: %d.%n",
                            words.size(),
                            rawSize - words.size());
            return new WordleDictionary(words, logWriter);
        } catch (FileNotFoundException e) {
            throw new DictionaryNotFoundException(String.format("файл %s словаря не найден", fileName), e.getCause());
        } catch (IOException | UncheckedIOException e) {
            throw new IOException("ошибка при создании словаря " + fileName, e);
        }
    }

    private List<String> filterWords(List<String> words) {
        words = words.stream().map(WordleDictionary::normalizeWord)
                              .filter(word -> word.length() == WordleRules.WORD_LENGTH)
                              .distinct()
                              .toList();

        return words;
    }
}