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


}
