package org.solarcore.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Map;

class DefinedFolder extends Defined {
    private String folderName;
    private String folderDescription;

    DefinedFolder(Node node, SolarcoreLog pLog) {
        super(node, pLog);

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "parentFolder":
                        folderID = currentNode.getTextContent();
                        break;
                    case "name":
                        folderName = currentNode.getTextContent();
                        break;
                    case "description":
                        folderDescription = currentNode.getTextContent();
                        break;
                    case "custom":
                        SolarcoreConfig.parseCustomVariable(currentNode, customVariables);
                        break;
                    default:
                        SolarcoreConfig.unsupportedNode(currentNode, node);
                        break;
                }
            }
        }
    }

    @Override
    Node toXML(Document doc) {
        Element folderElement = doc.createElement("folder");
        folderElement.setAttribute("id", id);

        SolarcoreSystem.addXMLEntry(doc, folderElement, "name", folderName);
        SolarcoreSystem.addXMLEntry(doc, folderElement, "description", folderDescription);
        SolarcoreSystem.addXMLEntry(doc, folderElement, "parent", folderID);

        return folderElement;
    }

    String getFolderName() {
        return folderName;
    }

    String getFolderDescription() {
        return folderDescription;
    }

    public boolean validate() {
        return folderName != null && folderDescription != null;
    }
}
