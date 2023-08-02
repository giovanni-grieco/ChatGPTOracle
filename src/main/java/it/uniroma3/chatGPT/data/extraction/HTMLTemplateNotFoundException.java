package it.uniroma3.chatGPT.data.extraction;

public class HTMLTemplateNotFoundException extends Exception{
    public HTMLTemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTMLTemplateNotFoundException(String message) {
        super(message);
    }
}
