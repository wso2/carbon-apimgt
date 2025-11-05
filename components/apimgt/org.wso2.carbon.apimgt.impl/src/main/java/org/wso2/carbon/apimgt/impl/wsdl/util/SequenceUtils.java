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

import io.swagger.inflector.examples.models.ArrayExample;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.ObjectExample;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SOAPToRestSequence;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.ConfigContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Util class used for sequence generation of the soap to rest converted operations.
 */
public class SequenceUtils {
    private static final Logger log = LoggerFactory.getLogger(SequenceUtils.class);
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
            String resourcePath, String apiResourceName) throws APIManagementException {
        try {
            Resource regResource;
            if (apiResourceName.startsWith(SOAPToRESTConstants.SequenceGen.PATH_SEPARATOR)) {
                apiResourceName = apiResourceName.substring(1);
            }
            if (!registry.resourceExists(resourcePath)) {
                regResource = registry.newResource();
            } else {
                regResource = registry.get(resourcePath);
            }
            regResource.setContent(sequence);
            regResource.addProperty(SOAPToRESTConstants.METHOD, method);
            if (regResource.getProperty(SOAPToRESTConstants.Template.RESOURCE_PATH) != null) {
                regResource.removeProperty(SOAPToRESTConstants.Template.RESOURCE_PATH);
            }
            regResource.addProperty(SOAPToRESTConstants.Template.RESOURCE_PATH, apiResourceName);
            regResource.setMediaType("text/xml");
            registry.put(resourcePath, regResource);
        } catch (RegistryException e) {
            handleException("Error occurred while accessing the registry to save api sequence", e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            handleException("Error occurred while saving api sequence", e);
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

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
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
            APIUtil.loadTenantRegistry(tenantId);
            registry = registryService.getGovernanceSystemRegistry(tenantId);

            JSONObject sequences = (JSONObject) jsonParser.parse(content);
            for (Object sequence : sequences.keySet()) {
                String sequenceContent = (String) ((JSONObject) sequences.get(sequence)).get("content");
                String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        APIUtil.replaceEmailDomain(provider) + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version
                        + RegistryConstants.PATH_SEPARATOR + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE
                        + RegistryConstants.PATH_SEPARATOR + seqType + RegistryConstants.PATH_SEPARATOR + sequence
                        + ".xml";

                Resource regResource;
                if (registry.resourceExists(resourcePath)) {
                    regResource = registry.get(resourcePath);
                    regResource.setContent(sequenceContent);
                    if (log.isDebugEnabled()) {
                        log.debug("Api sequence content for " + resourcePath + " is: " + sequenceContent);
                    }
                    regResource.setMediaType("text/xml");
                    registry.put(resourcePath, regResource);
                }
            }
        } catch (ParseException e) {
            handleException("Error occurred while parsing the sequence json", e);
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        } catch (RegistryException e) {
            handleException("Error when create registry instance", e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            handleException("Error while creating registry resource", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Updates resource policy resource for the given resource id from the registry.
     *
     * @param identifier API identifier
     * @param resourceId Resource identifier
     * @param content    resource policy content
     * @throws APIManagementException
     */
    public static void updateResourcePolicyFromRegistryResourceId(APIIdentifier identifier, String resourceId, String content)
            throws APIManagementException {

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            APIUtil.loadTenantRegistry(tenantId);
            UserRegistry registry = registryService.getGovernanceSystemRegistry(tenantId);
            String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getApiName()
                    + RegistryConstants.PATH_SEPARATOR + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR
                    + SOAPToRESTConstants.SOAP_TO_REST_RESOURCE;
            Collection collection = (Collection) registry.get(resourcePath);
            String[] resources = collection.getChildren();

            if (resources == null) {
                handleException("Cannot find any resource policies at the path: " + resourcePath);
            }
            for (String path : resources) {
                Collection resourcePolicyCollection = (Collection) registry.get(path);
                String[] resourcePolicies = resourcePolicyCollection.getChildren();
                if (resourcePolicies == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Cannot find resource policies under path: " + path);
                    }
                    continue;
                }
                for (String resourcePolicyPath : resourcePolicies) {
                    Resource resource = registry.get(resourcePolicyPath);
                    if (StringUtils.isNotEmpty(resourceId) && resourceId.equals(((ResourceImpl) resource).getUUID())) {
                        resource.setContent(content);
                        resource.setMediaType(SOAPToRESTConstants.TEXT_XML);
                        registry.put(resourcePolicyPath, resource);
                        break;
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Number of REST resources for " + resourcePath + " is: " + resources.length);
            }
        } catch (UserStoreException e) {
            handleException("Error while reading tenant information", e);
        } catch (RegistryException e) {
            handleException("Error when create registry instance", e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            handleException("Error while setting the resource policy content for the registry resource", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Gets resource policy resource for the given resource id from the registry.
     *
     * @param API  api
     * @param resourceId Resource identifier
     * @return resource policy string for the given resource id
     * @throws APIManagementException
     */
    public static String getResourcePolicyFromRegistryResourceId(API api, String resourceId)
            throws APIManagementException {
        String response = null;
        List<SOAPToRestSequence> sequences = api.getSoapToRestSequences();
        if (sequences == null) {
            handleException("Cannot find any resource policies for the api " + api.getUuid());
        }
        boolean found = false;
        for (SOAPToRestSequence soapToRestSequence : sequences) {
            if (soapToRestSequence.getUuid().equals(resourceId)) {
                JSONObject resultJson = new JSONObject();

                String content = soapToRestSequence.getContent();
                String resourceName = soapToRestSequence.getPath();
                String httpMethod = soapToRestSequence.getMethod();
                Map<String, String> resourceMap = new HashMap<>();
                resourceMap.put(SOAPToRESTConstants.RESOURCE_ID, soapToRestSequence.getUuid());
                resourceMap.put(SOAPToRESTConstants.METHOD, httpMethod);
                resourceMap.put(SOAPToRESTConstants.CONTENT, content);
                resultJson.put(resourceName, resourceMap);
                response = resultJson.toJSONString();
                found = true;
                break;

            }
        }
        if (!found) {
            handleException("Cannot find any resource policies with policy id : " + resourceId);
        }
        return response;
    }

    /**
     * Gets soap to rest converted sequence from the registry
     * <p>
     * Note: this method is directly invoked from the jaggery layer
     *
     * @param api API
     * @param seqType  to identify the sequence is whether in/out sequence
     * @return converted sequences string for a given operation
     * @throws APIManagementException throws exceptions on unsuccessful retrieval of resources in registry
     */
    public static String getRestToSoapConvertedSequence(API api, String seqType) throws APIManagementException {
        JSONObject resultJson = new JSONObject();
        List<SOAPToRestSequence> sequences = api.getSoapToRestSequences();
        if (sequences == null) {
            handleException("Cannot find any resource policies for the api " + api.getUuid());
        }
        for (SOAPToRestSequence sequence : sequences) {
            if (sequence.getDirection().toString().equalsIgnoreCase(seqType)) {
                String content = sequence.getContent();
                String resourceName = sequence.getPath();
                String httpMethod = sequence.getMethod();
                Map<String, String> resourceMap = new HashMap<>();
                resourceMap.put(SOAPToRESTConstants.RESOURCE_ID, sequence.getUuid());
                resourceMap.put(SOAPToRESTConstants.METHOD, httpMethod);
                resourceMap.put(SOAPToRESTConstants.CONTENT, content);
                resultJson.put(resourceName + "_" + httpMethod, resourceMap);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Saved sequence for type " + seqType + " for api:" + api.getId().getProviderName() + "-"
                    + api.getId().getApiName() + "-" + api.getId().getVersion() + " is: " + resultJson.toJSONString());
        }
        return resultJson.toJSONString();
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
        JSONArray parameters = (JSONArray) content.get(SOAPToRESTConstants.Swagger.PARAMETERS);
        List<JSONObject> mappingList = new ArrayList<>();
        for (Object param : parameters) {
            String inputType = String.valueOf(((JSONObject) param).get(SOAPToRESTConstants.Swagger.IN));
            if (inputType.equals(SOAPToRESTConstants.Swagger.BODY)) {
                JSONObject schema = (JSONObject) ((JSONObject) param).get(SOAPToRESTConstants.Swagger.SCHEMA);
                String definitionPath = String.valueOf(schema.get(SOAPToRESTConstants.Swagger.REF));
                String definition = definitionPath
                        .replaceAll(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT, SOAPToRESTConstants.EMPTY_STRING);
                JSONObject definitions = (JSONObject) ((JSONObject) swaggerObj
                        .get(SOAPToRESTConstants.Swagger.DEFINITIONS)).get(definition);
                JSONObject properties = (JSONObject) definitions.get(SOAPToRESTConstants.Swagger.PROPERTIES);

                for (Object property : properties.entrySet()) {
                    Map.Entry entry = (Map.Entry) property;
                    String paramName = String.valueOf(entry.getKey());
                    JSONObject value = (JSONObject) entry.getValue();
                    JSONArray propArray = new JSONArray();
                    if (value.get(SOAPToRESTConstants.Swagger.REF) != null) {
                        String propDefinitionRef = String.valueOf(value.get(SOAPToRESTConstants.Swagger.REF))
                                .replaceAll(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT,
                                        SOAPToRESTConstants.EMPTY_STRING);
                        getNestedDefinitionsFromSwagger(
                                (JSONObject) swaggerObj.get(SOAPToRESTConstants.Swagger.DEFINITIONS), propDefinitionRef,
                                propDefinitionRef, propArray);
                        JSONObject refObj = new JSONObject();
                        refObj.put(paramName, propArray);
                        if (log.isDebugEnabled()) {
                            log.debug("Properties for from resource parameter: " + paramName + " are: " + propArray
                                    .toJSONString());
                        }
                        mappingList.add(refObj);
                    } else if (String.valueOf(value.get(SOAPToRESTConstants.Swagger.TYPE))
                            .equals(SOAPToRESTConstants.ParamTypes.ARRAY)) {
                        JSONObject arrObj = new JSONObject();
                        arrObj.put(((Map.Entry) property).getKey(), ((Map.Entry) property).getValue());
                        mappingList.add(arrObj);
                        if (log.isDebugEnabled()) {
                            log.debug("Properties for from array type resource parameter: " + ((Map.Entry) property)
                                    .getKey() + " are: " + arrObj.toJSONString());
                        }
                    }
                }
            } else {
                JSONObject queryObj = new JSONObject();
                queryObj.put(((JSONObject) param).get(SOAPToRESTConstants.Swagger.NAME), param);
                mappingList.add(queryObj);
                if (log.isDebugEnabled()) {
                    log.debug("Properties for from query type resource parameter: " + queryObj.toJSONString());
                }
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
        if (SOAPToRESTConstants.ParamTypes.ARRAY.equals(propDefinitions.get(SOAPToRESTConstants.TYPE_ATTRIBUTE))) {
            props = (JSONObject) propDefinitions.get(SOAPToRESTConstants.Swagger.ITEMS);
            if (props.get(SOAPToRESTConstants.Swagger.REF) == null) {
                JSONObject arrayProperty = new JSONObject();
                String key = jsonPath + "." + props.get(SOAPToRESTConstants.TYPE_ATTRIBUTE);
                arrayProperty.put(key, props.get(SOAPToRESTConstants.TYPE_ATTRIBUTE));
                arrayProperty.put(SOAPToRESTConstants.TYPE_ATTRIBUTE, SOAPToRESTConstants.ParamTypes.ARRAY);
                arrayProperty.put(SOAPToRESTConstants.SequenceGen.PARAMETER_NAME,
                        props.get(SOAPToRESTConstants.TYPE_ATTRIBUTE));
                arrayProperty.put(SOAPToRESTConstants.SequenceGen.XPATH, jsonPath);
                propArray.add(arrayProperty);
                return;
            }
        } else {
            props = (JSONObject) propDefinitions.get(SOAPToRESTConstants.Swagger.PROPERTIES);
        }
        for (Object property : props.entrySet()) {
            Map.Entry entry = (Map.Entry) property;
            String paramName = String.valueOf(entry.getKey());
            JSONObject value = (JSONObject) entry.getValue();
            if (value.get(SOAPToRESTConstants.Swagger.REF) != null) {
                String propDefinitionRef = String.valueOf(value.get(SOAPToRESTConstants.Swagger.REF))
                        .replaceAll(SOAPToRESTConstants.Swagger.DEFINITIONS_ROOT, SOAPToRESTConstants.EMPTY_STRING);
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
            if (log.isDebugEnabled()) {
                log.debug("json path for definition: " + definition + " is: " + jsonPath);
            }
        }
    }

    /**
     * Generate json paths for the elements of a given json
     *
     * @param json                     json payload
     * @param parameterJsonPathMapping map to be populated with the respective json paths
     * @throws JSONException
     */
    public static void listJson(org.json.JSONObject json, Map<String, String> parameterJsonPathMapping)
            throws JSONException {
        listJSONObject(SOAPToRESTConstants.EMPTY_STRING, json, parameterJsonPathMapping);
    }

    private static void listObject(String parent, Object data, Map<String, String> parameterJsonPathMapping)
            throws JSONException {
        if (data instanceof org.json.JSONObject) {
            listJSONObject(parent, (org.json.JSONObject) data, parameterJsonPathMapping);
        } else if (data instanceof org.json.JSONArray) {
            listJSONArray(parent, (org.json.JSONArray) data, parameterJsonPathMapping);
        } else {
            listPrimitive(parent, data, parameterJsonPathMapping);
        }
    }

    private static void listJSONObject(String parent, org.json.JSONObject json,
            Map<String, String> parameterJsonPathMapping) throws JSONException {

        Iterator it = json.keys();
        if (!it.hasNext()) {
            if (log.isDebugEnabled()) {
                log.debug("JSON path of the parameter: " + parent + " and type: object");
            }

            String[] types = parent.split(",");
            if (types.length > 1) {
                parameterJsonPathMapping.put(types[0], types[1]);
            } else {
                parameterJsonPathMapping.put(types[0], "object");
            }
        }
        while (it.hasNext()) {
            String key = (String) it.next();
            Object child = json.get(key);
            String childKey = parent.isEmpty() ? key : parent + "." + key;
            listObject(childKey, child, parameterJsonPathMapping);
        }
    }

    private static void listJSONArray(String parent, org.json.JSONArray json,
            Map<String, String> parameterJsonPathMapping) throws JSONException {

        for (int i = 0; i < json.length(); i++) {
            Object data = json.get(i);
            if (data instanceof org.json.JSONObject) {
                listObject(parent, data, parameterJsonPathMapping);
            } else {
                listObject(parent + ",array", data, parameterJsonPathMapping);
            }
        }
    }

    private static void listPrimitive(String parent, Object obj, Map<String, String> parameterJsonPathMapping) {

        log.debug("JSON path of the parameter: " + parent + " and type: primitive");
        String[] types = parent.split(",");
        if (types.length > 1) {
            parameterJsonPathMapping.put(types[0], types[1]);
        } else {
            parameterJsonPathMapping.put(types[0], "simple");
        }
    }

    public static void listExamples(Example example, Map<String, String> parameterJsonPathMapping)
            throws JSONException {
        listExamples(SOAPToRESTConstants.EMPTY_STRING, example, parameterJsonPathMapping);
    }

    /**
     * Recursively go through the example schema. A schema can be either a primitive type such as string, int or a
     * structured such as object or array. If it's a primitive type, If it's a primitive type, schema does not proceed
     * further and this ends the branch. If it is an object type or an array type, we need to proceed further as objects
     * can have further schemas included in and array can have either objects in its array elements or primitive
     * elements. Objects are stored as LinkedHashMaps in the Example data structure and Arrays are stored as ArrayList.
     * Each schema path will have a value either simple or array mapped which will be used in later operations. In this
     * method, a simple type will be assigned if the schema ends with a primitive type in an object schema. If it ends
     * as primitive elements in an array, array type is assigned.
     *
     * @param parent                   Parent nodes schema path
     * @param example                  Example data node
     * @param parameterJsonPathMapping Map to be populated with the respective schema paths
     */
    public static void listExamples(String parent, Example example, Map<String, String> parameterJsonPathMapping) {

        if (example == null) {
            parameterJsonPathMapping.put(parent, "simple");
            return;
        }
        if (SOAPToRESTConstants.Swagger.OBJECT_TYPE.equals(example.getTypeName())) {
            Map<String, Example> values = ((ObjectExample) example).getValues();
            if (values != null) {
                for (Map.Entry<String, Example> entry : values.entrySet()) {
                    String childKey = parent.isEmpty() ? entry.getKey() : parent + "." + entry.getKey();
                    listExamples(childKey, entry.getValue(), parameterJsonPathMapping);
                }
            } else {
                parameterJsonPathMapping.put(parent, "simple");
            }
        } else if (SOAPToRESTConstants.Swagger.ARRAY_TYPE.equals(example.getTypeName())) {
            List<Example> exampleArray = ((ArrayExample) example).getItems();
            if (exampleArray.size() > 0) {
                for (Example exampleItem : exampleArray) {
                    if (SOAPToRESTConstants.Swagger.OBJECT_TYPE.equals(exampleItem.getTypeName())) {
                        listExamples(parent, exampleItem, parameterJsonPathMapping);
                    } else {
                        parameterJsonPathMapping.put(parent, "array");
                    }
                }
            }
        } else {
            parameterJsonPathMapping.put(parent, "simple");
        }
    }
}
