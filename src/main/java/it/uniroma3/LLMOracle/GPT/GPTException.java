package it.uniroma3.LLMOracle.GPT;

public class GPTException extends Exception {
    public GPTException(String message, Throwable cause) {
        super(message, cause);
    }

    public GPTException(String message) {
        super(message);
    }

}
