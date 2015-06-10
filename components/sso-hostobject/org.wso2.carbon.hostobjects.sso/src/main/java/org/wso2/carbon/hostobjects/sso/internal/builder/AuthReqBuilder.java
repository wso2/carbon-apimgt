/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.hostobjects.sso.internal.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.wso2.carbon.hostobjects.sso.internal.SSOConstants;
import org.wso2.carbon.hostobjects.sso.internal.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthReqBuilder {

    private static Log log = LogFactory.getLog(AuthReqBuilder.class);
        /**
     * Generate an authentication request.
     *
     * @return AuthnRequest Object
     * @throws Exception error when bootstrapping
     */
    public AuthnRequest buildAuthenticationRequest(String issuerId) throws Exception {
        Util.doBootstrap();
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer( issuerId));
        authnRequest.setNameIDPolicy(buildNameIDPolicy());
        return authnRequest;
    }

    /**
     * Generate an Authentication request with passiveAuth and assertionConsumerServiceURL
     *
     * @return AuthnRequest Object
     * @throws Exception error when bootstrapping
     */
    public AuthnRequest buildPassiveAuthenticationRequest(String issuerId, String acsUrl) throws Exception  {
        Util.doBootstrap();
        //matches shortest segments that are between '{' and '}'
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(acsUrl);
        while (matcher.find()) {
            String match = matcher.group(1);
            String property = System.getProperty(match);
            if (property != null) {
                acsUrl = acsUrl.replace("{" + match + "}", property);
            } else {
                log.warn("System Property " + match + " is not set");
            }
        }
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer( issuerId));
        authnRequest.setNameIDPolicy(buildNameIDPolicy());
        authnRequest.setIsPassive(true);
        authnRequest.setAssertionConsumerServiceURL(acsUrl);
        return authnRequest;
    }

    /**
     * Build the issuer object
     *
     * @return Issuer object
     */
    private static Issuer buildIssuer(String issuerId) {
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        return issuer;
    }

    /**
     * Build the NameIDPolicy object
     *
     * @return NameIDPolicy object
     */
    private static NameIDPolicy buildNameIDPolicy() {
        NameIDPolicy nameIDPolicy = new NameIDPolicyBuilder().buildObject();
        nameIDPolicy.setFormat(SSOConstants.SAML2_NAME_ID_POLICY);
        nameIDPolicy.setAllowCreate(true);
        return nameIDPolicy;
    }
}
