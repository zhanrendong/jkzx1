package tech.tongyu.core.processor;


import javax.lang.model.element.Element;

public class ProcessingException extends Exception {
    Element element;

    public ProcessingException(Element element, String message, Object... args) {
        super(String.format(message, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
