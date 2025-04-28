package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.TenantUtils;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.TenantInfoListDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.Tenant;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class TenantInfoApiServiceImpl implements TenantInfoApiService {

    public Response tenantInfoGet(String xWSO2Tenant, String tenants, MessageContext messageContext) throws APIManagementException {
        String tenantDomain = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        String[] filters = new String[]{"*"};
        if (StringUtils.isNotEmpty(tenants)) {
            byte[] decodedValue = Base64.decodeBase64(tenants.getBytes());
            filters = new String(decodedValue).split("\\|");
        }
        Tenant[] allTenants = TenantUtils.getAllTenants(tenantDomain, filters);
        return Response.ok().entity(getTenantInfoListDTO(allTenants)).build();
    }

    private TenantInfoListDTO getTenantInfoListDTO(Tenant[] allTenants) {
        TenantInfoListDTO tenantInfoListDTO = new TenantInfoListDTO();
        List<TenantInfoDTO> tenantInfoList = tenantInfoListDTO.getTenants();
        for (Tenant tenant : allTenants) {
            TenantInfoDTO tenantInfoDTO = new TenantInfoDTO();
            tenantInfoDTO.setDomain(tenant.getDomain());
            tenantInfoDTO.setTenantId(tenant.getId());
            tenantInfoDTO.setActive(tenant.isActive());
            tenantInfoDTO.setEmail(tenant.getEmail());
            tenantInfoDTO.setAdminFirstName(tenant.getAdminFirstName());
            tenantInfoDTO.setAdminLastName(tenant.getAdminLastName());
            tenantInfoDTO.setAdminFullName(tenant.getAdminFullName());
            tenantInfoDTO.setAdmin(tenant.getAdminName());
            tenantInfoList.add(tenantInfoDTO);
        }
        tenantInfoListDTO.setTenants(tenantInfoList);
        return tenantInfoListDTO;
    }
}
