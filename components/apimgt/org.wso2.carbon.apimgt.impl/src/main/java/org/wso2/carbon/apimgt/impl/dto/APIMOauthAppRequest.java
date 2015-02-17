package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OauthAppRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIMOauthAppRequest extends OauthAppRequest {
    private String clientSecret;
    private String clientName;
    private String configUrl;
    private String configAccessToken;
    private String authMethod;

    private List<String> redirectURI = new ArrayList<String>();
    private String scope;
    private List<String> contacts = new ArrayList<String>();
    private List<String> grantTypes = new ArrayList<String>();
    private List<String> responseType = new ArrayList<String>();
    private Map<String,Object> paramMap = new HashMap<String,Object>();
    private String keyType;
    private String clientId;
    private String mappingId;

    public String getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    public String getConfigAccessToken() {
        return configAccessToken;
    }

    public void setConfigAccessToken(String configAccessToken) {
        this.configAccessToken = configAccessToken;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<String> getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(List<String> redirectURI) {
        this.redirectURI = redirectURI;
    }

    public List<String> getContacts() { return this.contacts; }

    public void setContacts(List<String> contacts){ this.contacts = contacts; }

    public void addParameter(String key, Object value){
        paramMap.put(key,value);
    }

    public Object getParameter(String key){
        return paramMap.get(key);
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getGrantTypes() { return this.grantTypes; }

    public void setGrantTypes(List<String> grantTypes){ this.grantTypes = grantTypes; }

    public List<String> getResponseType() { return this.responseType; }

    public void setResponseType(List<String> responseType){ this.responseType = responseType; }

    public String getAuthMethod() { return this.authMethod; }

    public void setAuthMethod(String authMethod){ this.authMethod = authMethod; }


    public void initialiseDTO(Map<String, Object> params) {
        if (params != null) {
            //this.setClientId((String) params.get("consumerKey"));
            //this.setClientSecret((String) params.get("consumerSecret"));
            this.setClientId((String) params.get(ApplicationConstants.OAUTH_CLIENT_ID));
            this.setClientSecret((String) params.get(ApplicationConstants.OAUTH_CLIENT_SECRET));
            this.setClientName((String) params.get(ApplicationConstants.OAUTH_CLIENT_NAME));
            this.setRedirectURI((List<String>) params.get(ApplicationConstants.OAUTH_REDIRECT_URIS));
            this.setKeyType((String) params.get(ApplicationConstants.APP_KEY_TYPE));
            this.setContacts((List<String>) params.get(ApplicationConstants.OAUTH_CLIENT_CONTACT));
            this.setScope((String) params.get(ApplicationConstants.OAUTH_CLIENT_SCOPE));
            this.setGrantTypes((List<String>) params.get(ApplicationConstants.OAUTH_CLIENT_GRANT));
            this.setResponseType((List<String>) params.get(ApplicationConstants.OAUTH_CLIENT_RESPONSETYPE));
            this.setAuthMethod((String) params.get(ApplicationConstants.OAUTH_CLIENT_AUTHMETHOD));
        }
    }


    public void saveDTO() throws APIManagementException {
        //  OIDCDao.saveAppInfoDTO(this);
    }


    public void retrieveDTO() throws APIManagementException {
        // OIDCDao.retrieveAppInfoDTO(this);
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }
}
