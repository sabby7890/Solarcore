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

    public String getAlias() {
        return alias;
    }

    public boolean validate() {
        return alias != null && firstname != null &&  lastname != null && email != null && phone != null && location != null;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }
}