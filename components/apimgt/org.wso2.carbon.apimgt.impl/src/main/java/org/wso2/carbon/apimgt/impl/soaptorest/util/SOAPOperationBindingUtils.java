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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.soaptorest.WSDL11SOAPOperationExtractor;
import org.wso2.carbon.apimgt.impl.soaptorest.WSDL20SOAPOperationExtractor;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Util class used for soap operation binding related.
 */
public class SOAPOperationBindingUtils {
    private static final Logger log = LoggerFactory.getLogger(SOAPOperationBindingUtils.class);
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
        String operationMapping = "";
        try {
            operations = processor.getWsdlInfo().getSoapBindingOperations();
            populateSoapOperationParameters(operations);
            operationMapping = new Gson().toJson(operations);
        } catch (APIMgtWSDLException e) {
            handleException("Error in soap to rest conversion for wsdl url: " + url, e);
        }
        return operationMapping;
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
                APIUtil.loadTenantRegistry(tenantId);
                registry = registryService.getGovernanceSystemRegistry(tenantId);
                String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        provider + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE;
                return registry.resourceExists(resourcePath);
            } catch (RegistryException e) {
                handleException("Error when create registry instance", e);
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information", e);
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
        if (soapOperations != null) {
            for (WSDLSOAPOperation operation : soapOperations) {
                String resourcePath;
                String operationName = operation.getName();
                operation.setSoapBindingOpName(operationName);
                if (operationName.toLowerCase().startsWith("get")) {
                    resourcePath = operationName.substring(3, operationName.length());
                    operation.setHttpVerb(HTTPConstants.HTTP_METHOD_GET);
                } else {
                    resourcePath = operationName;
                    operation.setHttpVerb(HTTPConstants.HTTP_METHOD_POST);
                }
                resourcePath =
                        resourcePath.substring(0, 1).toLowerCase() + resourcePath.substring(1, resourcePath.length());
                operation.setName(resourcePath);
                if (log.isDebugEnabled()) {
                    log.debug("REST resource path for SOAP operation: " + operationName + " is: " + resourcePath);
                }

                List<WSDLOperationParam> params = operation.getParameters();
                if (log.isDebugEnabled()) {
                    log.debug("SOAP operation: " + operationName + " has " + params.size() + " parameters");
                }
                if(params != null) {
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
                } else {
                    log.info("No parameters found for SOAP operation: " + operationName);
                }
            }
        } else {
            log.info("No SOAP operations found in the WSDL");
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
            } else {
                throw new APIManagementException("No WSDL processor found to process WSDL content");
            }
        } catch (APIMgtWSDLException e) {
            throw new APIManagementException("Error while instantiating wsdl processor class", e);
        }
    }

    /**
     * Returns the appropriate WSDL 1.1/WSDL 2.0 based on the file path {@code wsdlPath}.
     *
     * @param wsdlPath File path containing WSDL files and dependant files
     * @return WSDL 1.1 processor for the provided content
     * @throws APIManagementException If an error occurs while determining the processor
     */
    public static WSDLSOAPOperationExtractor getWSDLProcessor(String wsdlPath) throws APIManagementException {
        WSDLSOAPOperationExtractor wsdl11Processor = new WSDL11SOAPOperationExtractor();
        WSDLSOAPOperationExtractor wsdl20Processor = new WSDL20SOAPOperationExtractor();
        boolean canProcess;
        try {
            canProcess = wsdl11Processor.initPath(wsdlPath);
            if (canProcess) {
                return wsdl11Processor;
            } else if (wsdl20Processor.initPath(wsdlPath)){
                return wsdl20Processor;
            }
        } catch (APIMgtWSDLException e) {
            handleException("Error while instantiating wsdl processor class.", e);
        }
        //no processors found if this line reaches
        throw new APIManagementException("No WSDL processor found to process WSDL content.");
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
