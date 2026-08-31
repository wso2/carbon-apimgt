package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.RevokedjwtApiService;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedEventsDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTConsumerKeyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTSubjectEntityDTO;
import org.wso2.carbon.apimgt.internal.service.model.RevokedJWTEventData;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;

public class RevokedjwtApiServiceImpl implements RevokedjwtApiService {

    @Override
    public Response revokedjwtGet(MessageContext messageContext) throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!isValidTenantContext(tenantDomain, tenantId)) {
            return Response.ok().entity(new RevokedEventsDTO()).build();
        }
        RevokedJWTEventData revokedJWTEventData = BlockConditionDBUtil.getRevokedJWTEventsWithOwnership();
        return Response.ok().entity(filterRevokedJWTEvents(revokedJWTEventData, tenantDomain, tenantId)).build();
    }

    static RevokedEventsDTO filterRevokedJWTEvents(RevokedJWTEventData revokedJWTEventData, String tenantDomain,
                                                   int tenantId) {

        RevokedEventsDTO revokedEventsDTO = new RevokedEventsDTO();
        if (revokedJWTEventData == null || !isValidTenantContext(tenantDomain, tenantId)) {
            return revokedEventsDTO;
        }

        boolean isSuperTenant = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain);
        for (RevokedJWTEventData.RevokedJWTData revokedJWT : revokedJWTEventData.getRevokedJWTList()) {
            if (isSuperTenant || revokedJWT.getTenantId() == tenantId) {
                RevokedJWTDTO revokedJWTDTO = new RevokedJWTDTO();
                revokedJWTDTO.setJwtSignature(revokedJWT.getJwtSignature());
                revokedJWTDTO.setExpiryTime(revokedJWT.getExpiryTime());
                revokedEventsDTO.getRevokedJWTList().add(revokedJWTDTO);
            }
        }
        for (RevokedJWTConsumerKeyDTO revokedConsumerKey :
                revokedJWTEventData.getRevokedJWTConsumerKeyList()) {
            if (isSuperTenant || tenantDomain.equalsIgnoreCase(revokedConsumerKey.getOrganization())) {
                revokedEventsDTO.getRevokedJWTConsumerKeyList().add(revokedConsumerKey);
            }
        }
        for (RevokedJWTSubjectEntityDTO revokedSubjectEntity :
                revokedJWTEventData.getRevokedJWTSubjectEntityList()) {
            if (isSuperTenant || tenantDomain.equalsIgnoreCase(revokedSubjectEntity.getOrganization())) {
                revokedEventsDTO.getRevokedJWTSubjectEntityList().add(revokedSubjectEntity);
            }
        }
        return revokedEventsDTO;
    }

    private static boolean isValidTenantContext(String tenantDomain, int tenantId) {

        if (StringUtils.isBlank(tenantDomain)) {
            return false;
        }
        boolean isSuperTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain);
        boolean isSuperTenantId = MultitenantConstants.SUPER_TENANT_ID == tenantId;
        return isSuperTenantDomain == isSuperTenantId && (isSuperTenantId || tenantId >= 0);
    }
}
