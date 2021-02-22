package org.wso2.carbon.graphql.api.devportal.modules;

public class DefaultAPIURLsDTO {

    private String http;
    private String https;
    private String ws;
    private String wss;

    public DefaultAPIURLsDTO(String http, String https, String ws, String wss) {

        this.http = http;
        this.https = https;
        this.ws = ws;
        this.wss = wss;
    }

    public DefaultAPIURLsDTO() {
    }

    public String getHttp() {
        return http;
    }

    public void setHttp(String http) {
        this.http = http;
    }

    public String getHttps() {
        return https;
    }

    public void setHttps(String https) {
        this.https = https;
    }

    public String getWs() {
        return ws;
    }

    public void setWs(String ws) {
        this.ws = ws;
    }

    public String getWss() {
        return wss;
    }

    public void setWss(String wss) {
        this.wss = wss;
    }
}
