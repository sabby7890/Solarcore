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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUserlevel() {
        return userlevel;
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

    public String getLocation() {
        return location;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }
}
