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