package it.uniroma3.chatGPT.GPT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AzureGPT extends ChatGPT{

    private final String endpoint = System.getenv("AZURE_ENDPOINT")+"/openai/deployments/gpt-35-turbo/chat/completions?api-version=2023-05-15";

    private String initChat = null;

    public AzureGPT(String assistantContent) {
        super(System.getenv("AZURE_GPT_API_KEY"), assistantContent);
    }
    public AzureGPT(String assistantContent, String chat) {
        super(System.getenv("AZURE_GPT_API_KEY"), assistantContent);
        this.initChat = chat;
    }

    @Override
    public String answerQuestionCompletion(String prompt, String modelName) throws IOException {
        try{
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("api-key", privateKey);
            conn.setRequestProperty("Content-Type", "application/json");
            String jsonBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"" + this.assistantContent + "\"}";
            if(this.initChat!=null){
                jsonBody += ","+initChat;
            }
            jsonBody+= ",{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            conn.setDoOutput(true);
            conn.getOutputStream().write(jsonBody.getBytes());
            // Response from AzureGPT
            String outputRaw = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).orElse("");
            return extractMessageFromJSONResponse(outputRaw,10);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}