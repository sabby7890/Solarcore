package org.solarcore.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static java.nio.file.Paths.get;

class SolarcoreConfig {
    private String systemname;
    private String adminFirstName;
    private String adminLastName;
    private String adminEmail;
    private String adminPhone;
    private String httpEnabled;
    private String httpBindAddress;
    private String httpBindPort;
    private String loglevel;

    private boolean emailStartTLS;
    private boolean emailAuthRequired;
    private String emailServer;
    private String emailPort;
    private String emailLoginUsername;
    private String emailPassword;
    private String emailSenderEmail;
    private String emailSenderName;

    private static SolarcoreLog log;
    private SolarcoreSystem sys;

    private HashMap<String, DefinedFolder> folders;
    private HashMap<String, DefinedContact> contacts;
    private HashMap<String, DefinedTarget> targets;
    private HashMap<String, DefinedUser> users;

    SolarcoreConfig(SolarcoreLog pLog, SolarcoreSystem pSys) {
        folders = new HashMap<>();
        contacts = new HashMap<>();
        targets = new HashMap<>();
        users = new HashMap<>();

        sys = pSys;

        log = pLog;
        try {
            reloadConfiguration();
        } catch (ParserConfigurationException|IOException|SAXException e) {
            log.error("Unable to load and parse configuration file!");
            System.exit(1);
        }

        log.info("System name: " + systemname);
        log.info("Administrator: " + adminFirstName + " " + adminLastName + " <" + adminEmail + ">");
    }

