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

    public static String[] models ={"text-davinci-003", "text-davinci-002", "text-davinci-001", "text-curie-001", "text-babbage-001", "text-ada-001", "davinci", "curie", "babbage", "ada"};
    private final static String URL_API = "https://api.openai.com/v1/completions";

    private final static String URL_AVAILABLE_MODELS = "https://api.openai.com/v1/models";
    //private static String model= "text-babbage-001";
    private static String privateKey = null;

    public ChatGPT() throws IOException {
        AppProperties proprieta = new AppProperties();
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

    public String buildQuestion(String field1, String field2) {
        StringBuilder sb = new StringBuilder("Say if ");

        sb.append("'"+field1+"'");
        sb.append(" and ");
        sb.append("'"+field2+"'");

        sb.append(" are the same real world object or attribute? Answer with 'yes' or 'no'.");

        return sb.toString();
    }
}