package org.solarcore.app;

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