package org.solarcore.app;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SolarcoreHTTPHandler implements HttpHandler {
    private SolarcoreConfig config;
    private SolarcoreLog log;
    private SolarcoreSystem sys;
    private ArrayList<SolarcoreSession> sessions;
    private DocumentBuilder documentBuilder;
    private Transformer xmlTransformer;

    SolarcoreHTTPHandler(SolarcoreConfig pConfig, SolarcoreLog pLog, SolarcoreSystem pSys) {
        log = pLog;
        config = pConfig;
        sys = pSys;
        sessions = new ArrayList<>();

        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = dFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        TransformerFactory xmlTransformerFactory = TransformerFactory.newInstance();

        try {
            xmlTransformer = xmlTransformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }

    }

    private HashMap<String, String> parseParams(String data) {
        HashMap<String, String> params = new HashMap<>();
        if (data == null) return params;

        String[] spl = data.split("&");

        for (String param : spl) {
            String key, value;
            try {
                String[] paramSplit = param.split("=");
                key = paramSplit[0];
                value = paramSplit[1];
            } catch (Exception e) {
                continue;
            }

            if (value == null) continue;
            params.put(key, value);
        }

        return params;
    }

    private String transformXML(Document document) {
        StringWriter writer = new StringWriter();
        String out;

        try {
            xmlTransformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        //out = writer.getBuffer().toString().replaceAll("\n|\r", "");
        out = writer.getBuffer().toString();

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    public void handle(HttpExchange t) {
        String requestMethod = t.getRequestMethod();

        URI g = t.getRequestURI();
        byte[] data = null;
        Headers responseHeaders = t.getResponseHeaders();


        if (g.getPath().startsWith("/static/")) {
            String filename = g.getPath().split("/static/")[1];
            try {
                data = sys.readFile("static" + File.separator + filename);
            } catch (IOException e) {
                log.error("Unable to serve static file: " + filename);
            }

            if (data != null) {
                if (filename.endsWith(".js"))
                    responseHeaders.set("Content-Type", "application/javascript");
                else if (filename.endsWith(".xml"))
                    responseHeaders.set("Content-Type", "text/xml");
                else if (filename.endsWith(".css"))
                    responseHeaders.set("Content-Type", "text/css");
                else if (filename.endsWith(".jpg"))
                    responseHeaders.set("Content-Type", "image/jpg");
                else if (filename.endsWith(".png"))
                    responseHeaders.set("Content-Type", "image/png");
                else if (filename.endsWith(".gif"))
                    responseHeaders.set("Content-Type", "image/gif");
                else if (filename.endsWith(".ico"))
                    responseHeaders.set("Content-Type", "image/x-icon");
                else
                    responseHeaders.set("Content-Type", "application/octet-stream");
            } else {
                responseHeaders.set("Content-Type", "text/plain");
            }
        } else {
            if (requestMethod.equals("GET")) {
                boolean authorized = false;
                List<String> cookies;
                try {
                    cookies = t.getRequestHeaders().get("Cookie");
                } catch (Exception e) {
                    cookies = null;
                }


                if (cookies != null) {
                    for (String e : cookies) {
                        for (SolarcoreSession s : sessions) {
                            String sessionCookieID = e.split("sessionid=")[1].split(" ")[0].split(";")[0];

                            if (s.getSessionID().equals(sessionCookieID)) {
                                authorized = true;
                            }
                        }
                    }
                }

                if (authorized && g.getPath().equals("/login")) {
                    responseHeaders.set("Location", "/");
                    try {
                        t.sendResponseHeaders(302, -1);
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (!authorized && !g.getPath().equals("/login") && !g.getPath().equals("/api/keepalive") && !g.getPath().equals("/api/folderlist")) {
                    responseHeaders.set("Location", "/login");
                    try {
                        t.sendResponseHeaders(302, -1);
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                HashMap<String, String> params = parseParams(t.getRequestURI().getQuery());
                HashMap<String, String> v = new HashMap<>();

                switch(g.getPath()) {
                    case "/":
                        v.put("title", "Dashboard | Solarcore");
                        data = parse("index.html", v);
                        break;

                    case "/logout":
                        logout(t, responseHeaders);
                        break;

                    case "/api/graphdata":
                        data = getGraphdata(params);
                        responseHeaders.set("Content-Type", "text/xml");
                        break;

                    case "/api/keepalive":
                        data = keepalive(authorized);
                        responseHeaders.set("Content-Type", "text/xml");
                        break;

                    case "/api/configdump":
                        data = configdump();
                        responseHeaders.set("Content-Type", "text/xml");
                        break;

                    case "/api/dashboard":
                        try {
                            data = dashboardApi(authorized);
                            responseHeaders.set("Content-Type", "text/xml");
                        } catch (ParserConfigurationException|TransformerException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "/api/folderlist":
                        data = folderList(params);
                        responseHeaders.set("Content-Type", "text/xml");
                        break;

                    case "/folders":
                        v.put("title", "Folders | Solarcore");
                        data = parse("folders.html", v);
                        break;
                    case "/reports":
                        v.put("title", "Reports | Solarcore");
                        data = parse("reports.html", v);
                        break;

                    case "/history":
                        v.put("title", "History | Solarcore");
                        data = parse("history.html", v);
                        break;

                    case "/configuration":
                        v.put("title", "Configuration | Solarcore");
                        v.put("input_systemname", config.getSystemname());
                        v.put("input_adminfirstname", config.getAdminFirstName());
                        v.put("input_adminlastname", config.getAdminLastName());
                        v.put("input_adminemail", config.getAdminEmail());
                        v.put("input_adminphonenumber", config.getAdminPhone());
                        v.put("input_httpbindaddress", config.getHttpBindAddress());
                        v.put("input_httpbindport", config.getHttpBindPort());
                        v.put("input_emailserver", config.getEmailServer());
                        v.put("input_emailport", config.getEmailPort());
                        v.put("input_emailusername", config.getEmailLoginUsername());
                        v.put("input_emailuserpass", config.getEmailPassword());
                        v.put("input_emailsender", config.getEmailSenderEmail());
                        v.put("input_emailsendername", config.getEmailSenderName());

                        if (config.getEmailAuthRequired()) {
                            v.put("input_auth_req_yes", "selected=\"selected\" ");
                            v.put("input_auth_req_no", "");
                        } else {
                            v.put("input_auth_req_yes", "");
                            v.put("input_auth_req_no", "selected=\"selected\" ");
                        }

                        if (config.getEmailStartTLS()) {
                            v.put("input_starttls_yes", "selected=\"selected\" ");
                            v.put("input_starttls_no", "");
                        } else {
                            v.put("input_starttls_yes", "");
                            v.put("input_starttls_no", "selected=\"selected\" ");
                        }

                        data = parse("configuration.html", v);
                        break;

                    case "/login":
                        v.put("title", "Login | Solarcore");
                        data = parse("login.html", v);
                        break;

                    default:
                        data = null;
                        break;
            }
            } else if (requestMethod.equals("POST")) {
                InputStream is = t.getRequestBody();
                BufferedReader b = new BufferedReader(new InputStreamReader(is));
                StringBuilder s = new StringBuilder();

                String r;

                try {
                    while ((r = b.readLine()) != null) {
                        s.append(r);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> postParams = parseParams(s.toString());
                HashMap<String, String> v = new HashMap<>();

                switch (g.getPath()) {
                    case "/login":
                        boolean loginValid = false;
                        try {
                            loginValid = parseLogin(postParams, responseHeaders);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (loginValid) {
                            responseHeaders.set("Location", "/");
                            try {
                                t.sendResponseHeaders(302, -1);
                                log.info("Successful login for user " + postParams.get("sc_login"));
                                return;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            log.info("Failed login for user " + postParams.get("sc_login"));
                            v.put("title", "Login | Solarcore");
                            data = parse("login_invalid.html", v);
                        }
                        break;
                    case "/api/configsave":
                        data = saveconfiguration(postParams);
                        break;
                }
            }
        }

        try {
            if (data != null) {
                writeHTTP(data, t);
            } else {
                writeHTTP404("File not found.", t);
            }
        } catch (IOException e) {
            log.error(String.format("HTTP: unable to write to client: %s", e.getMessage()));
        }
    }

    private String decodeUrlParameter(String url) {
        String out;
        try {
            out = new URI(url).getPath().replace("+", " ").replace("[SOLARCORE_PLUS]", "+");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return url;
        }

        return out;
    }

    private byte[] saveconfiguration(HashMap<String, String> params) {
        if (params.get("systemname") != null)
            config.setSystemname(decodeUrlParameter(params.get("systemname")));

        if (params.get("httpbindaddress") != null)
            config.setHttpBindAddress(decodeUrlParameter(params.get("httpbindaddress")));

        if (params.get("httpbindport") != null)
            config.setHttpBindPort(decodeUrlParameter(params.get("httpbindport")));

        if (params.get("adminfirstname") != null)
            config.setAdminFirstName(decodeUrlParameter(params.get("adminfirstname")));

        if (params.get("adminlastname") != null)
            config.setAdminLastName(decodeUrlParameter(params.get("adminlastname")));

        if (params.get("adminemail") != null)
            config.setAdminEmail(decodeUrlParameter(params.get("adminemail")));

        if (params.get("adminphonenumber") != null)
            config.setAdminPhone(decodeUrlParameter(params.get("adminphonenumber")));

        if (params.get("emailserver") != null)
            config.setEmailServer(decodeUrlParameter(params.get("emailserver")));

        if (params.get("emailport") != null)
            config.setEmailPort(decodeUrlParameter(params.get("emailport")));

        log.info("Auth required: " + decodeUrlParameter(params.get("emailauthrequired")));

        if (params.get("emailauthrequired") != null) {
            if (decodeUrlParameter(params.get("emailauthrequired")).equals("auth_required_yes")) {
                config.setEmailAuthRequired(true);
            }

            if (decodeUrlParameter(params.get("emailauthrequired")).equals("auth_required_no")) {
                config.setEmailAuthRequired(false);
            }
        }

        if (params.get("emailusername") != null)
            config.setEmailLoginUsername(decodeUrlParameter(params.get("emailusername")));

        if (params.get("emailuserpass") != null)
            config.setEmailPassword(decodeUrlParameter(params.get("emailuserpass")));

        log.info("Starttls: " + decodeUrlParameter(params.get("emailstarttls")));

        if (params.get("emailstarttls") != null) {
            if (decodeUrlParameter(params.get("emailstarttls")).equals("starttls_yes")) {
                config.setEmailStartTLS(true);
            }

            if (decodeUrlParameter(params.get("emailstarttls")).equals("starttls_no")) {
                config.setEmailStartTLS(false);
            }
        }

        if (params.get("emailsender") != null)
            config.setEmailSenderEmail(decodeUrlParameter(params.get("emailsender")));

        if (params.get("emailsendername") != null)
            config.setEmailSenderName(decodeUrlParameter(params.get("emailsendername")));

        config.saveConfiguration();
        try {
            config.reloadConfiguration();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return "OK".getBytes();
    }

    private void findSubFolders(String folderID, Document doc, Node attach) {
        for (Map.Entry<String, DefinedFolder> entry : config.getFolders().entrySet()) {
            DefinedFolder e = entry.getValue();

            if (e.getFolderID().equals(folderID)) {
                Node c = e.toXML(doc);
                findSubFolders(e.getID(), doc, c);
                attach.appendChild(c);
            }
        }

        for (Map.Entry<String, DefinedTarget> entry : config.getTargets().entrySet()) {
            DefinedTarget e = entry.getValue();

            if (e.getFolderID().equals(folderID)) {
                Node c = e.toXML(doc);
                attach.appendChild(c);
            }
        }

        for (Map.Entry<String, DefinedContact> entry : config.getContacts().entrySet()) {
            DefinedContact e = entry.getValue();

            if (e.getFolderID().equals(folderID)) {
                Node c = e.toXML(doc);
                attach.appendChild(c);
            }
        }

        for (Map.Entry<String, DefinedUser> entry : config.getUsers().entrySet()) {
            DefinedUser e = entry.getValue();

            if (e.getFolderID().equals(folderID)) {
                Node c = e.toXML(doc);
                attach.appendChild(c);
            }
        }
    }

    private byte[] folderList(HashMap<String,String> params) {
        Document doc = documentBuilder.newDocument();

        Element folderListElement = doc.createElement("folderList");
        doc.appendChild(folderListElement);

        if (params.containsKey("megatree")) {
            findSubFolders("0", doc, folderListElement);
        } else {
            for (Map.Entry<String, DefinedFolder> entry : config.getFolders().entrySet()) {
                folderListElement.appendChild(entry.getValue().toXML(doc));
            }
        }

        return SolarcoreSystem.convertXML(doc, xmlTransformer).getBytes();
    }

    private byte[] getGraphdata(HashMap<String, String> params) {
        String target = params.get("target");
        String probe = params.get("probe");
        String date_from = params.get("from");
        String date_to = params.get("to");
        String step = params.get("step");
        Probe c = null;

        Document doc = documentBuilder.newDocument();

        Element graphElement = doc.createElement("graph");
        doc.appendChild(graphElement);

        if (target == null || probe == null || date_from == null || date_to == null || step == null) {
            Element graphErrorElement = doc.createElement("error");
            graphErrorElement.appendChild(doc.createTextNode("Invalid usage."));
            graphElement.appendChild(graphErrorElement);
            return SolarcoreSystem.convertXML(doc, xmlTransformer).getBytes();
        }

        for (Map.Entry<String, DefinedTarget> entry : config.getTargets().entrySet()) {
            String targetID = entry.getKey();
            DefinedTarget targetClass  = entry.getValue();

            if (targetID.equals(target)) {
                log.info("Found graph target " + target);

                for (Map.Entry<String, Probe> probeEntry :  targetClass.getProbes().entrySet()) {
                    String probeID = probeEntry.getKey();
                    Probe probeClass = probeEntry.getValue();

                    if (probe.equals(probeID)) {
                        c = probeClass;
                    }
                }
            }
        }

        if (c == null) {
            Element graphErrorElement = doc.createElement("error");
            graphErrorElement.appendChild(doc.createTextNode("Probe not found."));
            graphElement.appendChild(graphErrorElement);
            return SolarcoreSystem.convertXML(doc, xmlTransformer).getBytes();
        }

        SolarcoreRRD rrd = c.getRrd();

        Element graphDataElement = doc.createElement("data");
        graphDataElement.appendChild(doc.createTextNode(rrd.getData(date_from, date_to, Integer.parseInt(step))));
        graphElement.appendChild(graphDataElement);


        return SolarcoreSystem.convertXML(doc, xmlTransformer).getBytes();
    }

    private byte[] configdump() {
        config.saveConfiguration();
        return config.dump().getBytes();
    }

    private byte[] keepalive(boolean authorized)  {
        Document document = documentBuilder.newDocument();
        Element mainElement = document.createElement("root");
        document.appendChild(mainElement);

        Element authStatusElement = document.createElement("login");
        if (authorized)
            authStatusElement.setAttribute("authorized", "true");
        else
            authStatusElement.setAttribute("authorized", "false");

        mainElement.appendChild(authStatusElement);

        return transformXML(document).getBytes();
    }

    private byte[] dashboardApi(boolean authorized) throws ParserConfigurationException, TransformerException {
        Document document = documentBuilder.newDocument();

        Element mainElement = document.createElement("root");
        document.appendChild(mainElement);

        if (authorized) {
            for (Map.Entry<String, DefinedTarget> entry : config.getTargets().entrySet()) {
                // String id = entry.getKey();
                DefinedTarget c = entry.getValue();

                Element targetElement = document.createElement("target");

                Element idElement = document.createElement("id");
                idElement.appendChild(document.createTextNode(c.getID()));
                targetElement.appendChild(idElement);

                Element nameElement = document.createElement("name");
                nameElement.appendChild(document.createTextNode(c.getName()));
                targetElement.appendChild(nameElement);

                Element folderElement = document.createElement("folder");
                folderElement.appendChild(document.createTextNode(c.getFolderID()));
                targetElement.appendChild(folderElement);

                Element descriptionElement = document.createElement("description");
                descriptionElement.appendChild(document.createTextNode(c.getDescription()));
                targetElement.appendChild(descriptionElement);

                Element locationElement = document.createElement("location");
                locationElement.appendChild(document.createTextNode(c.getDescription()));
                targetElement.appendChild(locationElement);

                Element statusElement = document.createElement("status");
                switch (c.getStatus()) {
                    case 0:
                        statusElement.appendChild(document.createTextNode("OK"));
                        break;
                    case 1:
                        statusElement.appendChild(document.createTextNode("WARN"));
                        break;
                    case 2:
                        statusElement.appendChild(document.createTextNode("ERROR"));
                        break;
                    default:
                        statusElement.appendChild(document.createTextNode("UNKNOWN"));
                        break;
                }

                targetElement.appendChild(statusElement);

                mainElement.appendChild(targetElement);
                for (Map.Entry<String, Probe> probeSet : c.getProbes().entrySet()) {
                    Probe p = probeSet.getValue();

                    Element probeElement = document.createElement("probe");
                    probeElement.setAttribute("type", p.getProbeType());
                    Element probeStateElement = document.createElement("state");

                    switch (p.getStatus()) {
                        case 0:
                            probeStateElement.appendChild(document.createTextNode("OK"));
                            break;
                        case 1:
                            probeStateElement.appendChild(document.createTextNode("WARN"));
                            break;
                        case 2:
                            probeStateElement.appendChild(document.createTextNode("ERROR"));
                            break;
                        default:
                            probeStateElement.appendChild(document.createTextNode("UNKNOWN"));
                            break;
                    }

                    switch (p.getCheckState()) {
                        case 0:
                            probeStateElement.setAttribute("type", "SOFT");
                            break;
                        case 1:
                            probeStateElement.setAttribute("type", "HARD");
                            break;
                        default:
                            probeStateElement.setAttribute("type", "UNKNOWN - " + p.getCheckState());
                            break;
                    }

                    Element probeMessageElement = document.createElement("message");
                    probeMessageElement.appendChild(document.createTextNode(p.getMessage()));
                    probeElement.appendChild(probeMessageElement);

                    probeElement.appendChild(probeStateElement);
                    targetElement.appendChild(probeElement);
                }
            }
        }


        return transformXML(document).getBytes();
    }

    private void logout(HttpExchange t, Headers responseHeaders) {
        List<String> cookies;
        try {
            cookies = t.getRequestHeaders().get("Cookie");
        } catch (Exception e) {
            cookies = null;
        }


        if (cookies != null) {
            for (String e : cookies) {
                for (SolarcoreSession s : sessions) {
                    String sessionCookieID = e.split("sessionid=")[1].split(" ")[0].split(";")[0];

                    if (s.getSessionID().equals(sessionCookieID)) {
                        sessions.remove(s);
                        responseHeaders.set("Location", "/");
                        try {
                            t.sendResponseHeaders(302, -1);
                            return;
                        } catch (IOException ec) {
                            ec.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private boolean parseLogin(HashMap<String, String> params, Headers responseHeaders) {
        String login = params.get("sc_login");
        String password = params.get("sc_password");
        String encPass = sys.getSHA512(password);
        DefinedUser loginUser = null;

        for (DefinedUser u: config.getUsers().values()) {
            if (login.equals(u.getUsername()) && encPass.equals(u.getPassword())) {
                loginUser = u;
                break;
            }
        }

        if (loginUser != null) {
            String sessionID = SolarcoreSession.generateSessionID();
            SolarcoreSession session = new SolarcoreSession(sessionID, loginUser);
            String sessionCookie = "sessionid=" + sessionID + "; path=/";
            responseHeaders.set("Set-Cookie", sessionCookie);
            sessions.add(session);
            return true;
        } else {
            return false;
        }
    }

    private void writeHTTP(byte[] body, HttpExchange t) throws IOException {
        String requestMethod = t.getRequestMethod();

        switch (requestMethod) {
            case "GET":
                t.sendResponseHeaders(200, body.length);
                try (BufferedOutputStream out = new BufferedOutputStream(t.getResponseBody())) {
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(body)) {
                        byte[] buffer = new byte [1024];
                        int count;
                        while ((count = bis.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                    }
                }
                break;
            case "POST":
                t.sendResponseHeaders(200, body.length);
                try (BufferedOutputStream out = new BufferedOutputStream(t.getResponseBody())) {
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(body)) {
                        byte[] buffer = new byte [1024];
                        int count;
                        while ((count = bis.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                    }
                }
                break;
            default:
                t.sendResponseHeaders(200, -1);
                break;
        }

    }

    private void writeHTTP404(String body, HttpExchange t) throws IOException {
        String requestMethod = t.getRequestMethod();

        switch (requestMethod) {
            case "GET":
                t.sendResponseHeaders(404, body.length());
                OutputStream os = t.getResponseBody();

                os.write(body.getBytes());
                os.close();
                break;
            default:
                t.sendResponseHeaders(404, -1);
                break;
        }
    }

    private byte[] parse(String filename, HashMap<String, String> variables) {
        byte[] data = null;
        try {
            data = sys.readFile("body" + File.separator + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return parseResponse(data, variables);
    }

    private byte[] parseResponse(byte[] data, HashMap<String, String> variables) {
        if (data == null) return null;
        String buffer = new String(data, StandardCharsets.UTF_8);

        Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(buffer);

        while(m.find()) {
            String command = m.group(1);
            command = command.replace("{", "").replace("}", "");

            if (command.startsWith("include")) {
                String inc = "body" + File.separator + command.split(" ")[1];
                String incData;
                try {
                    incData = new String(parseResponse(sys.readFile(inc), variables), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    continue;
                }

                String toReplace = String.format("\\{%s\\}", Pattern.quote(command));
                try {
                    buffer = buffer.replaceFirst(toReplace, incData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (variables != null) {
            for (String key : variables.keySet()) {
                String toReplace = String.format("\\{%s\\}", Pattern.quote(key));
                String incData = variables.get(key);
                try {
                    buffer = buffer.replaceFirst(toReplace, incData);
                } catch (Exception e) {
                    buffer = buffer.replaceFirst(toReplace, "");
                }
            }
        }

        return buffer.getBytes();
    }
}