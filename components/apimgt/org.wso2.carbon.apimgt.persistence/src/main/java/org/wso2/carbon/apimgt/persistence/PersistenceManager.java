package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.apimgt.api.APIPersistence;

public class PersistenceManager {
  //  String userName;
  //  Organization org;
    private static APIPersistence apiPersistenceInstance;

    public static APIPersistence getPersistenceInstance(String userName) {
        //condition to check the configured Persistence type
        // if (Persistence type is Registry)
//        if (apiPersistenceInstance == null) {
//            synchronized (RegistryPersistenceImpl.class) {
//                if (apiPersistenceInstance == null) {
//                    apiPersistenceInstance = new RegistryPersistenceImpl(userName);
//                }
//            }
//        }
        /*//else if (Persistence type is MongoDb)
        else if (apiPersistenceInstance == null) {
            synchronized (RegistryPersistenceImpl.class) {
                if (apiPersistenceInstance == null) {
                    apiPersistenceInstance = new MongoDBPersistenceImpl(userName);
                }
            }
        }*/

        synchronized (RegistryPersistenceImpl.class) {
            if (apiPersistenceInstance == null) {
                apiPersistenceInstance = new MongoDBPersistenceImpl();
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
