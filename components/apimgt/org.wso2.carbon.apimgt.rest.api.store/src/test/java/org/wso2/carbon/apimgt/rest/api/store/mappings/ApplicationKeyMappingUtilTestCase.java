package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.ApplicationToken;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenDTO;

import java.util.ArrayList;
import java.util.List;

public class ApplicationKeyMappingUtilTestCase {

    @Test
    public void testFromApplicationKeysToDTO() {

        String keyType = "PRODUCTION";

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setKeyType(keyType);
        oAuthApplicationInfo.setClientId("clientID");
        oAuthApplicationInfo.setClientSecret("clientSecret");
        oAuthApplicationInfo.setGrantTypes(grantTypes);

        ApplicationKeysDTO applicationKeysDTO =
                ApplicationKeyMappingUtil.fromApplicationKeysToDTO(oAuthApplicationInfo);

        Assert.assertEquals(applicationKeysDTO.getKeyType().toString(), keyType);
    }

    @Test
    public void testFromApplicationTokenToDTO() {

        String accessToken = "123123123123123123132";

        ApplicationToken applicationToken = new ApplicationToken();
        applicationToken.setAccessToken(accessToken);
        applicationToken.setScopes("Scope1");
        applicationToken.setValidityPeriod(100000);

        ApplicationTokenDTO applicationTokenDTO =
                ApplicationKeyMappingUtil.fromApplicationTokenToDTO(applicationToken);

        Assert.assertEquals(applicationTokenDTO.getAccessToken(), accessToken);
    }
}
