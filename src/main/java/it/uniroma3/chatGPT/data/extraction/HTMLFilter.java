package it.uniroma3.chatGPT.data.extraction;

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

    public static final Iterable<String> DEFAULT_TAGS = List.of("style", "script", "head", "meta", "img", "link");

    private static final ContentRichSection contentRichSection = new ContentRichSection(null, null, null);

    public static String filterTemplate(String html, Iterable<String> tagsToRemove, String templateName) throws IOException {
        String partialFilter = filter(html, tagsToRemove);
        loadContentRichSectionFromTemplate(templateName, FileRetriever.getFile("contentRichTemplate.txt"));
        String fromStart = partialFilter.substring(partialFilter.indexOf(contentRichSection.getStart()) + contentRichSection.getStart().length());
        return fromStart.substring(0, partialFilter.indexOf(contentRichSection.getEnd()));
    }

    private static String filter(String html, Iterable<String> tagsToRemove) {
        Document doc = Jsoup.parse(html);
        removeSpecifiedTags(doc, tagsToRemove);
        removeHTMLAttributes(doc);
        removeEmptyTags(doc);
        //return convert2Text(doc);
        return doc.text();
    }

    private static String convert2Text(Document doc) {
        //sostituisce i div con \n
        doc.select("div").append("\\n");
        return doc.select("body").text().replaceAll("\\\\n", "\n");
    }

    private static void removeSpecifiedTags(Document doc, Iterable<String> tagsToRemove) {
        //rimuove i tag specificati
        for (String tag : tagsToRemove) {
            doc.select(tag).remove();
        }
    }

    private static void removeEmptyTags(Document doc) {
        //rimuove tutti i tag vuoti
        for (Element element : doc.select("*")) {
            if (!element.hasText() && element.isBlock()) {
                element.remove();
            }
        }
    }

    private static void removeHTMLAttributes(Document doc) {
        //rimuove tutti gli attributi dei tag HTML
        Elements el = doc.getAllElements();
        for (Element e : el) {
            List<String> attToRemove = new ArrayList<>();
            Attributes at = e.attributes();
            for (Attribute a : at) {
                attToRemove.add(a.getKey());
            }
            for (String att : attToRemove) {
                e.removeAttr(att);
            }
        }
    }

    private static void loadContentRichSectionFromTemplate(String templateName, File contentRichTemplateInformation) throws IOException {
        String contentRichTemplate = Files.readString(contentRichTemplateInformation.toPath());
        String[] contentRichTemplateLines = contentRichTemplate.split("\n");
        for (String line : contentRichTemplateLines) {
            if (line.startsWith(templateName)) {
                String[] templateDetails = line.split("-");
                contentRichSection.setStart(templateDetails[1]);
                contentRichSection.setEnd(templateDetails[2]);
                contentRichSection.setTemplateName(templateName);
                return;
            }
        }
        throw new IOException("Template '" + templateName + "'not found in contentRichTemplate.txt");
    }
}
