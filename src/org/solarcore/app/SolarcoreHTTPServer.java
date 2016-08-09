package org.solarcore.app;

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