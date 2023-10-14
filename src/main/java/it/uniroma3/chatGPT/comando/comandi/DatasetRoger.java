package it.uniroma3.chatGPT.comando.comandi;

import it.uniroma3.chatGPT.Application;
import it.uniroma3.chatGPT.GPT.*;
import it.uniroma3.chatGPT.comando.Comando;
import it.uniroma3.chatGPT.GPT.Prompt;
import it.uniroma3.chatGPT.utils.FileRetriever;
import it.uniroma3.chatGPT.utils.FileSaver;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DatasetRoger implements Comando {
    @Override
    public void esegui(Application application) throws InterruptedException {
        String modello = "gpt-3.5-turbo";
        String pathToRogerDataset = "C:/Users/giovi/Documents/tirocinio dataset/oracle_ext_camera0_15.csv";
        try {
            File datasetFile = FileRetriever.getFile(pathToRogerDataset);
            String dataset = Files.readString(datasetFile.toPath());
            String[] lines = dataset.split("\n");
            List<Prompt> prompts = new ArrayList<>();
            System.out.print("Inserire numero dei primi n prompt da usare: ");
            Scanner keyboardScanner = new Scanner(System.in);
            int n = keyboardScanner.nextInt();
            for (int i =0; i<lines.length && i<n ; i++){
                String line = lines[i];
                try {
                    String[] splitLine = line.split(";");
                    String entityString1 = splitLine[0];
                    String entityString2 = splitLine[1];
                    String expectedAnswer = splitLine[2];
                    String promptString = "first: " + entityString1 + ". second: " + entityString2;
                    Prompt p = new ClassificationPrompt(promptString, Boolean.parseBoolean(expectedAnswer));
                    prompts.add(p);
                }catch(Exception e){
                    System.out.println("Error: "+e.getMessage());
                    System.out.println("Skipping to next prompt...");
                }
            }
            System.out.println(prompts);
            String chat = "{\"role\": \"user\", \"content\": \"first: Fujifilm Fujifilm FinePix S1 Black Digital 16800000 pixels 3.6 in 4.3 in 22.6 oz 5.2 in Storage 1 year(s) Quick Glance 1 year(s) 16400000 pixels $329.00 Newegg.com Newegg.com. second: Fujifilm FinePix S8500 8 5 5 4 Rs.19,999 Fujifilm US Write a review Digital Cameras Brand EAN: Exposure Compensation Connector Type Included Accessories Similar price Sensor Type Sensor Size Optical Zoom Focal Length Focal Length Equivalent to 35mm Digital Zoom Screen Size Canon SX50 HS\"}";
            chat += ",{\"role\":\"assistant\", \"content\": \"no\"}";
            chat += ",{\"role\": \"user\", \"content\": \"first: Nikon D5300 Body Only Hong Kong DSLR SD Card Nikon Grey 20.0MP Yes Grey 1 Day Universal Drop-shipping International Limited 2013 US$10 Million - US$50 Million 20.0MP 32191 3000 Piece/Pieces per Day Eastern Europe South America. second: Nikon D3300 DSLR Body Black 27,09 Nikon Supplied Battery USB Other Features Face Detection 460 gms Face Detection Dell Venue 8 Pro 32 GB Tablet Nov 01, Expert Review Cameras Price Nikon to high price 27 Delivery\"}";
            chat += ",{\"role\":\"assistant\", \"content\": \"no\"}";
            chat += ",{\"role\":\"user\", \"content\": \"first: Canon Eos 5d Mark iii (Body) Brightness Adjustment 1,96,196 22.30 Megapixels 2600mah Ebay Camera Cameras Cameras & Accessories SHOPCLUES Rs 196196 REDIFF Rs 196275 SHOPCLUES Rs 233996 PAYTM Rs 234776 INFIBEAM Rs 217995 SNAPDEAL. second: Memory Type: Sensor Details 5DIIIB Canon EOS 5D Mk III Body Weight Video Recording Format: Lens Mount 860g Image Format Full Frame - FX 22mp ALL-I H264 .MOV Canon EF CMOS Resolution: My personal.\"}";
            chat += ",{\"role\":\"assistant\", \"content\": \"yes\"}";
            chat += ",{\"role\":\"user\", \"content\": \"first: Nikon 16 30x Optical Zoom ebay Point & Shoot 35 (W) x 64 (H) x 110 (D) mm 16 ISO Rating 125 - 1600 Face Detection BSI CMOS 1/2.3 inch Max Resolution 4608 x 3456 Pixels 3 inch TFT LCD with anti-reflection coating 921K Focal Length Auto Focus Maximum Shutter Speed Minimum Shutter Speed Metering Flash Range Nikon Coolpix S9700. second: 1/2000 sec Max aperture (wide) 3.7 1cm Shutter speed min Shutter speed max ISO min ISO max Viewfinder type Screen size (inches) File formats Colour 228 16 CMOS 1 / 2.3 inch 4608 x 3456 30 750 25 Yes Max aperture (tele) 6.4 Minimum focus distance Image stabilisation 30 White Nikon 229.00 PXW9889 Nikon Coolpix S9700\"}";
            chat += ",{\"role\":\"assistant\", \"content\": \"yes\"}";
            LLM linkerLLM = new AzureGPT("You will be given 2 snippets of texts. You will have to answer whether the 2 texts are talking about the same entity, object or subject. Answer only with yes or no.");
            List<GPTQuery> answers = linkerLLM.processPrompts(prompts, modello, 0);
            String results = ScoreCalculator.calculateScore(answers).toString();
            System.out.println(results);

            LocalDate now = LocalDate.now();
            LocalTime nowTime = LocalTime.now();
            String fileName = "datasetRoger" + "-" + now + "_" + nowTime.getHour() + "-" + nowTime.getMinute() + "-" + nowTime.getSecond();
            FileSaver.saveFile("./results/", fileName + ".txt", results+"\n\n"+modello);
            System.out.println("File saved as ./results/" + fileName + ".txt");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
