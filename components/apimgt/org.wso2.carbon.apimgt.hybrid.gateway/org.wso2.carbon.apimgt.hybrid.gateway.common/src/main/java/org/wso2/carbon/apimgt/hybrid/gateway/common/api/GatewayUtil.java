/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Implements utility methods related to API creation/deployment in on premise gateway
 */
public class GatewayUtil {
    private static final Log log = LogFactory.getLog(GatewayUtil.class);
    private static final String CUSTOM_API_EXECUTOR_CLASS =
            "APIExecutionHandler";
    private static final String CLASS = "class";
    private static final String FOR_EVENT = "forEvent";
    private static final String STATE = "state";
    private static final String ID = "id";
    private static final String DATA = "data";
    private static final String API_LIFECYCLE = "APILifeCycle";
    private static final String EXECUTION_TAG = "execution";
    private static final String RE_PUBLISH_ATTRIBUTE = "Re-Publish";
    private static final String PUBLISH_ATTRIBUTE = "Publish";
    private static final String PUBLISHED_STATE = "Published";

    private static String tenantDomain;

    /**
     * Method to execute APILifecycleConfig xml changes required for synchronizing API updates with micro API gateway
     *
     */
    public static void updateAPILifecycleConfig() throws OnPremiseGatewayException {
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            String adminName = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminUserName();
            Registry configUserRegistry = ServiceReferenceHolder.getInstance().getRegistryService()
                            .getConfigUserRegistry(adminName, tenantId);
            String apiLifeCycleConfig = CommonUtil.getLifecycleConfiguration(API_LIFECYCLE, configUserRegistry);
            Document doc = convertStringToDocument(apiLifeCycleConfig);
            //Replace default API executor
            int executionElementNo = 0;
            if (doc != null) {
                executionElementNo = doc.getElementsByTagName(EXECUTION_TAG).getLength();
            }
            for (int i = 0; i < executionElementNo; i++) {
                Element element = (Element) doc.getElementsByTagName(EXECUTION_TAG).item(i);
                if (RE_PUBLISH_ATTRIBUTE.equals(element.getAttribute(FOR_EVENT)) ||
                        PUBLISH_ATTRIBUTE.equals(element.getAttribute(FOR_EVENT))) {
                    element.setAttribute(CLASS, CUSTOM_API_EXECUTOR_CLASS);
                }
            }
            //Add new execution for 'Published' state
            executionElementNo = 0;
            if (doc != null) {
                executionElementNo = doc.getElementsByTagName(STATE).getLength();
            }
            for (int i = 0; i < executionElementNo; i++) {
                Element element = (Element) doc.getElementsByTagName(STATE).item(i);
                Element dataElement;
                Element newElement;
                if (PUBLISHED_STATE.equals(element.getAttribute(ID))) {
                    dataElement = (Element) element.getElementsByTagName(DATA).item(0);
                    newElement = doc.createElement(EXECUTION_TAG);
                    newElement.setAttribute(FOR_EVENT, PUBLISH_ATTRIBUTE);
                    newElement.setAttribute(CLASS, CUSTOM_API_EXECUTOR_CLASS);
                    dataElement.appendChild(newElement);
                }
            }
            String updatedApiLifeCycleConfig = convertDocumentToString(doc);
            // ToDo Generalise the fix for automatic xmlns="" namespace (un)declaration
            updatedApiLifeCycleConfig = updatedApiLifeCycleConfig.replaceAll("xmlns=\"\"", "");
            CommonUtil.updateLifecycle(API_LIFECYCLE, updatedApiLifeCycleConfig, configUserRegistry,
                    CommonUtil.getRootSystemRegistry());
        } catch (XMLStreamException | RegistryException | UserStoreException | TransformerException | SAXException |
                ParserConfigurationException | IOException e) {
            throw new OnPremiseGatewayException("An error occurred while overriding default API execution class of " +
                    "tenant domain: " + tenantDomain + ".", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Method to convert a Document to string
     *
     * @param doc Document to be converted
     * @return string representation of the given document
     */
    private static String convertDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    /**
     * Method to convert a string to a Document
     *
     * @param xmlStr String to be converted to a Document
     * @return Document representation of the given string
     */
    private static Document convertStringToDocument(String xmlStr) throws IOException, SAXException,
            ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlStr)));

    }
}
