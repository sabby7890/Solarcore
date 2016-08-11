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

class DefinedContact extends Defined {
    private String alias;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String location;

    DefinedContact(Node node, SolarcoreLog pLog) {
        super(node, pLog);

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "parentFolder":
                        folderID = currentNode.getTextContent();
                        break;
                    case "alias":
                        alias = currentNode.getTextContent();
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
                    case "location":
                        location = currentNode.getTextContent();
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
        Element contactElement = doc.createElement("contact");
        contactElement.setAttribute("id", id);

        SolarcoreSystem.addXMLEntry(doc, contactElement, "alias", alias);
        SolarcoreSystem.addXMLEntry(doc, contactElement, "firstname", firstname);
        SolarcoreSystem.addXMLEntry(doc, contactElement, "lastname", lastname);
        SolarcoreSystem.addXMLEntry(doc, contactElement, "email", email);
        SolarcoreSystem.addXMLEntry(doc, contactElement, "phone", phone);
        SolarcoreSystem.addXMLEntry(doc, contactElement, "location", location);
        SolarcoreSystem.addXMLEntry(doc, contactElement, "parentFolder", folderID);

        return contactElement;
    }

    public boolean validate() {
        return alias != null && firstname != null &&  lastname != null && email != null && phone != null && location != null;
    }

    String getFirstname() {
        return firstname;
    }
    String getLastname() {
        return lastname;
    }
    String getEmail() {
        return email;
    }

    void setAlias(String pAlias) { alias = pAlias; }
    void setFirstname(String pFirstname) { firstname = pFirstname; }
    void setLastname(String pLastname) { lastname = pLastname; }
    void setEmail(String pEmail) { email = pEmail; }
    void setPhone(String pPhone) { phone = pPhone; }
    void setLocation(String pLocation) { location= pLocation; }
}