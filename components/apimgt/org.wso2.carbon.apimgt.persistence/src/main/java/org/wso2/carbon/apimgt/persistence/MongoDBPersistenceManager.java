package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.apimgt.api.PersistenceManager;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;

import java.util.Map;

public class MongoDBPersistenceManager implements PersistenceManager {

    @Override public void updateWsdlFromResourceFile() {

    }

    @Override public API getAPI(APIIdentifier apiIdentifier) {
        return null;
    }

    @Override public API getAPI(String s) {
        return null;
    }

    @Override public void updateApi(API api) {

    }

    @Override public void updateWsdlFromUrl(APIIdentifier apiIdentifier, String s) {

    }

    @Override public void updateWsdlFromWsdlFile(APIIdentifier apiIdentifier, ResourceFile resourceFile) {

    }

    @Override public void updateWsdlFromUrl(API api) {

    }

    @Override public void updateWsdlFromWsdlFile(API api, ResourceFile resourceFile) {

    }

    @Override public void updateDocVisibility(APIIdentifier apiIdentifier, String s, String s1,
                                    Documentation documentation) {

    }

    @Override public void addLifeCycle(API api) {

    }

    @Override public void createAPI(API api) {

    }

    @Override public Map<String, Object> searchPaginatedAPIs(String s, String s1, int i, int i1, boolean b) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIs(String s, String s1, int i, int i1, boolean b,
                                    boolean b1) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIsByContent(int i, String s, int i1, int i2, boolean b) {
        return null;
    }
}
