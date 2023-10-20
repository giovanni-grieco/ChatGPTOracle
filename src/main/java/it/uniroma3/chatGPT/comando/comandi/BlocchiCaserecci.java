package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.score.Score;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.comando.InterrogatoreGPTThread;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.EntityType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class BlocchiCaserecci implements Comando {

    private static final String[] blocksDiscriminants = {"Canon", "Nikon", "Panasonic"};
    //private static final String[] blocksDiscriminants = {"Nikon"};
    private final Map<String, Set<Entity>> blocksDiscriminantsMap;

    public BlocchiCaserecci(){
        this.blocksDiscriminantsMap = new HashMap<>();
        for(String discriminant : blocksDiscriminants){
            this.blocksDiscriminantsMap.put(discriminant, new HashSet<>());
        }
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException {

        List<Entity> entityListAllTypes = new ArrayList<>(application.getDataset().getEntities());
        List<Entity> entityList = new ArrayList<>();

        //filtriamo quelli di tipo 0 (fotocamere)

        for (Entity e : entityListAllTypes) {
            if (e.getType() == EntityType.CAMERA) {
                entityList.add(e);
            }
        }

        //filtriamo tutte le entità che hanno nel campo title il discriminante del blocco che vogliamo analizzare
        for (String blockDiscriminant : blocksDiscriminants) {
            for (Entity e : entityList) {
                for (Data d : e.getData()) {
                    if (belongToBlock(d.getTitle(), blockDiscriminant)) {
                        this.blocksDiscriminantsMap.get(blockDiscriminant).add(e);
                        break;
                    }
                }
            }
        }

        Workbook workbook = new XSSFWorkbook();
        for(String blockDiscriminant : blocksDiscriminants){
            Sheet sheet = workbook.createSheet(blockDiscriminant);
            List<Entity> inBlockEntities = new ArrayList<>(this.blocksDiscriminantsMap.get(blockDiscriminant));
            XSSFRow topRow = (XSSFRow) sheet.createRow(0);
            XSSFRow tpRow = (XSSFRow) sheet.createRow(1);
            XSSFRow tnRow = (XSSFRow) sheet.createRow(2);
            XSSFRow fpRow = (XSSFRow) sheet.createRow(3);
            XSSFRow fnRow = (XSSFRow) sheet.createRow(4);
            topRow.createCell(0).setCellValue("Percentuale positivi");
            tpRow.createCell(0).setCellValue("TP");
            tnRow.createCell(0).setCellValue("TN");
            fpRow.createCell(0).setCellValue("FP");
            fnRow.createCell(0).setCellValue("FN");
            //Stampa di prova
            System.out.println(blockDiscriminant +" entities: "+ inBlockEntities);

            int numeroDiPromptTotali = 2000;
            int percentualePositivi = 5;
            //Ho tentato di multithreaddare ma non funziona perché l'endpoint di Azure non riesce a gestire richieste concorrenti
            while (percentualePositivi != 100) {
                System.out.println("Creazione thread");
                InterrogatoreGPTThread t1 = new InterrogatoreGPTThread(application, EntityType.CAMERA, percentualePositivi, numeroDiPromptTotali, inBlockEntities, blockDiscriminant);
                t1.start();
                t1.join();
                Score score1 = t1.getFinalScore();
                topRow.createCell(t1.getPercentualePositivi()/5).setCellValue(t1.getPercentualePositivi() + "%");
                tpRow.createCell(t1.getPercentualePositivi()/5).setCellValue(score1.getTP());
                tnRow.createCell(t1.getPercentualePositivi()/5).setCellValue(score1.getTN());
                fpRow.createCell(t1.getPercentualePositivi()/5).setCellValue(score1.getFP());
                fnRow.createCell(t1.getPercentualePositivi()/5).setCellValue(score1.getFN());
                percentualePositivi += 5;
            }

        }
        //Aggiungiamo la data al nome
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        String dateAndTime = date +"_"+ time.getHour()+ "_"+ time.getMinute();
        FileOutputStream fileOut = new FileOutputStream("./spreadsheets/blocking/"+"blocchi"+"-"+dateAndTime+".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private boolean belongToBlock(String text, String blockDiscriminant) {
        String textLower = text.toLowerCase();
        String blockDiscriminantLower = blockDiscriminant.toLowerCase();
        return textLower.contains(blockDiscriminantLower);
    }

}
