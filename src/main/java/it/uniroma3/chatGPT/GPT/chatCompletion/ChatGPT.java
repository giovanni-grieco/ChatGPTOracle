package it.uniroma3.chatGPT.GPT.chatCompletion;
import it.uniroma3.chatGPT.GPT.LLM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Questa classe è l'implementazione dell'interfaccia LLM che permette di processare una lista di prompt usando l'API di ChatGPT di OpenAI.
 */
public class ChatGPT extends LLM {

    final String assistantContent;

    public ChatGPT(String apiKey, String assistantContent) {
        super(apiKey);
        this.assistantContent = assistantContent;
    }

    public String answerQuestionCompletion(String prompt, String modelName) throws IOException {
        String myToken = "Bearer " + " " + privateKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(CHAT_URL_API).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", myToken);
        conn.setRequestProperty("Content-Type", "application/json");
        // The request body
        String jsonBody = "{\"model\": \""+modelName+"\",\"messages\": [{\"role\": \"system\", \"content\": \"" + this.assistantContent + "\"},{ \"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
        conn.setDoOutput(true);
        conn.getOutputStream().write(jsonBody.getBytes());
        // Response from ChatGPT
        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).orElse("");
        return extractMessageFromJSONResponse(output,11);
    }

    static String extractMessageFromJSONResponse(String response, int startIndex) {
        int start = response.indexOf("content") + startIndex;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
