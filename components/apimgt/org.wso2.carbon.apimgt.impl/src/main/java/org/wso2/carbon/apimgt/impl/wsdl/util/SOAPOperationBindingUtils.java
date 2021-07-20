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
package org.wso2.carbon.apimgt.impl.wsdl.util;

import io.swagger.models.Info;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.Json;
import org.apache.axis2.transport.http.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.WSDL11SOAPOperationExtractor;
import org.wso2.carbon.apimgt.impl.wsdl.WSDLProcessor;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLOperationParam;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLSOAPOperation;
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

import java.net.URL;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.AbstractList;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Util class used for soap operation binding related.
 */
public class SOAPOperationBindingUtils {
    private static final Logger log = LoggerFactory.getLogger(SOAPOperationBindingUtils.class);
    /**
     * Gets soap operations to rest resources mapping for a WSDL url
     *
     * @param url WSDL URL
     * @return swagger json string with the soap operation mapping
     * @throws APIManagementException if an error occurs when generating swagger
     */
    public static String getSoapOperationMappingForUrl(String url) throws APIManagementException {
        URL wsdlUrl = APIMWSDLReader.getURL(url);
        WSDL11SOAPOperationExtractor processor = APIMWSDLReader.getWSDLSOAPOperationExtractorForUrl(wsdlUrl);
        WSDLInfo wsdlInfo = processor.getWsdlInfo();
        return getGeneratedSwaggerFromWSDL(wsdlInfo);
    }

    /**
     * Gets soap operations to rest resources mapping for a wsdl byte content
     *
     * @param wsdlContent WSDL byte content
     * @return swagger json string with the soap operation mapping
     * @throws APIManagementException if an error occurs when generating swagger
     */
    public static String getSoapOperationMapping(byte [] wsdlContent) throws APIManagementException {
        WSDL11SOAPOperationExtractor processor = APIMWSDLReader.getWSDLSOAPOperationExtractor(wsdlContent);
        WSDLInfo wsdlInfo = processor.getWsdlInfo();
        return getGeneratedSwaggerFromWSDL(wsdlInfo);
    }

    /**
     * Gets soap operations to rest resources mapping for a wsdl archive path
     *
     * @param path Path of the extracted WSDL archive
     * @return swagger json string with the soap operation mapping
     * @throws APIManagementException if an error occurs when generating swagger
     */
    public static String getSoapOperationMapping(String path) throws APIManagementException {
        APIMWSDLReader wsdlReader = new APIMWSDLReader(path);
        WSDL11SOAPOperationExtractor processor = APIMWSDLReader.getWSDLSOAPOperationExtractor(path, wsdlReader);
        WSDLInfo wsdlInfo = processor.getWsdlInfo();
        return getGeneratedSwaggerFromWSDL(wsdlInfo);
    }

