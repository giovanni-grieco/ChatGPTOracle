package it.uniroma3.chatGPT;

public class InvalidPropertiesException extends Exception{

    public InvalidPropertiesException(String message){
        super(message);
    }
    public InvalidPropertiesException(String message, Throwable cause){
        super(message,cause);
    }
}
