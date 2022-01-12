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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class used to generate Synapse Artifact.
 */
public class SynapsePolicyAggregator {

    private static final Log log = LogFactory.getLog(SynapsePolicyAggregator.class);

    public static String generatePolicySequence(String pathToAchieve, API api)
            throws APIManagementException {

        Set<URITemplate> uriTemplates = api.getUriTemplates();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Jinjava jinjava = new Jinjava();

        String sequenceTemplate =
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

        Map<String, Object> j2ConfigMap = new HashMap<>();
        List<Object> caseList = new ArrayList<>();
        for (URITemplate template : uriTemplates) {

            Map<String, Object> caseInFlowMap = new HashMap<>();
            Map<String, Object> caseOutFlowMap = new HashMap<>();
            Map<String, Object> caseFaultFlowMap = new HashMap<>();

            String uriTemplateString = template.getUriTemplate();
            String method = template.getHTTPVerb();
            String key = method + "_" + uriTemplateString.replaceAll("[\\W]", "\\\\$0");;

            List<String> caseBodyInFlow = new ArrayList<>();
            List<String> caseBodyOutFlow = new ArrayList<>();
            List<String> caseBodyFaultFlow = new ArrayList<>();

            List<OperationPolicy> operationPolicies = template.getOperationPolicies();
            for (OperationPolicy policy : operationPolicies) {
                Map<String, Object> policyParameters = policy.getParameters();

                String policyDefinition = ImportUtils.getOperationPolicyDefinitionFromFile(pathToAchieve,
                        policy.getPolicyName());
                if (policyDefinition == null) {
                    policyDefinition = apiProvider
                            .getAPISpecificPolicyDefinitionForPolicyName(api.getUuid(), policy.getPolicyName())
                            .getDefinition();
                }

                if (policyDefinition != null) {
                    String renderedTemplate = jinjava.render(policyDefinition, policyParameters);
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
        }

        APIIdentifier apiIdentifier = api.getId();

        j2ConfigMap.put("sequence_name", apiIdentifier.getApiName() + ":" + apiIdentifier.getVersion() + "--In");
        j2ConfigMap.put("case_list", caseList);

        String renderedTemplate = jinjava.render(sequenceTemplate, j2ConfigMap);

        return renderedTemplate;

    }





}
