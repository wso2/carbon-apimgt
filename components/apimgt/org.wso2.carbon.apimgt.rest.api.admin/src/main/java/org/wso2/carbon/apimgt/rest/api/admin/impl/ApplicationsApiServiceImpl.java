package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
public class ApplicationsApiServiceImpl extends ApplicationsApiService {

    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    @Override
    public Response applicationsApplicationIdChangeOwnerPost(String owner, String applicationId) {

        APIConsumer apiConsumer = null;
        try {
            apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(owner);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            boolean applicationUpdated = apiConsumer.updateApplicationOwner(owner, application);
            if (applicationUpdated) {
                return Response.ok().build();
            } else {
                RestApiUtil.handleInternalServerError("Error while updating application owner " + applicationId, log);
            }

        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while updating application owner " + applicationId, e, log);
        }

        return null;
    }

    @Override
    public Response applicationsGet(String user, Integer limit, Integer offset, String accept, String ifNoneMatch,
                                    String appTenantDomain) {

        // To store the initial value of the user (specially if it is null or empty)
        String givenUser = user;

        // if no username provided user associated with access token will be used
        if (user == null || StringUtils.isEmpty(user)) {
            user = RestApiUtil.getLoggedInUsername();
        }

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        ApplicationListDTO applicationListDTO;
        try {
            Application[] allMatchedApps;
            boolean migrationMode = Boolean.getBoolean(RestApiConstants.MIGRATION_MODE);
            if (!migrationMode) { // normal non-migration flow
                if (!MultitenantUtils.getTenantDomain(user).equals(RestApiUtil.getLoggedInUserTenantDomain())) {
                    String errorMsg = "User " + user + " is not available for the current tenant domain";
                    log.error(errorMsg);
                    return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();
                }
                APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(user);

                // If no user is passed, get the applications for the tenant (not only for the user)
                if (givenUser == null || StringUtils.isEmpty(givenUser)) {
                    APIAdmin apiAdmin = new APIAdminImpl();
                    int tenantId = APIUtil.getTenantId(user);
                    allMatchedApps = apiAdmin.getApplicationsByTenantIdWithPagination(tenantId, 0, limit,
                            "", "", APIConstants.APPLICATION_NAME,
                            RestApiConstants.DEFAULT_SORT_ORDER).toArray(new Application[0]);
                } else {
                    allMatchedApps = apiConsumer.getApplicationsByOwner(user);
                }
            } else { // flow at migration process
                if (StringUtils.isEmpty(appTenantDomain)) {
                    appTenantDomain = MultitenantUtils.getTenantDomain(user);
                }
                RestApiUtil.handleMigrationSpecificPermissionViolations(appTenantDomain, RestApiUtil.getLoggedInUsername());
                APIAdmin apiAdmin = new APIAdminImpl();
                allMatchedApps = apiAdmin.getAllApplicationsOfTenantForMigration(appTenantDomain);
            }
            //allMatchedApps are already sorted to application name
            applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(allMatchedApps, limit, offset);
            ApplicationMappingUtil.setPaginationParams(applicationListDTO, limit, offset,
                    allMatchedApps.length);

            return Response.ok().entity(applicationListDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving applications of the user " + user, e, log);
        }
        return null;
    }
}
