package org.solarcore.app;

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