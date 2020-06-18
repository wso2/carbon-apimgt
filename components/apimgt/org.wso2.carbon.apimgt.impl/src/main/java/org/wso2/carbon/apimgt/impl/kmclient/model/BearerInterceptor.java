package org.wso2.carbon.apimgt.impl.kmclient.model;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;

public class BearerInterceptor implements RequestInterceptor {

    private AccessTokenGenerator accessTokenGenerator;

    public BearerInterceptor(AccessTokenGenerator accessTokenGenerator) {

        this.accessTokenGenerator = accessTokenGenerator;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {

        String accessToken = accessTokenGenerator.getAccessToken(APIConstants.KEY_MANAGER_OAUTH2_REST_API_MGT_SCOPES);
        requestTemplate
                .header(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BEARER + accessToken);
    }
}
