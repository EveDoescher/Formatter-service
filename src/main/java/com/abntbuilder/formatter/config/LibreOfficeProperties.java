package com.abntbuilder.formatter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "formatter.libreoffice")
public class LibreOfficeProperties {

    private boolean enabled = false;
    private String executablePath = "soffice";
    private int timeoutSeconds = 60;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getExecutablePath() { return executablePath; }
    public void setExecutablePath(String executablePath) { this.executablePath = executablePath; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
