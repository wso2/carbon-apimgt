package org.wso2.carbon.graphql.api.devportal.modules;

public class APIDTO {
    private String id;
    private String name;
    private String context;
    private String version;
    private String provider;
    private String type;
    private String createdTime;
    private String lastUpdate;

    public APIDTO(String id,String name,String context,String version,String provider,String type,String createdTime,String lastUpdate){
            this.id =id;
            this.name =name;
            this.context = context;
            this.version = version;
            this.provider = provider;
            this.type = type;
            this.createdTime =createdTime;
            this.lastUpdate = lastUpdate;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
