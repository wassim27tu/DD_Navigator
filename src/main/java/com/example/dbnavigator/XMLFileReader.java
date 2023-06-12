package com.example.dbnavigator;

import org.slf4j.spi.DefaultLoggingEventBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class XMLFileReader {
    private Map<String, Map<Integer, Map<Integer, String>>> sectionData;
    private Logger logger = Logger.getLogger(XMLFileReader.class.getName());
    public XMLFileReader(String directoryPath) {
//        log to the console that we are initiating the XMLFileReader. use the log method of the Logger class
        logger.info("Initiating XMLFileReader...");

        sectionData = new HashMap<>();
        File directory = new File(directoryPath);
        File[] xmlFiles = directory.listFiles((dir, name) -> name.endsWith(".xml"));

        if (xmlFiles != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                for (File file : xmlFiles) {
                    Document document = builder.parse(file);
                    logger.info("processing file: " + file.getName());
                    processXMLDocument(document);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.info("XMLFileReader initiated.");
    }

    private void processXMLDocument(Document document) throws XPathExpressionException {
        Element rootElement = document.getDocumentElement();
        String station = getElementValue(rootElement, "shortcode");
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList trackNodes = (NodeList) xPath.compile("/station/tracks/track").evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < trackNodes.getLength(); i++) {
            Node trackNode = trackNodes.item(i);
            NodeList trainNodes = (NodeList) xPath.compile("trains/train").evaluate(trackNode, XPathConstants.NODESET);

            for (int j = 0; j < trainNodes.getLength(); j++) {
                Node trainNode = trainNodes.item(j);
                try {
                    Integer trainNumber = Integer.parseInt(getElementValue((Element) trainNode, "trainNumber"));
                    NodeList waggonNodes = (NodeList) xPath.compile("waggons/waggon").evaluate(trainNode, XPathConstants.NODESET);

                    for (int k = 0; k < waggonNodes.getLength(); k++) {
                        Node waggonNode = waggonNodes.item(k);

                        try {
                            Integer waggonIdentifier = Integer.parseInt(getElementValue((Element) waggonNode, "number"));
                            NodeList identifiers = ((Element) waggonNode).getElementsByTagName("identifier");
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int l = 0; l < identifiers.getLength(); l++) {
                                stringBuilder.append(identifiers.item(l).getTextContent()).append(" ");
                            }
//                                append the identifiers to the sectionData
                            sectionData.computeIfAbsent(station, l -> new HashMap<>())
                                    .computeIfAbsent(trainNumber, l -> new HashMap<>())
                                    .putIfAbsent(waggonIdentifier, stringBuilder.toString());
                        } catch (NumberFormatException e) {
                            continue;
                        }
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
    }

    private String getElementValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    public String getSection(String station, Integer trainNumber, Integer waggonIdentifier) {
        Map<Integer, Map<Integer, String>> stationMap = sectionData.get(station);
        if (stationMap != null) {
            Map<Integer, String> trainMap = stationMap.get(trainNumber);
            if (trainMap != null) {
                return trainMap.getOrDefault(waggonIdentifier, "No data found");
            }
        }
        return "No data found";
    }
}
