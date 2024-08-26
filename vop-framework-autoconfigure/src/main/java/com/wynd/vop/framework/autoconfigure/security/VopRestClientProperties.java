package com.wynd.vop.framework.autoconfigure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@ConfigurationProperties(prefix = "vop.framework.security.rest.client")
public class VopRestClientProperties {

    private boolean hideHeaders = true;
    private String headersToHide = "";

    public boolean getHideHeaders(){
        return hideHeaders;
    }

    public String getHeadersToHide() {
        return headersToHide;
    }

    public void setHeadersToHide(String headers) {
        this.headersToHide = headers;
    }

    public void setHideHeaders(boolean hideHeaders) {
        this.hideHeaders = hideHeaders;
    }
}
