package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.GPT.GPTQuery;
import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.Score;
import it.uniroma3.chatGPT.data.ScoreCalculator;
import it.uniroma3.chatGPT.utils.FileSaver;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InterrogaGpt implements Comando {

    @Override
    public void esegui(Application application) throws InterruptedException {

        List<Entity> entityList = new ArrayList<>(application.getEntities());
        List<String> prompts = new ArrayList<>();

        Scanner keyboardScanner = new Scanner(System.in);

        int entityType = -1;
        do {
            System.out.println("Inserisci il tipo di entità da usare nell'interrogazione");
            for (int i = 0; i < application.getEntityTypes(); i++) {
                System.out.println(i + " - " + application.getAppProperties().getDatasetFolders()[i]);
            }
            entityType = keyboardScanner.nextInt();
        } while (entityType < 0 || entityType > application.getEntityTypes());

        System.out.println("Selezionato il tipo di entità: " + application.getAppProperties().getDatasetFolders()[entityType]);
        // ECCO L'ERRORE, rimuovo un elemento dalla lista mentre viene usato un iteratore!!!!
        List<Entity> filteredEntityList = new ArrayList<>();
        for (Entity e : entityList) {
            if (e.getType() == entityType)
                filteredEntityList.add(e);
        }
        System.out.print("Inserisci il numero di prompt positivi: ");
        int matchingEntityPromptsAmount = keyboardScanner.nextInt();
        System.out.print("Inserisci il numero di prompt negativi: ");
        int nonMatchingEntityPromptsAmount = keyboardScanner.nextInt();
        System.out.println("Numero di prompt positivi: " + matchingEntityPromptsAmount);
        System.out.println("Numero di prompt negativi: " + nonMatchingEntityPromptsAmount);
        System.out.println("Creazione dei prompt...");
        //entità diverse fra loro

        PromptBuilder pb = new PromptBuilder(filteredEntityList, matchingEntityPromptsAmount, nonMatchingEntityPromptsAmount, true);

        pb.generateNonMatchingEntityPrompts(prompts);
        pb.generateMatchingEntityPrompts(prompts);

        System.out.println("Prompts size: " + prompts.size());

        ChatGPT gpt = new ChatGPT(application.getAppProperties().getAPIKey());
        List<GPTQuery> answers = gpt.processPrompts(prompts, "curie:ft-personal-2023-08-19-19-36-38", 10);

        Score score = ScoreCalculator.calculateScore(answers, nonMatchingEntityPromptsAmount);

        String results = score.toString();
        System.out.println(results);

        LocalDate now = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String fileName = application.getAppProperties().getDatasetFolders()[entityType] + "-" + now + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond();
        FileSaver.saveFile("./results/", fileName + ".txt", results);
        System.out.println("File saved as ./results/" + fileName + ".txt");
    }
}