    /**
     * Generate the swagger from the WSDL info
     *
     * @param wsdlInfo WSDLInfo object which has parsed WSDL data
     * @return Generated the swagger from the WSDL info
     * @throws APIManagementException if an error occurs when generating swagger
     */
    private static String getGeneratedSwaggerFromWSDL(WSDLInfo wsdlInfo) throws APIManagementException {
        Set<WSDLSOAPOperation> operations;
        Map<String, ModelImpl> paramModelMap;
        String swaggerStr = SOAPToRESTConstants.EMPTY_STRING;
        operations = wsdlInfo.getSoapBindingOperations();
        paramModelMap = wsdlInfo.getParameterModelMap();
        populateSoapOperationParameters(operations);
        Swagger swaggerDoc = new Swagger();

        for (WSDLSOAPOperation operation : operations) {
            Path path = new Path();
            Operation op = new Operation();
            List<ModelImpl> inputParameterModel = operation.getInputParameterModel();
            List<ModelImpl> outputParameterModel = operation.getOutputParameterModel();
            if (HTTPConstants.HTTP_METHOD_GET.equals(operation.getHttpVerb())) {
                for (ModelImpl input : inputParameterModel) {
                    if (input != null && operation.getName().equalsIgnoreCase(input.getName())) {
                        Map<String, Property> properties = input.getProperties();
                        if (properties != null) {
                            for (String property : properties.keySet()) {
                                QueryParameter param = new QueryParameter();
                                param.setName(property);
                                param.setType(properties.get(property).getType());
                                op.addParameter(param);
                            }
                        }
                        inputParameterModel.remove(input);
                        break;
                    }
                }
            } else {
                //adding body parameter
                BodyParameter param = new BodyParameter();
                param.setName(APIConstants.OperationParameter.PAYLOAD_PARAM_NAME);
                param.setIn(APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE);
                param.setRequired(true);
                RefModel model = new RefModel();
                model.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + operation.getName()
                        + SOAPToRESTConstants.Swagger.INPUT_POSTFIX);
                param.setSchema(model);
                op.addParameter(param);
            }

            //adding response
            Response response = new Response();
            RefProperty refProperty = new RefProperty();
            refProperty.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + operation.getName()
                    + SOAPToRESTConstants.Swagger.OUTPUT_POSTFIX);
            response.setSchema(refProperty);
            response.setDescription(SOAPToRESTConstants.EMPTY_STRING);
            op.addResponse("default", response);

            op.setOperationId(operation.getSoapBindingOpName());

            //setting vendor extensions
            Map<String, String> extensions = new HashMap<>();
            extensions.put(SOAPToRESTConstants.Swagger.SOAP_ACTION, operation.getSoapAction());
            extensions.put(SOAPToRESTConstants.Swagger.SOAP_OPERATION, operation.getSoapBindingOpName());
            extensions.put(SOAPToRESTConstants.Swagger.NAMESPACE, operation.getTargetNamespace());
            if (wsdlInfo.isHasSoap12BindingOperations()) {
                extensions.put(SOAPToRESTConstants.Swagger.SOAP_VERSION, SOAPToRESTConstants.SOAP_VERSION_12);
            } else if (wsdlInfo.hasSoapBindingOperations()) {
                extensions.put(SOAPToRESTConstants.Swagger.SOAP_VERSION, SOAPToRESTConstants.SOAP_VERSION_11);
            }
            op.setVendorExtension(SOAPToRESTConstants.Swagger.WSO2_SOAP, extensions);

