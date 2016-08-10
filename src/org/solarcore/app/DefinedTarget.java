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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class DefinedTarget extends Defined {
    private String address;
    private String name;
    private String description;
    private String location;
    private HashMap<String, Probe> probes;
    private SolarcoreLog log;
    private SolarcoreSystem sys;
    private byte status;

    DefinedTarget(Node node, SolarcoreLog pLog, SolarcoreSystem pSys) {
        super(node, pLog);
        ArrayList <Node> probeSave = new ArrayList<>();
        sys = pSys;
        log = pLog;
        status = -1;

        probes = new HashMap<>();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                switch (currentNode.getNodeName()) {
                    case "parentFolder":
                        folderID = currentNode.getTextContent();
                        break;
                    case "address":
                        address = currentNode.getTextContent();
                        break;
                    case "probe":
                        probeSave.add(currentNode);
                        break;
                    case "name":
                        name = currentNode.getTextContent();
                        break;
                    case "description":
                        description = currentNode.getTextContent();
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

        if (folderID == null) {
            log.error("No folder ID specified for target " + name);
            System.exit(254);
        }

        for (Node aProbeSave : probeSave) {
            parseProbe(aProbeSave);
        }
    }



    String getName() {
        return name;
    }

    String getDescription() { return description; }

    String getAddress() { return address; }

    byte getStatus() { return status; }

    @Override
    Node toXML(Document doc) {
        Element targetElement = doc.createElement("target");
        targetElement.setAttribute("id", id);

        SolarcoreSystem.addXMLEntry(doc, targetElement, "name", name);
        SolarcoreSystem.addXMLEntry(doc, targetElement, "description", description);
        SolarcoreSystem.addXMLEntry(doc, targetElement, "address", address);
        SolarcoreSystem.addXMLEntry(doc, targetElement, "location", location);
        SolarcoreSystem.addXMLEntry(doc, targetElement, "parentFolder", folderID);

        SolarcoreSystem.addCustomEntries(doc, targetElement, customVariables);

        for (Probe p : probes.values()) {
            targetElement.appendChild(p.toXML(doc));
        }

        return targetElement;
    }

    public boolean validate() {
        return address != null && name != null && description != null && location != null && folderID != null;
    }

    HashMap<String, Probe> getProbes() {
        return probes;
    }

    void poll() {
        for (Map.Entry<String, Probe> entry : probes.entrySet()) {
            Probe c = entry.getValue();
            if (((System.currentTimeMillis() / 1000L) - c.getLastCheck()) >= c.getCheckInterval()) {
                byte s = 0;
                if (!c.isRunning()) {
                    c.execute();
                    s = (byte)Math.max(s, c.getStatus());
                }
                status = s;
            }
        }
    }

    private void parseProbe(Node currentNode) {
        NodeList children = currentNode.getChildNodes();
        String probeType = null;
        String checkInterval = null;
        String maxChecks = null;

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                    case "type":
                        probeType = n.getTextContent();
                        break;
                    case "checkinterval":
                        checkInterval = n.getTextContent();
                        break;
                    case "maxchecks":
                        maxChecks = n.getTextContent();
                        break;
                    default:
                        SolarcoreConfig.unsupportedNode(currentNode, n);
                        break;
                }
            }
        }

        if (probeType == null || checkInterval == null || maxChecks == null) return;

        switch (probeType) {
            case "ping":
                Probe p = new ProbePing(address, Long.parseLong(checkInterval), sys, log, currentNode, id, Integer.parseInt(maxChecks));
                if (probes.containsKey(p.getID())) {
                    log.error("Duplicate probe id: " + p.getID());
                    System.exit(254);
                }
                probes.put(p.getID(), p);
                break;
            default:
                log.error(String.format("Unknown probe type: %s", probeType));
                break;
        }
    }
}
