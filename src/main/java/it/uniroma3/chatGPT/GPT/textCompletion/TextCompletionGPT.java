package it.uniroma3.chatGPT.GPT.textCompletion;

import it.uniroma3.chatGPT.GPT.LLM;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Questa classe implementa l'interfaccia LLM e permette di interrogare il modello di OpenAI per ottenere una risposta a un prompt. Utilizza l'API legacy di OpenAI.
 */
public class TextCompletionGPT extends LLM {

    public TextCompletionGPT(String apiKey) {
        super(apiKey);
    }

    public List<String> availableOpenAiModels() throws IOException {
        List<String> models = new ArrayList<>();
        String myToken = "Bearer " + " " + privateKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_AVAILABLE_MODELS).openConnection();
        conn.setRequestProperty("Authorization", myToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");
        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).orElse("");
        JSONObject obj = new JSONObject(output);
        JSONArray dataArray = obj.getJSONArray("data");

        for (Object JSONObj : dataArray) {
            JSONObject elem = (JSONObject) JSONObj;
            models.add(elem.get("id").toString());
        }
        return models;
    }

    @Override
    public String answerQuestionCompletion(String text, String model) throws IOException {

        String myToken = "Bearer " + " " + privateKey;
        // È necessario creare una connessione per risposta? Non basta una connessione a per lista di prompt?
        HttpURLConnection conn = (HttpURLConnection) new URL(COMPLETION_URL_API).openConnection();
        conn.setRequestProperty("Authorization", myToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");
        JSONObject data = new JSONObject();
        data.put("model", model);
        data.put("prompt", text);
        data.put("max_tokens", 10); // andrebbero diminuiti i max tokens, tanto deve dire solo si o no e rischieremmo di meno
        data.put("temperature", 0.15);
        conn.setDoOutput(true);
        conn.getOutputStream().write(data.toString().getBytes());
        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).orElse("");
        return new JSONObject(output).getJSONArray("choices").getJSONObject(0).getString("text");
    }
}