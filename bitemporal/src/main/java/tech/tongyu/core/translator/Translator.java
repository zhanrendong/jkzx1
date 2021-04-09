package tech.tongyu.core.translator;

public interface Translator<T> {
    T translate(String in);
}
