package org.wso2.carbon.apimgt.impl.dto;

public class TrustStoreDTO {

    private String type;
    private String location;
    private char[] password;

    public TrustStoreDTO(String location, String type, char[] password) {

        this.location = location;
        this.type = type;
        this.password = password;
    }

    public String getLocation() {

        return location;
    }

    public void setLocation(String location) {

        this.location = location;
    }

    public char[] getPassword() {

        return password;
    }

    public void setPassword(char[] password) {

        this.password = password;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }
}
