package org.solarcore.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

class SolarcoreSystem {
    enum osType {
        WINDOWS,
        LINUX,
        FREEBSD,
        SUN,
        MAC,
        OTHER
    }

    private osType os;

    SolarcoreSystem() {
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("windows")) os = osType.WINDOWS;
        else if (osName.toLowerCase().contains("linux")) os = osType.LINUX;
        else if (osName.toLowerCase().contains("freebsd")) os = osType.FREEBSD;
        else if (osName.toLowerCase().contains("mac os")) os = osType.MAC;
        else if (osName.toLowerCase().contains("sun os") || osName.toLowerCase().contains("solaris")) os = osType.SUN;
        else os = osType.OTHER;
    }

    osType getOsType() {
        return os;
    }

    String getCommandOutput(String cmd)  {
        Process p;
        StringBuilder out = new StringBuilder();
        String buf;

        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            return null;
        }

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));

        try {
            while ((buf = inputStream.readLine()) != null) {
                out.append(buf).append("\r\n");
            }
        } catch (IOException e) {
            return null;
        }

        return out.toString();
    }

    byte[] readFile(String filename) throws IOException {
        return readAllBytes(get(filename));
    }

    void writeFile(String filename, byte[] data) throws  IOException {
        Files.write(get(filename), data);
    }

    String getSHA512(String passwordToHash){
        StringBuilder generatedPassword = new StringBuilder();

        if (passwordToHash.length() == 0) {
            return "";
        }

        try {
            MessageDigest m = MessageDigest.getInstance("SHA-512");
            m.update(passwordToHash.getBytes());
            byte[] digest = m.digest();

            for (int i = 0; i < m.getDigestLength(); i++) {
                byte b = digest[i];
                String temp = Integer.toHexString(b);
                while (temp.length() < 2) { temp = "0" + temp; }
                temp = temp.substring(temp.length() - 2);
                generatedPassword.append(temp);
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return generatedPassword.toString();
    }

    static byte[] primitiveToBytes(long l) {
        byte[] result = new byte[Long.SIZE / 8];
        for (int i = (Long.SIZE / 8)- 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Long.SIZE / 8;
        }

        return result;
    }

    public static byte[] primitiveToBytes(int l) {
        byte[] result = new byte[Integer.SIZE / 8];
        for (int i = (Integer.SIZE / 8)- 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Integer.SIZE / 8;
        }

        return result;
    }

    public static byte[] primitiveToBytes(short l) {
        byte[] result = new byte[Short.SIZE / 8];
        for (int i = (Short.SIZE / 8)- 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Short.SIZE / 8;
        }

        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.SIZE / 8; i++) {
            result <<= Long.SIZE / 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    static long min(long a, long b) {
        return (a <= b) ? a : b;
    }

    static int min(int a, int b) {
        return (a <= b) ? a : b;
    }

    public static short min(short a, short b) {
        return (a <= b) ? a : b;
    }

    public static byte min(byte a, byte b) {
        return (a <= b) ? a : b;
    }

    public static long max(long a, long b) {
        return (a <= b) ? a : b;
    }

    public static int max(int a, int b) {
        return (a <= b) ? a : b;
    }

    public static short max(short a, short b) {
        return (a <= b) ? a : b;
    }

    public static byte max(byte a, byte b) {
        return (a <= b) ? a : b;
    }

    public static long last(long a, long b) {
        return b;
    }

    public static int last(int a, int b) {
        return b;
    }

    public static short last(short a, short b) {
        return b;
    }

    public static byte last(byte a, byte b) {
        return b;
    }

    public static long count(long a, long b) {
        return a + b;
    }

    public static int count(int a, int b) {
        return a + b;
    }

    public static short count(short a, short b) {
        return (short)(a + b);
    }

    public static byte count(byte a, byte b) {
        return (byte)(a + b);
    }

    public static long avg(long a, long b) {
        return (a + b) / 2;
    }

    public static int avg(int a, int b) {
        return (a + b) / 2;
    }

    public static short avg(short a, short b) {
        return (short)((a + b) / 2);
    }

    public static byte avg(byte a, byte b) {
        return (byte)((a + b) / 2);
    }

    public static void addXMLEntry(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.appendChild(doc.createTextNode(value));
        parent.appendChild(element);
    }

    public static String convertXML(Document doc, Transformer xmlTransformer) {
        StringWriter writer = new StringWriter();
        try {
            xmlTransformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return writer.getBuffer().toString();
    }

    public static void addCustomEntries(Document doc, Element targetElement, HashMap<String, String> customVariables) {
        for (Map.Entry<String, String> entry : customVariables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            Element customElement = doc.createElement("custom");

            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(key));

            Element valueElement = doc.createElement("value");
            valueElement.appendChild(doc.createTextNode(value));

            customElement.appendChild(nameElement);
            customElement.appendChild(valueElement);

            targetElement.appendChild(customElement);
        }
    }
}