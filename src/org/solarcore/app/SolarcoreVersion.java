package org.solarcore.app;

class SolarcoreVersion {
    private int major;
    private int minor;
    private int micro;
    private String codename;

    SolarcoreVersion() {
        major = 1;
        minor = 0;
        micro = 0;
        codename = "Guardian";
    }

    int getMajor() {
        return major;
    }

    int getMinor() {
        return minor;
    }

    int getMicro() {
        return micro;
    }

    String getCodename() {
        return codename;
    }
}
