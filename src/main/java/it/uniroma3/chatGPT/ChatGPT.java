package it.uniroma3.chatGPT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public class ChatGPT {

    private static String[] models ={"text-davinci-003", "text-davinci-002", "text-davinci-001", "text-curie-001", "text-babbage-001", "text-ada-001", "davinci", "curie", "babbage", "ada"};
    private static String URL_API = "https://api.openai.com/v1/completions";

    private static String URL_AVAILABLE_MODELS = "https://api.openai.com/v1/models";
    //private static String model= "text-babbage-001";
    private static String privateKey= "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";


    private static List<String> availableOpenAiModels() throws IOException {
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

    public static String answerQuestion(String text, String model) throws Exception {

        String myToken = "Bearer "+" "+privateKey;

        HttpURLConnection conn = (HttpURLConnection) new URL(URL_API).openConnection();
        conn.setRequestProperty("Authorization",myToken);
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestMethod("GET");

        JSONObject data = new JSONObject();
        data.put("model", model);
        data.put("prompt", text);
        data.put("max_tokens",700);
        data.put("temperature", 0);
        conn.setDoOutput(true);
        conn.getOutputStream().write(data.toString().getBytes());


        String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).get();

        return new JSONObject(output).getJSONArray("choices").getJSONObject(0).getString("text");

    }

    public static String buildQuestion(String field1, String field2) {
        StringBuilder sb = new StringBuilder("Say if ");

        sb.append("'"+field1+"'");
        sb.append(" and ");
        sb.append("'"+field2+"'");

        sb.append(" are the same real world object or attribute? Answer with 'yes' or 'no'.");

        return sb.toString();
    }


    public static void main(String[] args) throws Exception {
        System.out.println("All available models:");
        for(String model : availableOpenAiModels()){
            System.out.println(model);
        }
        //Usiamo una lista di modelli più piccola presa dal sito della OpenAI
        for(String modello : models){
            System.out.println("----");
            System.out.println("Model used-> "+modello);
            long initTime = System.currentTimeMillis();
            try {
                System.out.println(answerQuestion(buildQuestion("D. Gallinari", "Danilo Gallinari"), modello));
            }catch(Exception e){
             e.printStackTrace();
             System.out.println("Utilizzo del modello \""+modello+"\" fallito");
            }
            System.out.println("ChatGPT Query time:"+(System.currentTimeMillis()-initTime)+" ms");
            Thread.sleep(15000); // altrimenti si raggiunge un overflow di richieste e da errore 429
        }
    }
}