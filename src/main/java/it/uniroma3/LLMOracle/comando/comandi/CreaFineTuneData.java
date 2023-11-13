package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.GPT.fineTuning.JSONLineGenerator;
import it.uniroma3.LLMOracle.GPT.prompt.Prompt;
import it.uniroma3.LLMOracle.GPT.prompt.PromptBuilder;
import it.uniroma3.LLMOracle.comando.Comando;
import it.uniroma3.LLMOracle.data.Entity;
import it.uniroma3.LLMOracle.utils.file.FileSaver;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreaFineTuneData implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException {

        List<Entity> entityList = new ArrayList<>(application.getDataset().getEntities());
        List<Prompt> prompts = new ArrayList<>();

        Scanner keyboardScanner = new Scanner(System.in);
        System.out.print("Inserisci il numero di prompt positivi: ");
        int matchingEntityPromptsAmount = keyboardScanner.nextInt();
        System.out.print("Inserisci il numero di prompt negativi: ");
        int nonMatchingEntityPromptsAmount = keyboardScanner.nextInt();
        System.out.println("Numero di prompt positivi: " + matchingEntityPromptsAmount);
        System.out.println("Numero di prompt negativi: " + nonMatchingEntityPromptsAmount);
        System.out.println("Creazione dei prompt...");
        //entità diverse fra loro

        PromptBuilder pb = new PromptBuilder(entityList, matchingEntityPromptsAmount, nonMatchingEntityPromptsAmount, false);

        pb.generateNonMatchingEntityPrompts(prompts);
        pb.generateMatchingEntityPrompts(prompts);

        System.out.println("Prompts size: " + prompts.size());
        List<String> filteredPrompts = new ArrayList<>();

        for (Prompt prompt : prompts) {
            System.out.println(prompt);
            filteredPrompts.add(prompt.getTextPrompt().replaceAll("\"", "''"));
        }

        for (String prompt : filteredPrompts) {
            System.out.println(prompt);
        }

        String JSONLines = JSONLineGenerator.generateJSONLines(filteredPrompts);
        System.out.println(JSONLines);
        FileSaver.saveFile("./ftDataSet/", "prompts.jsonl", JSONLines);
    }
}
