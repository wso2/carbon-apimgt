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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLComplexType;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLOperationParam;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Util class used for sequence generation of the soap to rest converted operations.
 */
public class SequenceUtils {

    /**
     * Saves the converted api sequence in the registry for the given resource path
     *
     * @param registry     user registry reference
     * @param sequence     sequence to be saved
     * @param method       http method of the operation
     * @param resourcePath registry resource path
     * @throws APIManagementException throws errors on if the resource persistence gets unsuccessful
     */
    public static void saveRestToSoapConvertedSequence(UserRegistry registry, String sequence, String method,
            String resourcePath) throws APIManagementException {
        try {
            Resource regResource;
            if (!registry.resourceExists(resourcePath)) {
                regResource = registry.newResource();
            } else {
                regResource = registry.get(resourcePath);
            }
            regResource.setContent(sequence);
            regResource.addProperty(SOAPToRESTConstants.METHOD, method);
            regResource.setMediaType("text/xml");
            registry.put(resourcePath, regResource);
        } catch (RegistryException e) {
            handleException("Error occurred while accessing the registry to save api sequence ", e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            handleException("Error occurred while saving api sequence ", e);
        }
    }

    /**
     * Updates the api sequences where user will be able to edits the generated sequences
     * <p>
     * Note: this method is directly invoked from the jaggery layer
     *
     * @param name     api name
     * @param version  api version
     * @param provider api provider
     * @param seqType  to identify the sequence is whether in/out sequence
     * @param content  sequence content
     * @throws APIManagementException throws exceptions on unsuccessful persistence in registry or json parsing of the content
     */
    public static void updateRestToSoapConvertedSequences(String name, String version, String provider, String seqType,
            String content) throws APIManagementException {
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        boolean isTenantFlowStarted = false;

        JSONParser jsonParser = new JSONParser();
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
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            registry = registryService.getGovernanceSystemRegistry(tenantId);

            JSONObject sequences = (JSONObject) jsonParser.parse(content);
            for (Object sequence : sequences.keySet()) {
                String sequenceContent = (String) ((JSONObject) sequences.get(sequence)).get("content");
                String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        provider + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE
                        + RegistryConstants.PATH_SEPARATOR + seqType + RegistryConstants.PATH_SEPARATOR + sequence
                        + ".xml";

                Resource regResource;
                if (registry.resourceExists(resourcePath)) {
                    regResource = registry.get(resourcePath);
                    regResource.setContent(sequenceContent);
                    regResource.setMediaType("text/xml");
                    registry.put(resourcePath, regResource);
                }
            }
        } catch (ParseException e) {
            handleException("Error occurred while parsing the sequence json.", e);
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information ", e);
        } catch (RegistryException e) {
            handleException("Error when create registry instance ", e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            handleException("Error while creating registry resource", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Gets soap to rest converted sequence from the registry
     * <p>
     * Note: this method is directly invoked from the jaggery layer
     *
     * @param name     api name
     * @param version  api version
     * @param provider api provider
     * @param seqType  to identify the sequence is whether in/out sequence
     * @return converted sequences string for a given operation
     * @throws APIManagementException throws exceptions on unsuccessful retrieval of resources in registry
     */
    public static String getRestToSoapConvertedSequence(String name, String version, String provider, String seqType)
            throws APIManagementException {
        JSONObject resultJson = new JSONObject();

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
                String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + provider
                        + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE
                        + RegistryConstants.PATH_SEPARATOR + seqType;

                Collection collection = registry.get(resourcePath, 0, Integer.MAX_VALUE);
                String[] resources = collection.getChildren();

                for (String path : resources) {
                    Resource resource = registry.get(path);
                    String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                    String resourceName = ((ResourceImpl) resource).getName();
                    resourceName = resourceName.replaceAll("\\.xml", "");
                    String httpMethod = resource.getProperty(SOAPToRESTConstants.METHOD);
                    Map<String, String> resourceMap = new HashMap<>();
                    resourceMap.put(SOAPToRESTConstants.METHOD, httpMethod);
                    resourceMap.put(SOAPToRESTConstants.CONTENT, content);
                    resultJson.put(resourceName, resourceMap);
                }

            } catch (RegistryException e) {
                handleException("Error when create registry instance ", e);
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information ", e);
            } catch (org.wso2.carbon.registry.api.RegistryException e) {
                handleException("Error while creating registry resource", e);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return resultJson.toJSONString();
    }

    /**
     * Generates parameter mapping between swagger parameters and soap operation parameters
     * <p>
     * Note: this method is directly invoked from the jaggery layer
     *
     * @param params      soap operation parameters
     * @param mappingList swagger parameters
     * @return mapping json
     */
    public static JSONObject generateSoapToRestParameterMapping(List<WSDLOperationParam> params,
            List<JSONObject> mappingList) {
        JSONObject soapToRestParamMapping = new JSONObject();
        int i = mappingList.size() - 1;

        for (WSDLOperationParam param : params) {
            JSONObject paramObj = new JSONObject();
            String parameter = param.getName();
            String dataType = param.getDataType();
            if (dataType != null && !dataType.equals(SOAPToRESTConstants.PARAM_TYPES.ARRAY)) {
                paramObj.put(SOAPToRESTConstants.PARAM_TYPE, SOAPToRESTConstants.PARAM_TYPES.QUERY);
            }
            JSONObject mappingObj = mappingList.get(i);
            WSDLComplexType complexType = param.getWsdlComplexType();
            Iterator paramKeyIterator = mappingObj.keySet().iterator();
            String paramKey = "";
            if (paramKeyIterator.hasNext()) {
                paramKey = paramKeyIterator.next().toString();
            }
            JSONObject complexTypeObj = new JSONObject();
            if (complexType != null) {
                String complexTypeName = complexType.getName();
                List<WSDLOperationParam> complexTypes = complexType.getParamList();
                List<JSONObject> complexTypeList = new ArrayList<>();
                Iterator complexObjIterator = ((JSONObject) mappingObj.get(paramKey)).keySet().iterator();
                for (WSDLOperationParam operation : complexTypes) {
                    JSONObject innerParam = new JSONObject();
                    if (operation.isArray()) {
                        Map paramMap = new LinkedHashMap();
                        paramMap.put(SOAPToRESTConstants.PARAM_TYPE, SOAPToRESTConstants.PARAM_TYPES.ARRAY);
                        paramMap.put(operation.getName(), parameter);
                        paramObj.putAll(paramMap);
                    } else if (complexObjIterator.hasNext()) {
                        String complexParam = complexObjIterator.next().toString();
                        String jsonPath;
                        if (complexTypeName != null) {
                            jsonPath = parameter + "." + complexTypeName + "." + operation.getName();
                        } else {
                            jsonPath = parameter + "." + operation.getName();
                        }
                        innerParam.put(complexParam, jsonPath);
                        complexTypeList.add(innerParam);
                    }
                }
                complexTypeObj.put(SOAPToRESTConstants.PARAM_TYPE, SOAPToRESTConstants.PARAM_TYPES.OBJECT);
                complexTypeObj.put(SOAPToRESTConstants.PARAM_TYPE, SOAPToRESTConstants.PARAM_TYPES.OBJECT);
                complexTypeObj.put(complexTypeName, complexTypeList);
            }
            if (SOAPToRESTConstants.PARAM_TYPES.OBJECT.equals(complexTypeObj.get(SOAPToRESTConstants.PARAM_TYPE))
                    && dataType == null) {
                soapToRestParamMapping.put(parameter, complexTypeObj);
            } else {
                soapToRestParamMapping.put(parameter, paramObj);
            }
            i--;
        }
        return soapToRestParamMapping;
    }

    /**
     * Gets parameter definitions from swagger
     *
     * @param swaggerObj swagger json
     * @param resource   rest resource object
     * @param method     http method
     * @return parameter mapping for a resource from the swagger definitions
     */
    public static List<JSONObject> getResourceParametersFromSwagger(JSONObject swaggerObj, JSONObject resource,
            String method) {
        Map content = (HashMap) resource.get(method);
        JSONArray parameters = (JSONArray) content.get(SOAPToRESTConstants.SWAGGER.PARAMETERS);
        List<JSONObject> mappingList = new ArrayList<>();
        for (Object param : parameters) {
            String inputType = String.valueOf(((JSONObject) param).get(SOAPToRESTConstants.SWAGGER.IN));
            if (inputType.equals(SOAPToRESTConstants.SWAGGER.BODY)) {
                JSONObject schema = (JSONObject) ((JSONObject) param).get(SOAPToRESTConstants.SWAGGER.SCHEMA);
                String definitionPath = String.valueOf(schema.get(SOAPToRESTConstants.SWAGGER.REF));
                String definition = definitionPath.replaceAll(SOAPToRESTConstants.SWAGGER.DEFINITIONS_ROOT, "");
                JSONObject definitions = (JSONObject) ((JSONObject) swaggerObj.get(
                        SOAPToRESTConstants.SWAGGER.DEFINITIONS)).get(definition);
                JSONObject properties = (JSONObject) definitions.get(SOAPToRESTConstants.SWAGGER.PROPERTIES);

                for (Object property : properties.entrySet()) {
                    Map.Entry entry = (Map.Entry) property;
                    String paramName = String.valueOf(entry.getKey());
                    JSONObject value = (JSONObject) entry.getValue();
                    JSONArray propArray = new JSONArray();
                    if (value.get(SOAPToRESTConstants.SWAGGER.REF) != null) {
                        String propDefinitionRef = String.valueOf(value.get(SOAPToRESTConstants.SWAGGER.REF)).replaceAll(
                                SOAPToRESTConstants.SWAGGER.DEFINITIONS_ROOT, "");
                        getNestedDefinitionsFromSwagger((JSONObject) swaggerObj.get(SOAPToRESTConstants.SWAGGER.DEFINITIONS),
                                propDefinitionRef, propDefinitionRef, propArray);
                        JSONObject refObj = new JSONObject();
                        refObj.put(paramName, propArray);
                        mappingList.add(refObj);
                    } else if (String.valueOf(value.get(SOAPToRESTConstants.SWAGGER.TYPE)).equals(
                            SOAPToRESTConstants.PARAM_TYPES.ARRAY)) {
                        JSONObject arrObj = new JSONObject();
                        arrObj.put(((Map.Entry) property).getKey(), ((Map.Entry) property).getValue());
                        mappingList.add(arrObj);
                    }
                }
            } else {
                JSONObject queryObj = new JSONObject();
                queryObj.put(((JSONObject) param).get(SOAPToRESTConstants.SWAGGER.NAME), param);
                mappingList.add(queryObj);
            }
        }
        return mappingList;
    }

    /**
     * Gets nested swagger definitions for the complex types
     *
     * @param definitions swagger definition json
     * @param definition  parent definition
     * @param jsonPath    json path to be construct
     * @param propArray   properties for the nested definition
     */
    private static void getNestedDefinitionsFromSwagger(JSONObject definitions, String definition, String jsonPath,
            JSONArray propArray) {
        JSONObject propDefinitions = (JSONObject) (definitions).get(definition);
        JSONObject props;
        if (SOAPToRESTConstants.PARAM_TYPES.ARRAY.equals(propDefinitions.get(SOAPToRESTConstants.TYPE_ATTRIBUTE))) {
            props = (JSONObject) propDefinitions.get(SOAPToRESTConstants.SWAGGER.ITEMS);
            if (props.get(SOAPToRESTConstants.SWAGGER.REF) == null) {
                JSONObject arrayProperty = new JSONObject();
                String key = jsonPath + "." + props.get(SOAPToRESTConstants.TYPE_ATTRIBUTE);
                arrayProperty.put(key, props.get(SOAPToRESTConstants.TYPE_ATTRIBUTE));
                arrayProperty.put(SOAPToRESTConstants.TYPE_ATTRIBUTE, SOAPToRESTConstants.PARAM_TYPES.ARRAY);
                arrayProperty.put(SOAPToRESTConstants.SEQUENCE_GEN.PARAMETER_NAME,
                        props.get(SOAPToRESTConstants.TYPE_ATTRIBUTE));
                arrayProperty.put(SOAPToRESTConstants.SEQUENCE_GEN.XPATH, jsonPath);
                propArray.add(arrayProperty);
                return;
            }
        } else {
            props = (JSONObject) propDefinitions.get(SOAPToRESTConstants.SWAGGER.PROPERTIES);
        }
        for (Object property : props.entrySet()) {
            Map.Entry entry = (Map.Entry) property;
            String paramName = String.valueOf(entry.getKey());
            JSONObject value = (JSONObject) entry.getValue();
            if (value.get(SOAPToRESTConstants.SWAGGER.REF) != null) {
                String propDefinitionRef = String.valueOf(value.get(SOAPToRESTConstants.SWAGGER.REF))
                        .replaceAll(SOAPToRESTConstants.SWAGGER.DEFINITIONS_ROOT, "");
                jsonPath = definition + "." + propDefinitionRef;
                getNestedDefinitionsFromSwagger(definitions, propDefinitionRef, jsonPath, propArray);
            } else {
                JSONObject nestedProp = new JSONObject();
                String key;
                if (jsonPath.endsWith(definition)) {
                    key = jsonPath + "." + paramName;
                } else {
                    key = definition + "." + paramName;
                }
                nestedProp.put(key, value);
                propArray.add(nestedProp);
            }
        }
    }
}
