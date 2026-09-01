package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.KeyTemplatesApiService;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.core.Response;

public class KeyTemplatesApiServiceImpl implements KeyTemplatesApiService {

    @Override
    public Response keyTemplatesGet(MessageContext messageContext) throws APIManagementException {

        String authenticatedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Set<String> keyTemplates = isSuperTenant(authenticatedTenantDomain)
                ? BlockConditionDBUtil.getKeyTemplates() : Collections.emptySet();
        return Response.ok().entity(keyTemplates).build();
    }

    /**
     * The global custom-policy key templates returned here belong exclusively to the super tenant
     * (the table that backs them can only ever hold super-tenant rows — every write path is
     * super-tenant-gated). A non-super caller has no legitimate reason to read them; the gateway,
     * which does need the full set to build its throttling view, always authenticates as the super
     * tenant. An unresolved authenticated tenant is treated as non-super (fail closed).
     */
    static boolean isSuperTenant(String authenticatedTenantDomain) {

        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(authenticatedTenantDomain);
    }
}
