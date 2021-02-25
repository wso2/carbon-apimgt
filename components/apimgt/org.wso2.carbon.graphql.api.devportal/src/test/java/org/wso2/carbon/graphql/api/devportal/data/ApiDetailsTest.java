package org.wso2.carbon.graphql.api.devportal.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.service.RegistryPersistenceService;
import org.wso2.carbon.graphql.api.devportal.service.ApiRegistryService;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiRegistryService.class, RestApiUtil.class, RegistryPersistenceService.class,ApiMgtDAO.class})
class ApiDetailsTest {

    //public static ApiMgtDAO apiMgtDAO;
    @Test
    void getApiCount() throws  APIManagementException {
        ApiRegistryService apiDetails = new ApiRegistryService();

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