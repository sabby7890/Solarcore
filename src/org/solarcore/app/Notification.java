package org.solarcore.app;

abstract class Notification  {
    String recipent;
    private String serviceLogin;
    private String servicePassword;
    private String serviceAddress;
    private String serviceSender;
    String message;
    String subject;
    SolarcoreConfig config;
    SolarcoreLog log;

    Notification(SolarcoreConfig pConfig, SolarcoreLog pLog) {
        config = pConfig;
        log = pLog;
    }

    void setSubject(String pSubject) {
        subject = pSubject;
    }

    void setRecipent(String pRecipent) {
        recipent = pRecipent;
    }

    void setServiceCredentials(String pLogin, String pPassword) {
        serviceLogin = pLogin;
        servicePassword = pPassword;
    }

    void setServiceAddress(String pAddress) {
        serviceAddress = pAddress;
    }

    void setServiceSender(String pSender) {
        serviceSender = pSender;
    }

    public boolean send() {
        return true;
    }

    public void setMessage(String pMessage) {
        message = pMessage;
    }
}
