package org.wso2.carbon.graphql.api.devportal.modules;

public class TimeDTO {
    private String createdTime;
    private String lastUpdate;

    public TimeDTO(String createdTime,String lastUpdate){
        this.createdTime = createdTime;
        this.lastUpdate = lastUpdate;
    }
}
