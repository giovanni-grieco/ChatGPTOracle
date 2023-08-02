package it.uniroma3.chatGPT.GPT;

import it.uniroma3.chatGPT.AppProperties;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class ChatGPT {

    //public static String[] models ={"text-davinci-003", "text-davinci-002", "text-davinci-001", "text-curie-001", "text-babbage-001", "text-ada-001", "davinci", "curie", "babbage", "ada"};
    private final static String COMPLETION_URL_API = "https://api.openai.com/v1/completions";

    private final static String CHAT_URL_API = "https://api.openai.com/v1/chat/completions";

    private final static String URL_AVAILABLE_MODELS = "https://api.openai.com/v1/models";
    //private static String model= "text-babbage-001";
    private static String privateKey = null;

    public ChatGPT(String apiKey) throws IOException {
        privateKey = apiKey;
    }

    public List<String> availableOpenAiModels() throws IOException {
        List<String> models = new ArrayList<>();
        String myToken = "Bearer " + " " + privateKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_AVAILABLE_MODELS).openConnection();
        conn.setRequestProperty("Authorization", myToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");
        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).get();
        JSONObject obj = new JSONObject(output);
        JSONArray dataArray = obj.getJSONArray("data");

        for (Object JSONObj : dataArray) {
            JSONObject elem = (JSONObject) JSONObj;
            models.add(elem.get("id").toString());
        }
        return models;
    }

    public String answerQuestionCompletion(String text, String model) throws Exception {

        String myToken = "Bearer " + " " + privateKey;
        // E' necessario creare una connessione a risposta???? Non basta una connessione a per lista di prompt?
        HttpURLConnection conn = (HttpURLConnection) new URL(COMPLETION_URL_API).openConnection();
        conn.setRequestProperty("Authorization", myToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");

        JSONObject data = new JSONObject();
        data.put("model", model);
        data.put("prompt", text);
        data.put("max_tokens", 700); // andrebbero diminuiti i max tokens, tanto deve dire solo si o no e rischieremmo di meno
        data.put("temperature", 0.2);
        conn.setDoOutput(true);
        conn.getOutputStream().write(data.toString().getBytes());

        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).get();

        return new JSONObject(output).getJSONArray("choices").getJSONObject(0).getString("text");
    }

    // not working
    /*public String answerQuestionChat(String text, String model) throws Exception{
        String myToken = "Bearer "+" "+privateKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(COMPLETION_URL_API).openConnection();
        conn.setRequestProperty("Authorization",myToken);
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestMethod("GET");

        *//*JSONObject data = new JSONObject();
        data.put("model", model);
        data.put("messages", new JSONArray().put(new JSONObject().put("role","system").put("content", "You are a system capable of determine if a two snippets of text talk about the same object, entity or attribute. You answer only with yes or no.")).put(new JSONObject().put("role","user").put("content", text)));
        System.out.println(data);*//*
        String dataAMano = "{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"system\",\"content\": \"You are a helpful assistant.\"}, {\"role\": \"user\",\"content\": \"Hello!\"}]}";
        System.out.println(dataAMano);
        conn.setDoOutput(true);
        conn.getOutputStream().write(dataAMano.getBytes());
        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).get();
        return new JSONObject(output).getJSONArray("choices").getJSONObject(0).getJSONObject("messages").getJSONObject("content").toString();
    }*/

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
    public List<GPTQuery> processPrompts(List<String> prompts, String modelName, int millisDelay) throws InterruptedException {
        List<GPTQuery> outputs = new ArrayList<>();
        for (String prompt : prompts) {
            System.out.println("Asking: " + prompt);
            try {
                GPTQuery answer = this.processPrompt(prompt, modelName);
                outputs.add(answer);
                System.out.println("Answer: " + answer.getRisposta());
            } catch (GPTException e) {
                e.printStackTrace();
                System.out.println("Skipping to next prompt...");
            }
            System.out.println("Waiting " + millisDelay + "ms\n");
            Thread.sleep(millisDelay);
        }
        return outputs;
    }

    public static class PromptBuilder {

        public static String buildPromptTwoSnippets(String webPageA, String webPageB) {
            String prompt = "You will be given 2 snippets of text talking about an entity, object or attribute.\n";
            prompt += "First: " + webPageA + ".\n";
            prompt += "Second: " + webPageB + ".\n";
            prompt += "Are the 2 snippets talking about the same entity, object or attribute? Answer with 'yes' or 'no'";
            return prompt;
        }

    }
}