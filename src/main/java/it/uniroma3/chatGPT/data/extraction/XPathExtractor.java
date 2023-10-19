package it.uniroma3.chatGPT.data.extraction;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

public class XPathExtractor {

    private final String xpath;
    private String xml;

    public XPathExtractor(String xpath, String xml) {
        this.xpath = xpath;
        this.xml = xml;
    }

    public String extract() {
        try {
            this.xml = xml.replace("&nbsp;", "&#160;");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(this.xml)));
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression expression = xPath.compile(this.xpath);
            String result = (String) expression.evaluate(document, XPathConstants.STRING);
            if(result == null) System.out.println("result is null");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while extracting xpath: " + this.xpath);
            System.err.println("Errore message: " + e.getMessage());
            return null;
        }
    }

}
