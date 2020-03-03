package io.agora.vlive.proxy.struts.response;

public class AppVersionResponse extends Response {
    public Data data;

    public class Data {
        public String appCode;
        int osType;
        int terminalType;
        String appVersion;
        String latestVersion;
        String appPackage;
        String upgradeDescription;
        int forcedUpgrade;
        String upgradeUrl;
        int reviewing;
        int remindTimes;
        Config config;
    }

    private class Config {
        String appId;
    }
}
