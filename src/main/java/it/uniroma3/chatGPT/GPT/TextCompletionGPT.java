package it.uniroma3.chatGPT.GPT;

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
public class TextCompletionGPT extends LLM{

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


    public GPTQuery processPrompt(String prompt, String modelName) throws GPTException {
        try {
            String answer;
            System.out.println("Answering...");
            int initTime = (int) System.currentTimeMillis();
            answer = answerQuestionCompletion(prompt, modelName);
            int endTime = (int) System.currentTimeMillis();
            return new GPTQuery(answer, modelName, prompt, endTime - initTime);
        } catch (Exception e) {
            throw new GPTException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Interroga il modello della OpenAI specificato fornendo una lista di prompt
     *
     * @param prompts     Una lista d'interrogazioni testuali da fare al modello
     * @param modelName   modello da interrogare
     * @param millisDelay Delay fra una richiesta e l'altra
     * @return Una lista di risposte ottenute dal modello
     */

    @Override
    public List<GPTQuery> processPrompts(List<String> prompts, String modelName, int millisDelay) throws InterruptedException {
        List<GPTQuery> outputs = new ArrayList<>();
        for (String prompt : prompts) {
            System.out.println("Asking: " + prompt);
            try {
                GPTQuery answer = this.processPrompt(prompt, modelName);
                outputs.add(answer);
                System.out.println("Answer: " + answer.getRisposta());
            } catch (GPTException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Skipping to next prompt...");
            }
            System.out.println("Waiting " + millisDelay + "ms\n");
            Thread.sleep(millisDelay);
        }
        return outputs;
    }
}