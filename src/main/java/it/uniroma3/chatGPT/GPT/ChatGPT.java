package it.uniroma3.chatGPT.GPT;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Questa classe è l'implementazione dell'interfaccia LLM che permette di processare una lista di prompt usando l'API di ChatGPT di OpenAI.
 *
 */
public class ChatGPT extends LLM{

    public ChatGPT (String apiKey) {
        super(apiKey);
    }

    /**
     * Questo metodo processa una lista di prompt usando l'API di ChatGPT di OpenAI.
     * A differenza del metodo processPrompts nella classe TextCompletionGPT, qui la lista dei prompts appartengono a una stessa conversazione.
     * Quindi i vari prompts verranno messi in sequenza e saranno messaggi di una chat. Per iniziare una nuova chat è necessaria una nuova istanza di questa classe.
     * @param prompts     Una lista d'interrogazioni testuali da fare al modello
     * @param modelName   modello da interrogare
     * @param millisDelay Delay fra una richiesta e l'altra
     * @return Una lista di risposte ottenute dal modello
     * @throws InterruptedException se il thread viene interrotto mentre attende una risposta dal server
     */
    @Override
    public List<GPTQuery> processPrompts(List<String> prompts, String modelName, int millisDelay) throws InterruptedException {
        List<GPTQuery> gptQueries = new ArrayList<>();
        String myToken = "Bearer " + " " + privateKey;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(CHAT_URL_API).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", myToken);
            conn.setRequestProperty("Content-Type", "application/json");

            // The request body
            String jsonBody ="{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},{ \"role\": \"user\", \"content\": \"Hello!\"}]}";
            System.out.println(jsonBody);
            conn.setDoOutput(true);
            conn.getOutputStream().write(jsonBody.getBytes());
            // Response from ChatGPT
            String output = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().reduce((a, b) -> a + b).orElse("");
            System.out.println(output);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gptQueries;
    }

    public static String extractMessageFromJSONResponse(String response) {
        int start = response.indexOf("content")+ 11;

        int end = response.indexOf("\"", start);

        return response.substring(start, end);

    }
}
