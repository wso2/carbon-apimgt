/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.api.LoginPostExecutor;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class SAMLGroupIDExtractorImpl implements LoginPostExecutor {

    private static final Log log = LogFactory.getLog(SAMLGroupIDExtractorImpl.class);

    public String getGroupingIdentifiers(String loginResponse) {
        if (log.isDebugEnabled()) {
            log.debug("Login response " + loginResponse);
        }
        ByteArrayInputStream samlResponseStream = null;
        DocumentBuilder docBuilder;
        String username = "";
        String organization = "";
        try {
            String claim = "http://wso2.org/claims/organization";
            samlResponseStream = getByteArrayInputStream(loginResponse);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builderFactory.setNamespaceAware(true);
            docBuilder = builderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(samlResponseStream);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            Response response = (Response) unmarshaller.unmarshall(element);
            List<Assertion> assertions = response.getAssertions();
            if (assertions != null && assertions.size() > 0) {
                Subject subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        username = subject.getNameID().getValue();
                    }
                }
            }

            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            organization =
                    manager.getUserClaimValue(MultitenantUtils.getTenantAwareUsername(username), claim, null);
            if (log.isDebugEnabled()) {
                log.debug("User organization " + organization);
            }
            if (organization != null) {
                organization = tenantDomain + "/" + organization.trim();
            }
        } catch (ParserConfigurationException e) {
            String msg = "Error while parsing SAML Assertion";
            log.error(msg, e);
        } catch (UnmarshallingException e) {
            String msg = "Error while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (SAXException e) {
            String msg = "Parsing exception  occur while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (IOException e) {
            String msg = "IO exception happen while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (UserStoreException e) {
            log.error("User store exception occurred for user" + username, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while checking user existence for " + username, e);
        } finally {
            if (samlResponseStream != null) {
                try {
                    samlResponseStream.close();
                } catch (IOException e) {
                    //Ignore
                    log.error("ERROR_CLOSING_STREAM");
                }
            }
        }
        return organization;
    }

    protected ByteArrayInputStream getByteArrayInputStream(String loginResponse) {
        return new ByteArrayInputStream(loginResponse.getBytes());
    }

}
