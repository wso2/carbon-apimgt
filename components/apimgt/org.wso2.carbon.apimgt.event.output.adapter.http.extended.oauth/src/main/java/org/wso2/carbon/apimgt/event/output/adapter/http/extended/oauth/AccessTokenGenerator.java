package org.wso2.carbon.apimgt.event.output.adapter.http.extended.oauth;

public interface AccessTokenGenerator {
    String getAccessToken(String oauthUrl, String consumerKey, String consumerSecret);

}
