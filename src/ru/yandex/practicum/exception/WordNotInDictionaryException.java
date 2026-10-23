package ru.yandex.practicum.exception;

public class WordNotInDictionaryException extends InGameException {
    public WordNotInDictionaryException(String message) {
        super(message);
    }
}
