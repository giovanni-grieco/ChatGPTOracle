package it.uniroma3.LLMOracle.GPT.chatCompletion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Questa classe è l'implementazione dell'interfaccia LLM che permette di processare una lista di prompt usando l'API di ChatGPT di OpenAI.
 */
public class ChatGPT extends GPT {

    private Chat initChat = null;

    public ChatGPT(String assistantContent) {
        super(System.getenv("OPENAI_API_KEY"), assistantContent);
        this.initChat = null;
    }

    public ChatGPT(String assistantContent, Chat initChat) {
        super(System.getenv("OPENAI_API_KEY"), assistantContent);
        this.initChat = initChat;
    }

    public ChatGPT(String assistantContent, Chat initChat, String apiKey){
        super(apiKey, assistantContent);
        this.initChat = initChat;
    }

    @Override
    public String answerQuestionCompletion(String prompt, String modelName) throws IOException {
        String myToken = "Bearer " + " " + privateKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(CHAT_URL_API).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", myToken);
        conn.setRequestProperty("Content-Type", "application/json");
        // The request body
        String jsonBody = "{\"model\": \""+modelName+"\",\"messages\": [{\"role\": \"system\", \"content\": \"" + this.assistantContent + "\"}"+
                (this.initChat!=null ? ","+this.initChat.toJson() : "")
                +",{ \"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
        conn.setDoOutput(true);
        conn.getOutputStream().write(jsonBody.getBytes());
        // Response from ChatGPT
        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).orElse("");
        return extractMessageFromJSONResponse(output);
    }

    @Override
    protected String extractMessageFromJSONResponse(String response) {
        int startIndex = 11;
        int start = response.indexOf("content") + startIndex;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
