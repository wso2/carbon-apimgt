/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleConditionTypeException;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.DateCondition;
import org.wso2.carbon.apimgt.api.model.policy.DateRangeCondition;
import org.wso2.carbon.apimgt.api.model.policy.HTTPVerbCondition;
import org.wso2.carbon.apimgt.api.model.policy.HeaderCondition;
import org.wso2.carbon.apimgt.api.model.policy.IPCondition;
import org.wso2.carbon.apimgt.api.model.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.api.model.policy.Limit;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.BandwidthLimitDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DateConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DateRangeConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.HTTPVerbConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.HeaderConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.IPConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.JWTClaimsConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.QueryParameterConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.RequestCountLimitDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottlePolicyDTO;
import org.wso2.carbon.base.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

public class ThrottleMappingUtil {

    private static final Log log = LogFactory.getLog(ThrottleMappingUtil.class);

    public static APIPolicy fromAdvancedPolicyDTOToPolicy (AdvancedThrottlePolicyDTO dto)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        APIPolicy apiPolicy = new APIPolicy(dto.getPolicyName());
        apiPolicy = updateFieldsFromDTOToPolicy(dto, apiPolicy);
        apiPolicy.setUserLevel(mapAdvancedPolicyUserLevelFromDTOToModel(dto.getUserLevel()));

