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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProbePing extends Probe {
    ProbePing(String pAddress, long pCheckInterval, SolarcoreSystem pSys, SolarcoreLog pLog, Node pCurrentNode, String pTargetID, int pMaxChecks) {
        super(pAddress, pCheckInterval, pSys, pLog, pCurrentNode, pTargetID, pMaxChecks);
        rrd = new SolarcoreRRD(id, pLog, targetID, SolarcoreRRD.DataType.LONG, SolarcoreRRD.Period.ONE_HOUR,
                SolarcoreRRD.ConsolidationFunction.AVG, 10);

    }

    @Override
    Node toXML(Document doc) {
        Element probeElement = doc.createElement("probe");
        probeElement.setAttribute("id", id);

        Element typeElement = doc.createElement("type");
        typeElement.appendChild(doc.createTextNode("ping"));
        probeElement.appendChild(typeElement);

        Element checkIntervalElement = doc.createElement("checkinterval");
        checkIntervalElement.appendChild(doc.createTextNode(String.valueOf(checkInterval)));
        probeElement.appendChild(checkIntervalElement);

        Element maxChecksElement = doc.createElement("maxchecks");
        maxChecksElement.appendChild(doc.createTextNode(String.valueOf(maxChecks)));
        probeElement.appendChild(maxChecksElement);

        return probeElement;
    }

    @Override
    public void execute() {
        lastCheck = System.currentTimeMillis() / 1000L;
        if (sys.getOsType() == SolarcoreSystem.osType.WINDOWS) {
            parseWindowsPing(sys.getCommandOutput("ping -w 5000 -n 1 " + address));
        }
    }

    @Override
    public String getProbeType() {
        return "ping";
    }

    private int parseWindowsPing(String output) {
        isRunning = true;
        String[] split = output.split(Pattern.quote("\r\n"));
        ArrayList<Integer> vals = new ArrayList<>();

        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(split[split.length - 1]);
        while (m.find())
            vals.add(Integer.parseInt(m.group()));

        if (stateChecks < maxChecks) {
            stateChecks += 1;
        }

        if (vals.size() < 2) {
            message = "Host not responding";
            setStatus(2);
            isRunning = false;
            rrd.put(-1);
            return -1;
        } else {
            int avg = vals.get(2);
            message = String.format("PING time: %sms", avg);
            setStatus(0);
            isRunning = false;
            rrd.put(avg);
            return avg;
        }
    }
}