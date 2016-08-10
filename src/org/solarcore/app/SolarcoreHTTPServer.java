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

import com.sun.net.httpserver.HttpServer;

import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

class SolarcoreHTTPServer extends Thread {
    private HttpServer server;
    private SolarcoreConfig config;
    private SolarcoreLog log;
    private SolarcoreSystem sys;
    private boolean enabled;

    SolarcoreHTTPServer(SolarcoreConfig pConfig, SolarcoreLog pLog, SolarcoreSystem pSys) {
        config = pConfig;
        log = pLog;
        sys = pSys;
        setDaemon(true);

        if (!config.getHttpEnabled().equals("true")) {
            log.info("Not starting http server, disabled in configuration file.");
            return;
        }

        log.info(String.format("HTTP server starting, binding to %s:%s...",
                config.getHttpBindAddress(), config.getHttpBindPort()));

        try {
            InetAddress bind = InetAddress.getByName(config.getHttpBindAddress());
            int port = Integer.parseInt(config.getHttpBindPort());
            server = HttpServer.create(new InetSocketAddress(bind, port), 0);
        } catch (BindException e) {
            log.error("Unable to bind to requested address. HTTP server will not function properly.");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        enabled = true;
    }

    @Override
    public void run() {
        if (!enabled) return;

        server.createContext("/", new SolarcoreHTTPHandler(config, log, sys));

        server.setExecutor(Executors.newCachedThreadPool());
        log.info("Started http service.");
        server.start();
    }
}