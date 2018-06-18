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
package org.wso2.carbon.apimgt.impl.soaptorest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromOpenAPISpec;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLSOAPOperation;
import org.wso2.carbon.apimgt.impl.soaptorest.template.RESTToSOAPMsgTemplate;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SequenceUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Class uses to generate api sequences for soap to rest conversion.
 */
public class SequenceGenerator {
    private static final Logger log = LoggerFactory.getLogger(SequenceGenerator.class);

    private static APIDefinition definitionFromOpenAPISpec = new APIDefinitionFromOpenAPISpec();

    /**
     * generates api in/out sequences from the swagger to soap definitions
     * <p>
     * Note: this method is directly invoked from the jaggery layer
     *
     * @param apiDataStr           api object as a string
     * @param soapOperationMapping soap operation mapping from the wsdl
     * @throws APIManagementException throws if error occurred in registry access/ parsing definitions
     */
    public static void generateSequences(String apiDataStr, String soapOperationMapping) throws APIManagementException {
        JSONParser parser = new JSONParser();
        boolean isTenantFlowStarted = false;
        try {
            JSONObject apiData = (JSONObject) parser.parse(apiDataStr);
            String provider = (String) apiData.get("provider");
            String name = (String) apiData.get("name");
            String version = (String) apiData.get("version");

            if (provider != null) {
                provider = APIUtil.replaceEmailDomain(provider);
            }

            provider = (provider != null ? provider.trim() : null);
            name = (name != null ? name.trim() : null);
            version = (version != null ? version.trim() : null);
            APIIdentifier apiId = new APIIdentifier(provider, name, version);

            JSONObject apiJSON;

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

                apiJSON = (JSONObject) parser.parse(definitionFromOpenAPISpec.getAPIDefinition(apiId, registry));

                ObjectMapper mapper = new ObjectMapper()
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                TypeFactory typeFactory = mapper.getTypeFactory();
                List<WSDLSOAPOperation> soapOperations = mapper.readValue(soapOperationMapping,
                        typeFactory.constructCollectionType(List.class, WSDLSOAPOperation.class));

                if (apiJSON != null) {
                    Map pathMap = (HashMap) apiJSON.get(SOAPToRESTConstants.Swagger.PATHS);
                    for (Object resourceObj : pathMap.entrySet()) {
                        Map.Entry entry = (Map.Entry) resourceObj;
                        String resourcePath = (String) entry.getKey();
                        JSONObject resource = (JSONObject) entry.getValue();

                        Set methods = resource.keySet();
                        for (Object key1 : methods) {
                            String method = (String) key1;

                            List<JSONObject> mappingList = SequenceUtils
                                    .getResourceParametersFromSwagger(apiJSON, resource, method);
                            String inSequence = generateApiInSequence(mappingList, soapOperations, resourcePath);
                            String outSequence = generateApiOutSequence();
                            if (log.isDebugEnabled()) {
                                log.debug("Generated api in sequence for " + resource + " is: " + inSequence);
                                log.debug("Generated api out sequence for " + resource + " is: " + outSequence);
                            }
                            String resourceInPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                    provider + RegistryConstants.PATH_SEPARATOR + name
                                    + RegistryConstants.PATH_SEPARATOR + version + RegistryConstants.PATH_SEPARATOR
                                    + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_IN_RESOURCE + resourcePath
                                    + SOAPToRESTConstants.SequenceGen.RESOURCE_METHOD_SEPERATOR + method
                                    + SOAPToRESTConstants.SequenceGen.XML_FILE_EXTENSION;
                            String resourceOutPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                    provider + RegistryConstants.PATH_SEPARATOR + name
                                    + RegistryConstants.PATH_SEPARATOR + version + RegistryConstants.PATH_SEPARATOR
                                    + SOAPToRESTConstants.SequenceGen.SOAP_TO_REST_OUT_RESOURCE + resourcePath
                                    + SOAPToRESTConstants.SequenceGen.RESOURCE_METHOD_SEPERATOR + method
                                    + SOAPToRESTConstants.SequenceGen.XML_FILE_EXTENSION;
                            SequenceUtils.saveRestToSoapConvertedSequence(registry, inSequence, method, resourceInPath);
                            SequenceUtils
                                    .saveRestToSoapConvertedSequence(registry, outSequence, method, resourceOutPath);
                        }
                    }
                }
            } catch (RegistryException e) {
                handleException("Error when create registry instance", e);
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information", e);
            } catch (ParseException e) {
                handleException("Error while parsing soap operations json content", e);
            } catch (IOException e) {
                handleException("Error occurred when parsing soap operations json string", e);
            }
        } catch (ParseException e) {
            handleException("Error occurred when parsing api json string", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Generates api in sequence for api resource that needs to added to synapse api configs
     *
     * @param mappingList    swagger resource mapping
     * @param soapOperations soap operations taken from the wsdl
     * @param resourcePath   resource path of the http method
     * @param method         resource method
     * @return generated api in sequence
     * @throws APIManagementException
     */
    private static String generateApiInSequence(List<JSONObject> mappingList, List<WSDLSOAPOperation> soapOperations,
            String resourcePath) throws APIManagementException {
        RESTToSOAPMsgTemplate template = new RESTToSOAPMsgTemplate();

        String soapAction = "";
        String namespace = "";
        String opName = "";
        for (WSDLSOAPOperation operationParam : soapOperations) {
            if (operationParam.getName().equals(resourcePath.substring(1))) {
                opName = operationParam.getSoapBindingOpName();
                soapAction = operationParam.getSoapAction();
                namespace = operationParam.getTargetNamespace();
                if (log.isDebugEnabled()) {
                    log.debug("Soap operation name: " + opName + ", soap action: " + soapAction + ", namespace: "
                            + namespace);
                }
                break;
            }
        }
        JSONArray array = new JSONArray();
        Map<String, String> sequenceMap = createXMLFromMapping(mappingList, opName, namespace, array);
        return template.getMappingInSequence(sequenceMap, opName, soapAction, namespace, array);
    }

    /**
     * Generates api out sequence for api resource that needs to added to synapse api configs
     *
     * @return generated api out sequence
     */
    private static String generateApiOutSequence() {
        RESTToSOAPMsgTemplate template = new RESTToSOAPMsgTemplate();
        return template.getMappingOutSequence();
    }

    /**
     * Creates xml string needed to inject into the velocity templates.
     *
     * @param mappingList swagger definition mapping with the soap operations
     * @param opName      soap operation name
     * @param namespace   soap namespace of the operation
     * @param array       parameter array used to generate sequence for array type
     * @return xml string that needs to injected to the api sequence
     * @throws APIManagementException throws in xml to string transformation
     */
    private static Map<String, String> createXMLFromMapping(List<JSONObject> mappingList, String opName,
            String namespace, JSONArray array) throws APIManagementException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        StringWriter stringWriter = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Map<String, String> map = new HashMap<>();
        String argStr = "";
        String propertyStr = "";
        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            doc.createElementNS(namespace, SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                    + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + opName);
            Transformer transformer = transformerFactory.newTransformer();
            Element rootElement = doc.createElementNS(namespace, SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                    + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + opName);
            doc.appendChild(rootElement);
            int count = 1;
            for (JSONObject jsonObject : mappingList) {
                for (Object obj : jsonObject.keySet()) {
                    if (jsonObject.get(obj) instanceof JSONArray) {
                        JSONArray paramArr = (JSONArray) jsonObject.get(obj);
                        if (log.isDebugEnabled()) {
                            log.debug("Swagger parameter definition: " + paramArr.toJSONString());
                        }
                        for (Object paramObj : paramArr) {
                            JSONObject param = (JSONObject) paramObj;
                            String paramName = (String) param.keySet().iterator().next();
                            String xPath;
                            String paramElements = createParameterElements(paramName, SOAPToRESTConstants.Swagger.BODY);
                            String[] params = paramElements.split(SOAPToRESTConstants.SequenceGen.COMMA);
                            argStr += params[1] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
                            if (SOAPToRESTConstants.ParamTypes.ARRAY
                                    .equals(param.get(SOAPToRESTConstants.Swagger.TYPE))) {
                                xPath = (String) param.get(SOAPToRESTConstants.SequenceGen.XPATH);
                                JSONObject arrayObj = new JSONObject();
                                arrayObj.put(SOAPToRESTConstants.SequenceGen.PROPERTY_NAME, paramName);
                                arrayObj.put(SOAPToRESTConstants.SequenceGen.PARAMETER_NAME,
                                        param.get(SOAPToRESTConstants.SequenceGen.PARAMETER_NAME));
                                array.add(arrayObj);
                            } else {
                                propertyStr += params[0] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
                                JSONObject entry = (JSONObject) ((JSONObject) paramObj).get(paramName);
                                xPath = (String) entry.get(SOAPToRESTConstants.SequenceGen.XPATH);
                            }
                            if (xPath == null) {
                                throw new APIManagementException("Cannot map parameters without x-path property.");
                            }
                            String[] xPathElements = xPath.split(SOAPToRESTConstants.SequenceGen.PATH_SEPARATOR);
                            Element prevElement = rootElement;
                            int elemPos = 0;
                            for (String xPathElement : xPathElements) {
                                Element element = doc.createElementNS(namespace,
                                        SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                                                + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + xPathElement);
                                if (doc.getElementsByTagName(element.getTagName()).getLength() > 0) {
                                    prevElement = (Element) doc.getElementsByTagName(element.getTagName()).item(0);
                                } else {
                                    if (elemPos == xPathElements.length - 1) {
                                        element.setTextContent(SOAPToRESTConstants.SequenceGen.PROPERTY_ACCESSOR +
                                                count);
                                        count++;
                                    }
                                    prevElement.appendChild(element);
                                    prevElement = element;
                                }
                                elemPos++;
                                if (log.isDebugEnabled()) {
                                    log.debug("Current x path element  " + element.getNodeValue() + " at position: "
                                            + elemPos);
                                }
                            }
                        }
                    } else if (jsonObject.get(obj) instanceof JSONObject) {
                        JSONObject param = (JSONObject) jsonObject.get(obj);
                        Element element = doc.createElementNS(namespace,
                                SOAPToRESTConstants.SequenceGen.NAMESPACE_PREFIX
                                        + SOAPToRESTConstants.SequenceGen.NAMESPACE_SEPARATOR + param
                                        .get(SOAPToRESTConstants.Swagger.NAME).toString());
                        element.setTextContent(SOAPToRESTConstants.SequenceGen.PROPERTY_ACCESSOR + count);
                        rootElement.appendChild(element);
                        String paramElements = createParameterElements(
                                param.get(SOAPToRESTConstants.Swagger.NAME).toString(),
                                SOAPToRESTConstants.ParamTypes.QUERY);
                        String[] params = paramElements.split(SOAPToRESTConstants.SequenceGen.COMMA);
                        argStr += params[1] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
                        propertyStr += params[0] + SOAPToRESTConstants.SequenceGen.NEW_LINE_CHAR;
                        count++;
                    }
                }
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
        map.put("properties", propertyStr);
        map.put("args", argStr);
        map.put("sequence", stringWriter.toString());
        return map;
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
        String property = "";
        String argument = "";
        try {
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
}
