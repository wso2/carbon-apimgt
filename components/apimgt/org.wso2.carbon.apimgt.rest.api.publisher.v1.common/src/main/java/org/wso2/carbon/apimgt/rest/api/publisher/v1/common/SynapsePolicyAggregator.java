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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.OperationPolicyComparator;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class used to generate Synapse Artifact.
 */
public class SynapsePolicyAggregator {

    private static final Log log = LogFactory.getLog(SynapsePolicyAggregator.class);
    private static String defaultSequenceTemplate =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"{{sequence_name}}\">" +
                    "   <switch source=\"$ctx:URL_TEMPLATE_SWITCH\">" +
                    "    {% for case in case_list %}" +
                    "      <case regex=\"{{case.case_regex}}\">" +
                    "        {% for policy in case.policy_sequence %}" +
                    "        {{policy}}" +
                    "        {% endfor %}" +
                    "      </case>" +
                    "    {% endfor %}" +
                    "   </switch>" +
                    "</sequence>";

    public static String generatePolicySequenceForAPIs(String pathToAchieve, API api)
            throws APIManagementException {

        Set<URITemplate> uriTemplates = api.getUriTemplates();
        List<Object> caseList = new ArrayList<>();

        for (URITemplate template : uriTemplates) {
            populatePolicyCaseList(template, pathToAchieve, api.getRevisionedApiId(), caseList);
        }

        if (caseList.size() != 0) {
            Map<String, Object> configMap = new HashMap<>();
            String seqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_OPERATION_POLICY_SEQ_EXT;
            configMap.put("sequence_name", seqExt);
            configMap.put("case_list", caseList);

            return renderPolicyTemplate(defaultSequenceTemplate, configMap);
        } else {
            return "";
        }
    }

    public static String generatePolicySequenceForProducts(APIProduct apiProduct, Map<String, String> apiLocationsMap)
            throws APIManagementException {

        List<Object> caseList = new ArrayList<>();

        for (APIProductResource apiProductResource : apiProduct.getProductResources()) {
            String locationKey = APIUtil.getSequenceExtensionName(apiProductResource.getApiIdentifier().getName(),
                    apiProductResource.getApiIdentifier().getVersion());
            String pathToAchieve = apiLocationsMap.get(locationKey);

            URITemplate template = apiProductResource.getUriTemplate();
            populatePolicyCaseList(template, pathToAchieve, apiProduct.getRevisionedApiProductId(), caseList);
        }
        if (caseList.size() != 0) {
            Map<String, Object> configMap = new HashMap<>();
            String seqExt = APIUtil.getSequenceExtensionName(apiProduct.getId().getName(),
                    apiProduct.getId().getVersion()) + APIConstants.API_OPERATION_POLICY_SEQ_EXT;
            configMap.put("sequence_name", seqExt);
            configMap.put("case_list", caseList);

            return renderPolicyTemplate(defaultSequenceTemplate, configMap);
        } else {
            return "";
        }
    }

    public static List<Object> populatePolicyCaseList(URITemplate template, String pathToAchieve, String revisionUUID,
                                                      List<Object> caseList) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Map<String, Object> caseInFlowMap = new HashMap<>();
        Map<String, Object> caseOutFlowMap = new HashMap<>();
        Map<String, Object> caseFaultFlowMap = new HashMap<>();

        String uriTemplateString = template.getUriTemplate();
        String method = template.getHTTPVerb();
        String key = method + "_" + uriTemplateString.replaceAll("[\\W]", "\\\\$0");
        ;

        List<String> caseBodyInFlow = new ArrayList<>();
        List<String> caseBodyOutFlow = new ArrayList<>();
        List<String> caseBodyFaultFlow = new ArrayList<>();

        List<OperationPolicy> operationPolicies = template.getOperationPolicies();
        Collections.sort(operationPolicies, new OperationPolicyComparator());
        for (OperationPolicy policy : operationPolicies) {
            Map<String, Object> policyParameters = policy.getParameters();

            String policyDefinition = ImportUtils.getOperationPolicyDefinitionFromFile(pathToAchieve,
                    policy.getPolicyName());
            if (policyDefinition == null) {
                policyDefinition = apiProvider.getAPISpecificPolicyByPolicyName(revisionUUID,
                        policy.getPolicyName()).getDefinition();
            }

            if (policyDefinition != null) {
                String renderedTemplate = renderPolicyTemplate(policyDefinition, policyParameters);
                if (APIConstants.OPERATION_SEQUENCE_TYPE_IN.equals(policy.getDirection())) {
                    caseBodyInFlow.add(renderedTemplate);
                } else if (APIConstants.OPERATION_SEQUENCE_TYPE_OUT.equals(policy.getDirection())) {
                    caseBodyOutFlow.add(renderedTemplate);
                } else if (APIConstants.OPERATION_SEQUENCE_TYPE_FAULT.equals(policy.getDirection())) {
                    caseBodyFaultFlow.add(renderedTemplate);
                }
            } else {
                log.error("Policy definition for " + policy.getPolicyName() + " is not found in the artifact");
            }
        }

        if (caseBodyInFlow.size() != 0) {
            caseInFlowMap.put("case_regex", key + "_IN");
            caseInFlowMap.put("policy_sequence", caseBodyInFlow);
            caseList.add(caseInFlowMap);
        }

        if (caseBodyOutFlow.size() != 0) {
            caseOutFlowMap.put("case_regex", key + "_OUT");
            caseOutFlowMap.put("policy_sequence", caseBodyOutFlow);
            caseList.add(caseOutFlowMap);
        }

        if (caseBodyFaultFlow.size() != 0) {
            caseFaultFlowMap.put("case_regex", key + "_FAULT");
            caseFaultFlowMap.put("policy_sequence", caseBodyFaultFlow);
            caseList.add(caseFaultFlowMap);
        }

        return caseList;
    }

    public static String renderPolicyTemplate(String template, Map<String, Object> configMap) {

        if (configMap.size() == 0) {
            return "";
        } else {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(template, configMap);
        }
    }

}
