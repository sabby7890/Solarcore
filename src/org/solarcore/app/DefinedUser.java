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

class DefinedUser extends Defined {
    private String username;
    private String password;
    private String userlevel;
    private String firstname;
    private String lastname;
    private String email;
    private String location;
    private String phone;
    private String description;

    public boolean validate() {
        return username != null && password != null && userlevel != null && firstname != null && lastname != null && email != null;
    }

    DefinedUser(Node node, SolarcoreLog pLog) {
        super(node, pLog);

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "parentFolder":
                        folderID = currentNode.getTextContent();
                        break;
                    case "username":
                        username = currentNode.getTextContent();
                        break;
                    case "password":
                        password = currentNode.getTextContent();
                        break;
                    case "userlevel":
                        userlevel = currentNode.getTextContent();
                        break;
                    case "firstname":
                        firstname = currentNode.getTextContent();
                        break;
                    case "lastname":
                        lastname = currentNode.getTextContent();
                        break;
                    case "email":
                        email = currentNode.getTextContent();
                        break;
                    case "phone":
                        phone = currentNode.getTextContent();
                        break;
                    case "description":
                        description = currentNode.getTextContent();
                        break;
                    case "location":
                        location = currentNode.getTextContent();
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
        Element userElement = doc.createElement("user");
        userElement.setAttribute("id", id);

        SolarcoreSystem.addXMLEntry(doc, userElement, "username", username);
        SolarcoreSystem.addXMLEntry(doc, userElement, "password", password);
        SolarcoreSystem.addXMLEntry(doc, userElement, "userlevel", userlevel);
        SolarcoreSystem.addXMLEntry(doc, userElement, "firstname", firstname);
        SolarcoreSystem.addXMLEntry(doc, userElement, "lastname", lastname);
        SolarcoreSystem.addXMLEntry(doc, userElement, "email", email);
        SolarcoreSystem.addXMLEntry(doc, userElement, "parentFolder", folderID);
        SolarcoreSystem.addXMLEntry(doc, userElement, "phone", phone);
        SolarcoreSystem.addXMLEntry(doc, userElement, "location", location);

        return userElement;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getEmail() {
        return email;
    }

    String getDescription() {
        return description;
    }
}
