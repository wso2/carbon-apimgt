package org.wso2.carbon.graphql.api.devportal.modules;

public class APIURLsDTO {

    private String http;
    private String https;
    private String ws;
    private String wss;

    public APIURLsDTO(String http, String https, String ws, String wss) {

        this.http = http;
        this.https = https;
        this.ws = ws;
        this.wss = wss;
    }

}
