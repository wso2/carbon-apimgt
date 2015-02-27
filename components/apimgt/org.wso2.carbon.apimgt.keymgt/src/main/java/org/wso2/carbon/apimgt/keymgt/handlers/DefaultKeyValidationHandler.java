package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.applications.ApplicationCreator;
import org.wso2.carbon.apimgt.impl.clients.OAuth2TokenValidationServiceClient;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
                infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_ACCESS_TOKEN_EXPIRED);
                log.debug("Token " + validationContext.getAccessToken() + " expired.");
                return false;
            } else {
                return true;
            }
        }

        AccessTokenInfo tokenInfo = null;

        try {
            tokenInfo = ApplicationCreator.getKeyManager().getTokenMetaData(validationContext.getAccessToken());

            if (tokenInfo == null || !tokenInfo.isTokenValid()) {
                return false;
            }

            validationContext.setTokenInfo(tokenInfo);
            //TODO: Eliminate use of APIKeyValidationInfoDTO if possible

            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            validationContext.setValidationInfoDTO(apiKeyValidationInfoDTO);

            if (!hasTokenRequiredAuthLevel(validationContext.getRequiredAuthenticationLevel(), tokenInfo)) {
                apiKeyValidationInfoDTO.setAuthorized(false);
                apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE);
                return false;
            }

            apiKeyValidationInfoDTO.setAuthorized(tokenInfo.isTokenValid());
            apiKeyValidationInfoDTO.setEndUserName(tokenInfo.getEndUserName());
            apiKeyValidationInfoDTO.setConsumerKey(tokenInfo.getConsumerKey());
            apiKeyValidationInfoDTO.setIssuedTime(tokenInfo.getIssuedTime());
            apiKeyValidationInfoDTO.setValidityPeriod(tokenInfo.getValidityPeriod());

            if (tokenInfo.getScopes() != null) {
                Set<String> scopeSet = new HashSet<String>(Arrays.asList(tokenInfo.getScopes()));
                apiKeyValidationInfoDTO.setScopes(scopeSet);
            }

        } catch (APIManagementException e) {
            log.error("Error while obtaining Token Metadata from Authorization Server", e);
            throw new APIKeyMgtException("Error while obtaining Token Metadata from Authorization Server");
        }

        return tokenInfo.isTokenValid();
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

        if (scopesSet != null && !scopesSet.isEmpty()) {
            scopes = scopesSet.toArray(new String[scopesSet.size()]);
            if (log.isDebugEnabled() && scopes != null) {
                StringBuffer scopeList = new StringBuffer();
                for (String scope : scopes) {
                    scopeList.append(scope + ",");
                }
                scopeList.deleteCharAt(scopeList.length() - 1);
                log.debug("Scopes allowed for token : " + validationContext.getAccessToken() + " : " + scopeList.toString());
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

}
