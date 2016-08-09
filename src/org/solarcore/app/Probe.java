package org.solarcore.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

abstract class Probe {
    long checkInterval;
    private byte status;
    private byte checkState;

    String id;
    String targetID;
    SolarcoreRRD rrd;
    String address;
    String message;
    long lastCheck;
    int maxChecks;
    int stateChecks;
    SolarcoreSystem sys;
    boolean isRunning;


    Probe(String pAddress, long pCheckInterval, SolarcoreSystem pSys, SolarcoreLog pLog, Node pCurrentNode, String pTargetID, int pMaxChecks) {
        targetID = pTargetID;
        address = pAddress;
        checkInterval = pCheckInterval;
        isRunning = false;
        message = "Unknown";
        sys = pSys;
        stateChecks = 0;
        lastCheck = System.currentTimeMillis() / 1000L;
        status = -1;
        maxChecks = pMaxChecks;

        Element e = (Element) pCurrentNode;
        id = e.getAttribute("id");

        if (id.length() == 0) {
            pLog.error("No id for entry type: " + ((Element) pCurrentNode).getTagName());
            System.exit(254);
        }
    }

    abstract Node toXML(Document doc);

    abstract void execute();

    abstract String getProbeType();

    SolarcoreRRD getRrd() { return rrd; }

    String getMessage() { return message; }

    long getLastCheck() {
        return lastCheck;
    }

    long getCheckInterval() {
        return checkInterval;
    }

    int getCheckState() {
        return checkState;
    }

    void setStatus(int pStatus) {
        if (status != pStatus) checkState = 0;
        else if (stateChecks == maxChecks) checkState = 1;
        status = (byte)pStatus;
    }

    void setMaxChecks(int pMaxChecks) {
        System.out.println("SETT MAX CHECKS: " + pMaxChecks);
        maxChecks = pMaxChecks;
    }

    byte getStatus() {
        return status;
    }

    boolean isRunning() {
        return isRunning;
    }

    String getID() {
        return id;
    }
}