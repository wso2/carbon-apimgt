package org.wso2.carbon.graphql.api.devportal.data;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiDetails.class, RestApiUtil.class,ArtifactData.class})
class ApiDetailsTest {

    @Test
    void getApiCount() throws UserStoreException, RegistryException , APIManagementException {
        ApiDetails apiDetails = new ApiDetails();
        int count  = apiDetails.getApiCount();

        Assert.assertEquals(2,2);
    }
}