    void reloadConfiguration() throws ParserConfigurationException, IOException, SAXException {
        File fXmlFile = new File("solarcore.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        folders.clear();
        contacts.clear();
        targets.clear();
        users.clear();

        emailStartTLS = false;
        emailAuthRequired = false;

        NodeList n = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < n.getLength(); i++) {
            Node currentNode = n.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "folder":
                        DefinedFolder currentFolder = new DefinedFolder(currentNode, log);
                        if (currentFolder.validate()) {
                            if (folders.containsKey(currentFolder.getID())) {
                                log.error("Duplicate folder id: " + currentFolder.getID());
                                System.exit(254);
                            }
                            folders.put(currentFolder.getID(), currentFolder);
                            log.info(String.format("Added folder: %s", currentFolder.getFolderName()));
                        } else {
                            log.warning(String.format("Unable to validate folder: %s", currentFolder.getFolderName()));
                        }
                        break;
                    case "contact":
                        DefinedContact currentContact = new DefinedContact(currentNode, log);
                        if (currentContact.validate()) {
                            if (contacts.containsKey(currentContact.getID())) {
                                log.error("Duplicate contact id: " + currentContact.getID());
                                System.exit(254);
                            }
                            contacts.put(currentContact.getID(), currentContact);
                            log.info(String.format("Added contact: %s %s", currentContact.getFirstname(), currentContact.getLastname()));
                        } else {
                            log.warning(String.format("Unable to validate contact: %s %s", currentContact.getFirstname(), currentContact.getLastname()));
                        }
                        break;
                    case "user":
                        DefinedUser currentUser = new DefinedUser(currentNode, log);
                        if (currentUser.validate()) {
                            if (users.containsKey(currentUser.getID())) {
                                log.error("Duplicate user id: " + currentUser.getID());
                                System.exit(254);
                            }
                            users.put(currentUser.getID(), currentUser);
                            log.info(String.format("Added user: %s", currentUser.getUsername()));
                        } else {
                            log.warning(String.format("Unable to validate user: %s", currentUser.getUsername()));
                        }
                        break;
                    case "target":
                        DefinedTarget currentTarget = new DefinedTarget(currentNode, log, sys);
                        if (currentTarget.validate()) {
                            if (targets.containsKey(currentTarget.getID())) {
                                log.error("Duplicate target id: " + currentTarget.getID());
                                System.exit(254);
                            }
                            targets.put(currentTarget.getID(), currentTarget);
                            log.info(String.format("Added target: %s", currentTarget.getName()));
                        } else {
                            log.warning(String.format("Unable to validate target: %s", currentTarget.getName()));
                        }
                        break;

                    case "config":
                        parseConfig(currentNode);
                }
            }
        }

        log.info(String.format("Loaded targets: %s, folders: %s, contacts: %s, users: %s", getTargets().size(),
                getFolders().size(), getContacts().size(), getUsers().size()));
    }

    boolean saveConfiguration()  {
        log.info("Saving configuration...");
        try {
            Files.write(get("solarcore.xml"), dump().getBytes());
            log.info("Configuration saved successfully.");
            return true;
        } catch (IOException e) {
            log.error("Error saving configuration!");
            e.printStackTrace();
        }

        return false;
    }

    String dump() {
        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
        TransformerFactory xmlTransformerFactory = TransformerFactory.newInstance();
        Transformer xmlTransformer;
        DocumentBuilder documentBuilder;
        Document doc;

        try {
            documentBuilder = dFactory.newDocumentBuilder();
            xmlTransformer = xmlTransformerFactory.newTransformer();
        } catch (ParserConfigurationException|TransformerConfigurationException e) {
            e.printStackTrace();
            return null;
        }


        doc = documentBuilder.newDocument();
        Element solarcoreElement = doc.createElement("solarcore");
        doc.appendChild(solarcoreElement);

        for (DefinedTarget t : targets.values()) {
            solarcoreElement.appendChild(t.toXML(doc));
        }

        for (DefinedUser u : users.values()) {
            solarcoreElement.appendChild(u.toXML(doc));
        }

        for (DefinedFolder f : folders.values()) {
            solarcoreElement.appendChild(f.toXML(doc));
        }

        for (DefinedContact c : contacts.values()) {
            solarcoreElement.appendChild(c.toXML(doc));
        }


        Element configElement  = doc.createElement("config");

        SolarcoreSystem.addXMLEntry(doc, configElement, "loglevel", loglevel);
        SolarcoreSystem.addXMLEntry(doc, configElement, "systemname", systemname);

        Element adminElement = doc.createElement("admin");

        SolarcoreSystem.addXMLEntry(doc, adminElement, "firstname", adminFirstName);
        SolarcoreSystem.addXMLEntry(doc, adminElement, "lastname", adminLastName);
        SolarcoreSystem.addXMLEntry(doc, adminElement, "email", adminEmail);
        SolarcoreSystem.addXMLEntry(doc, adminElement, "phone", adminPhone);

        configElement.appendChild(adminElement);


        Element httpElement = doc.createElement("http");
        httpElement.setAttribute("enabled", httpEnabled);

        SolarcoreSystem.addXMLEntry(doc, httpElement, "address", httpBindAddress);
        SolarcoreSystem.addXMLEntry(doc, httpElement, "port", httpBindPort);

        Element emailElement = doc.createElement("email");

        SolarcoreSystem.addXMLEntry(doc, emailElement, "server", emailServer);
        SolarcoreSystem.addXMLEntry(doc, emailElement, "port", emailPort);
        SolarcoreSystem.addXMLEntry(doc, emailElement, "username", emailLoginUsername);
        SolarcoreSystem.addXMLEntry(doc, emailElement, "password", emailPassword);

        if (emailAuthRequired == true) {
            SolarcoreSystem.addXMLEntry(doc, emailElement, "authrequired", "true");
        } else {
            SolarcoreSystem.addXMLEntry(doc, emailElement, "authrequired", "false");
        }
        if (emailStartTLS == true) {
            SolarcoreSystem.addXMLEntry(doc, emailElement, "starttls", "true");
        } else {
            SolarcoreSystem.addXMLEntry(doc, emailElement, "starttls", "false");
        }

        SolarcoreSystem.addXMLEntry(doc, emailElement, "senderemail", emailSenderEmail);
        SolarcoreSystem.addXMLEntry(doc, emailElement, "sendername", emailSenderName);

        configElement.appendChild(httpElement);
        configElement.appendChild(adminElement);
        configElement.appendChild(emailElement);

        solarcoreElement.appendChild(configElement);

        doc.setXmlStandalone(false);

        xmlTransformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        return SolarcoreSystem.convertXML(doc, xmlTransformer);
    }

    private void parseConfig (Node config) {
        NodeList childNodes = config.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "systemname":
                        systemname = currentNode.getTextContent();
                        break;
                    case "loglevel":
                        loglevel = currentNode.getTextContent();
                        break;
                    case "admin":
                        parseAdminSection(currentNode);
                        break;
                    case "http":
                        parseHttpSection(currentNode);
                        break;
                    case "email":
                        parseEmailSection(currentNode);
                }
            }
        }
    }

    private void parseEmailSection(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) return;

        Element e = (Element) node;

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "server":
                        emailServer = currentNode.getTextContent();
                        break;
                    case "port":
                        emailPort = currentNode.getTextContent();
                        break;
                    case "username":
                        emailLoginUsername = currentNode.getTextContent();
                        break;
                    case "password":
                        emailPassword = currentNode.getTextContent();
                        break;
                    case "senderemail":
                        emailSenderEmail = currentNode.getTextContent();
                        break;
                    case "sendername":
                        emailSenderName = currentNode.getTextContent();
                        break;
                    case "starttls":
                        if (currentNode.getTextContent().equals("true")) {
                            emailStartTLS = true;
                        } else if (currentNode.getTextContent().equals("false")) {
                            emailStartTLS = false;
                        } else {
                            System.exit(254);
                            log.error("Invalid value for starttls tag in email config section: " + currentNode.getTextContent());
                        }
                        break;
                    case "authrequired":
                        if (currentNode.getTextContent().equals("true")) {
                            emailAuthRequired = true;
                        } else if (currentNode.getTextContent().equals("false")) {
                            emailAuthRequired = false;
                        } else {
                            System.exit(254);
                            log.error("Invalid value for authrequired tag in email config section: " + currentNode.getTextContent());
                        }
                        break;
                }

            }
        }
    }

    private void parseHttpSection(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) return;

        Element e = (Element)node;

        NodeList childNodes = node.getChildNodes();

        httpEnabled = e.getAttribute("enabled");

        if (!httpEnabled.equals("true") && !httpEnabled.equals("false")) {
            log.error("Invalid \"enabled\" attribute value in http section: should be \"true\" or \"false\".");
            System.exit(1);
        }

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "address":
                        httpBindAddress = currentNode.getTextContent();
                        break;
                    case "port":
                        httpBindPort = currentNode.getTextContent();
                        break;
                    default:
                        SolarcoreConfig.unsupportedNode(currentNode, node);
                        break;
                }
            }
        }
    }

    private void parseAdminSection(Node node) {
        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "firstname":
                        adminFirstName = currentNode.getTextContent();
                        break;
                    case "lastname":
                        adminLastName = currentNode.getTextContent();
                        break;
                    case "email":
                        adminEmail = currentNode.getTextContent();
                        break;
                    case "phone":
                        adminPhone = currentNode.getTextContent();
                        break;
                    default:
                        SolarcoreConfig.unsupportedNode(currentNode, node);
                        break;
                }
            }
        }
    }

    static void unsupportedNode(Node currentNode, Node parentNode) {
        log.warning(String.format("Unsupported node in \"%s\": %s", currentNode.getNodeName(), parentNode.getNodeName()));
    }

    static void parseCustomVariable(Node currentNode, HashMap<String, String> customVariables) {
        String key = null;
        String value = null;

        NodeList children = currentNode.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                    case "name":
                        key = n.getTextContent();
                        break;
                    case "value":
                        value = n.getTextContent();
                        break;
                    default:
                        SolarcoreConfig.unsupportedNode(currentNode, n);
                        break;
                }
            }
        }
        if (key != null && value != null) {
            customVariables.put(key, value);
        } else {
            log.warning(String.format("Error parsing configuration section %s", currentNode.getNodeName()));
        }
    }

    HashMap<String, DefinedContact> getContacts() {
        return contacts;
    }

    HashMap<String, DefinedFolder> getFolders() {
        return folders;
    }

    HashMap<String, DefinedTarget> getTargets() {
        return targets;
    }

    HashMap<String, DefinedUser> getUsers() {
        return users;
    }

    String getHttpBindAddress() {
        return httpBindAddress;
    }

    void setHttpBindAddress(String pHttpBindAddress) { httpBindAddress = pHttpBindAddress; }

    String getHttpBindPort() {
        return httpBindPort;
    }

    void setHttpBindPort(String pHttpBindPort) { httpBindPort = pHttpBindPort; }

    String getHttpEnabled() {
        return httpEnabled;
    }

    void setHttpEnabled(String pHttpEnabled) { httpEnabled = pHttpEnabled; }

    String getSystemname() { return systemname; }

    void setSystemname(String pSystemname) { systemname = pSystemname; }

    String getAdminFirstName() { return adminFirstName; }

    void setAdminFirstName(String pAdminFirstName) { adminFirstName = pAdminFirstName; }

    String getAdminLastName() { return adminLastName; }

    void setAdminLastName(String pAdminLastName) { adminLastName = pAdminLastName; }

    String getAdminEmail() { return adminEmail; }

    void setAdminEmail(String pAdminEmail) { adminEmail = pAdminEmail; }

    String getAdminPhone() { return adminPhone; }

    void setAdminPhone(String pAdminPhone) { adminPhone = pAdminPhone; }

    String getLoglevel() {
        return loglevel;
    }

    void setLoglevel(String pLogLevel) { loglevel = pLogLevel; }

    boolean getEmailStartTLS() {
        return emailStartTLS;
    }

    void setEmailStartTLS(boolean pEmailStartTLS) { emailStartTLS = pEmailStartTLS; }

    String getEmailServer() {
        return emailServer;
    }

    void setEmailServer(String pEmailServer) { emailServer = pEmailServer; }

    String getEmailPort() {
        return emailPort;
    }

    void setEmailPort (String pEmailPort) { emailPort = pEmailPort; }

    String getEmailLoginUsername() {
        return emailLoginUsername;
    }

    void setEmailLoginUsername(String pEmailLoginUsername) { emailLoginUsername = pEmailLoginUsername; }

    String getEmailPassword() {
        return emailPassword;
    }

    void setEmailPassword(String pEmailPassword) { emailPassword = pEmailPassword; }

    String getEmailSenderEmail() {
        return emailSenderEmail;
    }

    void setEmailSenderEmail(String pEmailSenderEmail) { emailSenderEmail = pEmailSenderEmail; }

    String getEmailSenderName() {
        return emailSenderName;
    }

    void setEmailSenderName(String pEmailSenderName) { emailSenderName = pEmailSenderName; }

    boolean getEmailAuthRequired() {
        return emailAuthRequired;
    }

    void setEmailAuthRequired(boolean pEmailAuthRequired) { emailAuthRequired = pEmailAuthRequired; }
}
