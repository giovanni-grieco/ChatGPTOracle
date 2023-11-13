package it.uniroma3.LLMOracle;

public class InvalidPropertiesException extends Exception{

    public InvalidPropertiesException(String message){
        super(message);
    }
    public InvalidPropertiesException(String message, Throwable cause){
        super(message,cause);
    }
}
