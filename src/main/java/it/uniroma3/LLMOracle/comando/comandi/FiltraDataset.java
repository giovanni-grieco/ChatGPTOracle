package it.uniroma3.LLMOracle.comando.comandi;

import it.uniroma3.LLMOracle.Application;
import it.uniroma3.LLMOracle.comando.Comando;
import it.uniroma3.LLMOracle.data.Data;
import it.uniroma3.LLMOracle.data.Entity;
import it.uniroma3.LLMOracle.data.extraction.HTMLFilter;
import it.uniroma3.LLMOracle.utils.file.FileSaver;

public class FiltraDataset implements Comando {

    @Override
    public void esegui(Application application) throws InterruptedException {
        String path = application.getAppProperties().getDatasetPath()+"/"+application.getAppProperties().getFilteredDataSetFolder();
        System.out.println("Files will be saved at: "+path);
        int filesAmount = 0;
        for(Entity e: application.getDataset().getEntities()){
            filesAmount += e.getData().size();
        }
        System.out.println("Files to filter: "+filesAmount);
        for(Entity e : application.getDataset().getEntities()){
            for(Data d : e.getData()){
                try {
                    String unfilteredText = d.getTextData();
                    String filteredText = HTMLFilter.filter(unfilteredText, HTMLFilter.DEFAULT_TO_REMOVE_TAGS);
                    FileSaver.saveFile(path+"/"+application.getAppProperties().getDatasetFolders()[e.getType().getTypeIndex()]+"/"+d.getDomain(), d.getId()+".html", filteredText);
                }catch(Exception ex){
                    ex.printStackTrace();
                    System.out.println("File not found: "+d.toFullPath());
                }
            }
        }
        System.out.println("Dataset filtered");
    }
}
