package org.wso2.carbon.apimgt.impl.dto;

import java.util.ArrayList;

public class ExternalAPIStoresConfigDTO {
    private String StoreURL;
    private ArrayList<ExternalAPIStore> externalAPIStoresList = new ArrayList<>();

    public String getStoreURL() {
        return StoreURL;
    }

    public void setStoreURL(String StoreURL) {
        this.StoreURL = StoreURL;
    }

    public ArrayList<ExternalAPIStore> getExternalAPIStoresList() {
        return externalAPIStoresList;
    }

    public void addExternalAPIStore(String id, String type, String className, String displayName, String endpoint,
            String username, String password) {
        ExternalAPIStore externalAPIStore = new ExternalAPIStore(id, type, className, displayName, endpoint, username,
                password);
        this.externalAPIStoresList.add(externalAPIStore);
    }

    public class ExternalAPIStore {
        private String id;
        private String type;
        private String className;
        private String displayName;
        private String endpoint;
        private String username;
        private String password;

        private ExternalAPIStore(String id, String type, String className, String displayName, String endpoint,
                String username, String password) {
            this.id = id;
            this.type = type;
            this.className = className;
            this.displayName = displayName;
            this.endpoint = endpoint;
            this.username = username;
            this.password = password;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }
}
