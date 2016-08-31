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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class SAMLGroupIDExtractorImpl implements LoginPostExecutor {

    private static final Log log = LogFactory.getLog(SAMLGroupIDExtractorImpl.class);

    public String getGroupingIdentifiers(String loginResponse) {
        ByteArrayInputStream samlResponseStream = null;
        DocumentBuilder docBuilder;
        String username = "";
        String organization = "";
        try {
            String claim = "http://wso2.org/claims/organization";
            samlResponseStream = new ByteArrayInputStream(loginResponse.getBytes());
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
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

}
