package it.uniroma3.chatGPT.data.extraction;

import it.uniroma3.chatGPT.GPT.ChatGPT;
import it.uniroma3.chatGPT.data.Entity;
import it.uniroma3.chatGPT.utils.FileRetriever;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HTMLFilter {

    public static final Iterable<String> DEFAULT_TAGS = List.of("style","script","head","meta","img","link");

    public static Iterable<String> filterEntityData(Iterable<Entity> entities) {
        List<String> prompts = new ArrayList<>();
        for(Entity firstEntity : entities) {
            try {
                for (int i = 0; i < firstEntity.getData().size(); i++) {
                    //l'unico di cui ho stabilito un template è ebay
                    if (firstEntity.getData().get(i).getDomain().equals("www.ebay.com")) {
                        String html = Files.readString(firstEntity.getData().get(i).toFullPath());
                        String htmlFiltratoA = HTMLFilter.filter(html, HTMLFilter.DEFAULT_TAGS, firstEntity.getData().get(i).getDomain());
                        for (int j = i + 1; j < firstEntity.getData().size(); j++) {
                            if (firstEntity.getData().get(j).getDomain().equals("www.ebay.com")) {
                                String html1 = Files.readString(firstEntity.getData().get(j).toFullPath());
                                String htmlFiltratoB = HTMLFilter.filter(html1, HTMLFilter.DEFAULT_TAGS, firstEntity.getData().get(j).getDomain());
                                prompts.add(ChatGPT.PromptBuilder.buildPromptTwoSnippetSameEntity(htmlFiltratoA, htmlFiltratoB));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                System.out.println(firstEntity.toString());
            }
        }
        return prompts;
    }

    private static String filter(String html, Iterable<String> tagsToRemove){
        Document doc = Jsoup.parse(html);
        removeSpecifiedTags(doc, tagsToRemove);
        removeHTMLAttributes(doc);
        removeEmptyTags(doc);
        //return convert2Text(doc);
        return doc.text();
    }

    public static String filter(String html, Iterable<String> tagsToRemove, String templateName) throws IOException {
        String partialFilter = filter(html, tagsToRemove);
        ContentRichSection section = loadContentRichSectionFromTemplate(templateName);
        return partialFilter.substring(partialFilter.indexOf(section.getStart())+section.getStart().length(),partialFilter.indexOf(section.getEnd())); // CHE ODIO!!!
    }

    private static String convert2Text(Document doc){
        //sostituisce i div con \n
        doc.select("div").append("\\n");
        return doc.select("body").text().replaceAll("\\\\n","\n");
    }

    private static void removeSpecifiedTags(Document doc, Iterable<String> tagsToRemove){
        //rimuove i tag specificati
        for(String tag : tagsToRemove){
            doc.select(tag).remove();
        }
    }

    private static void removeEmptyTags(Document doc){
        //rimuove tutti i tag vuoti
        for (Element element : doc.select("*")) {
            if (!element.hasText() && element.isBlock()) {
                element.remove();
            }
        }
    }
    private static void removeHTMLAttributes(Document doc){
        //rimuove tutti gli attributi dei tag HTML
        Elements el = doc.getAllElements();
        for (Element e : el) {
            List<String> attToRemove = new ArrayList<>();
            Attributes at = e.attributes();
            for (Attribute a : at) {
                attToRemove.add(a.getKey());
            }
            for(String att : attToRemove) {
                e.removeAttr(att);
            }
        }
    }

    private static ContentRichSection loadContentRichSectionFromTemplate(String templateName) throws IOException {
        File contentRichTemplateInformation = FileRetriever.getFile("contentRichTemplate.txt");
        String contentRichTemplate = Files.readString(contentRichTemplateInformation.toPath());
        String[] contentRichTemplateLines = contentRichTemplate.split("\n");
        for(String line : contentRichTemplateLines){
            if(line.startsWith(templateName)){
                String[] templateDetails = line.split("\\|");
                return new ContentRichSection(templateName,templateDetails[1], templateDetails[2]);
            }
        }
        throw new IOException("Template '"+templateName +"'not found in contentRichTemplate.txt");
    }
}
