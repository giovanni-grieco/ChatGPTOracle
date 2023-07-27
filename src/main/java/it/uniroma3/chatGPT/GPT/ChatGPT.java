package it.uniroma3.chatGPT.GPT;

import it.uniroma3.chatGPT.AppProperties;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ChatGPT {

    public static String[] models ={"text-davinci-003", "text-davinci-002", "text-davinci-001", "text-curie-001", "text-babbage-001", "text-ada-001", "davinci", "curie", "babbage", "ada"};
    private final static String URL_API = "https://api.openai.com/v1/completions";

    private final static String URL_AVAILABLE_MODELS = "https://api.openai.com/v1/models";
    //private static String model= "text-babbage-001";
    private static String privateKey = null;

    public ChatGPT(AppProperties proprieta) throws IOException {
        privateKey = proprieta.getAPIKey();
    }

    public List<String> availableOpenAiModels() throws IOException {
        List<String> models = new ArrayList<>();
        String myToken = "Bearer "+" "+privateKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_AVAILABLE_MODELS).openConnection();
        conn.setRequestProperty("Authorization",myToken);
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestMethod("GET");
        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).get();
        JSONObject obj = new JSONObject(output);
        JSONArray dataArray = obj.getJSONArray("data");

        for(Object JSONObj : dataArray){
            JSONObject elem = (JSONObject) JSONObj;
            models.add(elem.get("id").toString());
        }
        return models;
    }

    public String answerQuestion(String text, String model) throws Exception {

        String myToken = "Bearer "+" "+privateKey;
        // E' necessario creare una connessione a risposta???? Non basta una connessione a per lista di prompt?
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_API).openConnection();
        conn.setRequestProperty("Authorization",myToken);
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestMethod("GET");

        JSONObject data = new JSONObject();
        data.put("model", model);
        data.put("prompt", text);
        data.put("max_tokens",700); // andrebbero diminuiti i max tokens, tanto deve dire solo si o no e rischieremmo di meno
        data.put("temperature", 0);
        conn.setDoOutput(true);
        conn.getOutputStream().write(data.toString().getBytes());

        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).get();

        return new JSONObject(output).getJSONArray("choices").getJSONObject(0).getString("text");
    }

    public GPTQuery processPrompt(String prompt, String modelName){
        try {
            String answer;
            System.out.println("Answering: "+prompt);
            int initTime = (int) System.currentTimeMillis();
            answer = answerQuestion(prompt, modelName);
            int endTime = (int) System.currentTimeMillis();
            return new GPTQuery(answer, modelName, prompt, endTime-initTime);
        } catch (Exception e) {
            return new GPTQuery("Exception raised->"+e.getMessage(), modelName, prompt, 0);
        }
    }

    /**
     * Interroga il modello della OpenAI specificato fornendo una lista di prompt
     * @param prompts Una lista d'interrogazioni testuali da fare al modello
     * @param modelName modello da interrogare
     * @param millisDelay Delay fra una richiesta e l'altra
     * @return Una lista di risposte ottenute dal modello
     */
    public List<GPTQuery> processPrompts(List<String> prompts, String modelName , int millisDelay) throws InterruptedException {
        List<GPTQuery> outputs = new ArrayList<>();
        for(String prompt : prompts){
            System.out.println("Asking: "+prompt);
            outputs.add(this.processPrompt(prompt, modelName));
            System.out.println("Waiting "+millisDelay+"ms");
            Thread.sleep(millisDelay);
        }
        return outputs;
    }
}