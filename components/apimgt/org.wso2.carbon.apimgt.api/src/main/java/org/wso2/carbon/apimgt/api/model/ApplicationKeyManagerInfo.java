package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.List;

public class ApplicationKeyManagerInfo extends Application {

    private List<KeyManagerConfiguration> keyManagers = new ArrayList<>();

    public ApplicationKeyManagerInfo(String uuid) {
        super(uuid);
    }

    public ApplicationKeyManagerInfo(String name, Subscriber subscriber) {
        super(name, subscriber);
    }

    public List<KeyManagerConfiguration> getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(List<KeyManagerConfiguration> keyManagers) {
        this.keyManagers = keyManagers;
    }

}
