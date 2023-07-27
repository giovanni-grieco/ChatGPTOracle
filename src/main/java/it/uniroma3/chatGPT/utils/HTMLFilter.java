package it.uniroma3.chatGPT.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HTMLFilter {
    public static String filter(String html, Iterable<String> tagsToRemove){
        Document doc = Jsoup.parse(html);
        removeSpecifiedTags(doc, tagsToRemove);
        removeHTMLAttributes(doc);
        removeEmptyTags(doc);
        return convert2Text(doc);
        //return doc.toString();
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

}
