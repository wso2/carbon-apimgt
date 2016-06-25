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


package org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleConditionTypeException;
import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

public class CommonThrottleMappingUtil {

    private static final Log log = LogFactory.getLog(CommonThrottleMappingUtil.class);

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
        if (pipelines!= null) {
            for (Pipeline pipeline : pipelines) {
                groupDTOs.add(fromPipelineToConditionalGroupDTO(pipeline));
            }
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
        groupDTO.setId(pipeline.getId());
        groupDTO.setDescription(pipeline.getDescription());
        groupDTO.setEnabled(pipeline.isEnabled());
        groupDTO.setLimit(fromQuotaPolicyToDTO(pipeline.getQuotaPolicy()));

        List<ThrottleConditionDTO> conditionDTOList = fromConditionListToDTOList(pipeline.getConditions());
        groupDTO.setConditions(conditionDTOList);
        return groupDTO;
    }

    /////////////////////////// Condition LIST <---> DTO LIST ////////////////

    public static List<Condition> fromDTOListToConditionList(List<ThrottleConditionDTO> throttleConditionDTOs)
            throws UnsupportedThrottleConditionTypeException {
        List<Condition> conditions = new ArrayList<>();
        if (throttleConditionDTOs!=null) {
            for (ThrottleConditionDTO dto : throttleConditionDTOs) {
                conditions.add(fromDTOToCondition(dto));
            }
        }
        return conditions;
    }

    public static List<ThrottleConditionDTO> fromConditionListToDTOList(List<Condition> conditions)
            throws UnsupportedThrottleConditionTypeException {
        List<ThrottleConditionDTO> dtoList = new ArrayList<>();
        if (conditions!=null) {
            for (Condition condition : conditions) {
                dtoList.add(fromConditionToDTO(condition));
            }
        }
        return dtoList;
    }

    /////////////////////////// Condition <---> DTO ////////////////

    public static Condition fromDTOToCondition(ThrottleConditionDTO dto)   //.................
            throws UnsupportedThrottleConditionTypeException {
        if (dto instanceof IPConditionDTO) {
            return fromDTOToIPCondition((IPConditionDTO)dto);
        } else if (dto instanceof DateConditionDTO) {
            return fromDTOToDateCondition((DateConditionDTO)dto);
        } else if (dto instanceof DateRangeConditionDTO) {
            return fromDTOToDateRangeCondition((DateRangeConditionDTO)dto);
        } else if (dto instanceof HeaderConditionDTO) {
            return fromDTOToHeaderCondition((HeaderConditionDTO)dto);
        } else if (dto instanceof HTTPVerbConditionDTO) {
            return fromDTOToHTTPVerbCondition((HTTPVerbConditionDTO)dto);
        } else if (dto instanceof QueryParameterConditionDTO) {
            return fromDTOToQueryParameterCondition((QueryParameterConditionDTO)dto);
        } else if (dto instanceof JWTClaimsConditionDTO) {
            return fromDTOToJWTClaimsCondition((JWTClaimsConditionDTO)dto);
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
            return fromDateConditionToDTO((DateCondition)condition);
        } else if (condition instanceof DateRangeCondition) {
            return fromDateRangeConditionToDTO((DateRangeCondition)condition);
        } else if (condition instanceof HeaderCondition) {
            return fromHeaderConditionToDTO((HeaderCondition)condition);
        } else if (condition instanceof HTTPVerbCondition) {
            return fromHTTPVerbConditionToDTO((HTTPVerbCondition)condition);
        } else if (condition instanceof QueryParameterCondition) {
            return fromQueryParameterConditionToDTO((QueryParameterCondition)condition);
        } else if (condition instanceof JWTClaimsCondition) {
            return fromJWTClaimsConditionToDTO((JWTClaimsCondition)condition);
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


    //////////// condition type conversions ////////////////////

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
        dto.setType(ThrottleConditionDTO.TypeEnum.IPCondition);
        dto = updateFieldsFromConditionToDTO(ipCondition, dto);
        dto.setIpConditionType(ipConditionType);
        dto.setSpecificIP(ipCondition.getSpecificIP());
        dto.setStartingIP(ipCondition.getStartingIP());
        dto.setEndingIP(ipCondition.getEndingIP());
        return dto;
    }

    public static DateCondition fromDTOToDateCondition (DateConditionDTO dto) {
        DateCondition dateCondition = new DateCondition();
        dateCondition = updateFieldsFromDTOToCondition(dto, dateCondition);
        dateCondition.setSpecificDate(dto.getSpecificDate());
        return dateCondition;
    }

    public static DateConditionDTO fromDateConditionToDTO (DateCondition dateCondition) {
        DateConditionDTO dto = new DateConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.DateCondition);
        dto = updateFieldsFromConditionToDTO(dateCondition, dto);
        dto.setSpecificDate(dateCondition.getSpecificDate());
        return dto;
    }


    public static DateRangeCondition fromDTOToDateRangeCondition (DateRangeConditionDTO dto) {
        DateRangeCondition dateCondition = new DateRangeCondition();
        dateCondition = updateFieldsFromDTOToCondition(dto, dateCondition);
        dateCondition.setStartingDate(dto.getStartingDate());
        dateCondition.setEndingDate(dto.getEndingDate());
        return dateCondition;
    }

