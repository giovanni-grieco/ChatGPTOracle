package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.*;
import it.uniroma3.chatGPT.GPT.chatCompletion.AzureGPT;
import it.uniroma3.chatGPT.GPT.prompt.PromptBuilder;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.GPT.score.Score;
import it.uniroma3.chatGPT.GPT.score.ScoreCalculator;
import it.uniroma3.chatGPT.GPT.prompt.Prompt;
import it.uniroma3.chatGPT.data.EntityType;
import it.uniroma3.chatGPT.utils.file.FileSaver;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InterrogaGpt implements Comando {

    @Override
    public void esegui(Application application) throws InterruptedException{

        List<Entity> entityList = new ArrayList<>(application.getEntities());
        List<Prompt> prompts = new ArrayList<>();

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

        List<Entity> filteredEntityList = new ArrayList<>();
        for (Entity e : entityList) {
            if (e.getType() == EntityType.values()[entityType])
                filteredEntityList.add(e);
        }
        System.out.print("Inserisci il numero di prompt positivi: ");
        int matchingEntityPromptsAmount = keyboardScanner.nextInt();
        System.out.print("Inserisci il numero di prompt negativi: ");
        int nonMatchingEntityPromptsAmount = keyboardScanner.nextInt();
        System.out.println("Numero di prompt positivi: " + matchingEntityPromptsAmount);
        System.out.println("Numero di prompt negativi: " + nonMatchingEntityPromptsAmount);
        System.out.print("Inserire tempo di attesa tra una query e l'altra in millisecondi: ");
        int tempoAttesa = keyboardScanner.nextInt();
        System.out.println("Tempo di attesa tra una query e l'altra in millisecondi: " + tempoAttesa);
        System.out.println("Creazione dei prompt...");
        //entità diverse fra loro

        PromptBuilder pb = new PromptBuilder(filteredEntityList, matchingEntityPromptsAmount, nonMatchingEntityPromptsAmount, true);

        pb.generateNonMatchingEntityPrompts(prompts);
        pb.generateMatchingEntityPrompts(prompts);

        System.out.println("Prompts size: " + prompts.size());
        System.out.println("Inizio interrogazione...");
        System.out.println("Tempo attesa stimato: " + (tempoAttesa * (matchingEntityPromptsAmount + nonMatchingEntityPromptsAmount)) / 1000 + " secondi");
        LLM gpt = new AzureGPT("You will be given 2 snippets of texts. You will have to answer whether the 2 texts are talking about the same entity, object or subject. Answer only with yes or no.");
        //String modello = "curie:ft-personal-2023-08-19-19-36-38";
        String modello = "gpt-3.5-turbo";
        List<GPTQuery> answers = gpt.processPrompts(prompts, modello, tempoAttesa);

        Score score = ScoreCalculator.calculateScore(answers);

        String results = score.toString();
        results += "positive prompts: " + matchingEntityPromptsAmount + "\n";
        results += "negative prompts: " + nonMatchingEntityPromptsAmount + "\n";
        results += "percentage of positive prompts: "+ matchingEntityPromptsAmount*100/(matchingEntityPromptsAmount+nonMatchingEntityPromptsAmount)+"\n";
        results += "model: "+modello+"\n";
        System.out.println(results);

        LocalDate now = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String fileName = application.getAppProperties().getDatasetFolders()[entityType] + "-" + now + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond();
        FileSaver.saveFile("./results/", fileName + ".txt", results+"\n\n"+modello);
        System.out.println("File saved as ./results/" + fileName + ".txt");
    }
}
