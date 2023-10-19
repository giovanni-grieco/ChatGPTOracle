package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.prompt.Prompt;
import it.uniroma3.chatGPT.GPT.prompt.PromptBuilder;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Blocco;
import it.uniroma3.chatGPT.data.BlockData;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityType;
import it.uniroma3.chatGPT.data.extraction.BlockDataExtractor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlocchiRoger implements Comando {

    Map<BlockData, Entity> blockDataEntityMap = new HashMap<>();



    @Override
    public void esegui(Application application) throws InterruptedException, IOException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        String datasetPath = datasetFolderPath+"/"+"blockpages_camera0_15.csv";
        BlockDataExtractor blockEE = new BlockDataExtractor(datasetPath, EntityType.CAMERA);
        List<BlockData> blockData;
        List<String> titles = new ArrayList<>();
        while(blockEE.hasNextBlock()){
            Blocco blocco = blockEE.nextBlock();
            blockData=blocco.makeDataList();
            for(BlockData bd : blockData){
                try {
                    String title = bd.getTitle();
                    if(title.isEmpty() || title.isBlank()) continue;
                    titles.add(title);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Trovati "+titles.size()+" titoli");
    }
}