        List<Pipeline> pipelines = fromConditionalGroupDTOListToPipelineList(dto.getConditionalGroups());
        apiPolicy.setPipelines(pipelines);
        return apiPolicy;
    }

    public static AdvancedThrottlePolicyDTO fromAdvancedPolicyToDTO (APIPolicy apiPolicy)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        AdvancedThrottlePolicyDTO policyDTO = new AdvancedThrottlePolicyDTO();
        policyDTO = updateFieldsFromToPolicyToDTO(apiPolicy, policyDTO);
        policyDTO.setUserLevel(mapAdvancedPolicyUserLevelFromModelToDTO(apiPolicy.getUserLevel()));
        List<ConditionalGroupDTO> groupDTOs = fromPipelineListToConditionalGroupDTOList(apiPolicy.getPipelines());
        policyDTO.setConditionalGroups(groupDTOs);
        return policyDTO;
    }
    

    /////////////////  ConditionalGroupDTOList <---> PipelineList ///////////////////////////////////////
    
    public static List<Pipeline> fromConditionalGroupDTOListToPipelineList(
            List<ConditionalGroupDTO> conditionalGroupDTOs)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException { //done
        List<Pipeline> pipelines = new ArrayList<>();
        for (ConditionalGroupDTO dto : conditionalGroupDTOs) {
            pipelines.add(fromConditionalGroupDTOToPipeline(dto));
        }
        return pipelines;
    }


    public static List<ConditionalGroupDTO> fromPipelineListToConditionalGroupDTOList(
            List<Pipeline> pipelines)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException { //done
        List<ConditionalGroupDTO> groupDTOs = new ArrayList<>();
        for (Pipeline pipeline : pipelines) {
            groupDTOs.add(fromPipelineToConditionalGroupDTO(pipeline));
        }
        return groupDTOs;
    }
    
    /////////////////////////// ConditionalGroupDTO <---> Pipeline (==ConditionalGroups) ////////////////
    
    public static Pipeline fromConditionalGroupDTOToPipeline(ConditionalGroupDTO dto) //done
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        Pipeline pipeline = new Pipeline();
        pipeline.setDescription(dto.getDescription());
        pipeline.setEnabled(dto.getEnabled());
        pipeline.setQuotaPolicy(fromDTOToQuotaPolicy(dto.getLimit()));
        
        List<Condition> conditions = fromDTOListToConditionList(dto.getConditions());
        pipeline.setConditions(conditions);
        return pipeline;
    }

    public static ConditionalGroupDTO fromPipelineToConditionalGroupDTO(Pipeline pipeline)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        ConditionalGroupDTO groupDTO = new ConditionalGroupDTO();
        groupDTO.setDescription(pipeline.getDescription());
        groupDTO.setEnabled(pipeline.isEnabled());
        groupDTO.setLimit(fromQuotaPolicyToDTO(pipeline.getQuotaPolicy()));

        List<ThrottleConditionDTO> conditionDTOList = fromConditionListToDTOList(pipeline.getConditions());
        groupDTO.setConditions(conditionDTOList);
        return groupDTO;
    }

    /////////////////////////// Condition LIST <---> DTO LIST ////////////////
    
    public static List<Condition> fromDTOListToConditionList(List<ThrottleConditionDTO> throttleConditionDTOs) //done
            throws UnsupportedThrottleConditionTypeException {
        List<Condition> conditions = new ArrayList<>();
        for (ThrottleConditionDTO dto : throttleConditionDTOs) {
            conditions.add(fromDTOToCondition(dto));
        }
        return conditions;
    }

    public static List<ThrottleConditionDTO> fromConditionListToDTOList(List<Condition> conditions)
            throws UnsupportedThrottleConditionTypeException {
        List<ThrottleConditionDTO> dtoList = new ArrayList<>();
        for (Condition condition : conditions) {
            dtoList.add(fromConditionToDTO(condition));
        }
        return dtoList;
    }
    
    /////////////////////////// Condition <---> DTO ////////////////
    
    public static Condition fromDTOToCondition(ThrottleConditionDTO dto)   //.................
            throws UnsupportedThrottleConditionTypeException {
        if (dto instanceof IPConditionDTO) {
            return fromDTOToIPCondition((IPConditionDTO)dto);
        } else if (dto instanceof DateConditionDTO) {
            return null;
        } else if (dto instanceof DateRangeConditionDTO) {
            return null;
        } else if (dto instanceof HeaderConditionDTO) {
            return null;
        } else if (dto instanceof HTTPVerbConditionDTO) {
            return null;
        } else if (dto instanceof QueryParameterConditionDTO) {
            return null;
        } else if (dto instanceof JWTClaimsConditionDTO) {
            return null;
        } else {
            String msg = "Throttle Condition type " + dto.getClass().getName() + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleConditionTypeException(msg);
        }
    }

    public static ThrottleConditionDTO fromConditionToDTO(Condition condition)   //.................
            throws UnsupportedThrottleConditionTypeException {
        if (condition instanceof IPCondition) {
            return fromIPConditionToDTO((IPCondition) condition);
        } else if (condition instanceof DateCondition) {
            return null;
        } else if (condition instanceof DateRangeCondition) {
            return null;
        } else if (condition instanceof HeaderCondition) {
            return null;
        } else if (condition instanceof HTTPVerbCondition) {
            return null;
        } else if (condition instanceof QueryParameterCondition) {
            return null;
        } else if (condition instanceof JWTClaimsCondition) {
            return null;
        } else {
            String msg = "Throttle Condition type " + condition.getClass().getName() + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleConditionTypeException(msg);
        }
    }
    
    
    //////////////////////// Quota policy <---> LimitDTO   //////////////////////////////
    
    public static QuotaPolicy fromDTOToQuotaPolicy (ThrottleLimitDTO dto) throws UnsupportedThrottleLimitTypeException {
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setLimit(fromDTOToLimit(dto));
        quotaPolicy.setType(mapQuotaPolicyTypeFromDTOToModel(dto.getType()));
        return quotaPolicy;
    }

    public static ThrottleLimitDTO fromQuotaPolicyToDTO (QuotaPolicy quotaPolicy) throws UnsupportedThrottleLimitTypeException {
        if (PolicyConstants.REQUEST_COUNT_TYPE.equals(quotaPolicy.getType())){
            RequestCountLimit requestCountLimit = (RequestCountLimit)quotaPolicy.getLimit();
            return fromRequestCountLimitToDTO(requestCountLimit);
        } else if (PolicyConstants.BANDWIDTH_TYPE.equals(quotaPolicy.getType())) {
            BandwidthLimit bandwidthLimit = (BandwidthLimit) quotaPolicy.getLimit();
            return fromBandwidthLimitToDTO(bandwidthLimit);
        } else {
            String msg = "Throttle limit type " + quotaPolicy.getType() + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleLimitTypeException(msg);
        }
    }
    
    public static Limit fromDTOToLimit(ThrottleLimitDTO dto) throws UnsupportedThrottleLimitTypeException { //done
        if (dto instanceof BandwidthLimitDTO) {
            return fromDTOToBandwidthLimit((BandwidthLimitDTO)dto);
        } else if (dto instanceof RequestCountLimitDTO) {
            return fromDTOToRequestCountLimit((RequestCountLimitDTO)dto);
        } else {
            String msg = "Throttle limit type " + dto.getClass().getName() + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleLimitTypeException(msg);
        }
    }
    
    //////////////////////////////////////////////////////
    

    
    public static BandwidthLimit fromDTOToBandwidthLimit (BandwidthLimitDTO dto) {
        BandwidthLimit bandwidthLimit = new BandwidthLimit();
        bandwidthLimit = updateFieldsFromDTOToLimit(dto, bandwidthLimit);
        bandwidthLimit.setDataAmount(dto.getDataAmount());
        bandwidthLimit.setDataUnit(dto.getDataUnit());
        return bandwidthLimit;
    }
    
    public static RequestCountLimit fromDTOToRequestCountLimit (RequestCountLimitDTO dto) {
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit = updateFieldsFromDTOToLimit(dto, requestCountLimit);
        requestCountLimit.setRequestCount(dto.getRequestCount());
        return requestCountLimit;
    }

    public static BandwidthLimitDTO fromBandwidthLimitToDTO (BandwidthLimit bandwidthLimit) {  //done
        BandwidthLimitDTO dto = new BandwidthLimitDTO();
        dto = updateFieldsFromLimitToDTO(bandwidthLimit, dto);
        dto.setType(ThrottleLimitDTO.TypeEnum.BandwidthLimit);
        dto.setDataAmount(bandwidthLimit.getDataAmount());
        dto.setDataUnit(bandwidthLimit.getDataUnit());
        return dto;
    }

    public static RequestCountLimitDTO fromRequestCountLimitToDTO (RequestCountLimit requestCountLimit) { //done
        RequestCountLimitDTO dto = new RequestCountLimitDTO();
        dto = updateFieldsFromLimitToDTO(requestCountLimit, dto);
        dto.setType(ThrottleLimitDTO.TypeEnum.RequestCountLimit);
        dto.setRequestCount(requestCountLimit.getRequestCount());
        return dto;
    }


    public static IPCondition fromDTOToIPCondition (IPConditionDTO dto) {
        String ipConditionType = mapIPConditionTypeFromDTOToModel(dto.getIpConditionType());
        IPCondition ipCondition = new IPCondition(ipConditionType);
        ipCondition = updateFieldsFromDTOToCondition(dto, ipCondition);
        ipCondition.setSpecificIP(dto.getSpecificIP());
        ipCondition.setStartingIP(dto.getStartingIP());
        ipCondition.setEndingIP(dto.getEndingIP());
        return ipCondition;
    }

    public static IPConditionDTO fromIPConditionToDTO (IPCondition ipCondition) {
        IPConditionDTO.IpConditionTypeEnum ipConditionType = mapIPConditionTypeFromModelToDTO(ipCondition.getType());
        IPConditionDTO dto = new IPConditionDTO();
        dto = updateFieldsFromConditionToDTO(ipCondition, dto);

        dto.setIpConditionType(ipConditionType);
        dto.setSpecificIP(ipCondition.getSpecificIP());
        dto.setStartingIP(ipCondition.getStartingIP());
        dto.setEndingIP(ipCondition.getEndingIP());
        return dto;
    }


    public static <T extends Condition> T updateFieldsFromDTOToCondition (ThrottleConditionDTO dto, T condition) {
        condition.setConditionEnabled(dto.getEnabled().toString());
        condition.setInvertCondition(dto.getInvertCondition());
        return condition;
    }

    public static <T extends ThrottleConditionDTO> T updateFieldsFromConditionToDTO (Condition condition, T dto) {
        dto.setEnabled(Boolean.parseBoolean(condition.getConditionEnabled()));
        dto.setInvertCondition(condition.isInvertCondition());
        return dto;
    }

    public static <T extends Limit> T updateFieldsFromDTOToLimit (ThrottleLimitDTO dto, T limit) {
        limit.setTimeUnit(dto.getTimeUnit());
        limit.setUnitTime(dto.getUnitTime());
        return limit;
    }

    public static <T extends ThrottleLimitDTO> T updateFieldsFromLimitToDTO (Limit limit, T dto) {
        dto.setTimeUnit(limit.getTimeUnit());
        dto.setUnitTime(limit.getUnitTime());
        return dto;
    }

    public static <T extends Policy> T updateFieldsFromDTOToPolicy(ThrottlePolicyDTO dto, T policy) //todo tenantId
            throws UnsupportedThrottleLimitTypeException {

        policy.setTenantDomain(dto.getTenantDomain());
        policy.setTenantId(MultitenantConstants.SUPER_TENANT_ID);   // todo check if this needed
        policy.setDisplayName(dto.getDisplayName());
        policy.setDeployed(dto.getIsDeployed());
        policy.setDescription(dto.getDescription());
        policy.setPolicyName(dto.getPolicyName());
        policy.setDefaultQuotaPolicy(fromDTOToQuotaPolicy(dto.getDefaultLimit()));

        return policy;
    }

    public static <T extends ThrottlePolicyDTO> T updateFieldsFromToPolicyToDTO (Policy policy, T dto)
            throws UnsupportedThrottleLimitTypeException {  //todo tenantId

        dto.setTenantDomain(policy.getTenantDomain());
        dto.setDisplayName(policy.getDisplayName());
        dto.setIsDeployed(policy.isDeployed());
        dto.setDescription(policy.getDescription());
        dto.setPolicyName(policy.getPolicyName());
        dto.setDefaultLimit(fromQuotaPolicyToDTO(policy.getDefaultQuotaPolicy()));

        return dto;
    }
    
    private static String mapQuotaPolicyTypeFromDTOToModel(ThrottleLimitDTO.TypeEnum typeEnum) { //done
        switch (typeEnum) {
        case BandwidthLimit:
            return PolicyConstants.BANDWIDTH_TYPE;
        case RequestCountLimit:
            return PolicyConstants.REQUEST_COUNT_TYPE;
        default:
            return null;
        }
    }
    
    private static String mapIPConditionTypeFromDTOToModel(IPConditionDTO.IpConditionTypeEnum typeEnum) {
        switch (typeEnum) {
        case IPRange:
            return PolicyConstants.IP_RANGE_TYPE;
        case IPSpecific:
            return PolicyConstants.IP_SPECIFIC_TYPE;
        default:
            return null;
        }
    }

    private static IPConditionDTO.IpConditionTypeEnum mapIPConditionTypeFromModelToDTO(String ipConditionType) {
        switch (ipConditionType) {
        case PolicyConstants.IP_RANGE_TYPE:
            return IPConditionDTO.IpConditionTypeEnum.IPRange;
        case PolicyConstants.IP_SPECIFIC_TYPE:
            return IPConditionDTO.IpConditionTypeEnum.IPSpecific;
        default:
            return null;
        }
    }

    private static String mapAdvancedPolicyUserLevelFromDTOToModel(AdvancedThrottlePolicyDTO.UserLevelEnum userLevelEnum ) {
        switch (userLevelEnum) {
        case apiLevel:
            return PolicyConstants.ACROSS_ALL;
        case userLevel:
            return PolicyConstants.PER_USER;
        default:
            return null;
        }
    }

    private static AdvancedThrottlePolicyDTO.UserLevelEnum mapAdvancedPolicyUserLevelFromModelToDTO(String userLevel) {
        switch (userLevel) {
        case PolicyConstants.ACROSS_ALL:
            return AdvancedThrottlePolicyDTO.UserLevelEnum.apiLevel;
        case PolicyConstants.PER_USER:
            return AdvancedThrottlePolicyDTO.UserLevelEnum.userLevel;
        default:
            return null;
        }
    }
    
}
