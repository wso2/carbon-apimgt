/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplate;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceTemplateList;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.impl.DevportalGovernanceManager;
import org.wso2.carbon.apimgt.governance.rest.api.TemplatesApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.TemplateDefaultViolationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.TemplateDefaultViolationListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.mappings.TemplateMappingUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * This is the implementation class for the Devportal Governance Templates API.
 */
public class TemplatesApiServiceImpl implements TemplatesApiService {

    @Override
    public Response createDevportalGovernanceTemplate(DevportalGovernanceTemplateDTO templateDTO,
                                                      MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        DevportalGovernanceTemplate template =
                TemplateMappingUtil.fromDTOToDevportalGovernanceTemplate(templateDTO);
        template.setCreatedBy(username);

        DevportalGovernanceManager devportalGovernanceManager = new DevportalGovernanceManager();
        DevportalGovernanceTemplate createdTemplate =
                devportalGovernanceManager.createTemplate(template, organization);
        DevportalGovernanceTemplateDTO createdTemplateDTO =
                TemplateMappingUtil.fromDevportalGovernanceTemplateToDTO(createdTemplate);

        try {
            URI createdTemplateURI =
                    new URI(APIMGovernanceAPIConstants.TEMPLATE_PATH + "/" + createdTemplateDTO.getId());
            return Response.created(createdTemplateURI).entity(createdTemplateDTO).build();
        } catch (URISyntaxException e) {
            throw new APIMGovernanceException("Error while creating URI for new Devportal Governance Template",
                    e, APIMGovExceptionCodes.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response updateDevportalGovernanceTemplateById(String templateId,
                                                          DevportalGovernanceTemplateDTO templateDTO,
                                                          MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        DevportalGovernanceTemplate template =
                TemplateMappingUtil.fromDTOToDevportalGovernanceTemplate(templateDTO);
        template.setUpdatedBy(username);

        DevportalGovernanceManager devportalGovernanceManager = new DevportalGovernanceManager();
        DevportalGovernanceTemplate updatedTemplate =
                devportalGovernanceManager.updateTemplate(templateId, template, organization);
        DevportalGovernanceTemplateDTO updatedTemplateDTO =
                TemplateMappingUtil.fromDevportalGovernanceTemplateToDTO(updatedTemplate);
        return Response.status(Response.Status.OK).entity(updatedTemplateDTO).build();
    }

    @Override
    public Response deleteDevportalGovernanceTemplate(String templateId, MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        DevportalGovernanceManager devportalGovernanceManager = new DevportalGovernanceManager();
        devportalGovernanceManager.deleteTemplate(templateId, username, organization);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getDevportalGovernanceTemplateById(String templateId, MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        DevportalGovernanceManager devportalGovernanceManager = new DevportalGovernanceManager();
        DevportalGovernanceTemplate template =
                devportalGovernanceManager.getTemplateById(templateId, organization);
        return Response.status(Response.Status.OK)
                .entity(TemplateMappingUtil.fromDevportalGovernanceTemplateToDTO(template)).build();
    }

    @Override
    public Response getDefaultDevportalGovernanceTemplate(MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        DevportalGovernanceManager devportalGovernanceManager = new DevportalGovernanceManager();
        DevportalGovernanceTemplate template =
                devportalGovernanceManager.getDefaultTemplate(organization);
        return Response.status(Response.Status.OK)
                .entity(TemplateMappingUtil.fromDevportalGovernanceTemplateToDTO(template)).build();
    }

    @Override
    public Response getDevportalGovernanceTemplates(Integer limit, Integer offset, MessageContext messageContext)
            throws APIMGovernanceException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        DevportalGovernanceManager devportalGovernanceManager = new DevportalGovernanceManager();
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);

        DevportalGovernanceTemplateList templateList =
                devportalGovernanceManager.getTemplates(organization);
        DevportalGovernanceTemplateListDTO paginatedTemplateList =
                getPaginatedTemplates(templateList, limit, offset);
        return Response.status(Response.Status.OK).entity(paginatedTemplateList).build();
    }

    @Override
    public Response validateTemplateDefaults(String templateId, MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        DevportalGovernanceManager manager = new DevportalGovernanceManager();
        List<RuleViolation> violations = manager.validateTemplateDefaults(templateId, organization);

        TemplateDefaultViolationListDTO result = new TemplateDefaultViolationListDTO();
        result.setHasViolations(!violations.isEmpty());
        for (RuleViolation v : violations) {
            TemplateDefaultViolationDTO dto = new TemplateDefaultViolationDTO();
            dto.setRuleName(v.getRuleName());
            dto.setRulesetId(v.getRulesetId());
            dto.setViolatedPath(v.getViolatedPath());
            dto.setMessage(v.getRuleMessage());
            if (v.getSeverity() != null) {
                dto.setSeverity(TemplateDefaultViolationDTO.SeverityEnum.fromValue(v.getSeverity().toString()));
            }
            result.getViolations().add(dto);
        }
        return Response.status(Response.Status.OK).entity(result).build();
    }

    private DevportalGovernanceTemplateListDTO getPaginatedTemplates(
            DevportalGovernanceTemplateList templateList, int limit, int offset) {

        int templateCount = templateList.getCount();
        List<DevportalGovernanceTemplateDTO> paginatedTemplates = new ArrayList<>();
        DevportalGovernanceTemplateListDTO paginatedTemplateListDTO = new DevportalGovernanceTemplateListDTO();
        paginatedTemplateListDTO.setCount(Math.min(templateCount, limit));

        if (offset > templateCount) {
            offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        }

        int start = offset;
        int end = Math.min(templateCount, start + limit);
        List<DevportalGovernanceTemplate> templates = templateList.getTemplateList();
        for (int i = start; i < end; i++) {
            paginatedTemplates.add(TemplateMappingUtil.fromDevportalGovernanceTemplateToDTO(templates.get(i)));
        }
        paginatedTemplateListDTO.setList(paginatedTemplates);

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(templateCount);
        paginatedTemplateListDTO.setPagination(paginationDTO);

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, templateCount);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = APIMGovernanceAPIUtil.getPaginatedURL(
                    APIMGovernanceAPIConstants.TEMPLATES_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = APIMGovernanceAPIUtil.getPaginatedURL(
                    APIMGovernanceAPIConstants.TEMPLATES_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        paginationDTO.setPrevious(paginatedPrevious);
        paginationDTO.setNext(paginatedNext);

        return paginatedTemplateListDTO;
    }
}
