package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlocchiRoger implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException, IOException {
        String datasetFolderPath = application.getAppProperties().getDatasetPath();
        String datasetPath = datasetFolderPath+"/"+"blockpages_camera0_15.csv";
        BlockDataExtractor blockEE = new BlockDataExtractor(datasetPath, EntityType.CAMERA);
        List<? extends AbstractData> blockData;
        while(blockEE.hasNextBlock()){
            Blocco blocco = blockEE.nextBlock();
            System.out.println(blocco);
            blockData=blocco.makeDataList();
        }


    }
}
