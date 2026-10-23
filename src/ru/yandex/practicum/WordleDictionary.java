package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private final List<String> words;

    private final Set<String> wordSet;

    private final PrintWriter logWriter;

    public WordleDictionary(List<String> words,  PrintWriter logWriter) {
        this.words = words;
        this.logWriter = logWriter;
        this.wordSet = new TreeSet<>(words);
    }

    public int size() {
        return words.size();
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }

    public String getWordByNumber(int number) {
        return words.get(number);
    }

    public static String normalizeWord(String word) {
        return word.toLowerCase().trim().replace('ё', 'е');
    }

    public boolean contains(String word) {
        return wordSet.contains(word);
    }
}