/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.soaptorest.util;

import com.google.gson.Gson;
import org.apache.axis2.transport.http.HTTPConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.soaptorest.WSDL11SOAPOperationExtractor;
import org.wso2.carbon.apimgt.impl.soaptorest.WSDLSOAPOperationExtractor;
import org.wso2.carbon.apimgt.impl.soaptorest.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLOperationParam;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLSOAPOperation;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Util class used for soap operation binding related.
 */
public class SOAPOperationBindingUtils {

    /**
     * Gets soap operations to rest resources mapping
     * <p>
     * Note: This method directly called from the jaggery layer
     *
     * @param url WSDL URL
     * @return json string with the soap operation mapping
     * @throws APIManagementException if an error occurs when getting soap operations from the wsdl
     */
    public static String getSoapOperationMapping(String url) throws APIManagementException {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(url);
        byte[] wsdlContent = wsdlReader.getWSDL();
        WSDLSOAPOperationExtractor processor = getWSDLProcessor(wsdlContent, wsdlReader);
        Set<WSDLSOAPOperation> operations;
        try {
            operations = processor.getWsdlInfo().getSoapBindingOperations();
            populateSoapOperationParameters(operations);
            return new Gson().toJson(operations);
        } catch (APIMgtWSDLException e) {
            throw new APIManagementException("Error in soap to rest conversion for wsdl url: " + url, e);
        }
    }

    /**
     * Checks the api is a soap to rest converted one or a soap pass through
     * <p>
     * Note: This method directly called from the jaggery layer
     *
     * @param name     api name
     * @param version  api version
     * @param provider api provider
     * @return true if the api is soap to rest converted one. false if the user have a pass through
     * @throws APIManagementException if an error occurs when accessing the registry
     */
    public static boolean isSOAPToRESTApi(String name, String version, String provider) throws APIManagementException {
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        boolean isTenantFlowStarted = false;

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            int tenantId;
            UserRegistry registry;

            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                registry = registryService.getGovernanceSystemRegistry(tenantId);
                String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        provider + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE;
                return registry.resourceExists(resourcePath);
            } catch (RegistryException e) {
                handleException("Error when create registry instance ", e);
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information ", e);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return false;
    }

    /**
     * gets parameters from the soap operation and populates them in {@link WSDLSOAPOperation}
     *
     * @param soapOperations soap binding operations
     */
    private static void populateSoapOperationParameters(Set<WSDLSOAPOperation> soapOperations) {
        String[] primitiveTypes = { "string", "byte", "short", "int", "long", "float", "double", "boolean" };
        List primitiveTypeList = Arrays.asList(primitiveTypes);
        for (WSDLSOAPOperation op : soapOperations) {
            String resourcePath;
            String operationName = op.getName();
            op.setSoapBindingOpName(operationName);
            if (operationName.toLowerCase().startsWith("get")) {
                resourcePath = operationName.substring(3, operationName.length());
                op.setHttpVerb(HTTPConstants.HTTP_METHOD_GET);
            } else {
                resourcePath = operationName;
                op.setHttpVerb(HTTPConstants.HTTP_METHOD_POST);
            }
            resourcePath =
                    resourcePath.substring(0, 1).toLowerCase() + resourcePath.substring(1, resourcePath.length());
            op.setName(resourcePath);

            List<WSDLOperationParam> params = op.getParameters();
            for (WSDLOperationParam param : params) {
                if (param.getDataType() != null) {
                    String dataTypeWithNS = param.getDataType();
                    String dataType = dataTypeWithNS.substring(dataTypeWithNS.indexOf(":") + 1);
                    param.setDataType(dataType);
                    if (!primitiveTypeList.contains(dataType)) {
                        param.setComplexType(true);
                    }
                }
            }
        }
    }

    /**
     * Gets WSDL processor used to extract the soap binding operations
     *
     * @param content    WSDL content
     * @param wsdlReader WSDL reader used to parse the wsdl{@link APIMWSDLReader}
     * @return {@link WSDLSOAPOperationExtractor}
     * @throws APIManagementException
     */
    public static WSDLSOAPOperationExtractor getWSDLProcessor(byte[] content, APIMWSDLReader wsdlReader)
            throws APIManagementException {
        WSDLSOAPOperationExtractor processor = new WSDL11SOAPOperationExtractor(wsdlReader);
        try {
            boolean canProcess = processor.init(content);
            if (canProcess) {
                return processor;
            }
        } catch (APIMgtWSDLException e) {
            throw new APIManagementException("Error while instantiating wsdl processor class", e);
        }

        //no processors found if this line reaches
        throw new APIManagementException("No WSDL processor found to process WSDL content");
    }

    /**
     * converts a dom NodeList into a list of nodes
     *
     * @param list dom NodeList element
     * @return list of dom nodes
     */
    public static List<Node> list(final NodeList list) {
        return new AbstractList<Node>() {
            public int size() {
                return list.getLength();
            }

            public Node get(int index) {
                Node item = list.item(index);
                if (item == null)
                    throw new IndexOutOfBoundsException();
                return item;
            }
        };
    }

    public static List<Node> getElementsByTagName(Element e, String tag) {
        return list(e.getElementsByTagName(tag));
    }

}
