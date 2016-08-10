package org.solarcore.app;

/*
 This file is part of the Solarcore project (https://github.com/sabby7890/Solarcore).

 Solarcore is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Solarcore is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Solarcore.  If not, see <http://www.gnu.org/licenses/>.
*/

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