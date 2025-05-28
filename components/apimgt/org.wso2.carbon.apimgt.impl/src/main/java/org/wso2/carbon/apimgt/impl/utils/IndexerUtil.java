/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlSchemaType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Indexers.
 */
public class IndexerUtil {
    private static final Log log = LogFactory.getLog(IndexerUtil.class);

    /**
     * Method to fetch API details for a given resource.
     *
     * @param registry Registry
     * @param resource Resource
     * @param fields   Fields list
     * @throws RegistryException      on failure
     * @throws APIManagementException on failure
     */
    public static void fetchRequiredDetailsFromAssociatedAPI(Registry registry, Resource resource,
                                                             Map<String, List<String>> fields)
            throws RegistryException, APIManagementException {
        String resourceFilePath = resource.getPath();
        String apiPath = resourceFilePath.substring(0, resourceFilePath.lastIndexOf('/') + 1)
                + APIConstants.API_KEY;
        if (registry.resourceExists(apiPath)) {
            Resource apiResource = registry.get(apiPath);
            GenericArtifactManager apiArtifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiResource.getUUID());
            String apiStatus = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS).toLowerCase();
            String publisherRoles = apiResource.getProperty(APIConstants.PUBLISHER_ROLES);
            fields.put(APIConstants.API_OVERVIEW_STATUS, Arrays.asList(apiStatus));
            fields.put(APIConstants.PUBLISHER_ROLES, Arrays.asList(publisherRoles));
        } else {
            log.warn("API does not exist at " + apiPath);
        }
    }


    /**
     * This method can be used to filter out the json values, excluding keys from the json files
     *
     * @param jsonAsString JSON string
     * @return values string
     */
    public static String getValuesFromJsonString(String jsonAsString) {
        JSONObject jsonObject = new JSONObject(jsonAsString);
        StringBuilder values = new StringBuilder();
        extractJsonValues(jsonObject, values);
        return values.toString().trim();
    }

    /**
     * Extract json values as a string from JSON object recursively
     *
     * @param json   json object
     * @param values StringBuilder values
     */
    private static void extractJsonValues(Object json, StringBuilder values) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                extractJsonValues(jsonObject.get(key), values);
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.length(); i++) {
                extractJsonValues(jsonArray.get(i), values);
            }
        } else {
            values.append(json.toString()).append(" ");
        }
    }

    /**
     * This method is used to extract types from graphql schema
     *
     * @param schemaString Schema String
     * @return definition string
     */
    public static String getTypesFromGraphQLSchemaString(String schemaString) {
        List<GraphqlSchemaType> types = new GraphQLSchemaDefinition()
                .extractGraphQLTypeList(schemaString);
        StringBuilder definitionString = new StringBuilder();

        for (GraphqlSchemaType type : types) {
            definitionString.append(type.getType()).append(" ");
            definitionString.append(String.join(" ", type.getFieldList()));
        }
        return definitionString.toString();
    }

    /**
     * This method returns string content extracted from XML data
     *
     * @param xmlData xml data bytes
     * @return string content
     */
    public static String getContentFromXMLData(byte[] xmlData) {
        try {
            DocumentBuilderFactory factory = APIUtil.getSecuredDocumentBuilder();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData));
            doc.getDocumentElement().normalize();
            return extractTextAndAttributesFromElement(doc.getDocumentElement());
        } catch (Exception e) {
            log.error("Error while parsing XML data", e);
            return "";
        }
    }

    /**
     * This method is used to recursively extract text and attributes as a string
     *
     * @param element Doc element
     * @return text and attributes string
     */

    private static String extractTextAndAttributesFromElement(Element element) {
        StringBuilder text = new StringBuilder();

        // Extract attributes from the element
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            text.append(attr.getNodeName()).append("=").append(attr.getNodeValue()).append(" ");
        }

        // Extract text from child nodes
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // Recursively extract text and attributes
                text.append(extractTextAndAttributesFromElement((Element) node)).append(" ");
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                // Append text directly
                text.append(node.getTextContent().trim()).append(" ");
            }
        }
        return text.toString().trim();
    }

}

