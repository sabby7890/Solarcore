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

import java.util.Map;

class SolarcorePoller extends Thread {
    private SolarcoreConfig config;
    private SolarcoreLog log;

    private class Poll extends Thread {
        DefinedTarget target;
        SolarcoreLog log;

        Poll (DefinedTarget pTarget, SolarcoreLog pLog) {
            target = pTarget;
            log = pLog;
        }

        public void run() {
            target.poll();
        }
    }

    SolarcorePoller(SolarcoreConfig pConfig, SolarcoreLog pLog) {
        config = pConfig;
        log = pLog;
        setDaemon(true);
    }

    @Override
    public void run() {
        boolean running = true;
        log.info("Solarcore monitoring system started.");
        while (running) {
            poll();
            try { Thread.sleep(1000); }
            catch (InterruptedException e) {
                log.error("Thread interrupted!");
                running = false;
            }
        }
    }

    private void poll() {
        for (Map.Entry<String, DefinedTarget> entry : config.getTargets().entrySet()) {
            //String id = entry.getKey();
            DefinedTarget t = entry.getValue();
            Poll p = new Poll(t, log);
            p.start();
        }
    }
}