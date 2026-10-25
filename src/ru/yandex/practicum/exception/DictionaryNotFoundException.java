package ru.yandex.practicum.exception;

public class DictionaryNotFoundException extends ApplicationException {
    public DictionaryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
