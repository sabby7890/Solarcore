package org.solarcore.app;

import java.math.BigInteger;
import java.security.SecureRandom;

class SolarcoreSession {
    private String sessionID;
    private DefinedUser user;
    private static SecureRandom r = new SecureRandom();

    SolarcoreSession(String pSessionID, DefinedUser pUser) {
        sessionID = pSessionID;
        user = pUser;
    }

    static String generateSessionID() {
        return String.format("%s-%s", new BigInteger(130, r).toString(32), new BigInteger(130, r).toString(32));
    }

    String getSessionID() {
        return sessionID;
    }

    public DefinedUser getUserID() {
        return user;
    }
}
