package org.wso2.carbon.graphql.api.devportal;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;

import static org.wso2.carbon.utils.multitenancy.MultitenantUtils.getTenantAwareUsername;

public class ArtifactData {

    protected Registry registry;
    protected String username;
    protected String tenantDomain;



    public GenericArtifact getDevportalApis(String apiId) throws GovernanceException {
        boolean tenantFlowStarted = false;
        //String providername = null;
        GenericArtifact apiArtifact = null;
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);

        try{
            Registry registry;
            String requestedTenantDomain = org.getName();
            if (requestedTenantDomain  != null) {
                int id = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                if (APIConstants.WSO2_ANONYMOUS_USER.equals("wso2.anonymous.user")) {
                    registry = getRegistryService().getGovernanceUserRegistry("wso2.anonymous.user", id);
                } else if (this.tenantDomain != null && !this.tenantDomain.equals(requestedTenantDomain)) {
                    registry = getRegistryService().getGovernanceSystemRegistry(id);
                } else {
                    registry = this.registry;
                }
            } else {
                registry = this.registry;
            }

            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);


            apiArtifact = artifactManager.getGenericArtifact(apiId);
            //providername = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        }catch (RegistryException | org.wso2.carbon.user.api.UserStoreException | APIManagementException e){

        }finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return apiArtifact;


    }


    protected TenantManager getTenantManager() {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
    }
    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    public Registry getRegistry() throws UserStoreException, RegistryException {
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        //boolean tenantFlowStarted = false;
        Registry registry;
        String requestedTenantDomain = org.getName();
        if (requestedTenantDomain  != null) {
            int id = getTenantManager().getTenantId(requestedTenantDomain);
            RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
            //tenantFlowStarted = true;
            if (APIConstants.WSO2_ANONYMOUS_USER.equals("wso2.anonymous.user")) {
                registry = getRegistryService().getGovernanceUserRegistry("wso2.anonymous.user", id);
            } else if (this.tenantDomain != null && !this.tenantDomain.equals(requestedTenantDomain)) {
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                registry = this.registry;
            }
        } else {
            registry = this.registry;
        }
        return registry;
    }

    public String[] getTotalApisCount() throws APIManagementException, RegistryException, UserStoreException {
        //ArtifactData artifactData = new ArtifactData();

        boolean tenantFlowStarted = false;
        GenericArtifact apiArtifact = null;
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String[] allGenericArtifact=null;

        try {
            Registry registry = getRegistry();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            allGenericArtifact = artifactManager.getAllGenericArtifactIds();


        } catch (RegistryException | org.wso2.carbon.user.api.UserStoreException | APIManagementException e) {

        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return allGenericArtifact;
    }

}
