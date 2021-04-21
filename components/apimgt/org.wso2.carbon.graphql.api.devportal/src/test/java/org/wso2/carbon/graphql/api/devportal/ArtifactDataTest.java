package org.wso2.carbon.graphql.api.devportal;

import org.junit.jupiter.api.Test;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.utils.RegistrySearchUtil;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactDataTest {
   private static APIPersistence apiPersistenceInstance;

//    static {
//        try {
//            apiPersistenceInstance = CheckedConvention.class.getDeclaredConstructor();
//        } catch (NoSuchMethodException e) {
//            throw new ExceptionInInitializerError(e);
//        }
//    }

    @Test
    void getDevportalAPIS() {
    }

    @Test
    void getApiFromUUID(){

    }

    @Test
    void apiCount() throws Exception  {


    }
}