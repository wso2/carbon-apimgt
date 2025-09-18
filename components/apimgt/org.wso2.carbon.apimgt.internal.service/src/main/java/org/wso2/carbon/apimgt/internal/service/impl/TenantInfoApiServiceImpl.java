package org.wso2.carbon.apimgt.internal.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.LoadingTenants;
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

    private static final Log log = LogFactory.getLog(TenantInfoApiServiceImpl.class);

    public Response tenantInfoGet(String xWSO2Tenant, String filter, MessageContext messageContext)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving tenant info. Tenant: " + xWSO2Tenant + ", Filter: " + filter);
        }
        String tenantDomain = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        Tenant[] allTenants = TenantUtils.getAllTenants(tenantDomain, constructLoadingTenantsFromFilter(filter));
        log.info("Retrieved tenant information for " + (allTenants != null ? allTenants.length : 0) + " tenants");
        return Response.ok().entity(getTenantInfoListDTO(allTenants)).build();
    }

    private TenantInfoListDTO getTenantInfoListDTO(Tenant[] allTenants) {
        TenantInfoListDTO tenantInfoListDTO = new TenantInfoListDTO();
        List<TenantInfoDTO> tenantInfoList = new ArrayList<>();
        if (allTenants != null) {
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
        }
        tenantInfoListDTO.setTenants(tenantInfoList);
        return tenantInfoListDTO;
    }

    private LoadingTenants constructLoadingTenantsFromFilter(String filter) {
        LoadingTenants loadingTenants = new LoadingTenants();
        if (StringUtils.isNotEmpty(filter)) {
            byte[] decodedValue = Base64.decodeBase64(filter.getBytes(StandardCharsets.UTF_8));
            filter = new String(decodedValue, StandardCharsets.UTF_8);
            String[] filters = filter.split("&!");
            if ("*".equals(filters[0])) {
                loadingTenants.setIncludeAllTenants(true);
            } else {
                loadingTenants.setIncludeAllTenants(false);
                String[] includingTenants = filters[0].split("\\|");
                for (String tenant : includingTenants) {
                    loadingTenants.getIncludingTenants().add(tenant);
                }
            }
            if (filters.length >= 2 && StringUtils.isNotEmpty(filters[1])) {
                String[] excludingTenants = filters[1].split("\\|");
                for (String tenant : excludingTenants) {
                    loadingTenants.getExcludingTenants().add(tenant);
                }
            }
        } else {
            loadingTenants.setIncludeAllTenants(true);
        }
        return loadingTenants;
    }
}
