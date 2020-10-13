package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIPersistence;
import org.wso2.carbon.apimgt.api.model.Organization;

public class PersistenceManager {
    String userName;
    Organization org;
    private static APIPersistence apiPersistenceInstance;

    public static APIPersistence getPersistenceInstance(String userName, Organization org)
                                    throws APIManagementException {
        //condition to check the configured Persistence type
        // if (Persistence type is Registry)
        if (apiPersistenceInstance == null) {
            synchronized (RegistryPersistenceImpl.class) {
                if (apiPersistenceInstance == null) {
                    apiPersistenceInstance = new RegistryPersistenceImpl(userName);
                }
            }
        }
        //else if (Persistence type is MongoDb)
        if (apiPersistenceInstance == null) {
            synchronized (RegistryPersistenceImpl.class) {
                if (apiPersistenceInstance == null) {
                    apiPersistenceInstance = new RegistryPersistenceImpl(userName);
                }
            }
        }


        return apiPersistenceInstance;
    }

//    public static APIPersistence getInstance(String username) throws APIManagementException {
//        if (apiPersistenceInstance == null) {
//            synchronized (RegistryPersistenceManager.class) {
//                if (apiPersistenceInstance == null) {
//                    apiPersistenceInstance = new RegistryPersistenceManager(username);
//                }
//            }
//        }
//        return apiPersistenceInstance;
//    }
}
