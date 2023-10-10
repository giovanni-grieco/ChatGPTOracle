package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.Prompt;
import it.uniroma3.chatGPT.GPT.PromptBuilder;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.data.Data;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.data.extraction.HTMLFilter;
import it.uniroma3.chatGPT.utils.CosineSimilarityText;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blocking implements Comando {

    private Map<Integer, List<Pair<Data, Data>>> blocks2MatchingEntityBlocks;
    private Map<Integer, List<Pair<Data, Data>>> blocks2NonMatchingEntityBlocks;


    public Blocking(){
        blocks2MatchingEntityBlocks = new HashMap<>();
        blocks2NonMatchingEntityBlocks= new HashMap<>();
    }

    private void addToBlock(int block, Pair<Data, Data> pairOfData, Map<Integer, List<Pair<Data, Data>>> map){
        if(map.containsKey(block)){
            map.get(block).add(pairOfData);
        }else{
            List<Pair<Data, Data>> promptList = new ArrayList<>();
            promptList.add(pairOfData);
            map.put(block, promptList);
        }
    }

    @Override
    public void esegui(Application application) throws InterruptedException, IOException {

        //itero su tutte le entità
        //prendo il loro compo title e di fatto faccio un n*(n-1)/2 confronti in cui faccio blocking di coppie di title.
        //li posizioni in una hashmap che chiave un integer che da la classe al blocco e come value una lista di coppie di title su cui fare l'interrogazione a gpt

        List<Entity> entityList = new ArrayList<>(application.getEntities());
        Map<Data, String> data2Title = new HashMap<>();
        for(Entity e : entityList){
            for(Data d : e.getData()){
                String title = HTMLFilter.getTitle(d.getTextData());
                data2Title.put(d, title);
            }
        }
        //faccio prima i casi di matching
        for(Entity e : entityList){
            System.out.println(e.getName());
            List<Pair<Data, Data>> dataPairsOfSameEntity = pairData(e);
            for(Pair<Data, Data> pairOfData: dataPairsOfSameEntity){
                String title1 = data2Title.get(pairOfData.getLeft());
                System.out.println(title1);
                String title2 = data2Title.get(pairOfData.getRight());
                System.out.println(title2);
                if(title1.isEmpty() || title2.isEmpty() || title1.isBlank() || title2.isBlank()){
                    continue;
                }
                double distanceBetweenTitles = CosineSimilarityText.apply(title1.toLowerCase(), title2.toLowerCase());
                System.out.println(distanceBetweenTitles);
                addToBlock((int)Math.floor(distanceBetweenTitles*100), pairOfData, blocks2MatchingEntityBlocks);
            }
        }

        System.out.println(blocks2MatchingEntityBlocks.keySet());
        for(int n : blocks2MatchingEntityBlocks.keySet()){
            System.out.println("Block: " + n);
            System.out.println("Prompt in block: " + blocks2MatchingEntityBlocks.get(n).size());
        }
        //faccio i casi di non matching
        List<Entity> tempEntityList = new ArrayList<>(entityList);
        for(Entity e: entityList){
            tempEntityList.remove(e);
            List<Data> entityData = e.getData();
            for(Entity notE : tempEntityList){
                List<Data> notEntityData = notE.getData();
                for(Data d1 : entityData){
                    for(Data d2 : notEntityData){
                        String title1 = data2Title.get(d1);
                        String title2 = data2Title.get(d2);
                        if(title1.isEmpty() || title2.isEmpty() || title1.isBlank() || title2.isBlank()){
                            continue;
                        }
                        double distanceBetweenTitles = CosineSimilarityText.apply(title1.toLowerCase(), title2.toLowerCase());
                        addToBlock((int)Math.floor(distanceBetweenTitles*100), Pair.of(d1,d2), blocks2NonMatchingEntityBlocks);
                    }
                }
            }
        }

        System.out.println(blocks2NonMatchingEntityBlocks.keySet());
        for(int n : blocks2NonMatchingEntityBlocks.keySet()){
            System.out.println("Block: " + n);
            System.out.println("Prompt in block: " + blocks2NonMatchingEntityBlocks.get(n).size());
        }


        String matchingEntitySheetFilename = "blocks.xlsx";
        String pathToFolder = "./spreadsheets/blocking/";
        Workbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Matching");
        XSSFRow row0 = sheet.createRow((short)0);
        XSSFRow row1 = sheet.createRow((short)1);
        for(int n : blocks2MatchingEntityBlocks.keySet()){
            row1.createCell(n).setCellValue(n);
            row0.createCell(n).setCellValue(blocks2MatchingEntityBlocks.get(n).size());
        }
        XSSFSheet nonMatchSheet = (XSSFSheet) workbook.createSheet("NonMatching");
        XSSFRow row0NonMatch = nonMatchSheet.createRow((short)0);
        XSSFRow row1NonMatch = nonMatchSheet.createRow((short)1);
        for(int n : blocks2NonMatchingEntityBlocks.keySet()){
            row1NonMatch.createCell(n).setCellValue(n);
            row0NonMatch.createCell(n).setCellValue(blocks2NonMatchingEntityBlocks.get(n).size());
        }
        /*XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 2, 10, 20);
        XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(org.apache.poi.xddf.usermodel.chart.LegendPosition.TOP_RIGHT);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.BOTTOM);
        bottomAxis.setTitle("Distance");
        XDDFValueAxis leftAxis = chart.createValueAxis(org.apache.poi.xddf.usermodel.chart.AxisPosition.LEFT);
        leftAxis.setTitle("Amount of prompts composed of a pair of titles with that distance");
        leftAxis.setCrosses(org.apache.poi.xddf.usermodel.chart.AxisCrosses.AUTO_ZERO);
        XDDFDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(0, 0, 0, 10));
        //XDDFNumericalDataSource<Double> ys1 =
        //https://github.com/ashrawan/apache-poi-chart-sample*/

        FileOutputStream fileOut = new FileOutputStream(pathToFolder+matchingEntitySheetFilename);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }

    public List<Pair<Data, Data>> pairData(Entity e){
        List<Pair<Data, Data>> prompts = new ArrayList<>();
        for(int i = 0; i<e.getData().size(); i++){
            for(int j = i+1; j<e.getData().size(); j++){
                prompts.add(Pair.of(e.getData().get(i), e.getData().get(j)));
            }
        }
        return prompts;
    }
}
