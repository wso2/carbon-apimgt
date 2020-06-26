/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.tokenmgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.wso2.carbon.apimgt.tokenmgt.TokenMgtException;
import org.wso2.carbon.apimgt.tokenmgt.handlers.ResourceConstants;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenMgtUtil {

    private static final Log log = LogFactory.getLog(TokenMgtUtil.class);

    private static final String AUTHENTICATOR_NAME = ResourceConstants.SAML2_SSO_AUTHENTICATOR_NAME;

    /**
     * Get the role list from the SAML2 Assertion
     *
     * @param assertion SAML2 assertion
     * @return Role list from the assertion
     */
    public static String[] getRolesFromAssertion(Assertion assertion) {
        List<String> roles = new ArrayList<String>();
        String roleClaim = getRoleClaim();
        List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();

        if (attributeStatementList != null) {
            for (AttributeStatement statement : attributeStatementList) {
                List<Attribute> attributesList = statement.getAttributes();
                for (Attribute attribute : attributesList) {
                    String attributeName = attribute.getName();
                    if (attributeName != null && roleClaim.equals(attributeName)) {
                        List<XMLObject> attributeValues = attribute.getAttributeValues();
                        if (attributeValues != null && attributeValues.size() == 1) {
                            String attributeValueString = getAttributeValue(attributeValues.get(0));
                            String multiAttributeSeparator = getAttributeSeparator();
                            String[] attributeValuesArray = attributeValueString.split(multiAttributeSeparator);
                            if (log.isDebugEnabled()) {
                                log.debug("Adding attributes for Assertion: " + assertion + " AttributeName : " +
                                        attributeName + ", AttributeValue : " + Arrays.toString(attributeValuesArray));
                            }
                            roles.addAll(Arrays.asList(attributeValuesArray));
                        } else if (attributeValues != null && attributeValues.size() > 1) {
                            for (XMLObject attributeValue : attributeValues) {
                                String attributeValueString = getAttributeValue(attributeValue);
                                if (log.isDebugEnabled()) {
                                    log.debug("Adding attributes for Assertion: " + assertion + " AttributeName : " +
                                            attributeName + ", AttributeValue : " + attributeValue);
                                }
                                roles.add(attributeValueString);
                            }
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Role list found for assertion: " + assertion + ", roles: " + roles);
        }
        return roles.toArray(new String[roles.size()]);
    }

    private static String getAttributeValue(XMLObject attributeValue) {
        if (attributeValue == null){
            return null;
        } else if (attributeValue instanceof XSString){
            return getStringAttributeValue((XSString) attributeValue);
        } else if(attributeValue instanceof XSAnyImpl){
            return getAnyAttributeValue((XSAnyImpl) attributeValue);
        } else {
            return attributeValue.toString();
        }
    }

    private static String getStringAttributeValue(XSString attributeValue) {
        return attributeValue.getValue();
    }

    private static String getAnyAttributeValue(XSAnyImpl attributeValue) {
        return attributeValue.getTextContent();
    }

    /**
     * Get attribute separator from configuration or from the constants
     *
     * @return
     */
    private static String getAttributeSeparator() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(AUTHENTICATOR_NAME);

        if (authenticatorConfig != null) {
            Map<String, String> configParameters = authenticatorConfig.getParameters();
            if (configParameters.containsKey(ResourceConstants.ATTRIBUTE_VALUE_SEPARATOR)) {
                return configParameters.get(ResourceConstants.ATTRIBUTE_VALUE_SEPARATOR);
            }
        }

        return ResourceConstants.ATTRIBUTE_VALUE_SEPERATER;
    }

    /**
     * Role claim attribute value from configuration file or from constants
     *
     * @return
     */
    private static String getRoleClaim() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(AUTHENTICATOR_NAME);

        if (authenticatorConfig != null) {
            Map<String, String> configParameters = authenticatorConfig.getParameters();
            if (configParameters.containsKey(ResourceConstants.ROLE_CLAIM_ATTRIBUTE)) {
                return configParameters.get(ResourceConstants.ROLE_CLAIM_ATTRIBUTE);
            }
        }

        return ResourceConstants.ROLE_ATTRIBUTE_NAME;
    }

}
