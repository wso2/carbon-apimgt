/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import com.hubspot.jinjava.Jinjava;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.OperationPolicyComparator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class used to generate Synapse Artifact.
 */
public class SynapsePolicyAggregator {

    private static final Log log = LogFactory.getLog(SynapsePolicyAggregator.class);
    private static final String POLICY_SEQUENCE_TEMPLATE_LOCATION = CarbonUtils.getCarbonHome() + File.separator
            + "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator
            + "operation_policy_template.j2";
    private static final String CUSTOM_BACKEND_SEQUENCE_TEMPLATE_LOCATION =
            CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" + File.separator
                    + "api_templates" + File.separator + "custom_backend_sequence_template.j2";
    private static final String GATEWAY_POLICY_SEQUENCE_TEMPLATE_LOCATION = CarbonUtils.getCarbonHome() + File.separator
            + "repository" + File.separator + "resources" + File.separator + "templates" + File.separator
            + "gateway_policy_template.j2";

    public static String generatePolicySequenceForUriTemplateSet(Set<URITemplate> uriTemplates, API api,
                                                                 String sequenceName, String flow,
                                                                 String pathToAchieve)
            throws APIManagementException, IOException {

        List<Object> operationPolicyCaseList = new ArrayList<>();
        List<String> apiLevelPolicyRenderedList = new ArrayList<>();
        for (URITemplate template : uriTemplates) {
            populateOperationPolicyCaseList(template, pathToAchieve, flow, operationPolicyCaseList);
        }

        if (api != null) {
            if (api.getApiPolicies() != null && !api.getApiPolicies().isEmpty()) {
                apiLevelPolicyRenderedList = renderPolicyMapping(api.getApiPolicies(), pathToAchieve, flow);
            }
        }

        Map<String, Object> configMap = new HashMap<>();
        boolean render = false;
        String operationPolicyTemplate = FileUtil.readFileToString(POLICY_SEQUENCE_TEMPLATE_LOCATION)
                .replace("\\", ""); //Removing escape characters from the template
        configMap.put("sequence_name", sequenceName);
        if (APIConstants.OPERATION_SEQUENCE_TYPE_FAULT.equals(flow)) {
            configMap.put("fault_sequence", true);
        }

        if (!operationPolicyCaseList.isEmpty()) {
            configMap.put("case_list", operationPolicyCaseList);
            render = true;
        }

        if (!apiLevelPolicyRenderedList.isEmpty()) {
            configMap.put("api_level_policies", apiLevelPolicyRenderedList);
            render = true;
        }

        if (render) {
            return renderPolicyTemplate(operationPolicyTemplate, configMap);
        } else {
            return "";
        }
    }

    public static String generateSequenceBackendForAPIProducts(String seqName, String prodSeq, String pathToArchive,
            String endpointType) throws APIManagementException, IOException {
        Map<String, Object> configMap = new HashMap<>();
        String customBackendTemplate = FileUtil.readFileToString(CUSTOM_BACKEND_SEQUENCE_TEMPLATE_LOCATION)
                .replace("\\", "");
        // change sequence name from the upper function
        configMap.put("sequence_name", prodSeq);
        String sanitizedSequence = renderCustomBackendSequence(seqName, pathToArchive);
        if (sanitizedSequence == null) {
            return null;
        }
        configMap.put("custom_sequence", sanitizedSequence);
        configMap.put("endpoint_type", endpointType);
        return renderPolicyTemplate(customBackendTemplate, configMap);
    }

    public static String generateBackendSequenceForCustomSequence(String fileName, String pathToArchive,
            String endpointType, String apiSeqName) throws APIManagementException, IOException {
        Map<String, Object> configMap = new HashMap<>();
        String customBackendTemplate = FileUtil.readFileToString(CUSTOM_BACKEND_SEQUENCE_TEMPLATE_LOCATION)
                .replace("\\", "");
        // change sequence name from the upper function
        configMap.put("sequence_name", apiSeqName);
        String sanitizedSequence = renderCustomBackendSequence(fileName, pathToArchive);
        if (sanitizedSequence == null) {
            return null;
        }
        configMap.put("custom_sequence", sanitizedSequence);
        configMap.put("endpoint_type", endpointType);
        return renderPolicyTemplate(customBackendTemplate, configMap);
    }

