package it.uniroma3.LLMOracle.data.extraction;

public class HTMLTemplateException extends Exception{
    public HTMLTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
    public HTMLTemplateException(String message) {
        super(message);
    }
}