    public static DateRangeConditionDTO fromDateRangeConditionToDTO (DateRangeCondition dateCondition) {
        DateRangeConditionDTO dto = new DateRangeConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.DateRangeCondition);
        dto = updateFieldsFromConditionToDTO(dateCondition, dto);
        dto.setStartingDate(dateCondition.getStartingDate());
        dto.setEndingDate(dateCondition.getEndingDate());
        return dto;
    }


    public static HeaderCondition fromDTOToHeaderCondition (HeaderConditionDTO dto) {
        HeaderCondition headerCondition = new HeaderCondition();
        headerCondition = updateFieldsFromDTOToCondition(dto, headerCondition);
        headerCondition.setHeader(dto.getHeaderName());
        headerCondition.setValue(dto.getHeaderValue());
        return headerCondition;
    }

    public static HeaderConditionDTO fromHeaderConditionToDTO (HeaderCondition headerCondition) {
        HeaderConditionDTO dto = new HeaderConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.HeaderCondition);
        dto = updateFieldsFromConditionToDTO(headerCondition, dto);
        dto.setHeaderName(headerCondition.getHeaderName());
        dto.setHeaderValue(headerCondition.getValue());
        return dto;
    }


    public static HTTPVerbCondition fromDTOToHTTPVerbCondition (HTTPVerbConditionDTO dto) {
        HTTPVerbCondition httpVerbCondition = new HTTPVerbCondition();
        httpVerbCondition = updateFieldsFromDTOToCondition(dto, httpVerbCondition);
        httpVerbCondition.setHttpVerb(dto.getHttpVerb());
        return httpVerbCondition;
    }

    public static HTTPVerbConditionDTO fromHTTPVerbConditionToDTO (HTTPVerbCondition httpVerbCondition) {
        HTTPVerbConditionDTO dto = new HTTPVerbConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.HTTPVerbCondition);
        dto = updateFieldsFromConditionToDTO(httpVerbCondition, dto);
        dto.setHttpVerb(httpVerbCondition.getHttpVerb());
        return dto;
    }

    public static QueryParameterCondition fromDTOToQueryParameterCondition (QueryParameterConditionDTO dto) {
        QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
        queryParameterCondition = updateFieldsFromDTOToCondition(dto, queryParameterCondition);
        queryParameterCondition.setParameter(dto.getParameterName());
        queryParameterCondition.setValue(dto.getParameterValue());
        return queryParameterCondition;
    }

    public static QueryParameterConditionDTO fromQueryParameterConditionToDTO (QueryParameterCondition condition) {
        QueryParameterConditionDTO dto = new QueryParameterConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.QueryParameterCondition);
        dto = updateFieldsFromConditionToDTO(condition, dto);
        dto.setParameterName(condition.getParameter());
        dto.setParameterValue(condition.getValue());
        return dto;
    }

    public static JWTClaimsCondition fromDTOToJWTClaimsCondition (JWTClaimsConditionDTO dto) {
        JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
        jwtClaimsCondition = updateFieldsFromDTOToCondition(dto, jwtClaimsCondition);
        jwtClaimsCondition.setAttribute(dto.getAttribute());
        jwtClaimsCondition.setClaimUrl(dto.getClaimUrl());
        return jwtClaimsCondition;
    }

    public static JWTClaimsConditionDTO fromJWTClaimsConditionToDTO (JWTClaimsCondition condition) {
        JWTClaimsConditionDTO dto = new JWTClaimsConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.JWTClaimsCondition);
        dto = updateFieldsFromConditionToDTO(condition, dto);
        dto.setClaimUrl(condition.getClaimUrl());
        dto.setAttribute(condition.getAttribute());
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

    public static <T extends ThrottlePolicyDTO> T updateDefaultMandatoryFieldsOfThrottleDTO(T dto)
            throws UnsupportedThrottleLimitTypeException {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        dto.setTenantDomain(tenantDomain);
        return dto;
    }

    public static <T extends Policy> T updateFieldsFromDTOToPolicy(ThrottlePolicyDTO dto, T policy) 
            throws UnsupportedThrottleLimitTypeException {
        String tenantDomain = dto.getTenantDomain();
        policy.setTenantDomain(tenantDomain);
        policy.setTenantId(APIUtil.getTenantIdFromTenantDomain(tenantDomain));
        policy.setDisplayName(dto.getDisplayName());
        policy.setDeployed(dto.getIsDeployed());
        policy.setDescription(dto.getDescription());
        policy.setPolicyName(dto.getPolicyName());
        if (dto.getDefaultLimit() != null) {
            policy.setDefaultQuotaPolicy(fromDTOToQuotaPolicy(dto.getDefaultLimit()));
        }
        return policy;
    }

    public static <T extends ThrottlePolicyDTO> T updateFieldsFromToPolicyToDTO (Policy policy, T dto)
            throws UnsupportedThrottleLimitTypeException {

        if (policy.getTenantDomain() == null) {
            dto.setTenantDomain(RestApiUtil.getLoggedInUserTenantDomain());
        }
        dto.setDisplayName(policy.getDisplayName());
        dto.setIsDeployed(policy.isDeployed());
        dto.setDescription(policy.getDescription());
        dto.setPolicyName(policy.getPolicyName());
        //DefaultQuotaPolicy is null in Global Policy
        if (policy.getDefaultQuotaPolicy() != null) {
            dto.setDefaultLimit(fromQuotaPolicyToDTO(policy.getDefaultQuotaPolicy()));
        }
        return dto;
    }

    public static NameValuePairDTO getNameValuePair(String name, String value) {
        NameValuePairDTO nameValuePairDTO = new NameValuePairDTO();
        nameValuePairDTO.setName(name);
        nameValuePairDTO.setValue(value);
        return nameValuePairDTO;
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
    
}
