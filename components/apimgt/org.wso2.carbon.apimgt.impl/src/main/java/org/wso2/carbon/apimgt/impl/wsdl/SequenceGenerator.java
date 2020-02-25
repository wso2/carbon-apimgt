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
package org.wso2.carbon.apimgt.impl.wsdl;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.template.RESTToSOAPMsgTemplate;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Class uses to generate api sequences for soap to rest conversion.
 */
public class SequenceGenerator {
    private static final Logger log = LoggerFactory.getLogger(SequenceGenerator.class);

    /**
     * Generates in/out sequences from the swagger given
     *
     * @param swaggerStr swagger string
     * @param apiIdentifier api identifier object
     * @throws APIManagementException
     */
    public static void generateSequencesFromSwagger(String swaggerStr, APIIdentifier apiIdentifier)
            throws APIManagementException {

        Swagger swagger = new SwaggerParser().parse(swaggerStr);
        Map<String, Model> definitions = swagger.getDefinitions();

        // Configure serializers
        SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);
        Yaml.mapper().registerModule(simpleModule);

        Map<String, Path> paths = swagger.getPaths();

        for (String pathName : paths.keySet()) {
            Path path = paths.get(pathName);

            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            for (HttpMethod httpMethod : operationMap.keySet()) {
                boolean isResourceFromWSDL = false;
                Map<String, String> parameterJsonPathMapping = new HashMap<>();
                Map<String, String> queryParameters = new HashMap<>();
                Operation operation = operationMap.get(httpMethod);
                String operationId = operation.getOperationId();

                //get vendor extensions
                Map<String, Object> vendorExtensions = operation.getVendorExtensions();
                Object vendorExtensionObj = vendorExtensions.get("x-wso2-soap");

                String soapAction = SOAPToRESTConstants.EMPTY_STRING;
                String namespace = SOAPToRESTConstants.EMPTY_STRING;
                String soapVersion = SOAPToRESTConstants.EMPTY_STRING;
                if (vendorExtensionObj != null) {
                    soapAction = (String) ((LinkedHashMap) vendorExtensionObj).get("soap-action");
                    namespace = (String) ((LinkedHashMap) vendorExtensionObj).get("namespace");
                    soapVersion = (String) ((LinkedHashMap) vendorExtensionObj)
                            .get(SOAPToRESTConstants.Swagger.SOAP_VERSION);
                    isResourceFromWSDL = true;
                }
                String soapNamespace = SOAPToRESTConstants.SOAP12_NAMSPACE;
                if (StringUtils.isNotBlank(soapVersion) && SOAPToRESTConstants.SOAP_VERSION_11.equals(soapVersion)) {
                    soapNamespace = SOAPToRESTConstants.SOAP11_NAMESPACE;
                }

                List<Parameter> parameters = operation.getParameters();
                for (Parameter parameter : parameters) {
                    String name = parameter.getName();
                    if (parameter instanceof BodyParameter) {
                        Model schema = ((BodyParameter) parameter).getSchema();
                        if (schema instanceof RefModel) {
                            String $ref = ((RefModel) schema).get$ref();
                            if (StringUtils.isNotBlank($ref)) {
                                String defName = $ref.substring("#/definitions/".length());
                                Model model = definitions.get(defName);
                                Example example = ExampleBuilder
                                        .fromModel(defName, model, definitions, new HashSet<String>());

                                String jsonExample = Json.pretty(example);
                                try {
                                    org.json.JSONObject json = new org.json.JSONObject(jsonExample);
                                    SequenceUtils.listJson(json, parameterJsonPathMapping);
                                } catch (JSONException e) {
                                    log.error("Error occurred while generating json mapping for the definition", e);
                                }
                            }
                        }
                    }
                    if (parameter instanceof QueryParameter) {
                        String type = ((QueryParameter) parameter).getType();
                        queryParameters.put(name, type);
                    }
                }
                //populates body parameter json paths and query parameters to generate api sequence parameters
                populateParametersFromOperation(operation, definitions, parameterJsonPathMapping, queryParameters);

                Map<String, String> payloadSequence = createPayloadFacXMLForOperation(parameterJsonPathMapping, queryParameters,
                        namespace, SOAPToRESTConstants.EMPTY_STRING, operationId, definitions);
                try {
                    String[] propAndArgElements = getPropertyAndArgElementsForSequence(parameterJsonPathMapping,
                            queryParameters);
                    if (log.isDebugEnabled()) {
                        log.debug("properties string for the generated sequence: " + propAndArgElements[0]);
                        log.debug("arguments string for the generated sequence: " + propAndArgElements[1]);
                    }
                    org.json.simple.JSONArray arraySequenceElements = new org.json.simple.JSONArray();

                    //gets array elements for the sequence to be used
                    getArraySequenceElements(arraySequenceElements, parameterJsonPathMapping);
                    Map<String, String> sequenceMap = new HashMap<>();
                    sequenceMap.put("args", propAndArgElements[0]);
                    sequenceMap.put("properties", propAndArgElements[1]);
                    sequenceMap.put("sequence", payloadSequence.get(operationId));
                    RESTToSOAPMsgTemplate template = new RESTToSOAPMsgTemplate();
                    String inSequence = template.getMappingInSequence(sequenceMap, operationId, soapAction,
                            namespace, soapNamespace, arraySequenceElements);
                    String outSequence = template.getMappingOutSequence();
                    if (isResourceFromWSDL) {
                        saveApiSequences(apiIdentifier, inSequence, outSequence, httpMethod.toString().toLowerCase(),
                                pathName);
                    }
                } catch (APIManagementException e) {
                    handleException("Error when generating sequence property and arg elements for soap operation: " + operationId, e);
                }
            }
        }
    }

    private static void populateParametersFromOperation(Operation operation, Map<String, Model> definitions,
            Map<String, String> parameterJsonPathMapping, Map<String, String> queryParameters) {

        List<Parameter> parameters = operation.getParameters();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            if (parameter instanceof BodyParameter) {
                Model schema = ((BodyParameter) parameter).getSchema();
                if (schema instanceof RefModel) {
                    String $ref = ((RefModel) schema).get$ref();
                    if (StringUtils.isNotBlank($ref)) {
                        String defName = $ref.substring("#/definitions/".length());
                        Model model = definitions.get(defName);
                        Example example = ExampleBuilder.fromModel(defName, model, definitions, new HashSet<String>());

                        String jsonExample = Json.pretty(example);
                        try {
                            org.json.JSONObject json = new org.json.JSONObject(jsonExample);
                            SequenceUtils.listJson(json, parameterJsonPathMapping);
                        } catch (JSONException e) {
                            log.error("Error occurred while generating json mapping for the definition: " + defName, e);
                        }
                    }
                }
            }
            if (parameter instanceof QueryParameter) {
                String type = ((QueryParameter) parameter).getType();
                queryParameters.put(name, type);
            }
        }
    }

    private static void saveApiSequences(APIIdentifier apiIdentifier, String inSequence, String outSequence, String method,
            String resourcePath) throws APIManagementException {

        boolean isTenantFlowStarted = false;
        try {
            String provider = apiIdentifier.getProviderName();
            String name = apiIdentifier.getName();
            String version = apiIdentifier.getVersion();

            if (provider != null) {
                provider = APIUtil.replaceEmailDomain(provider);
            }

            provider = (provider != null ? provider.trim() : null);
            name = (name != null ? name.trim() : null);
            version = (version != null ? version.trim() : null);

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
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);
                APIUtil.loadTenantRegistry(tenantId);
                registry = registryService.getGovernanceSystemRegistry(tenantId);

                Pattern pattern = Pattern.compile("[{}]");
                Matcher hasSpecialCharacters = pattern.matcher(resourcePath);
                String resourcePathName = resourcePath;
                if (hasSpecialCharacters.find()) {
                    resourcePathName = resourcePath.split("[{]")[0];
                    if (resourcePathName.endsWith("/")) {
                        resourcePathName = StringUtils.removeEnd(resourcePathName, "/");
                    }
                }

                String resourceInPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        provider + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_IN_RESOURCE
                        + resourcePathName + SOAPToRESTConstants.SequenceGen.RESOURCE_METHOD_SEPERATOR + method
                        + SOAPToRESTConstants.SequenceGen.XML_FILE_EXTENSION;
                String resourceOutPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        provider + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_OUT_RESOURCE
                        + resourcePathName + SOAPToRESTConstants.SequenceGen.RESOURCE_METHOD_SEPERATOR + method
                        + SOAPToRESTConstants.SequenceGen.XML_FILE_EXTENSION;

                SequenceUtils.saveRestToSoapConvertedSequence(registry, inSequence, method, resourceInPath,
                        resourcePath);
                SequenceUtils.saveRestToSoapConvertedSequence(registry, outSequence, method, resourceOutPath,
                        resourcePath);
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information", e);
            } catch (RegistryException e) {
                handleException("Error while creating registry resource", e);
            } catch (APIManagementException e) {
                handleException(
                        "Error while saving the soap to rest converted sequence for resource path: " + resourcePath, e);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private static Map<String, String> createPayloadFacXMLForOperation(Map<String, String> parameterJsonPathMapping,
            Map<String, String> queryPathParamMapping, String namespace, String prefix, String operationId,
                                                                       Map<String, Model> definitions)
            throws APIManagementException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        DocumentBuilder docBuilder;
        StringWriter stringWriter = new StringWriter();
        Boolean isNamespaceQualified = false;
        Boolean isRootComplexType = false;

        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElementNS(namespace, SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                    + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + operationId);
            doc.appendChild(rootElement);
            int count = 1;
            for (String parameter : parameterJsonPathMapping.keySet()) {
                String parameterType = parameterJsonPathMapping.get(parameter);
                String[] parameterTreeNodes = parameter.split("\\.");

                Element prevElement = rootElement;
                int elemPos = 0;
                int length = parameterType.equals(SOAPToRESTConstants.ParamTypes.ARRAY) ?
                        parameterTreeNodes.length - 1 :
                        parameterTreeNodes.length;
                if (length > 0 && !isRootComplexType) {
                    isRootComplexType = true;
                }
                for (int i = 0; i < length; i++) {
                    String parameterTreeNode = parameterTreeNodes[i];
                    ModelImpl model = (ModelImpl) definitions.get(parameterTreeNode);
                    if (model != null) {
                        Map<String, Object> venderExtensions = model.getVendorExtensions();
                        if (venderExtensions.get(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED) != null &&
                                Boolean.parseBoolean(venderExtensions.get(SOAPToRESTConstants.X_NAMESPACE_QUALIFIED)
                                        .toString())) {
                            isNamespaceQualified = true;
                        }
                    }
                    if (StringUtils.isNotBlank(parameterTreeNode)) {
                        if (SOAPToRESTConstants.ATTR_CONTENT_KEYWORD.equalsIgnoreCase(parameterTreeNode)) {
                            String attName = parameterTreeNodes[++i];
                            prevElement
                                    .setAttribute(attName, SOAPToRESTConstants.SequenceGen.PROPERTY_ACCESSOR + count++);
                            break;
                        }
                        if (SOAPToRESTConstants.BASE_CONTENT_KEYWORD.equalsIgnoreCase(parameterTreeNode)) {
                            prevElement.setTextContent(SOAPToRESTConstants.SequenceGen.PROPERTY_ACCESSOR + count++);
                            break;
                        }
                        Element element;
                        if (isNamespaceQualified) {
                            element = doc.createElementNS(namespace, SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                                    + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + parameterTreeNode);
                        } else if (!isNamespaceQualified && isRootComplexType) {
                            element = doc.createElementNS(namespace, SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                                    + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + parameterTreeNode);
                            isRootComplexType = false;
                        } else {
                            element = doc.createElementNS(null, parameterTreeNode);
                            element.setAttribute(SOAPToRESTConstants.XMLNS,
                                    SOAPToRESTConstants.X_WSO2_UNIQUE_NAMESPACE);
                        }
                        String xPathOfNode = StringUtils.EMPTY;
                        if (doc.getElementsByTagName(element.getTagName()).getLength() > 0) {
                            xPathOfNode = getXpath(doc.getElementsByTagName(element.getTagName()).item(0));
                            xPathOfNode = xPathOfNode.replaceAll("/+", ".");
                            if (xPathOfNode.startsWith(".")) {
                                xPathOfNode = xPathOfNode.substring(1);
                            }
                            if (xPathOfNode.contains(operationId + ".")) {
                                xPathOfNode = xPathOfNode.replace(operationId + ".", "");
                            }
                        }

                        if (doc.getElementsByTagName(element.getTagName()).getLength() > 0 &&
                                parameter.contains(xPathOfNode)) {
                            prevElement = (Element) doc.getElementsByTagName(element.getTagName()).item(0);
                        } else {
                            if (elemPos == length - 1) {
                                element.setTextContent(SOAPToRESTConstants.SequenceGen.PROPERTY_ACCESSOR + count);
                                count++;
                            }
                            prevElement.appendChild(element);
                            prevElement = element;
                        }
                        elemPos++;
                    }
                }
            }
            count = 1;
            if (parameterJsonPathMapping.size() == 0) {
                for (String queryParam : queryPathParamMapping.keySet()) {
                    Element element = doc.createElementNS(namespace, SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                            + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + queryParam);
                    element.setTextContent(SOAPToRESTConstants.SequenceGen.PROPERTY_ACCESSOR + count);
                    count++;
                    rootElement.appendChild(element);
                }
            } else if (parameterJsonPathMapping.size() > 0 && queryPathParamMapping.size() > 0) {
                log.warn("Query parameters along with the body parameter is not allowed");
            }
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(SOAPToRESTConstants.SequenceGen.INDENT_PROPERTY,
                    SOAPToRESTConstants.SequenceGen.INDENT_VALUE);
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        } catch (ParserConfigurationException e) {
            handleException("Error occurred when building in sequence xml", e);
        } catch (TransformerConfigurationException e) {
            handleException("Error in transport configuration", e);
        } catch (TransformerException e) {
            handleException("Error occurred when transforming in sequence xml", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("parameter mapping for used in payload factory for soap operation:" + operationId + " is "
                    + stringWriter.toString());
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(operationId, processPayloadFactXML(stringWriter.toString()));
        return paramMap;
    }

    private static String getXpath(Node node) {
        if (node != null) {
            Node parent = node.getParentNode();
            if (parent == null && node.getLocalName() != null) {
                return node.getLocalName();
            } else if (node.getLocalName() != null) {
                return getXpath(parent) + SOAPToRESTConstants.SequenceGen.PATH_SEPARATOR + node.getLocalName();
            } else {
                return getXpath(parent);
            }
        }
        return SOAPToRESTConstants.EMPTY_STRING;
    }

    private static String[] getPropertyAndArgElementsForSequence(Map<String, String> parameterJsonPathMapping,
            Map<String, String> queryPathParamMapping) throws APIManagementException {

        String argStr = SOAPToRESTConstants.EMPTY_STRING;
        String propertyStr = SOAPToRESTConstants.EMPTY_STRING;
        for (String parameter : parameterJsonPathMapping.keySet()) {
            String parameterType = parameterJsonPathMapping.get(parameter);
            String paramElements = SequenceGenerator
                    .createParameterElements(parameter, SOAPToRESTConstants.Swagger.BODY);
            String[] params = paramElements.split(SOAPToRESTConstants.SequenceGen.COMMA);
            if (!SOAPToRESTConstants.ParamTypes.ARRAY.equals(parameterType)) {
                propertyStr += params[0] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
            }
            argStr += params[1] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
        }

        if (MapUtils.isEmpty(parameterJsonPathMapping)) {
            for (String queryParam : queryPathParamMapping.keySet()) {
                String paramElements = SequenceGenerator
                        .createParameterElements(queryParam, SOAPToRESTConstants.ParamTypes.QUERY);
                String[] params = paramElements.split(SOAPToRESTConstants.SequenceGen.COMMA);
                argStr += params[1] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
                propertyStr += params[0] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
            }
        } else if (parameterJsonPathMapping.size() > 0 && queryPathParamMapping.size() > 0) {
            log.warn("Query parameters along with the body parameter is not allowed");
        }

        return new String[] { argStr, propertyStr };
    }

    private static void getArraySequenceElements(org.json.simple.JSONArray array,
            Map<String, String> parameterJsonPathMapping) {

        for (String parameter : parameterJsonPathMapping.keySet()) {
            String parameterType = parameterJsonPathMapping.get(parameter);
            String[] parameterTreeNodes = parameter.split("\\.");

            if (SOAPToRESTConstants.ParamTypes.ARRAY.equals(parameterType)) {
                org.json.simple.JSONObject arrayObj = new org.json.simple.JSONObject();
                arrayObj.put(SOAPToRESTConstants.SequenceGen.PROPERTY_NAME, parameter);
                arrayObj.put(SOAPToRESTConstants.SequenceGen.PARAMETER_NAME,
                        parameterTreeNodes[parameterTreeNodes.length - 1]);
                array.add(arrayObj);
            }
        }
    }

    /**
     * Creates property and argument elements needed to inject to the sequence.
     *
     * @param jsonPathElement json path of a parameter
     * @param type            type of the parameter (i.e query, body, path)
     * @return string of property and argument elements for a given parameter
     * @throws APIManagementException throws in xml to string transformation
     */
    private static String createParameterElements(String jsonPathElement, String type) throws APIManagementException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        StringWriter stringWriter = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        String property = SOAPToRESTConstants.EMPTY_STRING;
        String argument = SOAPToRESTConstants.EMPTY_STRING;
        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element argElement = doc.createElement(SOAPToRESTConstants.SequenceGen.ARG_ELEMENT);
            Element propertyElement = doc.createElement(SOAPToRESTConstants.SequenceGen.PROPERTY_ELEMENT);
            argElement.setAttribute(SOAPToRESTConstants.SequenceGen.EVALUATOR_ATTR,
                    SOAPToRESTConstants.SequenceGen.XML_FILE);
            String expressionAttr = SOAPToRESTConstants.SequenceGen.EXPRESSION_FUNC_DEF + jsonPathElement
                    + SOAPToRESTConstants.SequenceGen.EXPRESSION_FUNC_DEF_CLOSING_TAG;
            argElement.setAttribute(SOAPToRESTConstants.SequenceGen.EXPRESSION_ATTR, expressionAttr);
            propertyElement.setAttribute(SOAPToRESTConstants.NAME_ATTRIBUTE,
                    SOAPToRESTConstants.SequenceGen.REQ_VARIABLE + jsonPathElement);
            if (SOAPToRESTConstants.ParamTypes.QUERY.equals(type)) {
                propertyElement.setAttribute(SOAPToRESTConstants.SequenceGen.EXPRESSION_ATTR,
                        SOAPToRESTConstants.SequenceGen.URL_OPERATOR + jsonPathElement);
            } else {
                propertyElement.setAttribute(SOAPToRESTConstants.SequenceGen.EXPRESSION_ATTR,
                        SOAPToRESTConstants.SequenceGen.JSON_EVAL + SOAPToRESTConstants.SequenceGen.ROOT_OPERATOR
                                + jsonPathElement + SOAPToRESTConstants.SequenceGen.CLOSING_PARANTHESIS);
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(propertyElement), new StreamResult(stringWriter));
            property = stringWriter.toString();
            stringWriter = new StringWriter();
            transformer.transform(new DOMSource(argElement), new StreamResult(stringWriter));
            argument = stringWriter.toString();
            if (log.isDebugEnabled()) {
                log.debug("Argument element for request parameter: " + jsonPathElement + " and parameter type: " + type
                        + " is: " + argument);
            }
        } catch (ParserConfigurationException e) {
            handleException("Error occurred when building in arg elements", e);
        } catch (TransformerConfigurationException e) {
            handleException("Error in transport configuration", e);
        } catch (TransformerException e) {
            handleException("Error occurred when transforming in sequence xml", e);
        }
        return property + SOAPToRESTConstants.SequenceGen.COMMA + argument;
    }

    private static String processPayloadFactXML(String xmlPayload) {
        // When setting namespace as xmlns="", Xerces process it as empty namespace and removes it
        // Hence following the string replace approach to add xmlns="".
        // Refer https://issues.apache.org/jira/browse/XERCESJ-1720
        String processedXMLPayload = xmlPayload.replaceAll(SOAPToRESTConstants.X_WSO2_UNIQUE_NAMESPACE, "");
        return processedXMLPayload;
    }
}
