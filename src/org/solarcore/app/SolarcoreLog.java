package org.solarcore.app;

import java.text.SimpleDateFormat;
import java.util.Date;

class SolarcoreLog {
    private int loglevel;
    SolarcoreLog(int pLoglevel) {
        loglevel = pLoglevel;
    }

    void info(String message) {
        if (loglevel > 0) write("info", message);
    }
    void warning(String message) {
        if (loglevel > 1) write("warn", message);
    }
    void error(String message) {
        if (loglevel > 2) write("error", message);
    }

    void setLoglevel(String pLogLevel) {
        if (pLogLevel == null) {
            info("No log level set, defaulting to " + loglevel);
            return;
        }

        try {
            loglevel = Integer.parseInt(pLogLevel);
        } catch (NumberFormatException e) {
            error("Unable to parse log level: " + pLogLevel);
        }

        info("Log level: " + loglevel);
    }

    private void write(String facility, String message) {
        SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println(String.format("%s solarcore [%s]: %s", date.format(new Date()), facility, message));
    }
}