    /**
     * This method will populate the operation policy case list.
     *
     * @param template      URI Template
     * @param pathToAchieve Path to the directory where the policies are located
     * @param flow          Flow (i.e. specifies whether request, response or fault flow)
     * @param caseList      List of cases
     * @return Returns the populated case list
     * @throws APIManagementException If an error occurs while populating the case list
     */
    private static List<Object> populateOperationPolicyCaseList(URITemplate template, String pathToAchieve, String flow,
            List<Object> caseList) throws APIManagementException {

        Map<String, Object> caseMap = new HashMap<>();
        String uriTemplateString = template.getUriTemplate();
        String method = template.getHTTPVerb();
        String key = method + "_" + uriTemplateString.replaceAll("[\\W]", "\\\\$0");

        // This will replace & with &amp; for query params
        key = StringEscapeUtils.escapeXml(StringEscapeUtils.unescapeXml(key));

        List<OperationPolicy> operationPolicies = template.getOperationPolicies();
        List<String> caseBody = renderPolicyMapping(operationPolicies, pathToAchieve, flow);

        if (caseBody.size() != 0) {
            caseMap.put("case_regex", key);
            caseMap.put("policy_sequence", caseBody);
            caseList.add(caseMap);
        }

        return caseList;
    }

    /**
     * This method will render the policy template with the given parameters.
     *
     * @param policyList    List of policies to be rendered
     * @param pathToAchieve Path to the directory where the policies are located
     * @param flow          Flow (i.e. specifies whether request, response or fault flow)
     * @return List of rendered policies
     * @throws APIManagementException If an error occurs while rendering the policy template
     */
    private static List<String> renderPolicyMapping(List<OperationPolicy> policyList, String pathToAchieve, String flow)
            throws APIManagementException {

        List<String> renderedPolicyMappingList = new ArrayList<>();
        String policyDirectory = pathToAchieve + File.separator + ImportExportConstants.POLICIES_DIRECTORY;

        Collections.sort(policyList, new OperationPolicyComparator());
        for (OperationPolicy policy : policyList) {
            if (flow.equals(policy.getDirection())) {
                Map<String, Object> policyParameters = policy.getParameters();
                String policyFileName = APIUtil.getOperationPolicyFileName(policy.getPolicyName(),
                        policy.getPolicyVersion(), policy.getPolicyType());
                OperationPolicySpecification policySpecification = ImportUtils
                        .getOperationPolicySpecificationFromFile(policyDirectory, policyFileName);
                if (policySpecification.getSupportedGateways()
                        .contains(APIConstants.OPERATION_POLICY_SUPPORTED_GATEWAY_SYNAPSE)) {
                    OperationPolicyDefinition policyDefinition =
                            APIUtil.getOperationPolicyDefinitionFromFile(policyDirectory, policyFileName,
                                    APIConstants.SYNAPSE_POLICY_DEFINITION_EXTENSION);
                    if (policyDefinition != null) {
                        try {
                            String renderedTemplate =
                                    renderPolicyTemplate(policyDefinition.getContent(), policyParameters);
                            if (renderedTemplate != null && !renderedTemplate.isEmpty()) {
                                String sanitizedPolicy;
                                try {
                                    sanitizedPolicy = sanitizeOMElementWithSuperParentNode(renderedTemplate);
                                } catch (Exception e) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cannot wrap the policy " + policy.getPolicyName()
                                                + " with a super parent. Trying without wrapping.");
                                    }
                                    // As we can't wrap the policy definition with a super parent, trying the build
                                    // OM element with the provided policy definition. This will select first child node
                                    // and drop the other child nodes if a parent node is not configured.
                                    sanitizedPolicy = APIUtil.buildSecuredOMElement(
                                            new ByteArrayInputStream(renderedTemplate.getBytes())).toString();
                                }
                                renderedPolicyMappingList.add(sanitizedPolicy);
                            }
                        } catch (Exception e) {
                            log.error("Error parsing the policy definition for " + policy.getPolicyName());
                        }

                    } else {
                        log.error("Policy definition for " + policy.getPolicyName() + " is not found in the artifact");
                    }
                } else {
                    log.error("Policy " + policy.getPolicyName() + " does not support Synapse gateway. " +
                            "Hence skipped");
                }
            }
        }
        return renderedPolicyMappingList;
    }

    private static String renderCustomBackendSequence(String sequenceName, String pathToArchive)
            throws APIManagementException {
        String policyDirectory = pathToArchive + File.separator + ImportExportConstants.CUSTOM_BACKEND_DIRECTORY;
        String sequence = APIUtil.getCustomBackendSequenceFromFile(policyDirectory, sequenceName,
                APIConstants.SYNAPSE_POLICY_DEFINITION_EXTENSION_XML);
        if (sequence == null) {
            return null;
        }
        return renderPolicyTemplate(sequence, new HashMap<>());
    }

    /**
     *  As there can be multiple child elements without a root element, for sanitization, we will
     *  first wrap the template with a dummy root element and build the OM element.
     *  This element will be removed after sanitization step.
     */
    private static String sanitizeOMElementWithSuperParentNode(String xmlString) throws Exception {

        String updatedXmlString = "<root>" + xmlString + "</root>";
        OMElement sanitizedPolicyElement =
                APIUtil.buildSecuredOMElement(new ByteArrayInputStream(updatedXmlString.getBytes()));
        StringBuilder filteredTemplate = new StringBuilder();
        for (Iterator childElements = sanitizedPolicyElement.getChildElements();
             childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            filteredTemplate.append(element.toString());
        }
        return filteredTemplate.toString();
    }

    public static String renderPolicyTemplate(String template, Map<String, Object> configMap) {

        Jinjava jinjava = new Jinjava();
        return jinjava.render(template, configMap);
    }

    public static String getSequenceExtensionFlow(String flow) {

        String sequenceExtension = null;
        switch (flow) {
            case APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST:
                sequenceExtension = APIConstants.API_CUSTOM_SEQ_IN_EXT;
                break;
            case APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE:
                sequenceExtension = APIConstants.API_CUSTOM_SEQ_OUT_EXT;
                break;
            case APIConstants.OPERATION_SEQUENCE_TYPE_FAULT:
                sequenceExtension = APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
                break;
        }
        return sequenceExtension;
    }

    /**
     * Render the gateway level policy mapping.
     *
     * @param gatewayPolicyDataList List of gateway policy data
     * @param gatewayPolicies       List of gateway policies
     * @param flow                  Flow type
     * @param sequenceName          Sequence name
     * @return Rendered gateway policy mapping
     * @throws IOException
     */
    public static String generateGatewayPolicySequenceForPolicyMapping(List<OperationPolicyData> gatewayPolicyDataList,
            List<OperationPolicy> gatewayPolicies, String flow, String sequenceName) throws IOException {

        List<String> gatewayLevelPolicyRenderedList = renderGatewayPolicyMapping(gatewayPolicyDataList, gatewayPolicies,
                flow);
        Map<String, Object> configMap = new HashMap<>();

        String gatewayPolicyTemplate = FileUtil.readFileToString(GATEWAY_POLICY_SEQUENCE_TEMPLATE_LOCATION)
                .replace("\\", ""); //Removing escape characters from the template
        configMap.put("sequence_name", sequenceName);

        if (!gatewayLevelPolicyRenderedList.isEmpty()) {
            configMap.put("gateway_policies", gatewayLevelPolicyRenderedList);
            return renderPolicyTemplate(gatewayPolicyTemplate, configMap);
        } else {
            return "";
        }
    }

    private static List<String> renderGatewayPolicyMapping(List<OperationPolicyData> gatewayPolicyDataList,
            List<OperationPolicy> gatewayPolicyList, String flow) {

        List<String> renderedPolicyMappingList = new ArrayList<>();
        Collections.sort(gatewayPolicyList, new OperationPolicyComparator());
        for (OperationPolicy policy : gatewayPolicyList) {
            if (flow.equals(policy.getDirection())) {
                OperationPolicySpecification policySpecification = null;
                OperationPolicyDefinition policyDefinition = null;
                for (OperationPolicyData operationPolicyData : gatewayPolicyDataList) {
                    if (policy.getPolicyId().equals(operationPolicyData.getPolicyId())) {
                        policySpecification = operationPolicyData.getSpecification();
                        policyDefinition = operationPolicyData.getSynapsePolicyDefinition();
                    }
                }
                Map<String, Object> policyParameters = policy.getParameters();
                if (policySpecification != null) {
                    if (policySpecification.getSupportedGateways()
                            .contains(APIConstants.OPERATION_POLICY_SUPPORTED_GATEWAY_SYNAPSE)) {
                        if (policyDefinition != null) {
                            try {
                                String renderedTemplate = renderPolicyTemplate(policyDefinition.getContent(),
                                        policyParameters);
                                if (renderedTemplate != null && !renderedTemplate.isEmpty()) {
                                    String sanitizedPolicy;
                                    try {
                                        sanitizedPolicy = sanitizeOMElementWithSuperParentNode(renderedTemplate);
                                    } catch (Exception e) {
                                        log.debug("Cannot wrap the policy " + policy.getPolicyName()
                                                + " with a super parent. Trying without wrapping.");
                                        // As we can't wrap the policy definition with a super parent, trying the build
                                        // OM element with the provided policy definition. This will select first child
                                        // node and drop the other child nodes if a parent node is not configured.
                                        sanitizedPolicy = APIUtil.buildSecuredOMElement(
                                                new ByteArrayInputStream(renderedTemplate.getBytes())).toString();
                                    }
                                    renderedPolicyMappingList.add(sanitizedPolicy);
                                }
                            } catch (Exception e) {
                                log.error("Error parsing the policy definition for " + policy.getPolicyName());
                            }
                        } else {
                            log.error("Policy definition for " + policy.getPolicyName()
                                    + " is not found in the artifact");
                        }
                    } else {
                        log.error("Policy " + policy.getPolicyName() + " does not support Synapse gateway. "
                                + "Hence skipped");
                    }
                } else {
                    log.error("Policy Specification for " + policy.getPolicyName() + " is not found in the artifact");
                }
            }
        } return renderedPolicyMappingList;
    }
}
