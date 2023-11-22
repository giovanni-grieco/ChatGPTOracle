package it.uniroma3.LLMOracle.GPT.chatCompletion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AzureGPT extends GPT {

    private final String endpoint = System.getenv("AZURE_ENDPOINT")+"/openai/deployments/gpt-35-turbo/chat/completions?api-version=2023-05-15";

    private final HttpClient httpClient;

    private Chat initChat = null;

    public AzureGPT(String assistantContent) {
        super(System.getenv("AZURE_GPT_API_KEY"), assistantContent);
        this.httpClient = HttpClient.newHttpClient();
    }
    public AzureGPT(String assistantContent, Chat chat) {
        super(System.getenv("AZURE_GPT_API_KEY"), assistantContent);
        this.initChat = chat;
        this.httpClient = HttpClient.newHttpClient();
    }

    public AzureGPT(String assistantContent, Chat initChat, String apiKey){
        super(apiKey, assistantContent);
        this.initChat = initChat;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String answerQuestionCompletion(String prompt, String modelName) throws IOException {
        try{
            /*HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("api-key", privateKey);
            conn.setRequestProperty("Content-Type", "application/json");*/
            String jsonBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"" + this.assistantContent + "\"}";
            if(this.initChat!=null){
                jsonBody += ","+initChat.toJson();
            }
            jsonBody+= ",{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            /*conn.setDoOutput(true);
            conn.getOutputStream().write(jsonBody.getBytes());*/
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URL(endpoint).toURI())
                    .header("api-key", privateKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            // Response from AzureGPT
            String outputRaw = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString()).body();
            return this.extractMessageFromJSONResponse(outputRaw);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String extractMessageFromJSONResponse(String response) {
        int startIndex = 10;
        int start = response.indexOf("content") + startIndex;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }


}
