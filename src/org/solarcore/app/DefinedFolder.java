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
        SolarcoreSystem.addXMLEntry(doc, folderElement, "parentFolder", folderID);

        return folderElement;
    }

    String getFolderName() {
        return folderName;
    }

    void setFolderName(String pFolderName) { folderName = pFolderName; }

    void setFolderDescription(String pFolderDescription) { folderDescription = pFolderDescription; }

    public boolean validate() {
        return folderName != null && folderDescription != null;
    }
}