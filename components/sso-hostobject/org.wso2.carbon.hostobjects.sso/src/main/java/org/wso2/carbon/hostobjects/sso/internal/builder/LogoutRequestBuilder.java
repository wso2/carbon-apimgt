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

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.wso2.carbon.hostobjects.sso.internal.SSOConstants;
import org.wso2.carbon.hostobjects.sso.internal.util.*;

/**
 * This class is used to generate the Logout Requests.
 */
public class LogoutRequestBuilder {

    /**
     * Build the logout request
     * @param subject name of the user
     * @param reason reason for generating logout request.
     * @return LogoutRequest object
     */
    public LogoutRequest buildLogoutRequest(String subject,String sessionIndexId, String reason,
                                            String issuerId) {
        Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Util.createID());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        logoutReq.setIssuer(issuer);

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat(SSOConstants.SAML2_NAME_ID_POLICY);
        nameId.setValue(subject);
        logoutReq.setNameID(nameId);

        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(sessionIndexId);
        logoutReq.getSessionIndexes().add(sessionIndex);

        logoutReq.setReason(reason);

        return logoutReq;
    }

    /**
     * Overload Logout request for sessionIndexId is not exist case
     * @param subject
     * @param reason
     * @param issuerId
     * @return
     */
    public LogoutRequest buildLogoutRequest(String subject, String reason,
                                            String issuerId) {
        Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Util.createID());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        logoutReq.setIssuer(issuer);

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat(SSOConstants.SAML2_NAME_ID_POLICY);
        nameId.setValue(subject);
        logoutReq.setNameID(nameId);

        logoutReq.setReason(reason);

        return logoutReq;
    }
}