            if (!HTTPConstants.HTTP_METHOD_GET.equals(operation.getHttpVerb())) {
                ModelImpl inputModel = new ModelImpl();
                inputModel.setName(operation.getName() + SOAPToRESTConstants.Swagger.INPUT_POSTFIX);
                inputModel.setType(ObjectProperty.TYPE);
                Map<String, Property> inputPropertyMap = new HashMap<>();
                for (ModelImpl input : inputParameterModel) {
                    if (input != null && input.getProperties() != null) {
                        RefProperty inputRefProp;
                        if (input.getProperties().containsKey(input.getName())) {
                            inputRefProp = (RefProperty) input.getProperties().get(input.getName());
                        } else {
                            inputRefProp = new RefProperty();
                            inputRefProp.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + input.getName());
                        }
                        inputPropertyMap.put(input.getName(), inputRefProp);
                    }
                }
                inputModel.setProperties(inputPropertyMap);
                swaggerDoc
                        .addDefinition(operation.getName() + SOAPToRESTConstants.Swagger.INPUT_POSTFIX, inputModel);
            }

            ModelImpl outputModel = new ModelImpl();
            outputModel.setName(operation.getName() + SOAPToRESTConstants.Swagger.OUTPUT_POSTFIX);
            outputModel.setType(ObjectProperty.TYPE);
            Map<String, Property> outputPropertyMap = new HashMap<>();
            for (ModelImpl output : outputParameterModel) {
                if (output != null && output.getProperties() != null) {
                    RefProperty outputRefProp;
                    if (output.getProperties().containsKey(output.getName())) {
                        outputRefProp = (RefProperty) output.getProperties().get(output.getName());
                    } else {
                        outputRefProp = new RefProperty();
                        outputRefProp.set$ref(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT + output.getName());
                    }
                    outputPropertyMap.put(output.getName(), outputRefProp);
                }
            }
            outputModel.setProperties(outputPropertyMap);
            swaggerDoc.addDefinition(operation.getName() + SOAPToRESTConstants.Swagger.OUTPUT_POSTFIX, outputModel);

            path.set(operation.getHttpVerb().toLowerCase(), op);
            swaggerDoc.path("/" + operation.getName(), path);
            Info info = new Info();
            info.setVersion(SOAPToRESTConstants.EMPTY_STRING);
            info.setTitle(SOAPToRESTConstants.EMPTY_STRING);
            swaggerDoc.info(info);
        }
        if (paramModelMap != null) {
            for (String propertyName : paramModelMap.keySet()) {
                swaggerDoc.addDefinition(propertyName, paramModelMap.get(propertyName));
            }
        }
        try {
            swaggerStr = Json.pretty(swaggerDoc);
        } catch (Exception e) {
            String msg = "Error occurred while deserialize swagger model.";
            handleException(msg, e);
        }
        if (log.isDebugEnabled()) {
            log.debug(swaggerStr);
        }
        return swaggerStr;
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
                String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR
                        + APIUtil.replaceEmailDomain(provider) + RegistryConstants.PATH_SEPARATOR + name
                        + RegistryConstants.PATH_SEPARATOR + version + RegistryConstants.PATH_SEPARATOR
                        + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE;
                if (log.isDebugEnabled()) {
                    log.debug("Resource path to the soap to rest converted sequence: " + resourcePath);
                }
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
                operation.setHttpVerb(HTTPConstants.HTTP_METHOD_POST);
                if (operationName.toLowerCase().startsWith("get") && operation.getInputParameterModel() != null
                        && operation.getInputParameterModel().size() <= 1) {

                    Map<String, Property> properties = null;
                    if (operation.getInputParameterModel().size() > 0
                            && operation.getInputParameterModel().get(0) != null) {
                        properties = operation.getInputParameterModel().get(0).getProperties();
                    }
                    if (properties == null) {
                        operation.setHttpVerb(HTTPConstants.HTTP_METHOD_GET);
                    } else if (properties.size() <= 1) {
                        for (String property : properties.keySet()) {
                            String type = properties.get(property).getType();
                            if (!(type.equals(ObjectProperty.TYPE) || type.equals(ArrayProperty.TYPE) || type
                                    .equals(RefProperty.TYPE))) {
                                operation.setHttpVerb(HTTPConstants.HTTP_METHOD_GET);
                            }
                        }
                    }
                }
                resourcePath = operationName;
                resourcePath =
                        resourcePath.substring(0, 1).toLowerCase() + resourcePath.substring(1, resourcePath.length());
                operation.setName(resourcePath);
                if (log.isDebugEnabled()) {
                    log.debug("REST resource path for SOAP operation: " + operationName + " is: " + resourcePath);
                }

                List<WSDLOperationParam> params = operation.getParameters();
                if (log.isDebugEnabled() && params != null) {
                    log.debug("SOAP operation: " + operationName + " has " + params.size() + " parameters");
                }
                if (params != null) {
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
        } else {
            log.info("No SOAP operations found in the WSDL");
        }
    }

    /**
     * Gets WSDL processor used to extract the soap binding operations
     *
     * @param content    WSDL content
     * @param wsdlReader WSDL reader used to parse the wsdl{@link APIMWSDLReader}
     * @return {@link WSDLProcessor}
     * @throws APIManagementException
     */
    public static WSDL11SOAPOperationExtractor getWSDL11SOAPOperationExtractor(byte[] content,
            APIMWSDLReader wsdlReader) throws APIManagementException {
        WSDL11SOAPOperationExtractor wsdl11SOAPOperationExtractor = new WSDL11SOAPOperationExtractor(wsdlReader);
        wsdl11SOAPOperationExtractor.init(content);
        return wsdl11SOAPOperationExtractor;
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
