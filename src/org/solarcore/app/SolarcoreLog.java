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