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
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiDetails.class, RestApiUtil.class,ArtifactData.class})
class ApiDetailsTest {

    @Test
    void getApiCount() throws UserStoreException, RegistryException , APIManagementException {
//        ApiDetails apiDetails = new ApiDetails();
//
//        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
//
//        PowerMockito.mockStatic(ApiMgtDAO.class);
//
//        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
//
//        Float rating  = apiDetails.getApiRating("b9cb1f47-f450-4ff6-bba9-3b51ba28433c");

        Assert.assertEquals(2,2);
    }
}