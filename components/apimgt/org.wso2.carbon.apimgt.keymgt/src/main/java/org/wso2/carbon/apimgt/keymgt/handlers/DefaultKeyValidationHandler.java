package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.identity.oauth2.validators.OAuth2TokenValidationMessageContext;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DefaultKeyValidationHandler extends AbstractKeyValidationHandler {

    private static final Log log = LogFactory.getLog(DefaultKeyValidationHandler.class);
    private ApiMgtDAO apiMgtDAO = new ApiMgtDAO();

    public DefaultKeyValidationHandler(){
        log.info(this.getClass().getName() + " Initialised");
    }

    @Override
    public boolean validateToken(TokenValidationContext validationContext) throws APIKeyMgtException {


        if (validationContext.isCacheHit()) {
            APIKeyValidationInfoDTO infoDTO = validationContext.getValidationInfoDTO();

            // TODO: This should only happen in GW
            checkClientDomainAuthorized(infoDTO, validationContext.getClientDomain());
            boolean tokenExpired = APIUtil.isAccessTokenExpired(infoDTO);
            if (tokenExpired) {
                infoDTO.setAuthorized(false);
                log.debug("Token " + validationContext.getAccessToken() + " expired.");
                return false;
            } else {
                return true;
            }
        }

        String context = validationContext.getContext();
        String version =  validationContext.getVersion();
        String requiredAuthenticationLevel= validationContext.getRequiredAuthenticationLevel();
        String accessToken = validationContext.getAccessToken();
        String matchingResource = validationContext.getMatchingResource();
        String httpVerb = validationContext.getHttpVerb();

        if(context == null || version == null || requiredAuthenticationLevel == null || accessToken == null ||
                matchingResource == null || httpVerb == null){
            throw new APIKeyMgtException("Required parameters are not set.");
        }

        //If validation info is not cached creates fresh api key validation information object and returns it
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = null;
        try {
            apiKeyValidationInfoDTO = apiMgtDAO.validateKey(context, version, accessToken,requiredAuthenticationLevel);
            validationContext.setValidationInfoDTO(apiKeyValidationInfoDTO);
        } catch (APIManagementException e) {
            log.error("Error occurred while calling validateKey method ",e);
            throw new APIKeyMgtException(e.getMessage(),e);
        }

        return apiKeyValidationInfoDTO.isAuthorized();
    }

    private void checkClientDomainAuthorized (APIKeyValidationInfoDTO apiKeyValidationInfoDTO, String clientDomain)
            throws APIKeyMgtException {
        if (clientDomain != null) {
            clientDomain = clientDomain.trim();
        }
        List<String> authorizedDomains = apiKeyValidationInfoDTO.getAuthorizedDomains();
        if (!(authorizedDomains.contains("ALL") || authorizedDomains.contains(clientDomain))) {
            log.error("Unauthorized client domain :" + clientDomain +
                    ". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
            throw new APIKeyMgtException("Unauthorized client domain :" + clientDomain +
                    ". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
        }

    }



    @Override
    public boolean validateScopes(TokenValidationContext validationContext) throws APIKeyMgtException {

        if(validationContext.isCacheHit()){
            return true;
        }

        OAuth2ScopeValidator scopeValidator = OAuthServerConfiguration.getInstance().getoAuth2ScopeValidator();


        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = validationContext.getValidationInfoDTO();

        if(apiKeyValidationInfoDTO == null){
            throw new APIKeyMgtException("Key Validation information not set");
        }

        String[] scopes = null;
        Set<String> scopesSet = apiKeyValidationInfoDTO.getScopes();
        if(scopesSet != null && !scopesSet.isEmpty()){
            scopes = scopesSet.toArray(new String[scopesSet.size()]);
            if(log.isDebugEnabled()){
                log.debug("Scopes allowed for token : "+validationContext.getAccessToken()+" : "+scopes);
            }
        }

        AccessTokenDO accessTokenDO = new AccessTokenDO(apiKeyValidationInfoDTO.getConsumerKey(),
                apiKeyValidationInfoDTO.getEndUserName(), scopes,
                null, apiKeyValidationInfoDTO.getValidityPeriod(),
                apiKeyValidationInfoDTO.getType());

        accessTokenDO.setAccessToken(validationContext.getAccessToken());

        try {
            if(scopeValidator != null){
                if(scopeValidator.validateScope(accessTokenDO,
                                                validationContext.getMatchingResource())){
                    return true;

                }else {
                    apiKeyValidationInfoDTO.setAuthorized(false);
                    apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
                }
            }
        } catch (IdentityOAuth2Exception e) {
            log.error("ERROR while validating token scope " + e.getMessage());
            apiKeyValidationInfoDTO.setAuthorized(false);
            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
        }

        return false;

    }

    @Override
    public boolean generateConsumerToken(TokenValidationContext oAuth2TokenValidationMessageContext) throws APIKeyMgtException {
        return true;
    }
}
