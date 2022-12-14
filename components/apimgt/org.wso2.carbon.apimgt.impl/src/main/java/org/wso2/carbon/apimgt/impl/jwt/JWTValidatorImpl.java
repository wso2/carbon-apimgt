/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.exception.CommonGatewayException;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwt.CommonJWTValidatorImpl;
import org.wso2.carbon.apimgt.common.gateway.jwt.JWTValidatorConfiguration;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.DefaultJWTTransformer;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.JWTTransformer;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.MalformedURLException;
import java.net.URL;

@Deprecated
public class JWTValidatorImpl extends CommonJWTValidatorImpl implements JWTValidator {

    private Log log = LogFactory.getLog(JWTValidatorImpl.class);

    @Override
    public JWTValidationInfo validateToken(SignedJWTInfo signedJWTInfo) throws APIManagementException {

        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        boolean state;
        try {
            state = validateSignature(signedJWTInfo.getSignedJWT());
            if (state) {
                JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
                state = isValidCertificateBoundAccessToken(signedJWTInfo);
                if (state) {
                    state = validateTokenExpiry(jwtClaimsSet);
                    if (state) {
                        jwtValidationInfo.setConsumerKey(getConsumerKey(jwtClaimsSet));
                        jwtValidationInfo.setScopes(getScopes(jwtClaimsSet));
                        jwtValidationInfo.setAppToken(getIsAppToken(jwtClaimsSet));
                        JWTClaimsSet transformedJWTClaimSet = transformJWTClaims(jwtClaimsSet);
                        createJWTValidationInfoFromJWT(jwtValidationInfo, transformedJWTClaimSet);
                        jwtValidationInfo.setRawPayload(signedJWTInfo.getToken());
                        return jwtValidationInfo;
                    } else {
                        jwtValidationInfo.setValid(false);
                        jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                        return jwtValidationInfo;
                    }
                } else {
                    jwtValidationInfo.setValid(false);
                    jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                    return jwtValidationInfo;

                }
            } else {
                jwtValidationInfo.setValid(false);
                jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return jwtValidationInfo;
            }
        } catch (JWTGeneratorException e) {
            throw new APIManagementException("Error while parsing JWT", e);
        } catch (CommonGatewayException e) {
            throw new APIManagementException("Error while validating the JWT signature");
        }
    }

    @Override
    public void loadTokenIssuerConfiguration(TokenIssuerDto tokenIssuerConfigurations) {

        this.tokenIssuer = tokenIssuerConfigurations;
        JWTTransformer jwtTransformer = ServiceReferenceHolder.getInstance().getJWTTransformer(tokenIssuer.getIssuer());
        if (jwtTransformer != null) {
            this.jwtTransformer = jwtTransformer;
        } else {
            this.jwtTransformer = new DefaultJWTTransformer();
        }
        this.jwtTransformer.loadConfiguration(tokenIssuer);
        boolean enableCertificateBoundAccessToken = false;

        JWTValidatorConfiguration.Builder builder = new JWTValidatorConfiguration.Builder()
                .jwtTransformer(ServiceReferenceHolder.getInstance().getJWTTransformer(tokenIssuer.getIssuer()))
                .jwtIssuer(tokenIssuerConfigurations)
                .trustStore(ServiceReferenceHolder.getInstance().getTrustStore());

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config != null) {
            String firstProperty = config
                    .getFirstProperty(APIConstants.ENABLE_CERTIFICATE_BOUND_ACCESS_TOKEN);
            // TODO: (VirajSalaka) Check the default behavior
            builder.enableCertificateBoundAccessToken(Boolean.parseBoolean(firstProperty));
        }
        long timestampSkew =
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getTimeStampSkewInSeconds();
        builder.timeStampSkewInSeconds(timestampSkew);
        super.loadConfiguration(builder.build());
    }
}
