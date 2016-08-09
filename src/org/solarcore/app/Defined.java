package org.solarcore.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;

abstract class Defined {
    HashMap<String, String> customVariables;
    String id;
    NodeList childNodes;
    String folderID;

    Defined(Node node, SolarcoreLog log) {
        if (node.getNodeType() != Node.ELEMENT_NODE) return;
        customVariables = new HashMap<>();

        childNodes = node.getChildNodes();

        Element e = (Element) node;
        id = e.getAttribute("id");

        if (id.length() == 0) {
            log.error("No id for entry type: " + ((Element) node).getTagName());
            System.exit(254);
        }
    }

    abstract Node toXML(Document doc);

    String getID() {
        return id;
    }

    String getFolderID() {
        return folderID;
    }

    public boolean validate() {
        return false;
    }
}