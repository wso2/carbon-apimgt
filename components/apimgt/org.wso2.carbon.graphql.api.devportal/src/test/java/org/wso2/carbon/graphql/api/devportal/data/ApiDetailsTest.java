package org.wso2.carbon.graphql.api.devportal.data;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiDetails.class, RestApiUtil.class,ArtifactData.class,ApiMgtDAO.class})
class ApiDetailsTest {

    //public static ApiMgtDAO apiMgtDAO;
    @Test
    void getApiCount() throws  APIManagementException {
        ApiDetails apiDetails = new ApiDetails();

        //ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);

        //PowerMockito.mockStatic(ApiMgtDAO.class);

       // Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

//        apiMgtDAO = ApiMgtDAO.getInstance();
//        Float rating = apiMgtDAO.getAverageRating(1);
//


    }

    @Test
    void getApi() throws APIPersistenceException {



    }




}