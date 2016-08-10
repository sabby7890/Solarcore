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

public class Solarcore {
    public static void main(String[] args) {
        SolarcoreLog log = new SolarcoreLog(5);
        SolarcoreVersion version = new SolarcoreVersion();
        SolarcoreSystem sys = new SolarcoreSystem();

        log.info(String.format("Solarcore monitoring system version %s.%s.%s codename \"%s\" starting",
                version.getMajor(), version.getMinor(), version.getMicro(), version.getCodename()));


        SolarcoreConfig config = new SolarcoreConfig(log, sys);

        log.setLoglevel(config.getLoglevel());

        SolarcoreHTTPServer http = new SolarcoreHTTPServer(config, log, sys);
        http.run();

        Notification e = new NotificationEmail(config, log);
        e.setSubject("Testowy email");
        e.setMessage("Dupaaaaa");
        e.setRecipent("tommy@luxperpetua.net");
        //e.send();

        SolarcorePoller poll = new SolarcorePoller(config, log);
        poll.run();
    }
}